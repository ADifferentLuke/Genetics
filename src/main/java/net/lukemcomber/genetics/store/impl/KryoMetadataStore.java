package net.lukemcomber.genetics.store.impl;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.util.Pool;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.Indexed;
import net.lukemcomber.genetics.store.Metadata;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.Primary;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * A searchable {@link MetadataStore} backed by a tmp file
 *
 * @param <T> type of data to store
 */
public class KryoMetadataStore<T extends Metadata> extends net.lukemcomber.genetics.store.SearchableMetadataStore<T> {

    private static final Logger logger = Logger.getLogger(KryoMetadataStore.class.getName());


    static class CachePosition {
        public AtomicLong startByte;
        public AtomicInteger length;
    }

    class MetadataComparator implements Comparator<T> {

        @Override
        public int compare(final T o1, final T o2) {
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return -1;
            if (o2 == null) return 1;

            final Field[] fields = o1.getClass().getDeclaredFields(); //o1.getClass().getFields();

            // Sort fields by annotation priority, then by name for determinism
            Arrays.sort(fields, Comparator
                    .comparingInt(this::getFieldPriority)
                    .thenComparing(Field::getName));

            for (final Field field : fields) {
                field.setAccessible(true);
                try {
                    final Object value1 = field.get(o1);
                    final Object value2 = field.get(o2);

                    final int result = compareValues(value1, value2);
                    if (result != 0){
                        return result;
                    }

                } catch (final IllegalAccessException e) {
                    throw new RuntimeException("Unable to access field: " + field.getName(), e);
                }
            }
            return 0;
        }

        private int getFieldPriority(final Field field) {
            if (field.isAnnotationPresent(Primary.class)) return 0; // Highest priority
            if (field.isAnnotationPresent(Indexed.class)) return 1;   // Medium priority
            return 2;  // Lowest priority (no annotation)
        }

        @SuppressWarnings("unchecked")
        private int compareValues(final Object v1, final Object v2) {
            if (v1 == v2) return 0;
            if (v1 == null) return -1;
            if (v2 == null) return 1;

            if (v1 instanceof Comparable<?> && v2 instanceof Comparable<?>) {
                return ((Comparable<Object>) v1).compareTo(v2);
            }
            return v1.toString().compareTo(v2.toString());
        }

        @Override
        public boolean equals(final Object obj) {
            return obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode() {
            return Objects.hash(MetadataComparator.class);
        }
    }

    public static final String PROPERTY_TYPE_TTL = "metadata.%s.ttl";

    private final ConcurrentSkipListMap<T, CachePosition> sortedMetadata;
    private final Map<String, ConcurrentSkipListMap<Object, LinkedBlockingQueue<CachePosition>>> indexedFields;
    private AtomicLong recordCount;
    private boolean enabled; //RW is atomic
    private final Thread writeThread;
    private final BlockingQueue<T> outputQueue;
    private final ReentrantReadWriteLock ioSystemLock;
    private final Path datFilePath;
    private final Path idxFilePath;
    private final RandomAccessFile datFile;
    private final RandomAccessFile idxFile;

    private final AtomicBoolean isInitialized;
    private final AtomicBoolean isRunning;
    private final AtomicBoolean isCleanedUp;
    private long cursor;
    private final Class<T> type;
    private final Pool<Kryo> kryoPool;

    private Callable<Void> onCleanUpHook;
    private Timer expirationTimer;
    private String primaryIndex;

    /**
     * Create new {@link net.lukemcomber.genetics.store.SearchableMetadataStore} of the specified type.
     *
     * @param type       type of data to store
     * @param properties config properties
     */
    public KryoMetadataStore(final Class<T> type, final UniverseConstants properties) throws EvolutionException {

        isCleanedUp = new AtomicBoolean(false);
        isInitialized = new AtomicBoolean(false);
        isRunning = new AtomicBoolean(false);

        sortedMetadata = new ConcurrentSkipListMap<>(new MetadataComparator().reversed());

        onCleanUpHook = null;

        expirationTimer = new Timer(true);
        //Using custom property first, but don't barf if it's not defined
        final long ttl;
        indexedFields = new ConcurrentSkipListMap<>();
        recordCount = new AtomicLong(0);
        final Integer cTtl = properties.get(String.format(PROPERTY_TYPE_TTL, type.getSimpleName()), Integer.class, -1);
        this.type = type;
        if (0 >= cTtl) {
            // Get global property
            ttl = properties.get(PROPERTY_DATASTORE_TTL, Integer.class);
        } else {
            ttl = cTtl;
        }

        for (final Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Primary.class)) {
                primaryIndex = field.getAnnotation(Primary.class).name();
                break;
            }
        }

