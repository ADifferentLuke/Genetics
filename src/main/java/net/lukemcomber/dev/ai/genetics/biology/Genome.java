package net.lukemcomber.dev.ai.genetics.biology;

import net.lukemcomber.dev.ai.genetics.biology.Gene;

/**
 * This is a common interface for genomes interfacing with the world
 */
public interface Genome {

    /**
     * Returns the number of genes for this genome.
     * @return number of genes
     */
    int getNumberOfGenes();

    /**
     * Given an index within the bounds of the number of genes in the genome, returns a reference to the
     * respective gene
     * @param i index of the genome to inspect
     * @return
     */
    Gene getGeneNumber(final int i );

    /**
     * Sets a gene at the specified index to with a reference to the supplied gene
     * @param i the index to replace
     * @param gene the gene to insert into the genome
     */
    void setGeneNumber( final int i, final Gene gene );


}
