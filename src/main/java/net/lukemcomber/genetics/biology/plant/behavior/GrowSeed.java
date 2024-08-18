package net.lukemcomber.genetics.biology.plant.behavior;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.world.terrain.Terrain;
import net.lukemcomber.genetics.biology.GenomeTransciber;

import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Grows a seed on the organism
 */
public class GrowSeed implements PlantBehavior {


    public final static String PROPERTY_GROW_SEED_COST = "action.seed.grow";

    private Logger logger = Logger.getLogger(GrowSeed.class.toString());

    private final Function<SpatialCoordinates, SpatialCoordinates> function;

    /**
     * Create a new instance with a callback to updated cell location
     *
     * @param func
     */
    public GrowSeed(final Function<SpatialCoordinates, SpatialCoordinates> func) {
        this.function = func;
    }

    /**
     * Attempts to grow a new seed cell
     *
     * @param terrain             the terrain
     * @param cell                cell performing the action
     * @param temporalCoordinates time
     * @param metadataStoreGroup  metadata cache
     * @return a seed cell
     */
    @Override
    public Cell performAction(final UniverseConstants properties, final Terrain terrain, final Organism organism, final Cell cell,
                              final TemporalCoordinates temporalCoordinates, final MetadataStoreGroup metadataStoreGroup) {
        final long cur = System.currentTimeMillis();
        Cell retVal = null;

        final SpatialCoordinates newSpatialCoordinates = function.apply(cell.getCoordinates());


        //The boolean logic looks weird, but we need to use AND for short circuit
        if ((!terrain.isOutOfBounds(newSpatialCoordinates)) && (!terrain.hasCell(newSpatialCoordinates))) {

            if (null != organism) {
                //Organism shouldn't be null, but we're in mid-redesign ... so blow up if it happens

                final GenomeTransciber transciber = organism.getTranscriber();

                /*
                 * DEV NOTE: This is where mutation is initiated!
                 */
                final SeedCell newCell = new SeedCell(cell, transciber.transcribe(terrain.getProperties(),
                        organism.getGenome()), newSpatialCoordinates, terrain.getProperties());
                logger.info("Created new seed: " + organism.getUniqueID() + " at " + newSpatialCoordinates);
                cell.addChild(newCell);
                terrain.setCell(newCell, organism);
                retVal = newCell;
            } else {
                throw new RuntimeException("Organism is null!");
            }
        } else {
            throw new EvolutionException("Seed growth failed. Collision detected.");
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
        return properties.get(PROPERTY_GROW_SEED_COST, Integer.class);
    }
}
