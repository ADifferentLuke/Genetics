package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;

public class SeedCell implements Cell {

    private final int xCoord;
    private final int yCoord;
    private final int zCoord;

    private final Organism organism;

    public SeedCell( final Organism organism, final int xPos, final int yPos, final int zPos ){
        this.organism = organism;
        xCoord = xPos;
        yCoord = yPos;
        zCoord = zPos;
    }
    /**
     * @return
     */
    @Override
    public Organism getOrganism() {
        return organism;
    }
}
