package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Gene;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import org.apache.commons.codec.binary.Hex;

import java.util.List;

public class PlantGenome implements Genome {

    public static final int GENE_COUNT = 13;

    private final Gene[] genome;

    public PlantGenome(final List<Gene> genes) {
        if( GENE_COUNT == genes.size() ) {
            genome = new Gene[GENE_COUNT];

            //just to be safe, deep copy. It's only 4 bytes each
            for( int i = 0; GENE_COUNT > i; ++i ){
                genome[i] = new Gene();
                genome[i].nucleotideA = genes.get(i).nucleotideA;
                genome[i].nucleotideB = genes.get(i).nucleotideB;
                genome[i].nucleotideC = genes.get(i).nucleotideC;
                genome[i].nucleotideD = genes.get(i).nucleotideD;
            }

        } else {
            throw new EvolutionException("Streams crossed in the PlantGenome");
        }


    }

    /**
     * @return
     */
    @Override
    public int getNumberOfGenes() {
        return GENE_COUNT;
    }

    /**
     * @param i
     * @return
     */
    @Override
    public Gene getGeneNumber(int i) {
        if (GENE_COUNT > i) {
            return genome[i];
        }
        return null;
    }

    /**
     * @param i
     * @param gene
     */
    @Override
    public void setGeneNumber(int i, Gene gene) {
        if (GENE_COUNT > i) {
            genome[i] = gene;
        }
    }

}
