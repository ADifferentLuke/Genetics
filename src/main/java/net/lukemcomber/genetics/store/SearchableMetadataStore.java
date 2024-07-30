package net.lukemcomber.genetics.store;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import java.io.IOException;
import java.util.List;

public abstract class SearchableMetadataStore<T extends Metadata> extends MetadataStore<T>{

    public abstract List<T> page(final String index, final int pageNumber, final int recordsPerPage);

    public abstract List<T> find(final String index, final Object value, final int limit) throws IOException;

    public abstract List<T> find(final Object value, final int limit) throws IOException;
}
