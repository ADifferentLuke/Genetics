package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;

public abstract class PlantCell extends Cell {

    public PlantCell(Cell parent ){super(parent);}

    public abstract boolean canCellSupport(final PlantBehavior behavior);
}
