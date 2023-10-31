package net.lukemcomber.dev.ai.genetics.biology.plant.cells;

import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantOrganism;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowRoot;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

public class SeedCell extends PlantCell {

    private final Coordinates coordinates;
    //TODO - This is essentially the root node of a tree
    public SeedCell(final Coordinates coordinates,final Genome genome) {
        //seed cells don't have parents
        super(null);
        setOrganism(new PlantOrganism(genome,this));

        this.coordinates = coordinates;
    }

    @Override
    public String getCellType() {
        return "seed";
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    @Override
    public int generateEnergy(Terrain terrain) {
        return 0;
    }

    @Override
    public int getMetabolismCost() {
        return 0;
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
