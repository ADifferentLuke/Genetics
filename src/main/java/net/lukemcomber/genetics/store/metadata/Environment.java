package net.lukemcomber.genetics.store.metadata;

import net.lukemcomber.genetics.store.Metadata;

public class Environment implements Metadata {

    public static final String PROPERTY_PERFORMANCE_ENABLE = "metadata.Environment.enabled";

    public Long tickCount;
    public Long totalOrganisms;
}
