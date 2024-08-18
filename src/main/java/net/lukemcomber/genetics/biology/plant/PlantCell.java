package net.lukemcomber.genetics.biology.plant;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;

/**
 * A cell specifically for plants
 */
public abstract class PlantCell extends Cell {

    /**
     * Create a new instance
     *
     * @param parent the parent cell
     */
    public PlantCell(final Cell parent) {
        super(parent);
    }

    /**
     * Return true if the cell is capable of performing the behavior
     *
     * @param behavior action to check
     * @return true if possible otherwise false
     */
    public abstract boolean canCellSupport(final PlantBehavior behavior);
}