        if (StringUtils.isEmpty(primaryIndex)) {
            throw new EvolutionException("Metadata class " + type.getSimpleName() + " does not have a primary index.");
        }

        kryoPool = new Pool<>(true, false, 8) {
            protected Kryo create() {
                Kryo kryo = new Kryo();
                kryo.register(type);
                return kryo;
            }
        };


        final String propertyName = String.format(PROPERTY_TYPE_ENABLED_TEMPLATE, type.getSimpleName());

        logger.info("Checking " + propertyName);

        enabled = properties.get(propertyName, Boolean.class, false);

        outputQueue = new LinkedBlockingQueue<>();
        ioSystemLock = new ReentrantReadWriteLock(true); //fair

        final long currentTimeMillis = System.currentTimeMillis();

        if (enabled) {
            try {
                datFilePath = Files.createTempFile("store-", String.format("-%d-%s.dat", currentTimeMillis, type.getSimpleName()));
                idxFilePath = Files.createTempFile("store-", String.format("-%d-%s.idx", currentTimeMillis, type.getSimpleName()));
                datFile = new RandomAccessFile(datFilePath.toFile(), "rw");
                idxFile = new RandomAccessFile(idxFilePath.toFile(), "rw");
            } catch (IOException e) {
                throw new EvolutionException(e);
            }

            logger.info(String.format("Store:\n\tIdx: %s\n\tDat: %s", idxFilePath.toFile().getAbsolutePath(), datFilePath.toFile().getAbsolutePath()));
            writeThread = new Thread(String.format("%s-%d-meta-poller", type.getSimpleName(), currentTimeMillis)) {

                @Override
                public void run() {

                    if (isInitialized.get() && isRunning.compareAndSet(false, true)) {
                        try {
                            logger.info("Beginning poller " + writeThread.getName());
                            while (true) {
                                try {
                                    //blocks
                                    final T metadata = outputQueue.poll(1, TimeUnit.MINUTES);
                                    if (null != metadata) {
                                        final ReentrantReadWriteLock.WriteLock writeLock = ioSystemLock.writeLock();
                                        try {
                                            writeLock.lock();
                                            /*
                                             * DEV NOTE: Turned out the juice is worth the squeeze
                                             */
                                            cursor = writeAndCacheMetadata(metadata, cursor, datFile, idxFile);

                                        } catch (IllegalAccessException e) {
                                            throw new RuntimeException(e);
                                        } finally {
                                            writeLock.unlock();
                                        }
                                    }
                                } catch (final InterruptedException e) {
                                    logger.info(writeThread.getName() + " woken up.");
                                    break; // I hate this but checking interrupt fails
                                }
                            }


                            expirationTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    cleanUp();
                                }
                            }, ttl * 1000);


                            synchronized (writeThread) {
                                writeThread.notifyAll();
                            }
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                        logger.info(writeThread.getName() + " shutting down.");
                    } else {
                        logger.info("Metadata Store is already running.");
                    }

                }
            };

            writeThread.setDaemon(true);
        } else {
            datFilePath = null;
            datFile = null;
            idxFile = null;
            idxFilePath = null;
            writeThread = null;
        }


    }

    private void cleanUp() {
        if (isRunning.compareAndSet(true, false) && isCleanedUp.compareAndSet(false, true)) {
            try {
                if (Objects.nonNull(onCleanUpHook)) {
                    try {
                        onCleanUpHook.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                ioSystemLock.writeLock().lock();
                indexedFields.clear();
                sortedMetadata.clear();
                datFile.close();
                idxFile.close();
                expirationTimer = null;
                logger.info("Deleting file " + datFilePath.toAbsolutePath());
                logger.info("Deleting file " + idxFilePath.toAbsolutePath());
                Files.deleteIfExists(datFilePath);
                Files.deleteIfExists(idxFilePath);

            } catch (final IOException e) {
                throw new RuntimeException(e);
            } finally {
                ioSystemLock.writeLock().unlock();
            }
        } else {
            logger.info("Store resources already freed.");
        }
    }

    public synchronized void freeResourcesAndTerminate() {
        if (isRunning.get()) {
            try {
                expire(true);
                expirationTimer.cancel();
                cleanUp();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * onCleanUpHook will be called before the expiration timer is set, but after reading the last message
     *
     * @param onCleanUpHook
     */
    @Override
    public void initialize(final Callable<Void> onCleanUpHook) {
        if (!isCleanedUp.get() && !isRunning.get() && isInitialized.compareAndSet(false, true)) {
            this.onCleanUpHook = onCleanUpHook;
            writeThread.start();
        } else {
            logger.warning("Metadata store already initialized.");
        }
    }

    /**
     * Store data in the metadata store
     *
     * @param data data to store
     */
    @Override
    public void store(final T data) {
        outputQueue.offer(data);
    }


    /**
     * Returns a count of records in the data store
     *
     * @return number of stored records
     */
    @Override
    public long count() {
        return recordCount.get();
    }

    @Override
    public Class<T> type() {
        return type;
    }

    /**
     * Returns a page of data stored in the datastore
     *
     * @param pageNumber     page number to return
     * @param recordsPerPage number of records per page
     * @return list of records
     */
    @Override
    public List<T> page(final int pageNumber, final int recordsPerPage) {

        final List<T> retVal;

        if (0 <= pageNumber && 0 < recordsPerPage) {
            retVal = readFromIndex(pageNumber, recordsPerPage);
        } else {
            throw new EvolutionException("Invalid page reference.");
        }
        return retVal;
    }

    /**
     * Search the {@link MetadataStore} at the provided index for the specified value.
     *
     * @param index index to search
     * @param value key value to search for
     * @param limit maximum number of results
     * @return a list of matching records
     * @throws IOException
     */
    @Override
    public List<T> find(final String index, final Object value, final int limit) throws IOException {
        final List<T> retVal = new LinkedList<>();
        if (indexedFields.containsKey(index)) {
            final ConcurrentSkipListMap<Object, LinkedBlockingQueue<CachePosition>> indexes = indexedFields.get(index);
            /*
             * There is no guarantee that our lookup value is the same type as the Map.
             *   so peek the first entry and compare types
             */
            if (!indexes.isEmpty()) {
                final Object indexKey = indexes.firstKey();

                if (indexKey.getClass() == value.getClass()) {
                    if (indexes.containsKey(value)) {
                        final LinkedBlockingQueue<CachePosition> positions = indexes.get(value);
                        CachePosition targetPosition;
                        while (null != (targetPosition = positions.poll())) {
                            retVal.add(readDataFromFile(targetPosition, datFile));
                        }
                    }
                } else {
                    logger.info("Invalid lookup type [" + value.getClass() + "] != [" + indexKey.getClass() + "] for index " + index);
                }
            }
        }
        return retVal;
    }

    /**
     * Search all indexes for the provided value. This tends to be significantly more expensive than
     * {@link net.lukemcomber.genetics.store.SearchableMetadataStore#find(String, Object, int)}
     *
     * @param value value to search for
     * @param limit maximum number of results
     * @return a list of matching records
     * @throws IOException
     */
    @Override
    public List<T> find(final Object value, final int limit) throws IOException {
        final List<T> retVal = new LinkedList<>();
        final Iterator<String> indexIterator = indexedFields.keySet().iterator();
        long results = 0;
        while (indexIterator.hasNext() && results < limit) {
            final String indexName = indexIterator.next();
            final List<T> resultList = find(indexName, value, limit);
            if (resultList.size() > limit) {
                retVal.addAll(resultList.subList(0, limit));
            } else {
                retVal.addAll(resultList);
            }
        }
        return retVal;
    }

    /**
     * Attempt to expire the data store. If the force flag is set, then force an expiration.
     * <p>
     * If the force flag is used, false will be returned while the system cleans up resources.
     * Once resources are freed, will return false
     *
     * @param block flag to force expiration
     * @return true if expired
     * @throws IOException
     */
    @Override
    public boolean expire(final boolean block) throws IOException {
        if (null != writeThread) {
            if (enabled) {
                enabled = false;
                writeThread.interrupt();
            }
            if (block) {
                synchronized (writeThread) {
                    try {
                        writeThread.join();
                    } catch (final InterruptedException e) {
                        throw new EvolutionException("Failed to join thread %s".formatted(writeThread.getName()));
                    }
                }
            }
        }
        return !enabled;
    }

    // may block
    @Override
    public boolean isExpired() {
        return isCleanedUp.get();
    }

    private long writeAndCacheMetadata(final T metadata, final long currentPosition,
                                       final RandomAccessFile datFile, final RandomAccessFile idxFile) throws IOException, IllegalAccessException {

        final ByteArrayOutputStream binaryStream = new ByteArrayOutputStream();


        final Output binaryKyroOutput = new Output(binaryStream);
        final Kryo kryo = kryoPool.obtain();
        kryo.writeObject(binaryKyroOutput, metadata);
        binaryKyroOutput.flush();
        binaryKyroOutput.close();

        final byte[] data = binaryStream.toByteArray();
        kryoPool.free(kryo);


        datFile.seek(currentPosition);
        datFile.write(data);

        recordCount.incrementAndGet();

        final CachePosition cachePosition = new CachePosition();
        cachePosition.length = new AtomicInteger(data.length);
        cachePosition.startByte = new AtomicLong(currentPosition);

        idxFile.writeLong(currentPosition);
        idxFile.writeInt(data.length);

        final Class<?> clazz = metadata.getClass();
        for (final Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Indexed.class) || field.isAnnotationPresent(Primary.class)) {

                final String key = null != field.getAnnotation(Indexed.class) ?
                        field.getAnnotation(Indexed.class).name() : field.getAnnotation(Primary.class).name();

                final ConcurrentSkipListMap<Object, LinkedBlockingQueue<CachePosition>> indexList;
                if (indexedFields.containsKey(key)) {
                    indexList = indexedFields.get(key);
                } else {
                    indexList = new ConcurrentSkipListMap<>();
                    indexedFields.put(key, indexList);
                }
                final Object val = field.get(metadata);

                sortedMetadata.put(metadata, cachePosition);

                if (!Objects.isNull(val)) {
                    final LinkedBlockingQueue<CachePosition> records;
                    if (indexList.containsKey(val)) {
                        records = indexList.get(val);
                    } else {
                        records = new LinkedBlockingQueue<>();
                    }
                    records.add(cachePosition);
                    indexList.put(val, records);


                } else {
                    logger.warning("Could not cache metadata. Index " + field.getName() + " is null.");
                }

            }
        }


        return currentPosition + data.length;
    }

    private List<T> readFromIndex(final int page, final long recordCount) {

        final List<T> results;

        results = sortedMetadata.entrySet().stream()
                .skip((long) page * recordCount)
                .limit(recordCount)
                .map(pos -> {
                    try {
                        return readDataFromFile(pos.getValue(), datFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        return results;

    }

    private T readDataFromFile(final CachePosition position, final RandomAccessFile file) throws IOException {

        final ReentrantReadWriteLock.ReadLock readLock = ioSystemLock.readLock();
        final T retVal;

        final long startPosition = position.startByte.get();
        final byte[] data = new byte[position.length.get()];
        int readCount = 0;
        boolean seeked = false;
        try {
            readLock.lock();
            if (file.getChannel().isOpen() && startPosition + data.length <= file.length()) {
                file.seek(startPosition);
                readCount = file.read(data);
                seeked = true;
            }

        } finally {
            readLock.unlock();
        }

        if (0 < readCount) {
            final Input input = new Input(data);
            final Kryo kryo = kryoPool.obtain();
            retVal = kryo.readObject(input, type);
            kryoPool.free(kryo);
        } else {
            logger.severe("readDataFromFile - setting a null. Read count: " + readCount);
            retVal = null;
            throw new RuntimeException("Null read!");
        }
        return retVal;
    }
}
