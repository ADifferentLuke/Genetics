package net.lukemcomber.genetics.biology.transcription;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Gene;
import net.lukemcomber.genetics.biology.Genome;
import net.lukemcomber.genetics.biology.GenomeTransciber;
import net.lukemcomber.genetics.biology.OrganismFactory;
import net.lukemcomber.genetics.model.UniverseConstants;

import java.util.LinkedList;

public class IdentityGenomeTranscriber implements GenomeTransciber {
    @Override
    public Genome transcribe(final UniverseConstants properties, final Genome genome) {
        final Genome newGenome = genome.clone();
        for( int i = 0; genome.getNumberOfGenes() > i; ++i ){
            final Gene originalGene = genome.getGeneNumber(i);
            final Gene newGene = newGenome.getGeneNumber(i);

            newGene.nucleotideA = transcribe(originalGene.nucleotideA);
            newGene.nucleotideB = transcribe(originalGene.nucleotideB);
            newGene.nucleotideC = transcribe(originalGene.nucleotideC);
            newGene.nucleotideD = transcribe(originalGene.nucleotideD);

        }
        return newGenome;
    }
    protected byte transcribe(final byte b){
        return b;
    }

}
