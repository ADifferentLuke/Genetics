package net.lukemcomber.dev.ai.genetics.biology.plant.behavior;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

import java.util.function.Function;
import java.util.logging.Logger;

public class GrowSeed implements PlantBehavior {

    private Logger logger = Logger.getLogger(GrowSeed.class.toString());


    private final Function<SpatialCoordinates, SpatialCoordinates> function;

    public GrowSeed(final Function<SpatialCoordinates, SpatialCoordinates> func){
        this.function = func;
    }
    /**
     * @param terrain
     * @param cell
     * @return
     */
    @Override
    public Cell performAction(final Terrain terrain,final Cell cell, final Organism organism) {
        Cell retVal = null;

        final SpatialCoordinates newSpatialCoordinates = function.apply(cell.getCoordinates());


        //The boolean logic looks weird, but we need to use AND for short circuit
        if((!terrain.isOutOfBounds(newSpatialCoordinates)) && (!terrain.hasCell(newSpatialCoordinates))){

            //TODO MUTATION NEEDED HERE

            if( null != organism ) {
                //Organism shouldn't be null, but we're in mid-redesign ... so blow up if it happens
                final SeedCell newCell = new SeedCell(cell, organism.getGenome(), newSpatialCoordinates);
                logger.info("Created new seed: " + organism.getUniqueID() + " at " + newSpatialCoordinates);
                retVal = newCell;
            } else {
                throw new RuntimeException("Organism is null!");
            }
        } else {
            throw new EvolutionException("Seed growth failed. Collision detected.");
        }

        return retVal;
    }


    /**
     * @return
     */
    @Override
    public int getEnergyCost() {
        return 10;
    }
}
