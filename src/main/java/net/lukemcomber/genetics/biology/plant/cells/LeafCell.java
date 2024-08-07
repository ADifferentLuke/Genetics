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

//Is able to gather energy from the sun
public class LeafCell extends PlantCell {

    public static final String TYPE = "leaf";

    public static final String PROPERTY_METACOST = "cell.leaf.metabolic-rate";
    public static final String PROPERTY_ENERGY = "cell.leaf.max-energy-production";
    private static final Logger logger = Logger.getLogger(LeafCell.class.getName());
    private final SpatialCoordinates spatialCoordinates;

    private final int metabolismCost;

    public LeafCell(final Cell parent, final SpatialCoordinates spatialCoordinates, final UniverseConstants properties){
        super(parent);
        this.spatialCoordinates = spatialCoordinates;
        this.metabolismCost = properties.get(PROPERTY_METACOST, Integer.class);
    }

    @Override
    public String getCellType() {
        return TYPE;
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
        final int maxEnergyInput = terrain.getProperties().get(PROPERTY_ENERGY, Integer.class);

        final TerrainProperty property = terrain.getTerrainProperty(spatialCoordinates, SolarEnergyTerrainProperty.ID);
        if( null != property ){
            final SolarEnergyTerrainProperty solar = (SolarEnergyTerrainProperty) property;
            int val = solar.getValue();;
            logger.info( String.format("LeafNode - Current Solar %d at (%d,%d)", val,
                    spatialCoordinates.xAxis, spatialCoordinates.yAxis));
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
        return retVal;
    }

    @Override
    public int getMetabolismCost() {
        return metabolismCost;
    }

    /**
     * @param behavior
     * @return
     */
    @Override
    public boolean canCellSupport(final PlantBehavior behavior) {
        return behavior instanceof GrowLeaf || behavior instanceof GrowSeed || behavior instanceof EjectSeed;
    }
}
