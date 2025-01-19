package net.lukemcomber.genetics.biology;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Interface for representing cells and providing a uniform interface for manipulating them
 */
public abstract class Cell {

    private final List<Cell> children;

    private Cell parent = null;


    /**
     * Creates a new cell with no children but with a parent
     *
     * @param parent parent cell
     */
    public Cell(final Cell parent) {
        children = new LinkedList<>();
        this.parent = parent;
    }

    /**
     * Creates a new cell with the provided parent and children
     *
     * @param parent   parent cell
     * @param children children cells
     */
    public Cell(final Cell parent, final List<Cell> children) {
        this.parent = parent;
        this.children = children;
    }

    /**
     * Adds a child to this cell
     *
     * @param child child cell
     */
    public void addChild(final Cell child) {
        children.add(child);
    }

    /**
     * Delete a child from this cell
     *
     * @param child child to remove
     * @return true if removed
     */
    public boolean removeChild(final Cell child) {
        return children.remove(child);
    }

    /**
     * Get the cell's parent
     *
     * @return parent
     */
    public Cell getParent() {
        return parent;
    }

    /**
     * Get a list of the cells children
     *
     * @return list
     */
    public List<Cell> getChildren() {
        return children;
    }

    public boolean changeParentCell(final Cell newParent){
        this.parent = newParent;
        return true;
    }

    /**
     * Gets the cell's type
     *
     * @return cell type
     */
    public abstract String getCellType();

    /**
     * Get the cell's location
     *
     * @return location
     */
    public abstract SpatialCoordinates getCoordinates();

    /**
     * Generate energy from resources
     *
     * @param terrain
     * @return amount of energy harvested
     */
    public abstract int generateEnergy(final Terrain terrain);

    /**
     * Get the cost of being alive besides existentialism
     *
     * @return cost
     */
    public abstract int getMetabolismCost();

}
