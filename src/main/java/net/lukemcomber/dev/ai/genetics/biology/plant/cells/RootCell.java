package net.lukemcomber.dev.ai.genetics.biology.plant.cells;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowRoot;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

//harvest soil nutrients
public class RootCell extends PlantCell {

    private final Coordinates coordinates;

    public RootCell(Cell parent, Organism organism, Coordinates coordinates) {
        super(parent,organism);
        this.coordinates = coordinates;
    }

    @Override
    public String getCellType() {
        return "root";
    }

    /**
     * @return
     */
    @Override
    public Coordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public int generateEnergy(Terrain terrain) {
        return 2;
    }

    @Override
    public int getMetabolismCost() {
        return 1;
    }

    /**
     * @param behavior
     * @return
     */
    @Override
    public boolean canCellSupport(final PlantBehavior behavior) {
        return behavior instanceof GrowRoot;
    }
}
