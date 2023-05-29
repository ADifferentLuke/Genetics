package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;

public class SeedCell implements Cell {

    private final Coordinates coordinates;
    private final Organism organism;

    public SeedCell(final Organism organism, final Coordinates coordinates ){
        this.organism = organism;
        this.coordinates = coordinates;
    }
    /**
     * @return
     */
    @Override
    public Organism getOrganism() {
        return organism;
    }

    public Coordinates getCoordinates(){
        return coordinates;
    }
}
