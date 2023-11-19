package net.lukemcomber.dev.ai.genetics.biology;

import net.lukemcomber.dev.ai.genetics.biology.plant.PlantGenome;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantOrganism;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.dev.ai.genetics.biology.transcription.AsexualTransposGenomeTranscriber;
import net.lukemcomber.dev.ai.genetics.biology.transcription.AsexualTransposeAndMutateGeneTranscriber;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;
import net.lukemcomber.dev.ai.genetics.model.TemporalCoordinates;
import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;

import java.util.LinkedList;
import java.util.List;

public class OrganismFactory {

    public static Organism create(final String parentId, final Genome genome, final SpatialCoordinates spatialCoordinates,
                                  final TemporalCoordinates temporalCoordinates, final UniverseConstants properties ) {
        final Organism retVal;
        if (genome instanceof PlantGenome) {

            final SeedCell seedCell = new SeedCell(null, genome, spatialCoordinates,properties);
            final GenomeTransciber transciber = new AsexualTransposeAndMutateGeneTranscriber();

            retVal = new PlantOrganism(parentId, seedCell, temporalCoordinates, properties, transciber);

        } else {
            throw new EvolutionException("Unknown species " + genome.toString());
        }
        return retVal;
    }

    public static Genome createGenome(final String type, final List<Gene> genes) {
        final Genome retVal;
        switch (type) {
            case PlantOrganism.TYPE:
                retVal = new PlantGenome(genes);
                break;
            default:
                throw new EvolutionException("Unknown species " + type);
        }
        return retVal;
    }
    public static Genome cloneGenome(final Genome genome){
        //sure wish java had copy constructor inheritance
        final Genome retVal;
        final List<Gene> geneList = new LinkedList<>();
        for( int i =0; i < genome.getNumberOfGenes(); ++i ) {
            geneList.add(genome.getGeneNumber(i));
        }
        retVal = createGenome(genome.getType(), geneList);

        return retVal;
    }
}
