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
     * @param rootCell
     * @return
     */
    @Override
    public Cell performAction(final Terrain terrain,final Cell rootCell) {

        Cell retVal = null;
        final Coordinates newCoordinates = function.apply(rootCell.getCoordinates());
        if( ! terrain.hasCell(newCoordinates)){
           final LeafCell newCell = new LeafCell( rootCell, rootCell.getOrganism(), newCoordinates);
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
