package net.lukemcomber.dev.ai.genetics.biology.plant.cells;


import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowSeed;
import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import net.lukemcomber.dev.ai.genetics.world.terrain.TerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.properties.SolarEnergyTerrainProperty;

import java.util.logging.Logger;

//Is able to gather energy from the sun
public class LeafCell extends PlantCell {

    private static final Logger logger = Logger.getLogger(LeafCell.class.getName());
    private final SpatialCoordinates spatialCoordinates;

    public LeafCell(final Cell parent, final SpatialCoordinates spatialCoordinates){
        super(parent);
        this.spatialCoordinates = spatialCoordinates;
    }

    @Override
    public String getCellType() {
        return "leaf";
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
        final TerrainProperty property = terrain.getTerrainProperty(spatialCoordinates, SolarEnergyTerrainProperty.ID);
        if( null != property ){
            final SolarEnergyTerrainProperty solar = (SolarEnergyTerrainProperty) property;
            int val = solar.getValue();;
            logger.info( String.format("LeafNode - Current Solar %d at (%d,%d)", val,
                    spatialCoordinates.xAxis, spatialCoordinates.yAxis));
            /*
             * DEV NOTE: Since the daily cost is 1, we need to gather at least 2 per tick
             */
            if( 1 < val){
                retVal += 2;
                solar.setValue(val-2);
            } else if( 1 == val ){
                retVal++;
                solar.setValue(--val);
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
        return behavior instanceof GrowLeaf || behavior instanceof GrowSeed;
    }
}
