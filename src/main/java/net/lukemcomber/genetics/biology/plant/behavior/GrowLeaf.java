package net.lukemcomber.genetics.biology.plant.behavior;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.genetics.biology.plant.cells.LeafCell;
import net.lukemcomber.genetics.biology.plant.cells.StemCell;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Grows a leaf cell
 */
public class GrowLeaf implements PlantBehavior {

    public final static String PROPERTY_GROW_LEAF_COST = "action.leaf.grow";
    private static final Logger logger = Logger.getLogger(GrowLeaf.class.getName());
    private final Function<SpatialCoordinates, SpatialCoordinates> function;

    /**
     * Create a new instance with a callback to updated cell location
     *
     * @param func
     */
    public GrowLeaf(final Function<SpatialCoordinates, SpatialCoordinates> func) {
        this.function = func;
    }

    /**
     * Attempts to grow a new leaf cell
     *
     * @param terrain             the terrain
     * @param cell                cell performing the action
     * @param temporalCoordinates time
     * @param metadataStoreGroup  metadata cache
     * @return a leaf cell
     */
    @Override
    public Cell performAction(final UniverseConstants properties, final Terrain terrain, final Organism organism, final Cell cell,
                              final TemporalCoordinates temporalCoordinates, final MetadataStoreGroup metadataStoreGroup) {

        Cell retVal = null;
        final SpatialCoordinates newSpatialCoordinates = function.apply(cell.getCoordinates());


        //The boolean logic looks weird, but we need to use AND for short circuit
        if ((!terrain.isOutOfBounds(newSpatialCoordinates)) && (!terrain.hasCell(newSpatialCoordinates))) {
            Cell parentCell = cell;
            if (cell instanceof LeafCell) {
                //TODO what if leaf's parent is null?
                logger.info("\tWe need to create a stem!");
                final Cell grandParentCell = cell.getParent();
                logger.info("Grandparent cell is: " + grandParentCell.getCellType() + " at " + grandParentCell.getCoordinates());
                final StemCell stemCell = new StemCell(grandParentCell, cell.getCoordinates(), terrain.getProperties());

                logger.info("Stem created at " + stemCell.getCoordinates());

                //body swap!
                grandParentCell.removeChild(cell);
                grandParentCell.addChild(stemCell);

                logger.info("Removed cell " + cell.getCellType() + " from grandparent");
                logger.info("Added cell " + stemCell.getCellType() + " from grandparent");


                //Now give all the child to the stem, even though there really shouldn't be any yet
                for (final Cell childChld : cell.getChildren()) {
                    stemCell.addChild(childChld);
                    logger.info("Copied child " + childChld.getCellType() + " at " + childChld.getCoordinates());
                }
                terrain.deleteCell(cell.getCoordinates());

                terrain.setCell(stemCell, organism);

                parentCell = stemCell;
            }
            final LeafCell newCell = new LeafCell(parentCell, newSpatialCoordinates, terrain.getProperties());

            logger.info("Creating Leaf at " + newSpatialCoordinates + " with parent " + parentCell.getCellType());

            parentCell.addChild(newCell);
            terrain.setCell(newCell, organism);
            retVal = newCell;
        } else {
            throw new EvolutionException("Leaf growth failed. Collision detected.");
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
        return properties.get(PROPERTY_GROW_LEAF_COST, Integer.class);
    }
}
