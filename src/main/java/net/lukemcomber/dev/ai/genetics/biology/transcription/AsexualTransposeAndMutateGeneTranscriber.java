package net.lukemcomber.dev.ai.genetics.biology.transcription;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.GenomeTransciber;
import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;

public class AsexualTransposeAndMutateGeneTranscriber implements GenomeTransciber {

    private final AsexualTransposGenomeTranscriber asexualTransposGenomeTranscriber;
    private final MutationGenomeTranscriber mutationGenomeTranscriber;

    public AsexualTransposeAndMutateGeneTranscriber(){
        asexualTransposGenomeTranscriber = new AsexualTransposGenomeTranscriber();
        mutationGenomeTranscriber = new MutationGenomeTranscriber();
    }
    @Override
    public Genome transcribe(final UniverseConstants properties, final Genome genome) {

        final Genome transposedGenome = asexualTransposGenomeTranscriber.transcribe(properties,genome);
        final Genome mutatedGenome = mutationGenomeTranscriber.transcribe(properties,transposedGenome);

        return mutatedGenome;
    }
}
