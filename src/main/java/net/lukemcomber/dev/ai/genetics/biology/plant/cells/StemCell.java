package net.lukemcomber.dev.ai.genetics.biology.plant.cells;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

//Support structure, does nothing else
public class StemCell extends PlantCell {

    private final SpatialCoordinates spatialCoordinates;

    public StemCell(final Cell parent, SpatialCoordinates spatialCoordinates) {
        super(parent);
        this.spatialCoordinates = spatialCoordinates;
    }

    @Override
    public String getCellType() {
        return "stem";
    }

    /**
     * @return
     */
    @Override
    public SpatialCoordinates getCoordinates() {
        return spatialCoordinates;
    }

    @Override
    public int generateEnergy(Terrain terrain) {
        return 0;
    }

    @Override
    public int getMetabolismCost() {
        return 1;
    }

    /**
     * @param behavior
     * @return
     */
    @Override
    public boolean canCellSupport(final PlantBehavior behavior) {
        return behavior instanceof GrowLeaf;
    }
}
