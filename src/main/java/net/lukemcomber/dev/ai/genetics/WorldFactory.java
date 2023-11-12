package net.lukemcomber.dev.ai.genetics;

import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;
import net.lukemcomber.dev.ai.genetics.universes.FlatFloraUniverse;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.FlatWorld;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.FlatWorldResourceManager;

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
