package net.lukemcomber.dev.ai.genetics.biology.plant.behavior;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.RootCell;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;
import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

import java.util.function.Function;

public class GrowRoot implements PlantBehavior {

    public final static String PROPERTY_GROW_ROOT_COST = "action.root.grow";

    private final Function<SpatialCoordinates, SpatialCoordinates> function;

    public GrowRoot(final Function<SpatialCoordinates, SpatialCoordinates> func) {
        this.function = func;
    }

    /**
     * @param terrain
     * @param rootCell
     * @return
     */
    @Override
    public Cell performAction(final Terrain terrain, final Cell rootCell, final Organism organism) {
        Cell retVal = null;
        final SpatialCoordinates newSpatialCoordinates = function.apply(rootCell.getCoordinates());
        if (!(terrain.isOutOfBounds(newSpatialCoordinates) || terrain.hasCell(newSpatialCoordinates))) {
            final RootCell newCell = new RootCell(rootCell, newSpatialCoordinates, terrain.getProperties());
            terrain.setCell(newCell,organism);
            rootCell.addChild(newCell);
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
    public int getEnergyCost(final UniverseConstants properties) {
        return properties.get(PROPERTY_GROW_ROOT_COST, Integer.class);
    }
}
