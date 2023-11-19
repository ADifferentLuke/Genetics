package net.lukemcomber.genetics;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.universes.FlatFloraUniverse;
import net.lukemcomber.genetics.world.terrain.impl.FlatWorld;
import net.lukemcomber.genetics.world.terrain.Terrain;

public class WorldFactory {

    public static Terrain createWorld(final String type) {
        Terrain world;
        switch (type) {
            case FlatFloraUniverse.ID:
                final UniverseConstants universe = new FlatFloraUniverse();
                world = new FlatWorld(universe);
                System.out.println("Allocated a new " + FlatWorld.ID);
                break;
            default:
                throw new EvolutionException("World type [" + type + "] not recognized.");
        }
        return world;
    }
}
