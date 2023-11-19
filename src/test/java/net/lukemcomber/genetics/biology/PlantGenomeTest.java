package net.lukemcomber.genetics.biology;

import net.lukemcomber.genetics.biology.plant.PlantGenome;
import net.lukemcomber.genetics.service.GenomeStdOutWriter;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Random;

@Test
public class PlantGenomeTest {



    public void testGenomeCreation(){

        final ArrayList<Gene> genes = new ArrayList<>(PlantGenome.GENE_COUNT);
        for( int i = 0; PlantGenome.GENE_COUNT > i; ++i ){
            final Gene gene = new Gene();
            gene.nucleotideA = PlantGenome.GROW_LEAF_UP;
            gene.nucleotideB = PlantGenome.GROW_LEAF_UP;
            gene.nucleotideC = PlantGenome.GROW_ROOT_DOWN;
            gene.nucleotideD = PlantGenome.GROW_SEED_LEFT;

            genes.add(gene);
        }
        final PlantGenome genome = new PlantGenome(genes);

        Gene gene = genome.getGeneNumber(0);
        assert(PlantGenome.GROW_LEAF_UP == gene.nucleotideA);

        System.out.println( "Created: ");
        GenomeStdOutWriter.hexPrintGenomeToStdOut(genome);

        final ArrayList<Gene> genome2 = new ArrayList<>(PlantGenome.GENE_COUNT);

        final Random rng = new Random(1337l); //constant seed
        for (int i = 0; PlantGenome.GENE_COUNT > i; ++i) {

            final Gene newGene = new Gene();
            newGene.nucleotideA = (byte) rng.nextInt(127);
            newGene.nucleotideB = (byte) rng.nextInt(127);
            newGene.nucleotideC = (byte) rng.nextInt(127);
            newGene.nucleotideD = (byte) rng.nextInt(127);

            genome2.add(newGene);
        }

        System.out.println( );
        final PlantGenome plantGenome = new PlantGenome(genome2);
        GenomeStdOutWriter.hexPrintGenomeToStdOut(plantGenome);


    }
    public void generateGenome(){

    }
}
