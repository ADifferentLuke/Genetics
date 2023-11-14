package net.lukemcomber.dev.ai.genetics.biology.transcription;

import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.GenomeTransciber;
import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;

import java.util.LinkedList;
import java.util.Random;

public class AsexualTransposGenomeTranscriber implements GenomeTransciber {

    public static final String GENOME_TRANSPOSE_PROBABILITY = "genome.transpose.probability";
    public static final String GENOME_TRANSPOSE_FLANK_LENGTH = "genome.transpose.flank-length";

    //Optional for determinism
    public static final String GENOME_TRANSPOSE_SEED = "genome.transpose.seed";

    private Random rng;
    @Override
    public Genome transcribe(final UniverseConstants properties, final Genome genome) {
        final float probability = 1 / properties.get(GENOME_TRANSPOSE_PROBABILITY, Float.class);
        final int flankLength = properties.get(GENOME_TRANSPOSE_FLANK_LENGTH, Integer.class, 2);
        final int randomSeed = properties.get( GENOME_TRANSPOSE_SEED, Integer.class, 0 );

        if( null == rng ){
           if( 0 < randomSeed ){
               rng = new Random(randomSeed);
           } else {
               rng = new Random();
           }
        }

        final float rngResult = rng.nextFloat();
        if( rng.nextFloat() <= probability ){
            //Transposition time!!!
        }

        return genome;
    }
}
