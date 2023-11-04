package net.lukemcomber.dev.ai.genetics.biology;

import net.lukemcomber.dev.ai.genetics.service.CellHelper;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

public interface Organism {

    Genome getGenome();

    String getOrganismType();

    Cell getCells();

    int getEnergy();

    void modifyEnergy(int delta);

    Cell performAction(final Terrain terrain);

    String getUniqueID();
    String getParentId();

    default void leechResources( final Terrain terrain){
        final List<Cell> cells = CellHelper.getAllOrganismsCells(getCells());
        int newEnergy = cells.stream().mapToInt(cell -> cell.generateEnergy(terrain)).sum();
        int metaCost = cells.stream().mapToInt(Cell::getMetabolismCost).sum();

        logger().info( "Gathered: " + newEnergy );
        logger().info( "Cost: " + (-metaCost));

        modifyEnergy(newEnergy);
        modifyEnergy(-metaCost);
    }

    void prettyPrint(final OutputStream out);

    private static Logger logger() {
        final class LogHolder {
            //Lazy load
            private static final Logger LOGGER = getLogger(Organism.class.getName());
        }
        return LogHolder.LOGGER;
    }


    //needs a genome
    //needs cells
    //needs age
    //needs energy
    //sensors
    //efficiency of energy consumption decreases with time

}
