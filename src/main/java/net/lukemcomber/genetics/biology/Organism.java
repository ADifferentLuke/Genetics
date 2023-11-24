package net.lukemcomber.genetics.biology;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.service.CellHelper;
import net.lukemcomber.genetics.store.MetadataStoreFactory;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.io.OutputStream;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

//TODO clean up this interface. It's NASTY!
public interface Organism {

    GenomeTransciber getTranscriber();

    Genome getGenome();

    String getOrganismType();

    Cell getCells();

    int getEnergy();

    boolean isAlive();

    void modifyEnergy(int delta);

    long getBirthTick();

    long getLastUpdatedTick();

    Cell performAction(final Terrain terrain, final TemporalCoordinates temporalCoordinates,
                       final BiConsumer<Organism,Cell> onCellDeath);

    void cleanup(final Terrain terrain);

    String getUniqueID();
    String getParentId();

    default int getMetabolismCost(){
        return CellHelper.getAllOrganismsCells(getCells()).stream().mapToInt(Cell::getMetabolismCost).sum();
    }

    default void leechResources( final Terrain terrain, final TemporalCoordinates temporalCoordinates){
        final List<Cell> cells = CellHelper.getAllOrganismsCells(getCells());
        int newEnergy = cells.stream().mapToInt(cell -> cell.generateEnergy(terrain)).sum();
        int metaCost = getMetabolismCost();

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


}
