package net.lukemcomber.genetics.biology;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;

import java.util.*;
import java.util.function.Function;

/**
 * This is a common interface for genomes interfacing with the world
 */
public abstract class Genome {

    /**
     * Enum of lambdas that provide travel across SpatialCoordinates
     */
    public enum SpatialTransformation {
        LEFT(c -> new SpatialCoordinates(c.xAxis() - 1, c.yAxis(), c.zAxis())),
        RIGHT(c -> new SpatialCoordinates(c.xAxis() + 1, c.yAxis(), c.zAxis())),
        UP(c -> new SpatialCoordinates(c.xAxis(), c.yAxis() + 1, c.zAxis())),
        DOWN(c -> new SpatialCoordinates(c.xAxis(), c.yAxis() - 1, c.zAxis())),
        FORWARD(c -> new SpatialCoordinates(c.xAxis(), c.yAxis(), c.zAxis() + 1)),
        BACK(c -> new SpatialCoordinates(c.xAxis(), c.yAxis(), c.zAxis() - 1));

        private final Function<SpatialCoordinates, SpatialCoordinates> spatialTransformation;

        SpatialTransformation(final Function<SpatialCoordinates, SpatialCoordinates> spatialConversionFunction) {
            this.spatialTransformation = spatialConversionFunction;
        }

        public Function<SpatialCoordinates, SpatialCoordinates> value() {
            return spatialTransformation;
        }

    }

    private final int numOfGenes;
    private final LinkedList<Gene> genes;

    private final String type;

    /**
     * Create a new empty instance
     *
     * @param numOfGenes number of genes
     * @param type       type of genome
     */
    public Genome(final int numOfGenes, final String type) {
        this.numOfGenes = numOfGenes;
        genes = new LinkedList<>();
        for (int i = 0; this.numOfGenes > i; ++i) {
            final Gene gene = new Gene();
            gene.nucleotideA = 0;
            gene.nucleotideB = 0;
            gene.nucleotideC = 0;
            gene.nucleotideD = 0;
            genes.add(gene);
        }
        this.type = type;
    }

    /**
     * Create a new instance
     *
     * @param genes genes in the genome
     * @param type  type of genome
     */
    public Genome(final List<Gene> genes, final String type) {
        numOfGenes = genes.size();
        this.genes = new LinkedList<>(genes);
        this.type = type;

    }

    /**
     * A utility method to convert a list of genes to a byte array
     *
     * @param geneList
     * @return list of genes as byte array
     */
    public static byte[] toBytes(final List<Gene> geneList) {
        return geneList.stream()
                .map(Gene::toBytes)
                .reduce(new byte[0], (a, b) -> {
                    byte[] result = new byte[a.length + b.length];
                    System.arraycopy(a, 0, result, 0, a.length);
                    System.arraycopy(b, 0, result, a.length, b.length);
                    return result;
                });
    }

    /**
     * Get the genomes type
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the number of genes for this genome.
     *
     * @return number of genes
     */
    public int getNumberOfGenes() {
        return numOfGenes;
    }

    /**
     * Given an index within the bounds of the number of genes in the genome, returns a reference to the
     * respective gene
     *
     * @param i index of the genome to inspect
     * @return
     */
    public Gene getGeneNumber(final int i) {
        if (numOfGenes > i) {
            return genes.get(i);
        } else {
            throw new EvolutionException("Attempting to read a gene outside the organisms genome.");
        }
    }

    /**
     * Sets a gene at the specified index to with a reference to the supplied gene
     *
     * @param i    the index to replace
     * @param gene the gene to insert into the genome
     */
    public void setGeneNumber(final int i, final Gene gene) {
        //TODO test this!!
        if (numOfGenes > i) {
            genes.remove(i);
            genes.add(i, gene);
        }
    }

    /**
     * Get an iterator to iterate
     *
     * @param bits
     * @return a new iterator
     */
    protected Iterator<Byte> iterator(final int bits) {
        return new GenomeIterator(genes, bits);
    }

    /**
     * Build and return an actionable behavior from the next gene in the genome
     *
     * @return a behavior object or null
     */
    public abstract PlantBehavior getNextAct();

    /**
     * Provide a deep clone of the current genome
     *
     * @return genome clone
     */
    public abstract Genome clone();

}
