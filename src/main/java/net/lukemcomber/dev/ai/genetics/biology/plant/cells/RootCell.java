package net.lukemcomber.dev.ai.genetics.biology.plant.cells;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowRoot;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import net.lukemcomber.dev.ai.genetics.world.terrain.TerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.SoilNutrientsTerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.SolarEnergyTerrainProperty;

//harvest soil nutrients
public class RootCell extends PlantCell {

    public static final int MAX_ENERGY_DRAW = 3;
    private final Coordinates coordinates;

    public RootCell(Cell parent, Organism organism, Coordinates coordinates) {
        super(parent,organism);
        this.coordinates = coordinates;
    }

    @Override
    public String getCellType() {
        return "root";
    }

    /**
     * @return
     */
    @Override
    public Coordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public int generateEnergy(final Terrain terrain) {
        int retVal = 0;
        final TerrainProperty property = terrain.getTerrainProperty(coordinates, SoilNutrientsTerrainProperty.ID);
        if( null != property ){
            final SoilNutrientsTerrainProperty soil = (SoilNutrientsTerrainProperty) property;
            int val = soil.getValue();
            System.out.println( String.format("RootNode - Current Soil %d at (%d,%d)", val,
                    coordinates.xAxis, coordinates.yAxis));
            if( MAX_ENERGY_DRAW >= val ){
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
