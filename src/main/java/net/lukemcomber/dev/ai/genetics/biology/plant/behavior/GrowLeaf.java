package net.lukemcomber.dev.ai.genetics.biology.plant.behavior;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.LeafCell;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

import java.util.function.Function;

public class GrowLeaf implements PlantBehavior {

    private final Function<Coordinates,Coordinates> function;

    public GrowLeaf(final Function<Coordinates,Coordinates> func){
       this.function = func;
    }
    /**
     * @param terrain
     * @param cell
     * @return
     */
    @Override
    public Cell performAction(final Terrain terrain,final Cell cell) {

        Cell retVal = null;
        final Coordinates newCoordinates = function.apply(cell.getCoordinates());


        //The boolean logic looks weird, but we need to use AND for short circuit
        if((!terrain.isOutOfBounds(newCoordinates)) && (!terrain.hasCell(newCoordinates))){
            final LeafCell newCell = new LeafCell( cell, cell.getOrganism(), newCoordinates);
            terrain.setCell(newCell);
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
