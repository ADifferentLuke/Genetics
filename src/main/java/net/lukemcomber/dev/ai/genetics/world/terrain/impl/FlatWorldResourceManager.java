package net.lukemcomber.dev.ai.genetics.world.terrain.impl;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;
import net.lukemcomber.dev.ai.genetics.service.CellHelper;
import net.lukemcomber.dev.ai.genetics.world.ResourceManager;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;


public class FlatWorldResourceManager implements ResourceManager {

    public static final int SOLAR_ENERGY_PER_DAY = 10;
    public static final int INITIAL_SOIL_NUTRIENTS = 100;


    private Terrain terrain;

    public FlatWorldResourceManager( final Terrain terrain){
        this.terrain = terrain;
    }
    @Override
    public boolean tickResources() {
        return true;
    }

    @Override
    public boolean renewDailyEnvironmentResource() {
        for (int x = 0; x < terrain.getSizeOfXAxis(); ++x) {
            for (int y = 0; y < terrain.getSizeOfYAxis(); ++y) {
                final SpatialCoordinates coord = new SpatialCoordinates(x,y,0);
                terrain.setTerrainProperty(coord, new SolarEnergyTerrainProperty(SOLAR_ENERGY_PER_DAY));
                terrain.setTerrainProperty(coord, new SoilToxicityTerrainProperty(0));
            }
        }
        return true;
    }

    @Override
    public boolean renewEnvironmentResourceFromCellDeath(final Organism organism, final Cell cell) {
        final int nutrients = Math.round(organism.getMetabolismCost() / 2);
        final SpatialCoordinates coords = cell.getCoordinates();
        SoilNutrientsTerrainProperty soil = (SoilNutrientsTerrainProperty) terrain.getTerrainProperty(coords,SoilNutrientsTerrainProperty.ID);
        if( null == soil ){
            //erm how?
            soil = new SoilNutrientsTerrainProperty(0);
        }
        soil.setValue(soil.getValue() + nutrients);
        terrain.setTerrainProperty(coords,soil);

        return true;
    }

    @Override
    public boolean initializeAllTerrainResources() {
        for (int x = 0; x < terrain.getSizeOfXAxis(); ++x) {
            for (int y = 0; y < terrain.getSizeOfYAxis(); ++y) {
                final SpatialCoordinates coord = new SpatialCoordinates(x,y,0);
                terrain.setTerrainProperty(coord, new SolarEnergyTerrainProperty(SOLAR_ENERGY_PER_DAY));
                terrain.setTerrainProperty(coord, new SoilNutrientsTerrainProperty(INITIAL_SOIL_NUTRIENTS));
                terrain.setTerrainProperty(coord, new SoilToxicityTerrainProperty(0));
            }
        }
        return true;
    }
}
