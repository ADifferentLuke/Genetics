package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Gene;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.SimpleGenomeTranscriber;

import java.util.LinkedList;

public class PlantGenomeTranscriber extends SimpleGenomeTranscriber {

    @Override
    protected byte transcribe(byte b) {
        return b;
    }

    @Override
    protected Genome createGenome(LinkedList<Gene> genes) {
        return new PlantGenome(genes);
    }
}
