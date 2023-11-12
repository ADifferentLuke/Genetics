package net.lukemcomber.dev.ai.genetics.universes;

import com.google.common.collect.ImmutableMap;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantOrganism;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.LeafCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.RootCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.StemCell;
import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.FlatWorldResourceManager;

import java.util.Map;

public final class FlatFloraUniverse extends UniverseConstants {

    public static final String ID = "flat-floral-universe";

    private static final Map<String, Object> CONSTANTS;

    static {
        CONSTANTS = new ImmutableMap.Builder<String, Object>()
                /* World Properties */
                .put(FlatWorldResourceManager.DAILY_SOLAR_PROPERTY, 10)
                .put(FlatWorldResourceManager.INITIAL_SOIL_PROPERTY, 100)
                /* Plant Properties */
                .put(PlantOrganism.PROPERTY_STARTING_ENERGY, 5)
                .put(PlantOrganism.PROPERTY_OLD_AGE_LIMIT, 100)
                .put(PlantOrganism.PROPERTY_STARVATION_LIMIT, 0)
                .put(PlantOrganism.PROPERTY_STAGNATION_LIMIT, 10)
                /* Cell Properties */
                .put(LeafCell.PROPERTY_ENERGY, 3)
                .put(LeafCell.PROPERTY_METACOST, 1)
                .put(SeedCell.PROPERTY_ENERGY, 0)
                .put(SeedCell.PROPERTY_METACOST, 0)
                .put(StemCell.PROPERTY_ENERGY, 0)
                .put(StemCell.PROPERTY_METACOST, 1)
                .put(RootCell.PROPERTY_ENERGY, 2)
                .put(RootCell.PROPERTY_METACOST, 1)
                .build();
    }

    public FlatFloraUniverse() {
        super(CONSTANTS);
    }
}
