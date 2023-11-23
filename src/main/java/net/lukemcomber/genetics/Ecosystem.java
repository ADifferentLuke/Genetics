package net.lukemcomber.genetics;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.service.LoggerOutputStream;
import net.lukemcomber.genetics.world.ResourceManager;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Ecosystem {

    private static final Logger logger = Logger.getLogger(Ecosystem.class.getName());
    private static final LoggerOutputStream loggerOutputStream = new LoggerOutputStream(logger, Level.INFO);
    private Terrain terrain;
    private final int ticksPerTurn;
    private final int ticksPerDay;

    public long getTotalTicks() {
        return totalTicks;
    }

    public void setTotalTicks(final long totalTicks) {
        this.totalTicks = totalTicks;
    }

    public long getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(final long totalDays) {
        this.totalDays = totalDays;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(final int currentTick) {
        this.currentTick = currentTick;
    }

    private long totalTicks;
    private long totalDays;
    private int currentTick;


    public Ecosystem(final int ticksPerTurn, final int ticksPerDay, final Terrain terrain) {
        this.terrain = terrain;
        this.ticksPerDay = ticksPerDay;
        this.ticksPerTurn = ticksPerTurn;

        totalDays = 0;
        totalTicks = 0;
        currentTick = 0;


        if (null != terrain.getResourceManager()) {
            terrain.getResourceManager().initializeAllTerrainResources();
        }
    }

    public int getTicksPerTurn() {
        return ticksPerTurn;
    }

    public int getTicksPerDay() {
        return ticksPerDay;
    }

    private void refreshResources() {

        final ResourceManager manager = getTerrain().getResourceManager();
        manager.renewDailyEnvironmentResource();
    }

    public void advance() {
        for (int i = 0; i < ticksPerTurn; ++i) {
            this.totalTicks++;
            this.currentTick++;

            logger.info("Tick:  " + this.totalTicks);

            if (this.currentTick >= ticksPerDay) {
                totalDays++;
                this.currentTick = 0;
                refreshResources();
            }
            final TemporalCoordinates temporalCoordinates = new TemporalCoordinates(this.totalTicks, this.totalDays, this.currentTick);
            logger.info( "Organism count " + terrain.getOrganismCount());

            /*
             * Organisms remove themselves (or rather the terrain removes them) from
             *  the world which can cause a Concurrency problem. We can solve this
             *  by creating a new reference to the collection.
             *
             * More thought should be given to making this purely asynchronous
             */
            for (final Iterator<Organism> it = terrain.getOrganisms(); it.hasNext(); ) {
                Organism organism = it.next();
                logger.info("Ticking Organism: " + organism.getUniqueID());
                organism.leechResources(terrain, temporalCoordinates);
                organism.performAction(terrain, temporalCoordinates, ((organism1, cell) -> {
                    final ResourceManager manager = terrain.getResourceManager();
                    manager.renewEnvironmentResourceFromCellDeath(organism, cell);
                }));
                organism.prettyPrint(loggerOutputStream);

            }
        }
    }

}