package net.lukemcomber.genetics.world;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;

public interface ResourceManager {

    boolean tickResources();

    boolean renewDailyEnvironmentResource();

    boolean renewEnvironmentResourceFromCellDeath(final Organism organism, final Cell cell);

    boolean initializeAllTerrainResources();
}
