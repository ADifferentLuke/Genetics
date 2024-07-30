package net.lukemcomber.genetics.store;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public abstract class MetadataStore<T extends Metadata> {

    public static final String PROPERTY_DATASTORE_TTL = "metadata.ttl";

    public abstract void store(final T data);

    //Returns true if the store is expired
    public abstract boolean expire(boolean force) throws IOException;

    public boolean expire() throws IOException {
        return expire(false);
    }

    public abstract List<T> retrieve() throws FileNotFoundException;

    public abstract List<T> page(int pageNumber, int countPerPage) throws FileNotFoundException;

    public abstract long count();

}

