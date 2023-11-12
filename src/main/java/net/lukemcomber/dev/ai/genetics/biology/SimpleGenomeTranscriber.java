package net.lukemcomber.dev.ai.genetics.biology;

import java.util.LinkedList;

public abstract class SimpleGenomeTranscriber implements GenomeTransciber{
    @Override
    public Genome transcribe(final Genome genome) {
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
        return createGenome(newGenome);
    }
    protected abstract byte transcribe(final byte b);

    protected abstract Genome createGenome(final LinkedList<Gene> genes);
}
