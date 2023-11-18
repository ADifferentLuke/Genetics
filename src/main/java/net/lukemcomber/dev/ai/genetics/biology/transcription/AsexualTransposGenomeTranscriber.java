package net.lukemcomber.dev.ai.genetics.biology.transcription;

import com.google.common.annotations.VisibleForTesting;
import net.lukemcomber.dev.ai.genetics.biology.Gene;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.GenomeTransciber;
import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

public class AsexualTransposGenomeTranscriber implements GenomeTransciber {

    private static final Logger logger = Logger.getLogger(AsexualTransposGenomeTranscriber.class.getName());
    public static final String GENOME_TRANSPOSE_PROBABILITY = "genome.transpose.probability";
    public static final String GENOME_TRANSPOSE_FLANK_LENGTH = "genome.transpose.flank-length";

    //Optional for determinism
    public static final String GENOME_TRANSPOSE_SEED = "genome.transpose.seed";

    private Random rng;

    @Override
    public Genome transcribe(final UniverseConstants properties, final Genome genome) {
        final float probability = 1 / properties.get(GENOME_TRANSPOSE_PROBABILITY, Integer.class);
        final int flankLength = properties.get(GENOME_TRANSPOSE_FLANK_LENGTH, Integer.class, 2);
        final int randomSeed = properties.get(GENOME_TRANSPOSE_SEED, Integer.class, 0);

        if (null == rng) {
            if (0 < randomSeed) {
                rng = new Random(randomSeed);
            } else {
                rng = new Random();
            }
        }

        if (rng.nextFloat() <= probability) {
            /*
             * We need to transpose. Pick a gene to mess with
             */
            final int geneNumber = rng.nextInt(genome.getNumberOfGenes());
            final Gene gene = genome.getGeneNumber(geneNumber);


            // Convert to binary array
            byte[] originalDna = gene.toBytes();
            int geneT = rng.nextInt(originalDna.length);

            logger.finest( "Select geneNume " + geneNumber );
            logger.finest( "DNA: " + byteArrayToString(originalDna));
            logger.finest( "Select geneT " + geneT);

            //do transcription
            final byte[] newByteArrGene = asexualTransposition(originalDna, flankLength, geneT);

            //set new gene in genome
            final Gene newGene = Gene.fromBytes(newByteArrGene);
            genome.setGeneNumber(geneT,newGene);
        }
        return genome;
    }

    @VisibleForTesting
    final byte[] asexualTransposition(final byte[] rawGeneData,final int flankingSize,final int geneT) {

        //Make a copy so we don't modify the original gene
        final byte[] chromosome = new byte[rawGeneData.length];
        System.arraycopy(rawGeneData, 0, chromosome, 0, rawGeneData.length);

        // Find our flanking sequence
        final int startFirstFlanking = (geneT - flankingSize + chromosome.length) % chromosome.length;
        final int endSecondFlanking = (geneT + flankingSize) % chromosome.length;

        byte[] transposon;
        if (startFirstFlanking < endSecondFlanking) {
            transposon = Arrays.copyOfRange(chromosome, startFirstFlanking, endSecondFlanking);
        } else {
            // Handle the circular structure
            final byte[] part1 = Arrays.copyOfRange(chromosome, startFirstFlanking, chromosome.length);
            final byte[] part2 = Arrays.copyOfRange(chromosome, 0, endSecondFlanking);

            transposon = Arrays.copyOf(part1, part1.length + part2.length);
            System.arraycopy(part2, 0, transposon, part1.length, part2.length);
        }

        final int insertionPoint = findInsertionPoint(chromosome, transposon, (endSecondFlanking + 1) % chromosome.length);
        // Excise the transposon
        for (int i = startFirstFlanking; i < endSecondFlanking; i++) {
            chromosome[i] = 0;
        }

        // Integrate the transposon at the insertion point
        for (int i = 0; i < transposon.length; i++) {
            chromosome[(insertionPoint + i) % chromosome.length] = transposon[i];
        }
        return chromosome;
    }

    private boolean isEqualOrInverse(final byte[] arr1, final byte[] arr2) {
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i] && arr1[i] + arr2[i] != 1) {
                return false;
            }
        }
        return true;
    }

    private int findInsertionPoint(final byte[] chromosome,final byte[] transposon,final int startIndex) {
        int insertionPoint = startIndex;

        while (!isEqualOrInverse(
                Arrays.copyOfRange(chromosome, insertionPoint, insertionPoint + transposon.length),
                transposon)) {
            insertionPoint = (insertionPoint + 1) % chromosome.length;

            // Break the loop if we have searched the entire chromosome
            if (insertionPoint == startIndex) {
                break;
            }
        }

        return insertionPoint;
    }


    // little method for debugging
    public static String byteArrayToString(byte[] array) {
        StringBuilder stringBuilder = new StringBuilder("[");
        for (byte b : array) {
            stringBuilder.append(b).append(", ");
        }
        stringBuilder.setLength(stringBuilder.length() - 2); // Remove the trailing comma and space
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
