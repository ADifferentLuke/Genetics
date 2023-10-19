package net.lukemcomber.dev.ai.genetics.biology.plant.cells;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowRoot;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;

public class SeedCell extends PlantCell {

    private final Coordinates coordinates;
    //TODO - This is essentially the root node of a tree
    public SeedCell(final Coordinates coordinates) {
        //seed cells don't have parents
        super();
        this.coordinates = coordinates;
    }

    @Override
    public String getCellType() {
        return "seed";
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * @param behavior
     * @return
     */
    @Override
    public boolean canCellSupport(final PlantBehavior behavior) {
        return behavior instanceof GrowRoot || behavior instanceof GrowLeaf;
    }
}
