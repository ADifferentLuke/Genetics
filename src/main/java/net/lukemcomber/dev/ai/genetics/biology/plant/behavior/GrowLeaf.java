package net.lukemcomber.dev.ai.genetics.biology.plant.behavior;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.LeafCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.StemCell;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

import java.util.function.Function;
import java.util.logging.Logger;

public class GrowLeaf implements PlantBehavior {

    private static final Logger logger = Logger.getLogger(GrowLeaf.class.getName());
    private final Function<SpatialCoordinates, SpatialCoordinates> function;

    public GrowLeaf(final Function<SpatialCoordinates, SpatialCoordinates> func){
       this.function = func;
    }
    /**
     * @param terrain
     * @param cell
     * @return
     */
    @Override
    public Cell performAction(final Terrain terrain, final Cell cell, final Organism organism) {

        Cell retVal = null;
        final SpatialCoordinates newSpatialCoordinates = function.apply(cell.getCoordinates());


        //The boolean logic looks weird, but we need to use AND for short circuit
        if((!terrain.isOutOfBounds(newSpatialCoordinates)) && (!terrain.hasCell(newSpatialCoordinates))){
            Cell parentCell = cell;
            if( cell instanceof LeafCell){
                //TODO what if leaf's parent is null?
                logger.info( "\tWe need to create a stem!");
                final Cell grandParentCell = cell.getParent();
                logger.info( "Grandparent cell is: " + grandParentCell.getCellType() + " at " + grandParentCell.getCoordinates());
                final StemCell stemCell = new StemCell(grandParentCell,  cell.getCoordinates());

                logger.info( "Stem created at " + stemCell.getCoordinates());

                //body swap!
                grandParentCell.removeChild(cell);
                grandParentCell.addChild(stemCell);

                logger.info("Removed cell " + cell.getCellType() + " from grandparent");
                logger.info("Added cell " + stemCell.getCellType() + " from grandparent");


                //Now give all the child to the stem, even though there really shouldn't be any yet
                for( final Cell childChld : cell.getChildren()){
                    stemCell.addChild(childChld);
                    logger.info( "Copied child " + childChld.getCellType() + " at " + childChld.getCoordinates());
                }
                terrain.deleteCell(cell.getCoordinates());

                terrain.setCell(stemCell,organism);

                parentCell = stemCell;
            }
            final LeafCell newCell = new LeafCell( parentCell, newSpatialCoordinates);

            logger.info( "Creating Leaf at " + newSpatialCoordinates + " with parent " + parentCell.getCellType());

            parentCell.addChild(newCell);
            terrain.setCell(newCell,organism);
            retVal = newCell;
        } else {
            throw new EvolutionException("Leaf growth failed. Collision detected.");
        }

        return retVal;
    }

    /**
     * @return
     */
    @Override
    public int getEnergyCost() {
        return 1;
    }
}
