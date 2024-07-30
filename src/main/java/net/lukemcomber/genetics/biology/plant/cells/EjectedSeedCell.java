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

public class EjectedSeedCell extends SeedCell implements PlantBehavior {

    private static final Logger logger = Logger.getLogger(EjectedSeedCell.class.getName());

    private int energy;
    private final Function<SpatialCoordinates, SpatialCoordinates> spatialTransformer;
    private SpatialCoordinates spatialCoordinates;

    public EjectedSeedCell(Genome genome, SpatialCoordinates spatialCoordinates, UniverseConstants properties,
                           final int energy, final Function<SpatialCoordinates, SpatialCoordinates> func) {
        super(null, genome, spatialCoordinates, properties);
        this.energy = energy;
        spatialTransformer = func;
        this.spatialCoordinates = spatialCoordinates;
    }

    @Override
    public void activate() {
        // Don't allow activation until we are in place
        logger.info("Squelching activate signal.");
    }

    @Override
    public Cell performAction(final UniverseConstants properties, final Terrain terrain, final Organism organism,
                              final Cell activeCell, final TemporalCoordinates temporalCoordinates,
                              final MetadataStoreGroup metadataStoreGroup) {

        if( 0 < energy ) {
            // move the cell through the terrain until collision or energy is depletedo
            final SpatialCoordinates newSpatialCoordinates = spatialTransformer.apply(getCoordinates());

            if ((!terrain.isOutOfBounds(newSpatialCoordinates)) && (!terrain.hasCell(newSpatialCoordinates))) {
                if (terrain.deleteCell(getCoordinates())) {
                    spatialCoordinates = newSpatialCoordinates;
                    terrain.setCell(this, organism);
                    energy = energy - getEnergyCost(properties);
                } else {
                    //throw new RuntimeException("This shouldn't happen");
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

    @Override
    public SpatialCoordinates getCoordinates() {
        return spatialCoordinates;
    }

    @Override
    public int getEnergyCost(UniverseConstants properties) {
        return 1; //Travel expense
    }
}
