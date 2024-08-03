package net.lukemcomber.genetics.store;

import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.impl.TmpSearchableMetadataStore;
import net.lukemcomber.genetics.store.impl.TmpMetadataStore;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetadataStoreGroup {

    private final Map<String, MetadataStore<?>> groupStore;
    private final UniverseConstants properties;

    protected MetadataStoreGroup(final UniverseConstants properties) {
        groupStore = new ConcurrentHashMap<>();
        this.properties = properties;
    }

    public synchronized <T extends Metadata> MetadataStore<T> get(final Class<T> clazz)
            throws EvolutionException {

        MetadataStore<T> metadataStore = (MetadataStore<T>) groupStore.get(clazz.getSimpleName());
        if (null == metadataStore) {

            if( ! clazz.isAnnotationPresent(Searchable.class)){
                metadataStore = new TmpMetadataStore<T>(clazz, properties);
            } else {
                metadataStore = new TmpSearchableMetadataStore<T>(clazz, properties);
            }
            groupStore.put(clazz.getSimpleName(), metadataStore);
        }
        return metadataStore;
    }

    public void expire(){
        groupStore.forEach( (key, store) -> {
            try {
                if( store.expire()) {
                    groupStore.remove(key);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
