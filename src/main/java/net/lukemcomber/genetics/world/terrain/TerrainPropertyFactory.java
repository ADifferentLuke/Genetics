package net.lukemcomber.genetics.world.terrain;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.world.terrain.properties.RadioactiveTerrainProperty;
import net.lukemcomber.genetics.world.terrain.properties.SoilNutrientsTerrainProperty;
import net.lukemcomber.genetics.world.terrain.properties.SoilToxicityTerrainProperty;
import net.lukemcomber.genetics.world.terrain.properties.SolarEnergyTerrainProperty;

/**
 * Factory for creating new {@link TerrainProperty}
 */
public class TerrainPropertyFactory {

    /**
     * Return a new {@link TerrainProperty} of type that matches the id
     *
     * @param id type of {@link TerrainProperty}
     * @return new {@link TerrainProperty}
     */
    public static TerrainProperty<?> createTerrainProperty(final String id) {
        final TerrainProperty<?> retVal;
        switch (id) {
            case SolarEnergyTerrainProperty.ID:
                retVal = new SolarEnergyTerrainProperty();
                break;
            case SoilNutrientsTerrainProperty.ID:
                retVal = new SoilNutrientsTerrainProperty();
                break;
            case SoilToxicityTerrainProperty.ID:
                retVal = new SoilToxicityTerrainProperty();
                break;
            case RadioactiveTerrainProperty.ID:
                retVal = new RadioactiveTerrainProperty();
                break;
            default:
                throw new EvolutionException("Unknown terrain property: " + id);
        }
        return retVal;
    }
}
