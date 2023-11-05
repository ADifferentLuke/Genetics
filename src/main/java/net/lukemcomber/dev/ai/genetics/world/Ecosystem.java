package net.lukemcomber.dev.ai.genetics.world;

import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;
import net.lukemcomber.dev.ai.genetics.service.LoggerOutputStream;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.SoilNutrientsTerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.SoilToxicityTerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.SolarEnergyTerrainProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Ecosystem {

    private static final Logger logger = Logger.getLogger(Ecosystem.class.getName());
    private static final LoggerOutputStream loggerOutputStream = new LoggerOutputStream(logger, Level.INFO);
    public final static int SOLAR_ENERGY_PER_DAY = 6;
    public final static int INITIAL_SOIL_NUTRIENTS = 10;

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

        refreshResources();
    }

    public int getTicksPerTurn() {
        return ticksPerTurn;
    }

    public int getTicksPerDay() {
        return ticksPerDay;
    }

    private void refreshResources(){

        //TODO we can make this more elegent and more efficient
        for (int x = 0; x < terrain.getSizeOfXAxis(); ++x) {
            for (int y = 0; y < terrain.getSizeOfYAxis(); ++y) {
                final Coordinates coord = new Coordinates(x,y,0);
                terrain.setTerrainProperty(coord, new SolarEnergyTerrainProperty(SOLAR_ENERGY_PER_DAY));
                terrain.setTerrainProperty(coord, new SoilNutrientsTerrainProperty(INITIAL_SOIL_NUTRIENTS));
                terrain.setTerrainProperty(coord, new SoilToxicityTerrainProperty(0));
            }
        }
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
            for (final Iterator<Organism> it = terrain.getOrganisms(); it.hasNext(); ) {
                Organism organism = it.next();
                logger.info( "Ticking Organism: " + organism.getUniqueID());
                organism.leechResources(terrain, this.totalTicks);
                organism.performAction(terrain,  this.totalTicks);
                organism.prettyPrint(loggerOutputStream);

                if( 0 >= organism.getEnergy()){
                    logger.info( "Organism has died");
                }

                // TODO Grim reaping
            }
        }
    }

}
