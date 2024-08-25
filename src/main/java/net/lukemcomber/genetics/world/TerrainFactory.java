package net.lukemcomber.genetics.world;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.world.terrain.impl.FlatWorld;
import net.lukemcomber.genetics.world.terrain.Terrain;

/**
 * A factory for creating new {@link Terrain}
 */
public class TerrainFactory {

    /**
     * Build a new {@link Terrain} from the given {@link UniverseConstants} and {@link MetadataStoreGroup}
     *
     * @param properties         configuration properties
     * @param metadataStoreGroup metadata store group
     * @return a ne {@link Terrain}
     */
    public static Terrain create(final SpatialCoordinates spatialBounds, final UniverseConstants properties, final MetadataStoreGroup metadataStoreGroup) {
        Terrain world;
        final String worldType = properties.get(Terrain.PROPERTY_TERRAIN_TYPE, String.class);
        switch (worldType) {
            case FlatWorld.ID:
                world = new FlatWorld(spatialBounds, properties, metadataStoreGroup);
                break;
            default:
                throw new EvolutionException("World type [" + worldType + "] not recognized.");
        }
        return world;
    }
}
