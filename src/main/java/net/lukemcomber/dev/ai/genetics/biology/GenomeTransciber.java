package net.lukemcomber.dev.ai.genetics.biology;

import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;

public interface GenomeTransciber {

    Genome transcribe(final UniverseConstants properties, final Genome genome );
}
