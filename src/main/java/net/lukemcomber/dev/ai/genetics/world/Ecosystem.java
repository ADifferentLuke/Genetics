package net.lukemcomber.dev.ai.genetics.world;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;
import net.lukemcomber.dev.ai.genetics.service.CellHelper;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import net.lukemcomber.dev.ai.genetics.world.terrain.TerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.SoilNutrientsTerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.SoilToxicityTerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.impl.SolarEnergyTerrainProperty;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Ecosystem {

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

    private List<Organism> population;

    public Ecosystem(final int ticksPerTurn, final int ticksPerDay, final Terrain terrain) {
        this.terrain = terrain;
        this.ticksPerDay = ticksPerDay;
        this.ticksPerTurn = ticksPerTurn;
        this.population = new LinkedList<>();

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

            System.out.println( "=======================================================");
            System.out.println( "=        Tick:  " + this.totalTicks);
            System.out.println( "=======================================================");

            if (this.currentTick >= ticksPerDay) {
                totalDays++;
                this.currentTick = 0;
                refreshResources();
            }
            for (final Organism organism : this.population) {
                organism.leechResources(terrain);
                organism.performAction(terrain);
                organism.prettyPrint(System.out);

                if( 0 >= organism.getEnergy()){
                    System.out.println( "Organism has died");
                }

                // TODO Grim reaping
            }
        }
    }

    public boolean addOrganism(final Organism organism) {
        boolean retVal = false;
        if (null != organism) {
            if (!population.contains(organism)) {
                final List<Cell> cells = CellHelper.getAllOrganismsCells(organism.getCells());
                // Before setting the cells, make sure there are no conflicts
                boolean doesOrganismFit = true;
                for (final Cell cell : cells) {
                    if (terrain.hasCell(cell.getCoordinates())) {
                        final Cell currentCell = terrain.getCell(cell.getCoordinates());
                        if (currentCell != cell) {
                            doesOrganismFit = false;
                        }
                    }
                }
                if (doesOrganismFit) {
                    cells.forEach(c -> terrain.setCell(c));
                    population.add(organism);
                    retVal = true;
                } else {
                    throw new RuntimeException("Failed to create terrain. Organisms physically conflict.");
                }
            }
        }
        return retVal;
    }

    public boolean deleteOrganism(final Organism organism) {
        throw new NotImplementedException();
    }

    public Iterator<Organism> getOrganisms() {
        return population.listIterator();
    }

}
