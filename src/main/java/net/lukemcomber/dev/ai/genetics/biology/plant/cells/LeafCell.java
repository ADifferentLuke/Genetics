package net.lukemcomber.dev.ai.genetics.biology.plant.cells;


import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowSeed;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;

//Is able to gather energy from the sun
public class LeafCell extends PlantCell {

    private final Coordinates coordinates;

    public LeafCell(final Cell parent, final Coordinates coordinates){
        super(parent);
        this.coordinates = coordinates;
    }
    /**
     * @return
     */
    @Override
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * @param behavior
     * @return
     */
    @Override
    public boolean canCellSupport(final PlantBehavior behavior) {
        return behavior instanceof GrowLeaf || behavior instanceof GrowSeed;
    }
}
