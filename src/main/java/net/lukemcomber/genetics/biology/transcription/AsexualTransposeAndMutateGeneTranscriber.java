package net.lukemcomber.genetics.biology.transcription;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Genome;
import net.lukemcomber.genetics.biology.GenomeTransciber;
import net.lukemcomber.genetics.model.UniverseConstants;

/**
 * A genome transcriber that supports both {@link AsexualTransposGenomeTranscriber}
 * and {@link MutationGenomeTranscriber}
 */
public class AsexualTransposeAndMutateGeneTranscriber implements GenomeTransciber {

    private final AsexualTransposGenomeTranscriber asexualTransposGenomeTranscriber;
    private final MutationGenomeTranscriber mutationGenomeTranscriber;

    /**
     * Creates a new instance
     */
    public AsexualTransposeAndMutateGeneTranscriber() {
        asexualTransposGenomeTranscriber = new AsexualTransposGenomeTranscriber();
        mutationGenomeTranscriber = new MutationGenomeTranscriber();
    }

    /**
     * Transcribe genome while randomly mutating and transposing
     *
     * @param properties configuration properties
     * @param genome     source genome
     * @return modified genome
     */
    @Override
    public Genome transcribe(final UniverseConstants properties, final Genome genome) {

        final Genome transposedGenome = asexualTransposGenomeTranscriber.transcribe(properties, genome);
        final Genome mutatedGenome = mutationGenomeTranscriber.transcribe(properties, transposedGenome);

        return mutatedGenome;
    }
}
