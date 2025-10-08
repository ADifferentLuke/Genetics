package net.lukemcomber.genetics.biology.plant.cells;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Genome;
import net.lukemcomber.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.genetics.biology.plant.PlantCell;
import net.lukemcomber.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.genetics.biology.plant.behavior.GrowRoot;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.world.terrain.Terrain;

/**
 * A seed cell that can grow any cell
 */
public class SeedCell extends PlantCell {

    public static final String TYPE = "seed";

    public static final String PROPERTY_METACOST = "cell.seed.metabolic-rate";
    public static final String PROPERTY_ENERGY = "cell.seed.max-energy-production";
    private final SpatialCoordinates spatialCoordinates;
    private final Genome genome;
    private final int metabolismCost;
    private boolean activated;
    private int totalEnergyCollected;

    /**
     * Create a new seed cell
     *
     * @param parent             parent cell
     * @param genome             cell genome
     * @param spatialCoordinates location
     * @param properties         configuration properties
     */
    public SeedCell(final Cell parent, final Genome genome, final SpatialCoordinates spatialCoordinates,
                    final UniverseConstants properties) {

        super(parent);

        this.genome = genome;
        this.spatialCoordinates = spatialCoordinates;
        this.activated = false;

        this.metabolismCost = properties.get(PROPERTY_METACOST, Integer.class);
        this.totalEnergyCollected = 0;

        // Seeds at the root of a plant are active
        if (null == parent) {
            activate();
        }
    }

    /**
     * Returns true if the cell is activated and can grow
     *
     * @return true if activated
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * Activate cell initiating growth
     */
    public void activate() {
        this.activated = true;
    }

    /**
     * Get the cell's genome
     *
     * @return genome
     */
    public Genome getGenome() {
        return genome;
    }

    /**
     * Gets the cell's type
     *
     * @return cell type
     */
    @Override
    public String getCellType() {
        return TYPE;
    }

    /**
     * Get the cell's location
     *
     * @return location
     */
    public SpatialCoordinates getCoordinates() {
        return spatialCoordinates;
    }

    /**
     * Generate energy from resources
     *
     * @param terrain
     * @return amount of energy harvested
     */
    @Override
    public int generateEnergy(final Terrain terrain) {
        int energy =  terrain.getProperties().get(PROPERTY_ENERGY, Integer.class);
        totalEnergyCollected += energy;
        return energy;
    }

    /**
     * Get the cost of being alive
     *
     * @return cost
     */
    @Override
    public int getMetabolismCost() {
        return metabolismCost;
    }

    @Override
    public int getTotalEnergyGenerated() {
        return totalEnergyCollected;
    }

    /**
     * Return true if the cell is capable of performing the behavior
     *
     * @param behavior action to check
     * @return true if possible otherwise false
     */
    @Override
    public boolean canCellSupport(final PlantBehavior behavior) {
        //Only allow growth if we are activated
        return activated ? behavior instanceof GrowRoot || behavior instanceof GrowLeaf : false;
    }
}
