package net.lukemcomber.genetics.biology.plant.cells;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.genetics.biology.plant.PlantCell;
import net.lukemcomber.genetics.biology.plant.behavior.EjectSeed;
import net.lukemcomber.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.genetics.biology.plant.behavior.GrowSeed;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.world.terrain.Terrain;
import net.lukemcomber.genetics.world.terrain.TerrainProperty;
import net.lukemcomber.genetics.world.terrain.properties.SolarEnergyTerrainProperty;

import java.util.logging.Logger;

/**
 * A leaf cell that is able to harvest energy from {@link SolarEnergyTerrainProperty}
 */
public class LeafCell extends PlantCell {

    public static final String TYPE = "leaf";

    public static final String PROPERTY_METACOST = "cell.leaf.metabolic-rate";
    public static final String PROPERTY_ENERGY = "cell.leaf.max-energy-production";
    private static final Logger logger = Logger.getLogger(LeafCell.class.getName());
    private final SpatialCoordinates spatialCoordinates;
    private int totalEnergyCollected;

    private final int metabolismCost;

    /**
     * Create a new instance
     * @param parent parent cell
     * @param spatialCoordinates location
     * @param properties configuration properties
     */
    public LeafCell(final Cell parent, final SpatialCoordinates spatialCoordinates, final UniverseConstants properties){
        super(parent);
        this.spatialCoordinates = spatialCoordinates;
        this.metabolismCost = properties.get(PROPERTY_METACOST, Integer.class);
        this.totalEnergyCollected = 0;
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
        final int maxEnergyInput = terrain.getProperties().get(PROPERTY_ENERGY, Integer.class);

        final TerrainProperty property = terrain.getTerrainProperty(spatialCoordinates, SolarEnergyTerrainProperty.ID);
        if( null != property ){
            final SolarEnergyTerrainProperty solar = (SolarEnergyTerrainProperty) property;
            int val = solar.getValue();;
            logger.info( String.format("LeafNode - Current Solar %d at (%d,%d)", val,
                    spatialCoordinates.xAxis(), spatialCoordinates.yAxis()));
            /*
             * DEV NOTE: Since the daily cost is 1, we need to gather at least 2 per tick
             */
            if( maxEnergyInput < val){
                retVal = maxEnergyInput;
                solar.setValue(val-maxEnergyInput);
            } else {
                retVal = val;
                solar.setValue(0);
            }
        }
        totalEnergyCollected += retVal;
        return retVal;
    }

    /**
     * Get the cost of being alive
     *
     * @return cost
     */
    @Override
    public int getMetabolismCost() {
        return metabolismCost;
    }

    @Override
    public int getTotalEnergyGenerated() {
        return totalEnergyCollected;
    }

    /**
     * Return true if the cell is capable of performing the behavior
     *
     * @param behavior action to check
     * @return true if possible otherwise false
     */
    @Override
    public boolean canCellSupport(final PlantBehavior behavior) {
        return behavior instanceof GrowLeaf || behavior instanceof GrowSeed || behavior instanceof EjectSeed;
    }
}
