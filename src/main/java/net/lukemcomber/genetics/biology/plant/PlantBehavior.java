package net.lukemcomber.genetics.biology.plant;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.world.terrain.Terrain;

/**
 * An interface for converting DNA to behavior
 */
public interface PlantBehavior {

    /**
     * Perform the appropriate behavior
     *
     * @param properties          configuration properties
     * @param terrain             the terrain
     * @param organism            the organism acting
     * @param activeCell          the cell to perform the behavior
     * @param temporalCoordinates the time
     * @param metadataStoreGroup  metadata cache
     * @return A new cell or null
     */
    Cell performAction(final UniverseConstants properties, final Terrain terrain,
                       final Organism organism,
                       final Cell activeCell,
                       final TemporalCoordinates temporalCoordinates,
                       final MetadataStoreGroup metadataStoreGroup);

    /**
     * Get the cost in energy units to perform this behavior
     *
     * @param properties configuration properties
     * @return cost
     */
    int getEnergyCost(final UniverseConstants properties);
}
