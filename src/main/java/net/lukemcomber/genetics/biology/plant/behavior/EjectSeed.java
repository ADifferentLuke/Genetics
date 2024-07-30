package net.lukemcomber.genetics.biology.plant.behavior;

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.GenomeTransciber;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.genetics.biology.plant.PlantOrganism;
import net.lukemcomber.genetics.biology.plant.cells.EjectedSeedCell;
import net.lukemcomber.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.util.function.Function;
import java.util.logging.Logger;

public class EjectSeed implements PlantBehavior {

    public final static String PROPERTY_EJECT_LEAF_COST = "action.seed.eject";


    private static final Logger logger = Logger.getLogger(EjectSeed.class.getName());
    private final Function<SpatialCoordinates, SpatialCoordinates> function;

    public EjectSeed(final Function<SpatialCoordinates, SpatialCoordinates> func) {
        this.function = func;
    }

    @Override
    public Cell performAction(final UniverseConstants properties, final Terrain terrain, final Organism organism,
                              final Cell activeCell, final TemporalCoordinates temporalCoordinates,
                              final MetadataStoreGroup metadataStoreGroup) {
        final long cur = System.currentTimeMillis();
        SeedCell retVal = null;

        final SpatialCoordinates newSpatialCoordinates = function.apply(activeCell.getCoordinates());


        //The boolean logic looks weird, but we need to use AND for short circuit
        if ((!terrain.isOutOfBounds(newSpatialCoordinates)) && (!terrain.hasCell(newSpatialCoordinates))) {

            if (null != organism) {
                //Organism shouldn't be null, but we're in mid-redesign ... so blow up if it happens

                final GenomeTransciber transciber = organism.getTranscriber();

                //We don't want to die from exhaustion after ejecting, so don't use ALL energy
                final int leftOverEnergy = organism.getEnergy() - getEnergyCost(properties);
                final int throwDistance;
                if (1 < leftOverEnergy) {
                    throwDistance = leftOverEnergy - 1;
                } else {
                    throwDistance = 0;
                }
                /*
                 * DEV NOTE: This is where mutation is initiated!
                 */
                retVal = new EjectedSeedCell(transciber.transcribe(terrain.getProperties(),
                        organism.getGenome()), newSpatialCoordinates, terrain.getProperties(), throwDistance,
                        function);

                final PlantOrganism plantOrganism = new PlantOrganism(organism.getUniqueID(), retVal,
                        temporalCoordinates, properties, transciber, organism.getFitnessFunction(), metadataStoreGroup);

                logger.info(String.format("Created %s at %s from EjectedSeed", plantOrganism.getUniqueID(), newSpatialCoordinates));

                terrain.setCell(retVal, organism);
                terrain.addOrganism(plantOrganism);


                //Energy dump
                organism.spendEnergy(getEnergyCost(properties) + throwDistance);
            } else {
                throw new RuntimeException("Organism is null!");
            }
        } else {
            throw new EvolutionException("Seed growth failed. Collision detected.");
        }

        return retVal;
    }

    @Override
    public int getEnergyCost(final UniverseConstants properties) {
        return properties.get(PROPERTY_EJECT_LEAF_COST, Integer.class);

    }
}
