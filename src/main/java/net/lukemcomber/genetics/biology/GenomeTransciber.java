package net.lukemcomber.genetics.biology;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.model.UniverseConstants;

public interface GenomeTransciber {

    Genome transcribe(final UniverseConstants properties, final Genome genome );
}
