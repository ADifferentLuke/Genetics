package net.lukemcomber.genetics;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.universes.FlatFloraUniverse;
import net.lukemcomber.genetics.world.terrain.impl.FlatWorld;
import net.lukemcomber.genetics.world.terrain.Terrain;

public class WorldFactory {

    public static Terrain createWorld(final UniverseConstants properties , final MetadataStoreGroup metadataStoreGroup) {
        Terrain world;
        final String worldType = properties.get(Terrain.PROPERTY_TERRAIN_TYPE,String.class);
        switch (worldType) {
            case FlatWorld.ID:
                world = new FlatWorld(properties, metadataStoreGroup);
                break;
            default:
                throw new EvolutionException("World type [" + worldType + "] not recognized.");
        }
        return world;
    }
}
