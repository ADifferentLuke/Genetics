package net.lukemcomber.dev.ai.genetics.biology;

import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

import java.io.OutputStream;
import java.util.List;

public interface Organism {

    Genome getGenome();

    String getOrganismType();

    Cell getCells();

    Cell performAction(final Terrain terrain);

    void leechResources( final Terrain terrain);

    void prettyPrint(final OutputStream out);


    //needs a genome
    //needs cells
    //needs age
    //needs energy
    //sensors
    //efficiency of energy consumption decreases with time

}
