package net.lukemcomber.dev.ai.genetics.biology.plant.cells;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantOrganism;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowRoot;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

public class SeedCell extends PlantCell {

    private final Coordinates coordinates;
    private final Genome genome;

    private boolean activated;

    public SeedCell(final Cell parent, final Genome genome, final Coordinates coordinates){

        super(parent);

        this.genome = genome;
        this.coordinates = coordinates;
        this.activated = false;
    }

   public void activate(){
        this.activated = true;
   }

    public Genome getGenome(){
        return genome;
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
        //Only allow growth if we are activated
        return activated ? behavior instanceof GrowRoot || behavior instanceof GrowLeaf : false;
    }
}
