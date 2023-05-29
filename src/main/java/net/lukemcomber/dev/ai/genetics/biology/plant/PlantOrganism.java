package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.Genome;

import java.util.LinkedList;
import java.util.List;

public class PlantOrganism implements Organism {

    public static final String TYPE = "PLANT";
    private final Genome genome;

    private final List<Cell> cells;

    //private PlantOrganism(){genome=null;}

    public PlantOrganism( final Genome genome ){
       this.genome = genome;
       cells = new LinkedList<>();
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

    /**
     * @param cell
     */
    @Override
    public void addCell(Cell cell) {
        cells.add(cell);
    }
}
