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
    public final static int SOLAR_ENERGY_PER_DAY = 6;
    public final static int INITIAL_SOIL_NUTRIENTS = 100;

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

        refreshResources(true);
    }

    public int getTicksPerTurn() {
        return ticksPerTurn;
    }

    public int getTicksPerDay() {
        return ticksPerDay;
    }

    private void refreshResources(final boolean refreshSoil){

        //TODO we can make this more elegent and more efficient
        for (int x = 0; x < terrain.getSizeOfXAxis(); ++x) {
            for (int y = 0; y < terrain.getSizeOfYAxis(); ++y) {
                final SpatialCoordinates coord = new SpatialCoordinates(x,y,0);
                terrain.setTerrainProperty(coord, new SolarEnergyTerrainProperty(SOLAR_ENERGY_PER_DAY));
                if( refreshSoil) {
                    terrain.setTerrainProperty(coord, new SoilNutrientsTerrainProperty(INITIAL_SOIL_NUTRIENTS));
                }
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
                refreshResources(false);
            }
            final TemporalCoordinates temporalCoordinates = new TemporalCoordinates(this.totalTicks,this.totalDays,this.currentTick);
            for (final Iterator<Organism> it = terrain.getOrganisms(); it.hasNext(); ) {
                Organism organism = it.next();
                if(organism.isAlive()) {
                    logger.info("Ticking Organism: " + organism.getUniqueID());
                    organism.leechResources(terrain, temporalCoordinates);
                    organism.performAction(terrain, temporalCoordinates);
                    organism.prettyPrint(loggerOutputStream);

                } else {
                    /*
                     A dead organism will be in the terrain for one turn before cleanup. To change this
                     to immediate death, move delete organism outside the else
                     */
                    terrain.deleteOrganism(organism);
                    //replenish soil

                    //TODO it would be nice if the soil recharged less and less each time, but that needs a state saved
                    final int nutrients = Math.round(organism.getMetabolismCost() / 2);

                    for( final Cell cell : CellHelper.getAllOrganismsCells(organism.getCells())){
                        final SpatialCoordinates coords = cell.getCoordinates();
                        SoilNutrientsTerrainProperty soil = (SoilNutrientsTerrainProperty) terrain.getTerrainProperty(coords,SoilNutrientsTerrainProperty.ID);
                        if( null == soil ){
                            //erm how?
                            soil = new SoilNutrientsTerrainProperty(0);
                        }
                        soil.setValue(soil.getValue() + nutrients);
                        terrain.setTerrainProperty(coords,soil);
                    }
                }
            }
        }
    }

}
