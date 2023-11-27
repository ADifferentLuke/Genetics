package net.lukemcomber.genetics.store;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.model.UniverseConstants;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Because this class is a singleton, we need to make sure it's threadsafe
 */
public class MetadataStoreFactory {

    private final Map<String, WeakReference<MetadataStoreGroup>> metadataStoreBySession;

    /*
     * We can do this in static context because we are using the default constructor
     */
    private static MetadataStoreFactory insance = new MetadataStoreFactory();

    private MetadataStoreFactory() {
        metadataStoreBySession = new ConcurrentHashMap<>();

        Thread expireThread = new Thread("metadataExpirationPoller") {
            @Override
            public void run() {
                try {
                    while (true) {
                        //TODO switch to configurable?
                        Thread.sleep(60000l);

                        metadataStoreBySession.forEach((sessionId, reference) -> {

                            final MetadataStoreGroup group = reference.get();
                            if( null != group ){
                                group.expire();
                            } else {
                                // The store has been GC'ed, remove the map entry
                                metadataStoreBySession.remove(sessionId);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        expireThread.setDaemon(true);
        expireThread.start();
    }


    /*
     * Returns a thread-safe data store backed by a tmp file
     */
    public synchronized static <T extends Metadata> MetadataStoreGroup getMetadataStore(
            final String simulation, final UniverseConstants properties)
            throws IOException {

        MetadataStoreGroup sessionStore = null;
        if (insance.metadataStoreBySession.containsKey(simulation)) {
            final WeakReference<MetadataStoreGroup> reference = insance.metadataStoreBySession.get(simulation);
            sessionStore = reference.get();
        }
        if( null == sessionStore ){
            sessionStore = new MetadataStoreGroup(properties);
            insance.metadataStoreBySession.put(simulation, new WeakReference<>(sessionStore));
        }
        return sessionStore;
    }
}
