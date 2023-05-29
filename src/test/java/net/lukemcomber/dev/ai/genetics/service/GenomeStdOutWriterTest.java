package net.lukemcomber.dev.ai.genetics.service;

import net.lukemcomber.dev.ai.genetics.biology.Gene;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantGenome;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Random;

@Test
public class GenomeStdOutWriterTest {

    public void test() {
        final ArrayList<Gene> genome = new ArrayList<>(PlantGenome.GENE_COUNT);

        final Random rng = new Random(1337l); //constant seed
        for (int i = 0; PlantGenome.GENE_COUNT > i; ++i) {

            final Gene newGene = new Gene();
            newGene.nucleotideA = (byte) rng.nextInt(127);
            newGene.nucleotideB = (byte) rng.nextInt(127);
            newGene.nucleotideC = (byte) rng.nextInt(127);
            newGene.nucleotideD = (byte) rng.nextInt(127);

            genome.add(newGene);
        }

        System.out.println( );
        final PlantGenome plantGenome = new PlantGenome(genome);
        GenomeStdOutWriter.prettyPrintGenomeToStdOut(plantGenome);

        System.out.println( );
        GenomeStdOutWriter.hexPrintGenomeToStdOut(plantGenome);

        System.out.println( );
        GenomeStdOutWriter.binaryPrintGenomeToStdOut(plantGenome);

    }
}
