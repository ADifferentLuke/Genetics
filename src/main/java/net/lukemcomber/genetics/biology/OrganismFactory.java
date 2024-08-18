package net.lukemcomber.genetics.biology;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.fitness.FitnessFunction;
import net.lukemcomber.genetics.biology.fitness.impl.BasicFitnessFunction;
import net.lukemcomber.genetics.biology.plant.PlantGenome;
import net.lukemcomber.genetics.biology.plant.PlantOrganism;
import net.lukemcomber.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.genetics.biology.transcription.AsexualTransposeAndMutateGeneTranscriber;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.MetadataStoreGroup;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A factory for creating new organisms
 */
public class OrganismFactory {

    private static final Logger logger = Logger.getLogger(OrganismFactory.class.getName());

    /**
     * Creates a new Organism using the {@link Genome} type to determine organism type
     *
     * @param parentId            the new organisms parent
     * @param genome              the new organisms genome
     * @param spatialCoordinates  location
     * @param temporalCoordinates time
     * @param properties          configuration properties
     * @param groupStore          metadata store
     * @return a new organism
     */
    public static Organism create(final String parentId, final Genome genome, final SpatialCoordinates spatialCoordinates,
                                  final TemporalCoordinates temporalCoordinates, final UniverseConstants properties,
                                  final MetadataStoreGroup groupStore) {
        final Organism retVal;
        if (genome instanceof PlantGenome) {

            final SeedCell seedCell = new SeedCell(null, genome, spatialCoordinates, properties);
            final GenomeTransciber transciber = new AsexualTransposeAndMutateGeneTranscriber();
            final FitnessFunction fitnessFunction = new BasicFitnessFunction(properties);

            retVal = new PlantOrganism(parentId, seedCell, temporalCoordinates, properties, transciber, fitnessFunction, groupStore);
            logger.info(String.format("Created %s at %s from OrganismFactory", retVal.getUniqueID(), spatialCoordinates));

        } else {
            throw new EvolutionException("Unknown species " + genome.toString());
        }
        return retVal;
    }


}
