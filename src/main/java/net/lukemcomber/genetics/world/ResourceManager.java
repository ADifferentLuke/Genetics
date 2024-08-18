package net.lukemcomber.genetics.world;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.world.terrain.TerrainProperty;

/**
 * Interface to provide terrain resource management
 */
public interface ResourceManager {

    /**
     * Tick the resources in the terrain
     *
     * @return true if resources are ticked
     */
    boolean tickResources();

    /**
     * Renew any daily resources
     *
     * @return true if resources are renewed
     */
    boolean renewDailyEnvironmentResource();

    /**
     * Renew any resources that cell death may provide
     *
     * @param organism organism that died
     * @param cell     cell that died
     * @return true if resources renewed
     */
    boolean renewEnvironmentResourceFromCellDeath(final Organism organism, final Cell cell);

    /**
     * Initialize all {@link TerrainProperty}
     *
     * @return true if properties initialized
     */
    boolean initializeAllTerrainResources();
}
