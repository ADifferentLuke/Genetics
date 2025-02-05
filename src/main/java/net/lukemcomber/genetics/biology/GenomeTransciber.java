package net.lukemcomber.genetics.biology;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.model.UniverseConstants;

/**
 * An interface for genome transcription
 */
public interface GenomeTransciber {

    /**
     * Transcribe one genome into another.
     *
     * @param properties configuration properties
     * @param genome     source genome
     * @return destination genome
     */
    Genome transcribe(final Genome genome);
}
