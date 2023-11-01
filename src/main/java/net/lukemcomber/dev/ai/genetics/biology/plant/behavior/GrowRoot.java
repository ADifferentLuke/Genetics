package net.lukemcomber.dev.ai.genetics.biology.plant.behavior;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.RootCell;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

import java.util.function.Function;

public class GrowRoot implements PlantBehavior {

    private final Function<Coordinates, Coordinates> function;

    public GrowRoot(final Function<Coordinates, Coordinates> func) {
        this.function = func;
    }

    /**
     * @param terrain
     * @param rootCell
     * @return
     */
    @Override
    public Cell performAction(final Terrain terrain, final Cell rootCell) {
        Cell retVal = null;
        final Coordinates newCoordinates = function.apply(rootCell.getCoordinates());
        if (!(terrain.isOutOfBounds(newCoordinates) || terrain.hasCell(newCoordinates))) {
            final RootCell newCell = new RootCell(rootCell, rootCell.getOrganism(), newCoordinates);
            terrain.setCell(newCell);
            retVal = newCell;
        } else {
            throw new EvolutionException("Root growth failed. Collision detected.");
        }
        return retVal;
    }

    /**
     * @return
     */
    @Override
    public int getEnergyCost() {
        return 2;
    }
}
