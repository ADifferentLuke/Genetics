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

    public Cell() {
        children = new LinkedList<>();
    }

    public Cell(final Cell parent) {
        children = new LinkedList<>();
        this.parent = parent;
    }

    public Cell(final Cell parent, final List<Cell> children) {
        this.parent = parent;
        this.children = children;
    }

    public void addChild( final Cell child ){
        children.add(child);
    }
    public boolean removeChild( final Cell child) {
        return children.remove(child);
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
