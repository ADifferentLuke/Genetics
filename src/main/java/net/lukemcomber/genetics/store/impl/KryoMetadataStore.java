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

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * A searchable {@link MetadataStore} backed by a tmp file
 *
 * @param <T> type of data to store
 */
public class KryoMetadataStore<T extends Metadata> extends net.lukemcomber.genetics.store.SearchableMetadataStore<T> {

    private static final Logger logger = Logger.getLogger(KryoMetadataStore.class.getName());


    class CachePosition {
        long startByte;
        int length;
    }

    public static final String PROPERTY_TYPE_TTL = "metadata.%s.ttl";

    private final Map<String, TreeMap<Object, List<CachePosition>>> indexedFields;
    private long recordCount;
    private boolean enabled; //RW is atomic
    private final Thread writeThread;
    private final BlockingQueue<T> outputQueue;
    private final ReentrantReadWriteLock ioSystemLock;
    private final Path datFilePath;
    private final Path idxFilePath;
    private final RandomAccessFile datFile;
    private final RandomAccessFile idxFile;

    private long cursor;
    private final Class<T> type;
    private final Pool<Kryo> kryoPool;

    /**
     * Create new {@link net.lukemcomber.genetics.store.SearchableMetadataStore} of the specified type.
     *
     * @param type       type of data to store
     * @param properties config properties
     */
    public KryoMetadataStore(final Class<T> type, final UniverseConstants properties) throws EvolutionException {

        //Using custom property first, but don't barf if it's not defined
        final long ttl;
        indexedFields = new HashMap<>();
        recordCount = 0;
        final Long cTtl = properties.get(String.format(PROPERTY_TYPE_TTL, type.getSimpleName()), Long.class, -1l);
        this.type = type;
        if (0 >= cTtl) {
            // Get global property
            ttl = properties.get(PROPERTY_DATASTORE_TTL, Long.class);
        } else {
            ttl = cTtl;
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

                    try {
                        logger.info("Beginning poller " + writeThread.getName());
                        while (true){
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
                        try {
                            ioSystemLock.writeLock().lock();
                            indexedFields.clear();
                            datFile.close();
                            idxFile.close();
                            Files.deleteIfExists(datFilePath);
                            Files.deleteIfExists(idxFilePath);

                        } finally {
                            ioSystemLock.writeLock().unlock();
                        }

                        synchronized (writeThread) {
                            writeThread.notifyAll();
                        }
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                    logger.info(writeThread.getName() + " shutting down.");
                }

            };

            writeThread.setDaemon(true);
            writeThread.start();
        } else {
            datFilePath = null;
            datFile = null;
            idxFile = null;
            idxFilePath = null;
            writeThread = null;
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
        return recordCount;
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
    public List<T> page(final String index, final int pageNumber, final int recordsPerPage) {

        final List<T> retVal;

        if (0 <= pageNumber && 0 < recordsPerPage) {
            if (indexedFields.containsKey(index)) {
                retVal = readFromIndex(index, pageNumber, recordsPerPage);
            } else {
                throw new RuntimeException("Index [" + index + "] not found");
            }
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
            final TreeMap<Object, List<CachePosition>> indexes = indexedFields.get(index);
            /*
             * There is no guarantee that our lookup value is the same type as the Map.
             *   so peek the first entry and compare types
             */
            if (!indexes.isEmpty()) {
                final Object indexKey = indexes.firstKey();

                if (indexKey.getClass() == value.getClass()) {
                    if (indexes.containsKey(value)) {
                        final List<CachePosition> positions = indexes.get(value);
                        for (int i = 0; i < positions.size() && i < limit; i++) {
                            retVal.add(readDataFromFile(positions.get(i), datFile));
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
            if( block ){
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

    @Override
    public boolean isExpired() {
        return !enabled;
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

        recordCount++;

        final CachePosition cachePosition = new CachePosition();
        cachePosition.length = data.length;
        cachePosition.startByte = currentPosition;

        idxFile.writeLong(cachePosition.startByte);
        idxFile.writeInt(cachePosition.length);

        final Class<?> clazz = metadata.getClass();
        for (final Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Indexed.class) || field.isAnnotationPresent(Primary.class)) {

                final String key = null != field.getAnnotation(Indexed.class) ?
                        field.getAnnotation(Indexed.class).name() : field.getAnnotation(Primary.class).name();

                final TreeMap<Object, List<CachePosition>> indexList;
                if (indexedFields.containsKey(key)) {
                    indexList = indexedFields.get(key);
                } else {
                    indexList = new TreeMap<>();
                    indexedFields.put(key, indexList);
                }
                final Object val = field.get(metadata);

                if (!Objects.isNull(val)) {
                    final List<CachePosition> records;
                    if (indexList.containsKey(val)) {
                        records = indexList.get(val);
                    } else {
                        records = new LinkedList<>();
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

    private List<T> readFromIndex(final String index, final int page, final long recordCount) {

        final List<T> results;
        if (indexedFields.containsKey(index)) {
            final TreeMap<Object, List<CachePosition>> indexes = indexedFields.get(index);
            results = indexes.descendingMap().entrySet().stream().flatMap(entry -> entry.getValue().stream())
                    .skip((long) page * recordCount)
                    .limit(recordCount)
                    .map(pos -> {
                        try {
                            return readDataFromFile(pos, datFile);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();

        } else {
            results = null;
        }
        return results;

    }

    private T readDataFromFile(final CachePosition position, final RandomAccessFile file) throws IOException {

        final ReentrantReadWriteLock.ReadLock readLock = ioSystemLock.readLock();
        final T retVal;

        final long startPosition = position.startByte;
        final byte[] data = new byte[position.length];
        int readCount = 0;
        try {

            readLock.lock();
            if (file.getChannel().isOpen() && startPosition + data.length < file.length()) {
                file.seek(startPosition);
                readCount = file.read(data);
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
            retVal = null;
        }
        return retVal;
    }
}
