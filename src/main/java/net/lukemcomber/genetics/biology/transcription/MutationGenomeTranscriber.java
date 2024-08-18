package net.lukemcomber.genetics.biology.transcription;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import com.google.common.annotations.VisibleForTesting;
import net.lukemcomber.genetics.biology.Gene;
import net.lukemcomber.genetics.biology.Genome;
import net.lukemcomber.genetics.biology.GenomeTransciber;
import net.lukemcomber.genetics.biology.OrganismFactory;
import net.lukemcomber.genetics.model.UniverseConstants;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Transcribe genome using {@link MutationGenomeTranscriber#GENOME_MUTATE_PROBABILITY} to determine
 * when to flip a bit
 */
public class MutationGenomeTranscriber implements GenomeTransciber {

    private static final Logger logger = Logger.getLogger(MutationGenomeTranscriber.class.getName());
    public static final String GENOME_MUTATE_PROBABILITY = "genome.mutate.probability";


    /**
     * Transcribe genome while randomly flipping bit
     *
     * @param properties     configuration properties
     * @param originalGenome source genome
     * @return modified genome
     */
    @Override
    public Genome transcribe(final UniverseConstants properties, final Genome originalGenome) {
        float mutationProbability = (float) 1 / properties.get(GENOME_MUTATE_PROBABILITY, Integer.class);
        final Genome genome = originalGenome.clone();
        mutate(genome, mutationProbability, null);

        return genome;
    }

    @VisibleForTesting
    void mutate(final Genome genome, final float probability, final Long seed) {
        Random random;
        if (null != seed) {
            random = new Random(seed);
        } else {
            random = new Random();
        }

        for (int i = 0; i < genome.getNumberOfGenes(); ++i) {
            Gene gene = genome.getGeneNumber(i);
            // Iterate through each gene in the genome
            if (random.nextFloat() < probability) {
                // If the random number is less than the mutation probability, perform mutation
                int bitToFlip = random.nextInt(32); // Assuming each nucleotide is a byte (8 bits)
                logger.info("Mutating gene " + gene + " position " + bitToFlip);
                flipBit(gene, bitToFlip);
            }
        }
    }

    @VisibleForTesting
    void flipBit(final Gene gene, final int bitIndex) {
        final int nucleotideIndex = bitIndex / 8; // Determine which nucleotide (A, B, C, or D)
        final int bitWithinNucleotide = (bitIndex % 8); // Determine the bit position within the nucleotide

        // Flip the bit at the specified index in the gene
        switch (nucleotideIndex) {
            case 0:
                gene.nucleotideA = flipBit(gene.nucleotideA, bitWithinNucleotide);
                break;
            case 1:
                gene.nucleotideB = flipBit(gene.nucleotideB, bitWithinNucleotide);
                break;
            case 2:
                gene.nucleotideC = flipBit(gene.nucleotideC, bitWithinNucleotide);
                break;
            case 3:
                gene.nucleotideD = flipBit(gene.nucleotideD, bitWithinNucleotide);
                break;
            default:
        }
    }

    @VisibleForTesting
    byte flipBit(final byte value, final int bitIndex) {
        if (bitIndex < 0 || bitIndex >= 8) {
            throw new IllegalArgumentException("Invalid bit index");
        }
        return (byte) (value ^ (1 << bitIndex));

    }
}
