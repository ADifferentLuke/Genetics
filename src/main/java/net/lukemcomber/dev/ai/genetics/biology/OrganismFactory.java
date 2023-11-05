package net.lukemcomber.dev.ai.genetics.biology;

import net.lukemcomber.dev.ai.genetics.biology.plant.PlantGenome;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantOrganism;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;

import java.util.List;

public class OrganismFactory {

    public static Organism create(final String parentId, final Genome genome, final Coordinates coordinates,
                                  final long currentTick) {
        final Organism retVal;
        if (genome instanceof PlantGenome) {
            //TODO MUTATION HERE

            final SeedCell seedCell = new SeedCell(null, genome, coordinates);

            retVal = new PlantOrganism(parentId, seedCell, currentTick);

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
