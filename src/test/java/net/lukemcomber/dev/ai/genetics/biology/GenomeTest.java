package net.lukemcomber.dev.ai.genetics.biology;


import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantGenome;
import net.lukemcomber.dev.ai.genetics.service.GenomeStdOutWriter;
import org.testng.annotations.Test;

import java.util.LinkedList;

@Test
public class GenomeTest {



    public void testGeneOrder(){
        final LinkedList<Gene> geneList = new LinkedList<>();
        for( int i = 0; 20 > i; ++i ){
            Gene gene = new Gene();
            gene.nucleotideA = (byte)i;
            gene.nucleotideB = (byte)i;
            gene.nucleotideC = (byte)i;
            gene.nucleotideD = (byte)i;
            geneList.addLast(gene);
        }

        final Genome genome = new Genome(geneList, "test") {
            @Override
            public PlantBehavior getNextAct() {
                return null;
            }
        };


        Gene gene = genome.getGeneNumber(13);
        assert(13 == gene.nucleotideA);

        Gene newGene = new Gene();
        newGene.nucleotideA = 23;
        newGene.nucleotideB = 23;
        newGene.nucleotideC = 23;
        newGene.nucleotideD = 23;

        genome.setGeneNumber(13, newGene );

        Gene gene12 = genome.getGeneNumber( 12 );
        assert( 12 == gene12.nucleotideA);
        Gene gene13 = genome.getGeneNumber( 13 );
        assert( 23 == gene13.nucleotideA);
        Gene gene14 = genome.getGeneNumber( 14 );
        assert( 14 == gene14.nucleotideA);
    }
}
