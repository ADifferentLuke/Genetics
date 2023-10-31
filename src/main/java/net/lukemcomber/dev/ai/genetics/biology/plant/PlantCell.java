package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;

public abstract class PlantCell extends Cell {

    public PlantCell(Cell parent, Organism organism){super(parent,organism);}

    public PlantCell(Organism organism) {
        super(organism);
    }

    public abstract boolean canCellSupport(final PlantBehavior behavior);
}
