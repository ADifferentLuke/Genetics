package net.lukemcomber.genetics.store;

import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.impl.TmpMetaDataStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Because this class is a singleton, we need to make sure it's threadsafe
 */
public class MetadataStoreFactory {

    // Session ID - Type - Store
    private final Map<String, Map<String, MetadataStore<?>>> metadataStoreBySession;

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
                        Thread.sleep(60000l);

                        final long currentTime = System.currentTimeMillis();

                        //the keyset call here is important
                        metadataStoreBySession.keySet().forEach(sessionId -> {

                            //TODO may need to change to more concurrrent functions
                            final Map<String, MetadataStore<?>> storeMap = metadataStoreBySession.get(sessionId);
                            final Set<String> metaKeys = storeMap.keySet();

                            for (String key : metaKeys) {
                                MetadataStore<?> store = storeMap.get(key);

                                try {
                                    if (store.expire()) {
                                        //TODO lock here
                                        storeMap.remove(store);
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                            }

                            if(0 == storeMap.size()){
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
    // TODO we can do better than locking the entire method
    public synchronized static <T extends Metadata> MetadataStore<T> getMetadataStore(
            final String session, final Class<T> clazz, final UniverseConstants properties)
            throws IOException {

        final Map<String, MetadataStore<?>> sessionStore;
        if (insance.metadataStoreBySession.containsKey(session)) {
            sessionStore = insance.metadataStoreBySession.get(session);
        } else {
            sessionStore = new ConcurrentHashMap<>();
            insance.metadataStoreBySession.put(session, sessionStore);
        }
        MetadataStore<T> metadataStore = (MetadataStore<T>) sessionStore.get(clazz.getName());
        if (null == metadataStore) {
            metadataStore = new TmpMetaDataStore<T>(clazz, properties);
            sessionStore.put(clazz.getName(), metadataStore);
        }
        return metadataStore;
    }
}
