package net.lukemcomber.genetics.world.terrain.impl;

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.world.ResourceManager;
import net.lukemcomber.genetics.world.terrain.Terrain;
import net.lukemcomber.genetics.world.terrain.properties.SoilNutrientsTerrainProperty;
import net.lukemcomber.genetics.world.terrain.properties.SolarEnergyTerrainProperty;


public class FlatWorldResourceManager implements ResourceManager {

    public static final String DAILY_SOLAR_PROPERTY = "daily.solar.property";
    public static final String INITIAL_SOIL_PROPERTY = "initial.soil.property";


    private final Terrain terrain;
    private final UniverseConstants properties;

    public FlatWorldResourceManager(final Terrain terrain, final UniverseConstants constants) {
        this.terrain = terrain;
        this.properties = constants;
    }

    @Override
    public boolean tickResources() {
        return true;
    }

    @Override
    public boolean renewDailyEnvironmentResource() {

        final Integer dailySolarRefresh = properties.get(DAILY_SOLAR_PROPERTY, Integer.class);

        for (int x = 0; x < terrain.getSizeOfXAxis(); ++x) {
            for (int y = 0; y < terrain.getSizeOfYAxis(); ++y) {
                final SpatialCoordinates coord = new SpatialCoordinates(x, y, 0);
                terrain.setTerrainProperty(coord, new SolarEnergyTerrainProperty(dailySolarRefresh));
            }
        }
        return true;
    }

    @Override
    public boolean renewEnvironmentResourceFromCellDeath(final Organism organism, final Cell cell) {
        final int nutrients = Math.round(organism.getMetabolismCost() / 2);
        final SpatialCoordinates coords = cell.getCoordinates();
        SoilNutrientsTerrainProperty soil = (SoilNutrientsTerrainProperty) terrain.getTerrainProperty(coords, SoilNutrientsTerrainProperty.ID);
        if (null == soil) {
            //erm how?
            soil = new SoilNutrientsTerrainProperty(properties.get(INITIAL_SOIL_PROPERTY,Integer.class));
        }
        soil.setValue(soil.getValue() + nutrients);
        terrain.setTerrainProperty(coords, soil);

        return true;
    }

    @Override
    public boolean initializeAllTerrainResources() {
        final Integer solarEnergy = properties.get(DAILY_SOLAR_PROPERTY,Integer.class);
        final Integer soilEnergy = properties.get(INITIAL_SOIL_PROPERTY,Integer.class);

        for (int x = 0; x < terrain.getSizeOfXAxis(); ++x) {
            for (int y = 0; y < terrain.getSizeOfYAxis(); ++y) {
                final SpatialCoordinates coord = new SpatialCoordinates(x, y, 0);
                terrain.setTerrainProperty(coord, new SolarEnergyTerrainProperty(solarEnergy));
                terrain.setTerrainProperty(coord, new SoilNutrientsTerrainProperty(soilEnergy));
            }
        }
        return true;
    }

}
