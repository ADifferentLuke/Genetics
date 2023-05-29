package net.lukemcomber.dev.ai.genetics.world.terrain;

import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.RadioactiveTerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.SoilNutrientsTerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.SoilToxicityTerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.SolarEnergyTerrainProperty;

public class TerrainPropertyFactory {

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
