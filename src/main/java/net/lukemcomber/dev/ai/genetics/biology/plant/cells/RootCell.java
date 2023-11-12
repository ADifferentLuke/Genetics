package net.lukemcomber.dev.ai.genetics.biology.plant.cells;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowRoot;
import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import net.lukemcomber.dev.ai.genetics.world.terrain.TerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.properties.SoilNutrientsTerrainProperty;

import java.util.logging.Logger;

//harvest soil nutrients
public class RootCell extends PlantCell {

    private static final Logger logger = Logger.getLogger(RootCell.class.getName());
    public static final int MAX_ENERGY_DRAW = 3;
    private final SpatialCoordinates spatialCoordinates;

    public RootCell(Cell parent, SpatialCoordinates spatialCoordinates) {
        super(parent);
        this.spatialCoordinates = spatialCoordinates;
    }

    @Override
    public String getCellType() {
        return "root";
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
        int retVal = 0;
        final TerrainProperty property = terrain.getTerrainProperty(spatialCoordinates, SoilNutrientsTerrainProperty.ID);
        if( null != property ){
            final SoilNutrientsTerrainProperty soil = (SoilNutrientsTerrainProperty) property;
            int val = soil.getValue();
            logger.info( String.format("RootNode - Current Soil %d at (%d,%d)", val,
                    spatialCoordinates.xAxis, spatialCoordinates.yAxis));
            if( MAX_ENERGY_DRAW < val ){
                retVal = MAX_ENERGY_DRAW;
                soil.setValue(val-MAX_ENERGY_DRAW);
            } else {
                retVal = val;
                soil.setValue(0);
            }
        }
        return retVal;
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
        return behavior instanceof GrowRoot;
    }
}
