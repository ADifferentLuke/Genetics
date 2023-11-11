package net.lukemcomber.dev.ai.genetics.world;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;
import net.lukemcomber.dev.ai.genetics.model.TemporalCoordinates;
import net.lukemcomber.dev.ai.genetics.service.CellHelper;
import net.lukemcomber.dev.ai.genetics.service.LoggerOutputStream;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import net.lukemcomber.dev.ai.genetics.world.terrain.TerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.SoilNutrientsTerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.SoilToxicityTerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.SolarEnergyTerrainProperty;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Ecosystem {

    private static final Logger logger = Logger.getLogger(Ecosystem.class.getName());
    private static final LoggerOutputStream loggerOutputStream = new LoggerOutputStream(logger, Level.INFO);
    private Terrain terrain;
    private ResourceManager resourceManager;

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

        if( null != terrain.getResourceManager()){
            terrain.getResourceManager().initializeAllTerrainResources();
        }
    }

    public int getTicksPerTurn() {
        return ticksPerTurn;
    }

    public int getTicksPerDay() {
        return ticksPerDay;
    }

    private void refreshResources(){

        final ResourceManager manager = getTerrain().getResourceManager();
        manager.renewDailyEnvironmentResource();
    }

    public void advance() {
        for (int i = 0; i < ticksPerTurn; ++i) {
            this.totalTicks++;
            this.currentTick++;

            logger.info( "Tick:  " + this.totalTicks);

            if (this.currentTick >= ticksPerDay) {
                totalDays++;
                this.currentTick = 0;
                refreshResources();
            }
            final TemporalCoordinates temporalCoordinates = new TemporalCoordinates(this.totalTicks,this.totalDays,this.currentTick);
            for (final Iterator<Organism> it = terrain.getOrganisms(); it.hasNext(); ) {
                Organism organism = it.next();
                if(organism.isAlive()) {
                    logger.info("Ticking Organism: " + organism.getUniqueID());
                    organism.leechResources(terrain, temporalCoordinates);
                    organism.performAction(terrain, temporalCoordinates, ((organism1, cell) -> {
                        final ResourceManager manager = terrain.getResourceManager();
                        manager.renewEnvironmentResourceFromCellDeath(organism,cell);
                    }));
                    organism.prettyPrint(loggerOutputStream);

                } else {
                    /*
                     A dead organism will be in the terrain for one turn before cleanup. To change this
                     to immediate death, move delete organism outside the else
                     */
                    terrain.deleteOrganism(organism);
                }
            }
        }
    }

}
