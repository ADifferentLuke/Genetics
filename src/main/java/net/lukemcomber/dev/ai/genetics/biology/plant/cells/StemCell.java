package net.lukemcomber.dev.ai.genetics.biology.plant.cells;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;
import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

//Support structure, does nothing else
public class StemCell extends PlantCell {

    public static final String PROPERTY_METACOST = "cell.stem.metabolic-rate";
    public static final String PROPERTY_ENERGY = "cell.stem.max-energy-production";
    private final SpatialCoordinates spatialCoordinates;

    private final int metabolismCost;

    public StemCell(final Cell parent, final SpatialCoordinates spatialCoordinates,
                    final UniverseConstants properties ) {
        super(parent);
        this.spatialCoordinates = spatialCoordinates;
        this.metabolismCost = properties.get(PROPERTY_METACOST, Integer.class);
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
    public int generateEnergy(final Terrain terrain) {
        return terrain.getProperties().get(PROPERTY_ENERGY, Integer.class);
    }

    @Override
    public int getMetabolismCost() {
        return this.metabolismCost;
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
