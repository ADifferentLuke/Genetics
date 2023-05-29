package net.lukemcomber.dev.ai.genetics.biology;

import net.lukemcomber.dev.ai.genetics.biology.plant.PlantGenome;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantOrganism;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;

import java.util.List;

public class OrganismFactory {

    public static Organism create(final String type, final Genome genome ){
        final Organism retVal;
        switch (type){
            case PlantOrganism.TYPE:
                retVal = new PlantOrganism( genome );
                break;
            default:
                throw new EvolutionException("Unknown species " + type );
        }
        return retVal;
    }

    public static Genome createGenome(final String type, final List<Gene> genes ){
        final Genome retVal;
        switch (type){
            case PlantOrganism.TYPE:
                retVal = new PlantGenome( genes );
                break;
            default:
                throw new EvolutionException("Unknown species " + type );
        }
        return retVal;
    }
}
