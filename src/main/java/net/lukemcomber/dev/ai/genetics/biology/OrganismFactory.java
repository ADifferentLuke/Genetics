package net.lukemcomber.dev.ai.genetics.biology;

import net.lukemcomber.dev.ai.genetics.biology.plant.PlantGenome;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantOrganism;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;
import net.lukemcomber.dev.ai.genetics.model.TemporalCoordinates;

import java.util.List;

//TODO might be redudant now
public class OrganismFactory {

    public static Organism create(final String parentId, final Genome genome, final SpatialCoordinates spatialCoordinates,
                                  final TemporalCoordinates temporalCoordinates) {
        final Organism retVal;
        if (genome instanceof PlantGenome) {

            final SeedCell seedCell = new SeedCell(null, genome, spatialCoordinates);

            retVal = new PlantOrganism(parentId, seedCell, temporalCoordinates);

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
}
