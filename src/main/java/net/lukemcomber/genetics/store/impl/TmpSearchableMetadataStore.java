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
import net.lukemcomber.genetics.store.SearchableMetadataStore;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class TmpSearchableMetadataStore<T extends Metadata> extends SearchableMetadataStore<T> {

    private static final Logger logger = Logger.getLogger(TmpSearchableMetadataStore.class.getName());


    class CachePosition {
        long startByte;
        int length;
    }

    public static final String PROPERTY_TYPE_ENABLED = "metadata.%s.enabled";
    public static final String PROPERTY_TYPE_TTL = "metadata.%s.ttl";

    private Map<String, TreeMap<Object, List<CachePosition>>> indexedFields;
    private AtomicLong lastAccessed; //longs are not atomic!!!!!
    private long recordCount;
    private boolean enabled; //RW is atomic
    private boolean forceShutdown; //RW is atomic
    private Thread writeThread;
    private final BlockingQueue<T> outputQueue;
    private final ReentrantReadWriteLock ioSystemLock;
    private final Path tmpFilePath;
    private final RandomAccessFile ioFile;

    private long cursor;
    private final Class<T> type;
    private final Pool<Kryo> kryoPool;

    /*
     * This class represents a logical unit that corresponds to a OS tmp file. The goal is
     * to delete the entire file when it expires.
     */
    public TmpSearchableMetadataStore(final Class<T> type, final UniverseConstants properties) throws EvolutionException {

        //Using custom property first, but don't barf if it's not defined
        final long ttl;
        indexedFields = new HashMap<>();
        recordCount = 0;
        final Long cTtl = properties.get(String.format(PROPERTY_TYPE_TTL, type.getSimpleName()), Long.class, -1l);
        this.type = type;
        if (0 >= cTtl) {
            // Get global property
            ttl = properties.get(PROPERTY_DATASTORE_TTL, Long.class); //24hrs in seconds
        } else {
            ttl = cTtl;
        }

        kryoPool = new Pool<>(true, false, 8) {
            protected Kryo create () {
                Kryo kryo = new Kryo();
                kryo.register(type);
                return kryo;
            }
        };



        final String propertyName = String.format(PROPERTY_TYPE_ENABLED, type.getSimpleName());

        logger.info("Checking " + propertyName);

        forceShutdown = false;
        enabled = properties.get(propertyName, Boolean.class, false);

        outputQueue = new LinkedBlockingQueue<>();
        lastAccessed = new AtomicLong();
        ioSystemLock = new ReentrantReadWriteLock(true); //fair

        final long currentTimeMillis = System.currentTimeMillis();

        lastAccessed.set(currentTimeMillis / 1000); //seconds

        if (enabled) {
            try {
                tmpFilePath = Files.createTempFile("store-", String.format("-%d-%s", currentTimeMillis, type.getSimpleName()));
                ioFile = new RandomAccessFile(tmpFilePath.toFile(), "rw");
            } catch (IOException e) {
                throw new EvolutionException(e);
            }

            logger.info("Create tmp file " + tmpFilePath);

            writeThread = new Thread(String.format("%s-%d-meta-poller", type.getSimpleName(), currentTimeMillis)) {

                @Override
                public void run() {

                    try (final Output kyroOutput = new Output(new FileOutputStream(tmpFilePath.toFile()));) {
                        logger.info("Beginning poller " + writeThread.getName());
                        while (enabled) {
                            try {
                                //blocks
                                final T metadata = outputQueue.poll(1, TimeUnit.SECONDS);
                                if (null != metadata) {
                                    final ReentrantReadWriteLock.WriteLock writeLock = ioSystemLock.writeLock();
                                    try {
                                        writeLock.lock();
                                        /*
                                         * DEV NOTE: Turned out the juice is worth the squeeze
                                         */
                                        cursor = writeAndCacheMetadata(metadata, cursor, ioFile);

                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    } finally {
                                        writeLock.unlock();
                                    }
                                }
                                final long inactiveTime = (System.currentTimeMillis() / 1000) - lastAccessed.get();
                                if (forceShutdown || inactiveTime > ttl) {

                                    try {
                                        ioSystemLock.writeLock().lock();
                                        enabled = false;
                                        ioFile.close();
                                        Files.deleteIfExists(tmpFilePath);
                                        indexedFields.clear();

                                    } finally {
                                        ioSystemLock.writeLock().unlock();
                                    }

                                } else {
                                    //We have not expired yet
                                }
                            } catch (final InterruptedException e) {
                                logger.info(writeThread.getName() + " woken up.");
                            }
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
            tmpFilePath = null;
            ioFile = null;
        }

    }

    @Override
    public void store(final T data) {
        if (outputQueue.offer(data)) {
            lastAccessed.set(System.currentTimeMillis() / 1000); //seconds
        }
    }

    @Override
    public boolean expire(final boolean force) throws IOException {
        if (enabled) {
            this.forceShutdown = force; //boolean assignment is atomic
            if (force) {
                writeThread.interrupt();
            }

            /*
             * DEV NOTE: There is a window here where the writer thread is
             *  woken up and begun file cleanup, but hasn't changed enabled before
             *  we return the value.
             *
             * As a result, this store will report enabled in the process of cleanup, but
             * will report expired on the next call.
             *
             * This means we can be disabled, have the tmp file removed, but the calling process
             * still thinks we're enabled. Make sure to gracefully handle that scenario.
             */
        }
        return !enabled;
    }

    public List<T> retrieve() throws FileNotFoundException {
        return page(0, (int) count());
    }

    @Override
    public List<T> page(final int pageNumber, final int countPerPage) throws FileNotFoundException {

        List<T> retVal = new LinkedList<>();

        if (null != indexedFields) {

            final String key = indexedFields.keySet().stream().findFirst().orElse("default");
            retVal = readFromIndex(key, pageNumber, countPerPage);

        }
        return retVal;
    }

    @Override
    public long count() {
        return recordCount;
    }

    private long writeAndCacheMetadata(final T metadata, final long currentPosition, final RandomAccessFile file) throws IOException, IllegalAccessException {

        final ByteArrayOutputStream binaryStream = new ByteArrayOutputStream();


        final Output binaryKyroOutput = new Output(binaryStream);
        final Kryo kryo = kryoPool.obtain();
        kryo.writeObject(binaryKyroOutput, metadata);
        binaryKyroOutput.flush();
        binaryKyroOutput.close();

        final byte[] data = binaryStream.toByteArray();
        kryoPool.free(kryo);


        file.seek(currentPosition);
        file.write(data);

        recordCount++;

        final CachePosition cachePosition = new CachePosition();
        cachePosition.length = data.length;
        cachePosition.startByte = currentPosition;

        Class<?> clazz = metadata.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Indexed.class)) {
                final String key = field.getAnnotation(Indexed.class).name();

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
            TreeMap<Object, List<CachePosition>> indexes = indexedFields.get(index);
            results = indexes.descendingMap().entrySet().stream().flatMap(entry -> entry.getValue().stream())
                    .skip((long) page * recordCount)
                    .limit(recordCount)
                    .map(pos -> {
                        try {
                            return readDataFromFile(pos, ioFile);
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
            if( file.getChannel().isOpen()) {
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

    @Override
    public List<T> page(String index, int pageNumber, int recordsPerPage) {

        final List<T> retVal;

        if( 0 <= pageNumber && 0 < recordsPerPage ) {
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

    @Override
    public List<T> find(String index, Object value, int limit) throws IOException {
        final List<T> retVal = new LinkedList<>();
        if (indexedFields.containsKey(index)) {
            final TreeMap<Object, List<CachePosition>> indexes = indexedFields.get(index);
            /*
             * There is no guarantee that our lookup value is the same type as the Map.
             *   so peek the first entry and compage types
             */
            if( ! indexes.isEmpty() ) {
                final Object indexKey = indexes.firstKey();

                if( indexKey.getClass() == value.getClass() ) {
                    if (indexes.containsKey(value)) {
                        final List<CachePosition> positions = indexes.get(value);
                        for (int i = 0; i < positions.size() && i < limit; i++) {
                            retVal.add(readDataFromFile(positions.get(i), ioFile));
                        }
                    }
                } else {
                    logger.info( "Invalid lookup type [" + value.getClass() + "] != [" + indexKey.getClass() + "] for index " + index);
                }
            }
        }
        return retVal;
    }

    @Override
    public List<T> find(Object value, int limit) throws IOException {
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

}
