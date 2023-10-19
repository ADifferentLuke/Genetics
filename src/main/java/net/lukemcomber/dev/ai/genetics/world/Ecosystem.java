package net.lukemcomber.dev.ai.genetics.world;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.service.CellHelper;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Ecosystem {

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

    public Terrain getTerrain(){
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

    public Ecosystem( final int ticksPerTurn, final int ticksPerDay, final Terrain terrain ){
        this.terrain = terrain;
        this.ticksPerDay = ticksPerDay;
        this.ticksPerTurn = ticksPerTurn;
        this.population = new LinkedList<>();

        totalDays = 0;
        totalTicks = 0;
        currentTick = 0;
    }

    public boolean addOrganism(final Organism organism) {
        boolean retVal = false;
        if( null != organism){
            if(!population.contains(organism)){
                final List<Cell> cells = CellHelper.getAllOrganismsCells(organism.getCells());
                // Before setting the cells, make sure there are no conflicts
                boolean doesOrganismFit = true;
                for( final Cell cell : cells ){
                    if( terrain.hasCell(cell.getCoordinates())){
                        final Cell currentCell = terrain.getCell(cell.getCoordinates());
                        if( currentCell != cell ){
                            doesOrganismFit = false;
                        }
                    }
                }
                if( doesOrganismFit ){
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
