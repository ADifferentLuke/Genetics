package net.lukemcomber.genetics.biology.plant;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;

public abstract class PlantCell extends Cell {

    public PlantCell(Cell parent ){super(parent);}

    public abstract boolean canCellSupport(final PlantBehavior behavior);
}
