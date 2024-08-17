package net.lukemcomber.genetics.universes;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.UniverseConstants;

/**
 * Factory class for building new {@link UniverseConstants}
 */
public class UniverseFactory {

    /**
     * Return of new {@link UniverseConstants} of the given type
     *
     * @param type type of world
     * @return configuration properties
     */
    public static UniverseConstants get(final String type) {
        final UniverseConstants retVal;
        switch (type) {
            case FlatFloraUniverse.ID -> {
                retVal = new FlatFloraUniverse();
            }
            default -> throw new EvolutionException("Unknown universe " + type);
        }
        return retVal;
    }
}
