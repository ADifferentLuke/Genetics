package net.lukemcomber.dev.ai.genetics.biology.transcription;

import net.lukemcomber.dev.ai.genetics.biology.Gene;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.GenomeTransciber;
import net.lukemcomber.dev.ai.genetics.biology.OrganismFactory;
import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;

import java.util.LinkedList;

public class IdentityGenomeTranscriber implements GenomeTransciber {
    @Override
    public Genome transcribe(final UniverseConstants properties, final Genome genome) {
        final LinkedList<Gene> newGenome = new LinkedList<>();
        for( int i = 0; genome.getNumberOfGenes() > i; ++i ){
            final Gene originalGene = genome.getGeneNumber(i);
            final Gene newGene = new Gene();

            newGene.nucleotideA = transcribe(originalGene.nucleotideA);
            newGene.nucleotideB = transcribe(originalGene.nucleotideB);
            newGene.nucleotideC = transcribe(originalGene.nucleotideC);
            newGene.nucleotideD = transcribe(originalGene.nucleotideD);

            newGenome.addLast(newGene);
        }
        return OrganismFactory.createGenome(genome.getType(), newGenome);
    }
    protected byte transcribe(final byte b){
        return b;
    }

}
