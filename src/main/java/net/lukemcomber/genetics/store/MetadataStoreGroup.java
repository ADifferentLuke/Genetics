package net.lukemcomber.genetics.store;

import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.impl.KryoMetadataStore;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A thread safe collection of {@link MetadataStore} objects
 */
public class MetadataStoreGroup {

    private static final Logger logger = Logger.getLogger(MetadataStoreGroup.class.getName());


    private final Map<String, MetadataStore<? extends Metadata>> groupStore;
    private final UniverseConstants properties;

    /**
     * Creates a new collection of {@link MetadataStore} objects
     *
     * @param properties configuration properties
     */
    protected MetadataStoreGroup(final UniverseConstants properties) {
        groupStore = new ConcurrentHashMap<>();
        this.properties = properties;
    }

    /**
     * Returns the {@link MetadataStore} for a {@link Metadata} type
     *
     * @param clazz type of the {@link Metadata}
     * @param <T>   type of the {@link Metadata}
     * @return a usable {@link MetadataStore}
     * @throws EvolutionException
     */
    public synchronized <T extends Metadata> MetadataStore<T> get(final Class<T> clazz)
            throws EvolutionException {

        MetadataStore<T> metadataStore = (MetadataStore<T>) groupStore.get(clazz.getSimpleName());
        if (null == metadataStore) {

            metadataStore = new KryoMetadataStore<T>(clazz, properties);
            metadataStore.initialize(() -> {
                if (groupStore.containsKey(clazz.getSimpleName())) {
                    if (Objects.nonNull(groupStore.get(clazz.getSimpleName()))) {
                        groupStore.remove(clazz.getSimpleName());
                    }
                }
                return null;
            });
            groupStore.put(clazz.getSimpleName(), metadataStore);
        }
        return metadataStore;
    }

    public synchronized Set<Class<? extends Metadata>> getActiveMetadataStores() {
        return groupStore.values().stream().map(MetadataStore::type).collect(Collectors.toSet());
    }

    /**
     * Expires the all the {@link MetadataStore} and clean up resources
     */
    public void markForExpiration() {
        groupStore.forEach((key, store) -> {
            try {
                store.expire(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void freeResourcesAndTerminate() {

        for (final MetadataStore<?> store : groupStore.values()) {
            store.freeResourcesAndTerminate();
        }
    }
}
