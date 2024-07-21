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

public interface PlantBehavior {

    Cell performAction(final UniverseConstants properties, final Terrain terrain,
                       final Organism organism,
                       final Cell activeCell,
                       final TemporalCoordinates temporalCoordinates,
                       final MetadataStoreGroup metadataStoreGroup);

    int getEnergyCost(final UniverseConstants properties);
}
