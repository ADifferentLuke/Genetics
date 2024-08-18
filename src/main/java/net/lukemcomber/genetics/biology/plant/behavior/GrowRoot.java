package net.lukemcomber.genetics.biology.plant.behavior;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.genetics.biology.plant.cells.RootCell;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.util.function.Function;

/**
 * Grows a root cell
 */
public class GrowRoot implements PlantBehavior {

    public final static String PROPERTY_GROW_ROOT_COST = "action.root.grow";

    private final Function<SpatialCoordinates, SpatialCoordinates> function;

    /**
     * Create a new instance with a callback to updated cell location
     *
     * @param func
     */
    public GrowRoot(final Function<SpatialCoordinates, SpatialCoordinates> func) {
        this.function = func;
    }

    /**
     * Attempts to grow a new root cell
     *
     * @param terrain             the terrain
     * @param rootCell            cell performing the action
     * @param temporalCoordinates time
     * @param metadataStoreGroup  metadata cache
     * @return a root cell
     */
    @Override
    public Cell performAction(final UniverseConstants properties, final Terrain terrain, final Organism organism, final Cell rootCell,
                              final TemporalCoordinates temporalCoordinates, final MetadataStoreGroup metadataStoreGroup) {
        Cell retVal = null;
        final SpatialCoordinates newSpatialCoordinates = function.apply(rootCell.getCoordinates());
        if (!(terrain.isOutOfBounds(newSpatialCoordinates) || terrain.hasCell(newSpatialCoordinates))) {
            final RootCell newCell = new RootCell(rootCell, newSpatialCoordinates, terrain.getProperties());
            terrain.setCell(newCell, organism);
            rootCell.addChild(newCell);
            retVal = newCell;
        } else {
            throw new EvolutionException("Root growth failed. Collision detected.");
        }
        organism.spendEnergy(getEnergyCost(properties));

        return retVal;
    }

    /**
     * Get the cost in energy units to perform this behavior
     *
     * @param properties configuration properties
     * @return cost
     */
    @Override
    public int getEnergyCost(final UniverseConstants properties) {
        return properties.get(PROPERTY_GROW_ROOT_COST, Integer.class);
    }
}
