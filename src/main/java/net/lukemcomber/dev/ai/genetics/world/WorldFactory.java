package net.lukemcomber.dev.ai.genetics.world;

import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.FlatWorld;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.FlatWorldResourceManager;

public class WorldFactory {

    public static Terrain createWorld(final String type) {
        Terrain world;
        switch (type) {
            case FlatWorld.ID:
                world = new FlatWorld();
                System.out.println("Allocated a new " + FlatWorld.ID);
                break;
            default:
                throw new EvolutionException("World type [" + type + "] not recognized.");
        }
        return world;
    }
}
