package net.lukemcomber.genetics.store;

import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.impl.TmpSearchableMetadataStore;
import net.lukemcomber.genetics.store.impl.TmpMetadataStore;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread safe collection of {@link MetadataStore} objects
 */
public class MetadataStoreGroup {

    private final Map<String, MetadataStore<?>> groupStore;
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

            if (!clazz.isAnnotationPresent(Searchable.class)) {
                metadataStore = new TmpMetadataStore<T>(clazz, properties);
            } else {
                metadataStore = new TmpSearchableMetadataStore<T>(clazz, properties);
            }
            groupStore.put(clazz.getSimpleName(), metadataStore);
        }
        return metadataStore;
    }

    /**
     * Expires the all the {@link MetadataStore} and clean up resources
     */
    public void expire() {
        groupStore.forEach((key, store) -> {
            try {
                if (store.expire()) {
                    groupStore.remove(key);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
