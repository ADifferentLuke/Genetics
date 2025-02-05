package net.lukemcomber.genetics.biology.transcription;

import com.google.common.collect.ImmutableMap;
import net.lukemcomber.genetics.model.UniverseConstants;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Random;

import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

@Test
public class AsexualTransposeGenomeTranscriberTest {

    public static class TestUniverse extends UniverseConstants {
        public TestUniverse(Map<String, Object> map) {
            super(map);
        }
    }

    final MutationGenomeTranscriberTest.TestUniverse testUniverse = new MutationGenomeTranscriberTest.TestUniverse(ImmutableMap.of(
            MutationGenomeTranscriber.GENOME_MUTATE_PROBABILITY, 1,
            MutationGenomeTranscriber.MUTATION_RAND_SEED, 1337
    ));
    public void test() {

        final byte[] b = generateRandomChromosome(16);
        final Random rng = new Random(1337);
        int geneT = rng.nextInt(b.length);

        final byte[] copy = new byte[b.length];
        System.arraycopy(b, 0, copy, 0, b.length);

        AsexualTransposGenomeTranscriber transcriber = new AsexualTransposGenomeTranscriber(testUniverse);

        System.out.println("geneT: " + geneT);

        System.out.println("Original Chromosome: " + AsexualTransposGenomeTranscriber.byteArrayToString(b));
        final byte[] n = transcriber.asexualTransposition(b, 2, geneT);
        System.out.println("Child Chromosome " + AsexualTransposGenomeTranscriber.byteArrayToString(n));

        assertArrayEquals(b, copy);

        byte[] chromosome = new byte[]{0b10, 0b10, 0b1001, 0b1100};
        final int geneT2 = 1;

        final byte[] result = transcriber.asexualTransposition(chromosome, 2, geneT2);
        System.out.println( "Finished.");


    }



    private static byte[] generateRandomChromosome(int length) {
        byte[] chromosome = new byte[length];
        new Random().nextBytes(chromosome);
        for (int i = 0; i < length; i++) {
            chromosome[i] = (byte) (chromosome[i] & 1); // Ensure values are 0 or 1
        }
        return chromosome;
    }
}
