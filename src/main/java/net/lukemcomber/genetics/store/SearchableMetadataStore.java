package net.lukemcomber.genetics.store;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import java.io.IOException;
import java.util.List;

/**
 * Provides interface for a searchable {@link MetadataStore}
 *
 * @param <T> Type to store
 */
public abstract class SearchableMetadataStore<T extends Metadata> extends MetadataStore<T> {


    /**
     * Search the {@link MetadataStore} at the provided index for the specified value.
     *
     * @param index index to search
     * @param value key value to search for
     * @param limit maximum number of results
     * @return a list of matching records
     * @throws IOException
     */
    public abstract List<T> find(final String index, final Object value, final int limit) throws IOException;

    /**
     * Search all indexes for the provided value. This tends to be significantly more expensive than
     * {@link SearchableMetadataStore#find(String, Object, int)}
     *
     * @param value value to search for
     * @param limit maximum number of results
     * @return a list of matching records
     * @throws IOException
     */
    public abstract List<T> find(final Object value, final int limit) throws IOException;
}
