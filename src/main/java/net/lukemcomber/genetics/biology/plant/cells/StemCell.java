package net.lukemcomber.genetics.biology.plant.cells;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.genetics.biology.plant.PlantCell;
import net.lukemcomber.genetics.biology.plant.behavior.EjectSeed;
import net.lukemcomber.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.genetics.biology.plant.behavior.GrowSeed;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.world.terrain.Terrain;

/**
 * Not that kind of stem cell. Cell for the stem of a plant
 */
public class StemCell extends PlantCell {

    public static final String TYPE = "stem";

    public static final String PROPERTY_METACOST = "cell.stem.metabolic-rate";
    public static final String PROPERTY_ENERGY = "cell.stem.max-energy-production";
    private final SpatialCoordinates spatialCoordinates;

    private final int metabolismCost;

    /**
     * Creates a new cell for a stem
     *
     * @param parent             parent cell
     * @param spatialCoordinates location
     * @param properties         configuration properties
     */
    public StemCell(final Cell parent, final SpatialCoordinates spatialCoordinates,
                    final UniverseConstants properties) {
        super(parent);
        this.spatialCoordinates = spatialCoordinates;
        this.metabolismCost = properties.get(PROPERTY_METACOST, Integer.class);
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
    @Override
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
        return terrain.getProperties().get(PROPERTY_ENERGY, Integer.class);
    }

    /**
     * Get the cost of being alive
     *
     * @return cost
     */
    @Override
    public int getMetabolismCost() {
        return this.metabolismCost;
    }

    /**
     * Return true if the cell is capable of performing the behavior
     *
     * @param behavior action to check
     * @return true if possible otherwise false
     */
    @Override
    public boolean canCellSupport(final PlantBehavior behavior) {
        return behavior instanceof GrowLeaf || behavior instanceof GrowSeed || behavior instanceof EjectSeed;
    }
}
