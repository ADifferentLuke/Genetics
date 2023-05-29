package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.Genome;

public class PlantOrganism implements Organism {

    public static final String TYPE = "PLANT";
    private final Genome genome;

    //private PlantOrganism(){genome=null;}

    public PlantOrganism( final Genome genome ){
       this.genome = genome;
    }

    public Genome getGenome() {
        return genome;
    }

    /**
     * @return
     */
    @Override
    public String getOrganismType() {
        return TYPE;
    }
}
