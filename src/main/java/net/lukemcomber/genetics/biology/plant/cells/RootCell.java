package net.lukemcomber.genetics.biology.plant.cells;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.genetics.biology.plant.PlantCell;
import net.lukemcomber.genetics.biology.plant.behavior.GrowRoot;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.world.terrain.Terrain;
import net.lukemcomber.genetics.world.terrain.TerrainProperty;
import net.lukemcomber.genetics.world.terrain.properties.SoilNutrientsTerrainProperty;

import java.util.logging.Logger;

/**
 * A root cell that can harvest energy from {@link SoilNutrientsTerrainProperty}
 */
public class RootCell extends PlantCell {

    public static final String TYPE = "root";

    public static final String PROPERTY_METACOST = "cell.root.metabolic-rate";
    public static final String PROPERTY_ENERGY = "cell.root.max-energy-production";
    private static final Logger logger = Logger.getLogger(RootCell.class.getName());
    private final SpatialCoordinates spatialCoordinates;
    private final int metabolicCost;

    /**
     * Creates a new root cell
     * @param parent parent cell
     * @param spatialCoordinates location
     * @param properties configuration properties
     */
    public RootCell(final Cell parent, final SpatialCoordinates spatialCoordinates, final UniverseConstants properties) {
        super(parent);
        this.spatialCoordinates = spatialCoordinates;
        this.metabolicCost = properties.get(PROPERTY_METACOST, Integer.class);
    }

    /**
     * Gets the cell's type
     *
     * @return cell type
     */
    @Override
    public String getCellType() {
        return TYPE;
    }

    /**
     * Get the cell's location
     *
     * @return location
     */
    @Override
    public SpatialCoordinates getCoordinates() {
        return spatialCoordinates;
    }

    /**
     * Generate energy from resources
     *
     * @param terrain
     * @return amount of energy harvested
     */
    @Override
    public int generateEnergy(final Terrain terrain) {
        int retVal = 0;
        final int maxEnergyInput = terrain.getProperties().get(PROPERTY_ENERGY,Integer.class);
        final TerrainProperty property = terrain.getTerrainProperty(spatialCoordinates, SoilNutrientsTerrainProperty.ID);
        if( null != property ){
            final SoilNutrientsTerrainProperty soil = (SoilNutrientsTerrainProperty) property;
            int val = soil.getValue();
            logger.info( String.format("RootNode - Current Soil %d at (%d,%d)", val,
                    spatialCoordinates.xAxis(), spatialCoordinates.yAxis()));
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

    /**
     * Get the cost of being alive
     *
     * @return cost
     */
    @Override
    public int getMetabolismCost() {
        return metabolicCost;
    }

    /**
     * Return true if the cell is capable of performing the behavior
     *
     * @param behavior action to check
     * @return true if possible otherwise false
     */
    @Override
    public boolean canCellSupport(final PlantBehavior behavior) {
        return behavior instanceof GrowRoot;
    }
}
