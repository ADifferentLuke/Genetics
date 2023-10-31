package net.lukemcomber.dev.ai.genetics.biology;

import net.lukemcomber.dev.ai.genetics.model.Coordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

import java.util.LinkedList;
import java.util.List;

/**
 * Interface for representing cells and providing a uniform interface for manipulating them
 */
public abstract class Cell {

    private final List<Cell> children;

    private Cell parent = null;
    private Organism organism;

    public Cell(final Organism organism) {
        this.organism = organism;
        children = new LinkedList<>();
    }

    public Cell(final Cell parent, final Organism organism) {
        children = new LinkedList<>();
        this.parent = parent;
        this.organism = organism;
    }

    public Cell(final Cell parent, final Organism organism, final List<Cell> children) {
        this.parent = parent;
        this.children = children;
        this.organism = organism;
    }
    protected void setOrganism(final Organism organism ){
       this.organism = organism;
    }

    public Organism getOrganism(){
        return organism;
    }

    public void addChild( final Cell child ){
        children.add(child);
    }

    public Cell getParent() {
        return parent;
    }

    public List<Cell> getChildren() {
        return children;
    }

    public abstract String getCellType();

    public abstract Coordinates getCoordinates();

    public abstract int generateEnergy(final Terrain terrain);

    public abstract int getMetabolismCost();

}
