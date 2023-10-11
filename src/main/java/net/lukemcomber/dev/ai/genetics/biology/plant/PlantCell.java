package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Cell;

public abstract class PlantCell extends Cell {

    public PlantCell(Cell parent){super(parent);}

    public PlantCell() {
        super();
    }

    public abstract boolean canCellSupport(final PlantBehavior behavior);
}
