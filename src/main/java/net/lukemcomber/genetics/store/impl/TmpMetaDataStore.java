package net.lukemcomber.genetics.store.impl;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.Metadata;
import net.lukemcomber.genetics.store.MetadataStore;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

//TODO support part files
//TODO we could also play fancy tricks for efficiency, like in memroy cache for fs dump
//TODO verify all the locking

public class TmpMetaDataStore<T extends Metadata> extends MetadataStore<T> {

    private static final Logger logger = Logger.getLogger(TmpMetaDataStore.class.getName());

    public static final String PROPERTY_TYPE_ENABLED = "metadata.%s.enabled";
    public static final String PROPERTY_TYPE_TTL = "metadata.%s.ttl";

    private final long ttl;
    private long lastAccessed;
    private boolean enabled;
    private Thread writeThread;
    private BlockingQueue<T> outputQueue;
    private ReentrantReadWriteLock ioSystemLock;
    private final Path tmpFilePath;
    private final Class<T> type;
    private final Kryo kryo;

    /*
     * This class represents a logical unit that corresponds to a OS tmp file. The goal is
     * to delete the entire file when it expires.
     */
    public TmpMetaDataStore(final Class<T> type, final UniverseConstants properties) throws IOException {

        //Using custom property first, but don't barf if it's not defined
        final Long cTtl = properties.get(String.format(PROPERTY_TYPE_TTL, type.getSimpleName()), Long.class, -1l);
        this.type = type;
        if (0 >= cTtl) {
            // Get global property
            ttl = properties.get(PROPERTY_DATASTORE_TTL, Long.class); //24hrs in seconds
        } else {
            ttl = cTtl;
        }

        kryo = new Kryo();

        kryo.register(type);

        final String propertyName = String.format(PROPERTY_TYPE_ENABLED, type.getSimpleName());

        logger.info("Checking " + propertyName);

        enabled = properties.get(propertyName, Boolean.class, false);
        lastAccessed = System.currentTimeMillis() / 1000; //seconds
        outputQueue = new LinkedBlockingQueue<>();
        ioSystemLock = new ReentrantReadWriteLock(true); //fair

        if (enabled) {
            tmpFilePath = Files.createTempFile("store-", String.format("-%s", type.getSimpleName()));

            logger.info( "Create tmp file " + tmpFilePath);


            writeThread = new Thread(String.format("%s-meta-poller", type.getSimpleName())) {

                @Override
                public void run() {
                    //TODO should this thread just call expire?
                    try (final Output kyroOutput = new Output(new FileOutputStream(tmpFilePath.toFile()));) {
                        logger.info("Beginning poller " + writeThread.getName());
                        while (enabled) {
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
                                    kryo.writeObject(kyroOutput, metadata);
                                    kyroOutput.flush();

                                } finally {
                                    writeLock.unlock();
                                }
                            }
                        }
                    } catch (final InterruptedException e) {
                        logger.info(writeThread.getName() + " shutting down.");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
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
        lastAccessed = System.currentTimeMillis() / 1000; //seconds
        if (outputQueue.offer(data)) {
            //Do something?
        }
    }

    @Override
    public boolean expire() throws IOException {
        final long inactiveTime = (System.currentTimeMillis() / 1000) - lastAccessed;
        boolean retVal = false;
        if (inactiveTime > ttl ) {
            //TODO what happens if called twice
            enabled = false;
            if (null != tmpFilePath) {

                writeThread.interrupt();
                ioSystemLock.writeLock().lock();
                Files.deleteIfExists(tmpFilePath);
                ioSystemLock.writeLock().unlock();

            }
            retVal = true;
        } else {
            logger.info( "Expire failed: " + inactiveTime + "<= " + ttl);
        }

        return retVal;
    }

    /**
     * Returns null for an expired or deleted or file, returns
     * empty list for an empty file, otherwise returns results.
     *
     * @return
     * @throws FileNotFoundException
     */
    public List<T> retrieve() throws FileNotFoundException {
        final List<T> retVal;
        if (null != tmpFilePath && Files.exists(tmpFilePath)) {
            retVal = new LinkedList<>();
            try (final Input kryoInput = new Input(new FileInputStream(tmpFilePath.toFile()))) {

                final ReentrantReadWriteLock.ReadLock readLock = ioSystemLock.readLock();

                while (!kryoInput.end()) {

                    readLock.lock();

                    T t = kryo.readObject(kryoInput, type);
                    retVal.add(t);

                    readLock.unlock();
                }
            }
        } else {
            retVal = null;
        }
        return retVal;
    }
}
