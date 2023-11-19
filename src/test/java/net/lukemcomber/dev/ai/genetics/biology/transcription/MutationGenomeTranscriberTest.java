package net.lukemcomber.dev.ai.genetics.biology.transcription;


import net.lukemcomber.dev.ai.genetics.biology.Gene;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import org.testng.annotations.Test;

import java.util.logging.Logger;

import static org.testng.AssertJUnit.assertEquals;

@Test
public class MutationGenomeTranscriberTest {
    private static final Logger logger = Logger.getLogger(MutationGenomeTranscriber.class.getName());

    public void testMutatesGenome() {
        // Create a mock implementation of Genome for testing
        Genome genome = new Genome(3, "MockGenome") {
            @Override
            public PlantBehavior getNextAct() {
                // Mock implementation, not needed for this test
                return null;
            }
        };

        logger.info("Testing mutation with 100% probability ...... ");
        // Set known initial values for the genes (for easier testing)
        for (int i = 0; i < genome.getNumberOfGenes(); i++) {
            Gene gene = genome.getGeneNumber(i);
            gene.nucleotideA = 0;
            gene.nucleotideB = 0;
            gene.nucleotideC = 0;
            gene.nucleotideD = 0;
        }


        // Set the random seed to ensure consistent test results
        // In a real scenario, you might not want to set the seed to allow for true randomness
        long seed = 123;

        // Set a specific bit to be flipped
        int bitToFlip = 7;

        /*
         * bitToFlip matches the results of random.nextInt(32) inside MutationGenomeTranscriber
         *  when using seed 123
         */
        MutationGenomeTranscriber transcriber = new MutationGenomeTranscriber();

        transcriber.mutate(genome, 1.0f, seed);

        byte newValue = genome.getGeneNumber(0).nucleotideA;

        byte expectedValue = (byte)(1 << ( bitToFlip % 8 ));

        assertEquals(expectedValue,newValue);


        logger.info("Testing mutation with 0% probability ...... ");
        // Reset the gene values for the next test
        for (int i = 0; i < genome.getNumberOfGenes(); i++) {
            Gene gene = genome.getGeneNumber(i);
            gene.nucleotideA = 0;
            gene.nucleotideB = 0;
            gene.nucleotideC = 0;
            gene.nucleotideD = 0;
        }

        // Set the random seed to ensure consistent test results
        // In a real scenario, you might not want to set the seed to allow for true randomness
        seed = 456;
        transcriber.mutate(genome, 0.0f, seed); // Mutate with 0% probability for testing
        assertEquals(0, getBitValue(genome.getGeneNumber(0), bitToFlip));
    }

    public void testBitFlip() {
        logger.info("Testing raw bitFlip logic in mutations ...... ");
        MutationGenomeTranscriber transcriber = new MutationGenomeTranscriber();
        byte TWO = 0b10;
        byte THREE = 0b11;
        byte shouldBeThree = transcriber.flipBit(TWO, 0);

        assertEquals(THREE, shouldBeThree);
    }

    public void testGeneFlip() {
        logger.info("Testing gene bitFlip logic in mutations ...... ");
        MutationGenomeTranscriber transcriber = new MutationGenomeTranscriber();

        Genome genome = new Genome(3, "MockGenome") {
            @Override
            public PlantBehavior getNextAct() {
                // Mock implementation, not needed for this test
                return null;
            }
        };
        byte TWO = 0b10;
        byte THREE = 0b11;
        byte TEN = 0b1010;


        for (int i = 0; i < genome.getNumberOfGenes(); i++) {
            Gene gene = genome.getGeneNumber(i);
            gene.nucleotideA = TWO;
            gene.nucleotideB = THREE;
            gene.nucleotideC = 0b10001;
            gene.nucleotideD = 0;
        }

        transcriber.flipBit(genome.getGeneNumber(0), 0);

        assertEquals(THREE, genome.getGeneNumber(0).nucleotideA);

        transcriber.flipBit(genome.getGeneNumber(2), 17);

        assertEquals(19, genome.getGeneNumber(2).nucleotideC);

        transcriber.flipBit(genome.getGeneNumber(2), 24);
        assertEquals(1, genome.getGeneNumber(2).nucleotideD);
    }

    public void testGetBitValue() {
        Gene gene = new Gene();
        gene.nucleotideA = (byte) 0b10101010; // Binary representation for testing
        gene.nucleotideB = (byte) 0b11001100;
        gene.nucleotideC = (byte) 0b11110001;
        gene.nucleotideD = (byte) 0b00001111;

        byte r = getBitValue(gene, 0);
        assertEquals(0, r);
        assertEquals(1, getBitValue(gene, 7));
        assertEquals(0, getBitValue(gene, 8));

        // Test specific bit positions in each nucleotide
        assertEquals(0, getBitValue(gene, 0)); // Gene.nucleotideA, bit 7
        assertEquals(1, getBitValue(gene, 1)); // Gene.nucleotideA, bit 6
        assertEquals(0, getBitValue(gene, 8)); // Gene.nucleotideB, bit 7
        assertEquals(0, getBitValue(gene, 9)); // Gene.nucleotideB, bit 6
        assertEquals(1, getBitValue(gene, 16)); // Gene.nucleotideC, bit 7
        assertEquals(0, getBitValue(gene, 17)); // Gene.nucleotideC, bit 6
    }

    private byte getBitValue(Gene value, int bitIndex) {
        int nucleotideIndex = bitIndex / 8; // Determine which nucleotide (A, B, C, or D)
        int bitWithinNucleotide = (bitIndex % 8); // Determine the bit position within the nucleotide
        switch (nucleotideIndex) {
            case 0:
                return (byte) (value.nucleotideA >>> bitWithinNucleotide & 1);
            case 1:
                return (byte) (value.nucleotideB >>> bitWithinNucleotide & 1);
            case 2:
                return (byte) (value.nucleotideC >>> bitWithinNucleotide & 1);
            case 3:
                return (byte) (value.nucleotideD >>> bitWithinNucleotide & 1);
            default:
                return (byte) 0;
        }
    }
}
