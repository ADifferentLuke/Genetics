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
import net.lukemcomber.genetics.store.Metadata;
import net.lukemcomber.genetics.store.MetadataStore;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class TmpMetadataStore<T extends Metadata> extends MetadataStore<T> {

    private static final Logger logger = Logger.getLogger(TmpMetadataStore.class.getName());

    public static final String PROPERTY_TYPE_ENABLED = "metadata.%s.enabled";
    public static final String PROPERTY_TYPE_TTL = "metadata.%s.ttl";

    private AtomicLong lastAccessed; //longs are not atomic!!!!!
    private long recordCount;
    private boolean enabled; //RW is atomic
    private boolean forceShutdown; //RW is atomic
    private Thread writeThread;
    private final BlockingQueue<T> outputQueue;
    private final ReentrantReadWriteLock ioSystemLock;
    private final Path tmpFilePath;
    private final Class<T> type;
    private final Pool<Kryo> kryoPool;

    /*
     * This class represents a logical unit that corresponds to a OS tmp file. The goal is
     * to delete the entire file when it expires.
     */
    public TmpMetadataStore(final Class<T> type, final UniverseConstants properties) {

        //Using custom property first, but don't barf if it's not defined
        final long ttl;
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
                                         * DEV NOTE: we can't use compression with random access files because
                                         * the compression will change the byte size of the records
                                         *
                                         * We could probably do some fancy tricks regarding estimating size of
                                         * record types and adding padding, but the juice isn't worth
                                         * the squeeze here
                                         */
                                        recordCount++;
                                        final Kryo kryo = kryoPool.obtain();
                                        kryo.writeObject(kyroOutput, metadata);
                                        kyroOutput.flush();
                                        kryoPool.free(kryo);

                                    } finally {
                                        writeLock.unlock();
                                    }
                                }
                                final long inactiveTime = (System.currentTimeMillis() / 1000) - lastAccessed.get();
                                if (forceShutdown || inactiveTime > ttl) {

                                    try {
                                        ioSystemLock.writeLock().lock();
                                        enabled = false;
                                        Files.deleteIfExists(tmpFilePath);
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
                    logger.info( writeThread.getName() + " shutting down.");
                }

            };

            writeThread.setDaemon(true);
            writeThread.start();
        } else {
            tmpFilePath = null;
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
            if( force ) {
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
        return retrieve(-1);
    }

    /**
     * Returns null for an expired or deleted or file, returns
     * empty list for an empty file, otherwise returns results.
     *
     * May block
     *
     * @return
     * @throws FileNotFoundException
     */
    private List<T> retrieve(final long count) throws FileNotFoundException {
        final List<T> retVal;
        long currentCount = 0;
        if (null != tmpFilePath && Files.exists(tmpFilePath)) {
            retVal = new LinkedList<>();
            try (final Input kryoInput = new Input(new FileInputStream(tmpFilePath.toFile()))) {

                final ReentrantReadWriteLock.ReadLock readLock = ioSystemLock.readLock();

                while (!kryoInput.end() && ( 0 >= count  || currentCount < count)) {

                    try {
                        readLock.lock();
                        final Kryo kryo = kryoPool.obtain();
                        retVal.add(kryo.readObject(kryoInput, type));
                        kryoPool.free(kryo);
                        currentCount++;

                    } finally {
                        readLock.unlock();
                    }
                }
            }
        } else {
            retVal = null;
        }
        return retVal;
    }


    @Override
    public List<T> page(final int pageNumber, final int countPerPage) throws FileNotFoundException {
        // This wasn't really meant for pagination
        final List<T> retVal;
        if( 0 <= pageNumber && 0 < countPerPage ) {
            final List<T> fullRecordList = retrieve(pageNumber * countPerPage + countPerPage);
            int currentPageInRecords = pageNumber * countPerPage;
            if (currentPageInRecords < fullRecordList.size()) {
                int recordCount = fullRecordList.size() - currentPageInRecords;

                retVal = fullRecordList.subList(currentPageInRecords, currentPageInRecords + recordCount);
            } else {
                // requested a page past the number of records
                retVal = new ArrayList<>(0);
            }
        } else {
            throw new EvolutionException("Invalid page reference.");
        }
        return retVal;
    }

    @Override
    public long count() {
        return recordCount;
    }

}
