package net.lukemcomber.dev.ai.genetics.world;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;

public interface ResourceManager {

    boolean tickResources();

    boolean renewDailyEnvironmentResource();

    boolean renewEnvironmentResourceFromCellDeath(final Organism organism, final Cell cell);

    boolean initializeAllTerrainResources();
}
