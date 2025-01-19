package net.lukemcomber.genetics.biology.plant.cells;

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Genome;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Create a new seed that can be ejected from the plant
 */
public class EjectedSeedCell extends SeedCell implements PlantBehavior {

    private static final Logger logger = Logger.getLogger(EjectedSeedCell.class.getName());
    private int energy;
    private final Function<SpatialCoordinates, SpatialCoordinates> spatialTransformer;
    private SpatialCoordinates spatialCoordinates;

    /**
     * Creates a new instance
     *
     * @param genome             cell genome
     * @param spatialCoordinates location
     * @param properties         configuration properties
     * @param energy             travel energy
     * @param func               location modification function
     */
    public EjectedSeedCell(final Genome genome, final SpatialCoordinates spatialCoordinates,
                           final UniverseConstants properties, final int energy,
                           final Function<SpatialCoordinates, SpatialCoordinates> func) {
        super(null, genome, spatialCoordinates, properties);
        this.energy = energy;
        spatialTransformer = func;
        this.spatialCoordinates = spatialCoordinates;
    }

    /**
     * Does nothing. The cell will be activated when it reaches its final location
     */
    @Override
    public void activate() {
        // Don't allow activation until we are in place
        logger.info("Squelching activate signal.");
    }

    /**
     * Cell will use travel energy to change its spatial coordinates. After it has either
     * collided or run out of energy, the seed cell is activated to start growing
     *
     * @param terrain             the terrain
     * @param activeCell          cell performing the action
     * @param temporalCoordinates time
     * @param metadataStoreGroup  metadata cache
     * @return null
     */
    @Override
    public Cell performAction(final UniverseConstants properties, final Terrain terrain, final Organism organism,
                              final Cell activeCell, final TemporalCoordinates temporalCoordinates,
                              final MetadataStoreGroup metadataStoreGroup) {

        if (0 < energy) {
            // move the cell through the terrain until collision or energy is depletedo
            final SpatialCoordinates newSpatialCoordinates = spatialTransformer.apply(getCoordinates());

            if ((!terrain.isOutOfBounds(newSpatialCoordinates)) && (!terrain.hasCell(newSpatialCoordinates))) {
                if (terrain.deleteCell(getCoordinates(), organism.getUniqueID())) {
                    spatialCoordinates = newSpatialCoordinates;
                    terrain.setCell(this, organism);
                    energy = energy - getEnergyCost(properties);
                } else {
                    throw new RuntimeException("This shouldn't happen");
                }
            } else {
                super.activate();
                logger.info("Collision: Ejected seed activated with wasted energy.");
            }
        } else {
            super.activate();
            logger.info("Ejected seed activated.");
        }

        return null;
    }

    /**
     * Get the current location
     *
     * @return location
     */
    @Override
    public SpatialCoordinates getCoordinates() {
        return spatialCoordinates;
    }

    /**
     * Get the cost of traveling once per tick
     *
     * @param properties configuration properties
     * @return cost
     */
    @Override
    public int getEnergyCost(UniverseConstants properties) {
        return 1; //Travel expense
    }
}
