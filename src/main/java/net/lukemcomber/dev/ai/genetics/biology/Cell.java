package net.lukemcomber.dev.ai.genetics.biology;

import net.lukemcomber.dev.ai.genetics.model.Coordinates;

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

    public Cell getParent() {
        return parent;
    }

    public List<Cell> getChildren() {
        return children;
    }

    public abstract Coordinates getCoordinates();
}
