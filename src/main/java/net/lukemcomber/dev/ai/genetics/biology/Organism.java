package net.lukemcomber.dev.ai.genetics.biology;

import java.util.List;

public interface Organism {

    Genome getGenome();

    String getOrganismType();

    void addCell( Cell cell );

    List<Cell> getCells();


    //needs a genome
    //needs cells
    //needs age
    //needs energy
    //sensors
    //efficiency of energy consumption decreases with time

}
