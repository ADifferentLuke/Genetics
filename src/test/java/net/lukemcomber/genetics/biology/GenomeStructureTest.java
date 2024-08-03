package net.lukemcomber.genetics.biology;


import com.google.common.collect.ImmutableList;
import net.lukemcomber.genetics.biology.plant.PlantBehavior;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

@Test
public class GenomeStructureTest {


    // Helper method to generate a random chromosome for testing

    public void genomeByteArrayTest(){
        // Sample Gene objects and their byte arrays
        Gene gene1 = new Gene();
        gene1.nucleotideA = 1;
        gene1.nucleotideB = 2;
        gene1.nucleotideC = 3;
        gene1.nucleotideD = 4;

        Gene gene2 = new Gene();
        gene2.nucleotideA = 5;
        gene2.nucleotideB = 6;
        gene2.nucleotideC = 7;
        gene2.nucleotideD = 8;

        Gene gene3 = new Gene();
        gene3.nucleotideA = 9;
        gene3.nucleotideB = 10;
        gene3.nucleotideC = 11;
        gene3.nucleotideD = 12;

        final List<Gene> geneArray = ImmutableList.of( gene1, gene2, gene3 );

        // Expected result: [1, 2, 3, 4, 5, 6, 7, 8, 9]
        byte[] expectedResult = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

        // Call the method to concatenate byte arrays
        byte[] resultByteArray = Genome.toBytes(geneArray);

        // Assert that the result matches the expected result
        assertArrayEquals(expectedResult, resultByteArray);
    }

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

        final Genome genome = new TestGenome(geneList);


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
