package net.lukemcomber.dev.ai.genetics.universes;

import com.google.common.collect.ImmutableMap;
import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.FlatWorldResourceManager;
import net.lukemcomber.dev.ai.genetics.world.terrain.properties.SolarEnergyTerrainProperty;

import java.util.Map;

public final class FlatFloraUniverse extends UniverseConstants {

    public static final String ID = "flat-floral-universe";

    private static final Map<String, Object> CONSTANTS;

    static {
        CONSTANTS = new ImmutableMap.Builder<String, Object>()
                .put(FlatWorldResourceManager.DAILY_SOLAR_PROPERTY, 10)
                .put(FlatWorldResourceManager.INITIAL_SOIL_PROPERTY, 100)
                .build();
    }

    public FlatFloraUniverse() {
        super(CONSTANTS);
    }
}
