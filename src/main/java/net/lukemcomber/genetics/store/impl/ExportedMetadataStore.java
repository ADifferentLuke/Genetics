package net.lukemcomber.genetics.store.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.Metadata;
import net.lukemcomber.genetics.store.MetadataStore;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ExportedMetadataStore<T extends Metadata> extends MetadataStore<T> {

    private static final Logger logger = Logger.getLogger(ExportedMetadataStore.class.getName());

    public static final String PROPERTY_TYPE_PATH = "metadata.export.path";
    public static final String PROPERTY_FILE_POSTFIX = "metadata.export.postfix";
    public static final String PROPERTY_CACHE_SIZE = "metadata.export.cache.size";

    private final Class<T> type;
    private final BlockingQueue<T> writeQueue;
    private final BlockingQueue<T> readQueue;
    private final ReentrantReadWriteLock ioSystemLock;
    private final Thread writeThread;
    private final AtomicLong lastAccessed; //longs are not atomic!!!!!
    private final Path fullOutputPathAndFile;
    private boolean enabled;
    final ObjectMapper objectMapper;

    public ExportedMetadataStore(final Class<T> type, final UniverseConstants properties, final String simulation) {
        super();

        this.type = type;
        this.objectMapper = new ObjectMapper();

        lastAccessed = new AtomicLong();

        final String writePath = properties.get(PROPERTY_TYPE_PATH, String.class);
        final String filePostfix = properties.get(PROPERTY_FILE_POSTFIX, String.class, "");
        final long ttl = properties.get(PROPERTY_DATASTORE_TTL, Long.class); //24hrs in seconds
        final String propertyName = String.format(PROPERTY_TYPE_ENABLED_TEMPLATE, type.getSimpleName());
        final int cacheSize = properties.get(PROPERTY_CACHE_SIZE, int.class, 50);

        writeQueue = new LinkedBlockingQueue<>();
        readQueue = new LinkedBlockingQueue<>();
        ioSystemLock = new ReentrantReadWriteLock(true); //fair


        enabled = properties.get(propertyName, Boolean.class, false);

        final long currentTimeMillis = System.currentTimeMillis();

        lastAccessed.set(System.currentTimeMillis() / 1000);


        final String fullOutputPathString = "%s/%s_%s%s.txt.gz".formatted(
                writePath.endsWith(File.separator) ? writePath.substring(0, writePath.length() - 1) : writePath,
                type.getSimpleName(), simulation,
                StringUtils.isNotBlank(filePostfix) ? "_" + filePostfix : "");
        fullOutputPathAndFile = Path.of(fullOutputPathString);
        if (enabled) {

            try {
                Files.createDirectories(Paths.get(writePath));
            } catch (final IOException e) {
                throw new EvolutionException("Failed to create output path [%s].".formatted(writePath));
            }

            logger.info( "Exporting data to %s".formatted(fullOutputPathString));

            writeThread = new Thread(String.format("%s-%d-file-poller", type.getSimpleName(), currentTimeMillis)) {

                @Override
                public void run() {


                    try (final FileOutputStream output = new FileOutputStream(fullOutputPathAndFile.toFile());
                         final Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), StandardCharsets.UTF_8)) {

                        logger.severe("Beginning poller " + writeThread.getName());
                        final ReentrantReadWriteLock.WriteLock writeLock = ioSystemLock.writeLock();
                        while (enabled) {
                            try {
                                //blocks
                                final T metadata = writeQueue.poll(1, TimeUnit.SECONDS);
                                if (null != metadata) {

                                    final String logEntry = objectMapper.writeValueAsString(metadata) + "\n";

                                    try {
                                        writeLock.lock();
                                        writer.write(logEntry);

                                    } finally {
                                        writeLock.unlock();
                                    }

                                    readQueue.add(metadata);
                                    if (cacheSize <= readQueue.size()) {
                                        readQueue.remove();
                                    }

                                    final long inactiveTime = (System.currentTimeMillis() / 1000) - lastAccessed.get();
                                    if (inactiveTime > ttl) {
                                        enabled = false;
                                    }
                                }
                            } catch (final InterruptedException e) {
                                if (enabled) {
                                    throw new RuntimeException(e);
                                } else {
                                    logger.info("Caught %s shutdown signal.".formatted(writeThread.getName()));
                                }
                            } catch (final Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        try {
                            writeLock.lock();

                            writer.flush();
                            writer.close();
                        } finally {
                            writeLock.unlock();
                        }
                        readQueue.clear();;

                        synchronized (writeThread) {
                            writeThread.notifyAll();
                        }
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                    logger.info("Thread %s shutting down.".formatted(writeThread.getName()));
                }
            };
            writeThread.start();
        } else {
            writeThread = null;
        }

    }


    @Override
    public void store(final T data) {
        if (writeQueue.offer(data)) {
            lastAccessed.set(System.currentTimeMillis() / 1000); //seconds
        }
    }

    @Override
    public List<T> retrieve() throws FileNotFoundException {
        // Hard coded to 50 for efficiency. Use page for full access
        return retrieve(1, 50);
    }

    @Override
    public List<T> page(final int pageNumber, final int countPerPage) throws FileNotFoundException {
        return retrieve(pageNumber, countPerPage);
    }

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
    public long count() {
        return readQueue.size();
    }

    private List<T> retrieve(final int pageNumber, final int countPerPage) {
        return new LinkedList<>(readQueue);
    }
}
