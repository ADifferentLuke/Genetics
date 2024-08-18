package net.lukemcomber.genetics.world.terrain.impl;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.world.ResourceManager;
import net.lukemcomber.genetics.world.terrain.Terrain;
import net.lukemcomber.genetics.world.terrain.TerrainProperty;
import net.lukemcomber.genetics.world.terrain.properties.SoilNutrientsTerrainProperty;
import net.lukemcomber.genetics.world.terrain.properties.SolarEnergyTerrainProperty;


/**
 * The {@link ResourceManager} for {@link FlatWorld}
 */
public class FlatWorldResourceManager implements ResourceManager {

    public static final String DAILY_SOLAR_PROPERTY = "daily.solar.property";
    public static final String INITIAL_SOIL_PROPERTY = "initial.soil.property";

    private final Terrain terrain;
    private final UniverseConstants properties;

    /**
     * Creates a new instance
     *
     * @param terrain   terrain to manage resources for
     * @param constants configuration properties
     */
    public FlatWorldResourceManager(final Terrain terrain, final UniverseConstants constants) {
        this.terrain = terrain;
        this.properties = constants;
    }

    /**
     * Tick the resources in the terrain
     *
     * @return true if resources are ticked
     */
    @Override
    public boolean tickResources() {
        return true;
    }

    /**
     * Renew any daily resources
     *
     * @return true if resources are renewed
     */
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

    /**
     * Renew any resources that cell death may provide
     *
     * @param organism organism that died
     * @param cell     cell that died
     * @return true if resources renewed
     */
    @Override
    public boolean renewEnvironmentResourceFromCellDeath(final Organism organism, final Cell cell) {
        final int nutrients = (int) Math.round(Math.log(organism.getMetabolismCost()));
        final SpatialCoordinates coords = cell.getCoordinates();
        SoilNutrientsTerrainProperty soil = (SoilNutrientsTerrainProperty) terrain.getTerrainProperty(coords, SoilNutrientsTerrainProperty.ID);
        if (null == soil) {
            //erm how?
            soil = new SoilNutrientsTerrainProperty(properties.get(INITIAL_SOIL_PROPERTY, Integer.class));
        }
        soil.setValue(soil.getValue() + nutrients);
        terrain.setTerrainProperty(coords, soil);

        return true;
    }

    /**
     * Initialize all {@link TerrainProperty}
     *
     * @return true if properties initialized
     */
    @Override
    public boolean initializeAllTerrainResources() {
        final Integer solarEnergy = properties.get(DAILY_SOLAR_PROPERTY, Integer.class);
        final Integer soilEnergy = properties.get(INITIAL_SOIL_PROPERTY, Integer.class);

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
