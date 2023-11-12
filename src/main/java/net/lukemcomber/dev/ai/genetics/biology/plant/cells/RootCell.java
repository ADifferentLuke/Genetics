package net.lukemcomber.dev.ai.genetics.biology.plant.cells;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowRoot;
import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;
import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import net.lukemcomber.dev.ai.genetics.world.terrain.TerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.properties.SoilNutrientsTerrainProperty;

import java.util.logging.Logger;

//harvest soil nutrients
public class RootCell extends PlantCell {

    public static final String PROPERTY_METACOST = "cell.root.metabolic-rate";
    public static final String PROPERTY_ENERGY = "cell.root.max-energy-production";
    private static final Logger logger = Logger.getLogger(RootCell.class.getName());
    private final SpatialCoordinates spatialCoordinates;

    private final int metabolicCost;

    public RootCell(final Cell parent, final SpatialCoordinates spatialCoordinates, final UniverseConstants properties) {
        super(parent);
        this.spatialCoordinates = spatialCoordinates;
        this.metabolicCost = properties.get(PROPERTY_METACOST, Integer.class);
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
        final int maxEnergyInput = terrain.getProperties().get(PROPERTY_ENERGY,Integer.class);
        final TerrainProperty property = terrain.getTerrainProperty(spatialCoordinates, SoilNutrientsTerrainProperty.ID);
        if( null != property ){
            final SoilNutrientsTerrainProperty soil = (SoilNutrientsTerrainProperty) property;
            int val = soil.getValue();
            logger.info( String.format("RootNode - Current Soil %d at (%d,%d)", val,
                    spatialCoordinates.xAxis, spatialCoordinates.yAxis));
            if( maxEnergyInput < val ){
                retVal = maxEnergyInput;
                soil.setValue(val-maxEnergyInput);
            } else {
                retVal = val;
                soil.setValue(0);
            }
        }
        return retVal;
    }

    @Override
    public int getMetabolismCost() {
        return metabolicCost;
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
