package net.lukemcomber.genetics.store;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Provides methods for accessing the tracked metadata
 *
 * @param <T> type of the metadata
 */
public abstract class MetadataStore<T extends Metadata> {

    public static final String PROPERTY_DATASTORE_TTL = "metadata.ttl";
    public static final String PROPERTY_TYPE_ENABLED_TEMPLATE = "metadata.%s.enabled";
    public static final String METADATA_EXPORT = "metadata.export";
    /**
     * Store data in the metadata store
     *
     * @param data data to store
     */
    public abstract void store(final T data);

    /**
     * Returns a list of all data stored in the datastore
     *
     * @return list of records
     * @throws FileNotFoundException
     */
    public abstract List<T> retrieve() throws FileNotFoundException;

    /**
     * Returns a page of data stored in the datastore
     *
     * @param pageNumber   page number to return
     * @param countPerPage number of records per page
     * @return list of records
     * @throws FileNotFoundException
     */
    public abstract List<T> page(int pageNumber, int countPerPage) throws FileNotFoundException;

    /**
     * Expire the data store. If blocked is true, wait until thread shutdown to return
     *
     * @param block wait for shutdown
     * @return true if enable
     * @throws IOException
     */
    public abstract boolean expire(boolean block) throws IOException;


    /**
     * Attempts to expire the datastore
     *
     * @return true if expired
     * @throws IOException
     */
    public boolean expire() throws IOException {
        return expire(false);
    }

    /**
     * Returns a count of records in the data store
     *
     * @return number of stored records
     */
    public abstract long count();


}

