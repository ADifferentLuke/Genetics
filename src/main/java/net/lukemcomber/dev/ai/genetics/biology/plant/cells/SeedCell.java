package net.lukemcomber.dev.ai.genetics.biology.plant.cells;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowRoot;
import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;
import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

public class SeedCell extends PlantCell {

    public static final String PROPERTY_METACOST = "cell.seed.metabolic-rate";
    public static final String PROPERTY_ENERGY = "cell.seed.max-energy-production";

    private final SpatialCoordinates spatialCoordinates;
    private final Genome genome;

    private final int metabolismCost;

    private boolean activated;

    public SeedCell(final Cell parent, final Genome genome, final SpatialCoordinates spatialCoordinates,
                    final UniverseConstants properties ){

        super(parent);

        this.genome = genome;
        this.spatialCoordinates = spatialCoordinates;
        this.activated = false;

        this.metabolismCost = properties.get(PROPERTY_METACOST, Integer.class);
    }
    public boolean isActivated(){
        return activated;
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

    public SpatialCoordinates getCoordinates() {
        return spatialCoordinates;
    }

    @Override
    public int generateEnergy(final Terrain terrain) {
        return terrain.getProperties().get(PROPERTY_ENERGY, Integer.class);
    }

    @Override
    public int getMetabolismCost() {
        return metabolismCost;
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
