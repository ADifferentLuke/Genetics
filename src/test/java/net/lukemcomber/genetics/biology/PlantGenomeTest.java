package net.lukemcomber.genetics.biology;

import net.lukemcomber.genetics.biology.plant.PlantGenome;
import net.lukemcomber.genetics.service.GenomeStdOutWriter;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Random;

@Test
public class PlantGenomeTest {

    public static final int GENE_COUNT = 20;

    public void testGenomeWithBehavior(){

        final ArrayList<Gene> genes = new ArrayList<>(GENE_COUNT);

        // Create genes for the desired actions
        genes.add(createGene(PlantGenome.GeneExpression.GROW_LEAF_LEFT.value()));
        genes.add(createGene(PlantGenome.GeneExpression.GROW_LEAF_RIGHT.value()));
        genes.add(createGene(PlantGenome.GeneExpression.EJECT_SEED_UP.value()));

        for (int i = 3; i < GENE_COUNT; i++) {
            genes.add(createGene((byte) 0b11111));  // assuming 0b11111 represents junk DNA
        }

        System.out.println( "Ejecting genome:");
        PlantGenome genome = new PlantGenome(genes);
        GenomeStdOutWriter.hexPrintGenomeToStdOut(genome);



    }

    private static Gene createGene(byte action) {
        Gene gene = new Gene();
        gene.nucleotideA = action;
        gene.nucleotideB = 0;
        gene.nucleotideC = 0;
        gene.nucleotideD = 0;
        return gene;
    }



    public void testGenomeCreation(){

        final ArrayList<Gene> genes = new ArrayList<>(GENE_COUNT);
        for( int i = 0; GENE_COUNT > i; ++i ){
            final Gene gene = new Gene();
            gene.nucleotideA = PlantGenome.GeneExpression.GROW_LEAF_UP.value();
            gene.nucleotideB = PlantGenome.GeneExpression.GROW_LEAF_UP.value();
            gene.nucleotideC = PlantGenome.GeneExpression.GROW_ROOT_DOWN.value();
            gene.nucleotideD = PlantGenome.GeneExpression.GROW_SEED_LEFT.value();

            genes.add(gene);
        }
        final PlantGenome genome = new PlantGenome(genes);

        Gene gene = genome.getGeneNumber(0);
        assert(PlantGenome.GeneExpression.GROW_LEAF_UP.value() == gene.nucleotideA);

        System.out.println( "Created: ");
        GenomeStdOutWriter.hexPrintGenomeToStdOut(genome);

        final ArrayList<Gene> genome2 = new ArrayList<>(GENE_COUNT);

        final Random rng = new Random(1337l); //constant seed
        for (int i = 0; GENE_COUNT > i; ++i) {

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
