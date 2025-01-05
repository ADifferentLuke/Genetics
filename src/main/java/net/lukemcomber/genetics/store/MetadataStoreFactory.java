package net.lukemcomber.genetics.store;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.model.UniverseConstants;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe {@link MetadataStoreGroup} factory.
 */
public class MetadataStoreFactory {

    private final Map<String, WeakReference<MetadataStoreGroup>> metadataStoreBySession;

    /*
     * We can do this in static context because we are using the default constructor
     */
    private static MetadataStoreFactory instance = new MetadataStoreFactory();

    private MetadataStoreFactory() {
        metadataStoreBySession = new ConcurrentHashMap<>();
    }


    /**
     * Returns the {@link MetadataStoreGroup} for the given simulation. {@link MetadataStoreGroup}
     * objects are singletons across simulations.
     *
     * @param simulation name of the simulation
     * @param properties configuration properties
     * @return a MetadataStoreGroup
     * @throws IOException
     */
    public synchronized static MetadataStoreGroup getMetadataStore(
            final String simulation, final UniverseConstants properties)
            throws IOException {

        MetadataStoreGroup sessionStore = null;
        if (instance.metadataStoreBySession.containsKey(simulation)) {
            final WeakReference<MetadataStoreGroup> reference = instance.metadataStoreBySession.get(simulation);
            sessionStore = reference.get();
        }
        if (null == sessionStore) {
            sessionStore = new MetadataStoreGroup(properties);
            instance.metadataStoreBySession.put(simulation, new WeakReference<>(sessionStore));
        }
        return sessionStore;
    }

    public synchronized static void freeResourcesAndTerminate() {

        for (final WeakReference<MetadataStoreGroup> reference : instance.metadataStoreBySession.values()) {
            final MetadataStoreGroup group = reference.get();
            if (Objects.nonNull(group)) {
                group.freeResourcesAndTerminate();
            }
        }
    }

}
