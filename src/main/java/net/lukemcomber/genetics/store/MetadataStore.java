package net.lukemcomber.genetics.store;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public abstract class MetadataStore<T extends Metadata> {

    public static final String PROPERTY_DATASTORE_TTL = "metadata.ttl";

    public abstract void store(final T data);

    //Returns true if the store is expired
    public abstract boolean expire() throws IOException;

    public abstract List<T> retrieve() throws FileNotFoundException;

}

