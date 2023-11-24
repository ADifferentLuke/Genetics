package net.lukemcomber.genetics.store.metadata;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.store.Metadata;

public class Genome implements Metadata {

    public static final String PROPERTY_GENOME_ENABLE = "metadata.Genome.enabled";

    public String name = "";
    public String dna = "";
}
