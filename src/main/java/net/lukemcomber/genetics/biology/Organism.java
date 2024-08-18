package net.lukemcomber.genetics.biology;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.fitness.FitnessFunction;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.io.CellHelper;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.io.OutputStream;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.logging.Logger.getLogger;

/**
 * Represents an organism in the simulation
 */
public interface Organism {

    String DEFAULT_PARENT = "GOD";

    enum CauseOfDeath {
        Unknown,
        Stagnation,
        Exhaustion,
        OldAge;

        public final static int count = CauseOfDeath.values().length;
    }

    /**
     * Allow the organism to perform it's next action
     *
     * @param terrain             the terrain
     * @param temporalCoordinates time
     * @param onCellDeath         callback if a cell dies
     */
    void performAction(final Terrain terrain, final TemporalCoordinates temporalCoordinates,
                       final BiConsumer<Organism, Cell> onCellDeath);

    /**
     * Clean up all cells from the terrain
     *
     * @param terrain
     */
    void cleanup(final Terrain terrain);

    /**
     * Get the organisms unique id
     *
     * @return unique id
     */
    String getUniqueID();

    /**
     * Get the organism's parent id
     *
     * @return parent id
     */
    String getParentId();

    /**
     * Get the current fitness function
     *
     * @return fitness function
     */
    FitnessFunction getFitnessFunction();

    /**
     * Get the current genome transcriber
     *
     * @return transcriber
     */
    GenomeTransciber getTranscriber();

    /**
     * Get the organisms genome
     *
     * @return genome
     */
    Genome getGenome();

    /**
     * Get the organism's type
     *
     * @return type
     */
    String getOrganismType();

    /**
     * Get the first cell of the organism's life
     *
     * @return cell
     */
    Cell getFirstCell();

    /**
     * Get the organisms current energy
     *
     * @return energy
     */
    int getEnergy();

    /**
     * Check if organism is alive
     *
     * @return true if alive
     */
    boolean isAlive();

    /**
     * Get the organisms birth tick
     *
     * @return tick
     */
    long getBirthTick();

    /**
     * Get the last updated tick of the organism
     *
     * @return tick
     */
    long getLastUpdatedTick();

    /**
     * Add energy to the organism from the environment
     *
     * @param energy
     */
    void addEnergyFromEcosystem(int energy);

    /**
     * Remove energy from the organism for metabolism
     *
     * @param energy
     */
    void removeEnergyFromMetabolism(int energy);

    /**
     * Spend energy to perform some action
     *
     * @param energy
     */
    void spendEnergy(int energy);

    /**
     * Kill the organism
     *
     * @param temporalCoordinates time
     * @param causeOfDeath        cause of death
     * @param reason              human-readable message
     */
    void kill(final TemporalCoordinates temporalCoordinates, CauseOfDeath causeOfDeath, final String reason);

    /**
     * Utility method for nicely formatting organism information into the provided {@link OutputStream}
     *
     * @param out
     */
    void prettyPrint(final OutputStream out);


    /**
     * Get the organism's cost of being alive in energy
     *
     * @return energy
     */
    default int getMetabolismCost() {
        return CellHelper.getAllOrganismsCells(getFirstCell()).stream().mapToInt(Cell::getMetabolismCost).sum();
    }

    /**
     * Gather resource from the environment and metabolise it
     *
     * @param terrain
     * @param temporalCoordinates time
     */
    default void leechResources(final Terrain terrain, final TemporalCoordinates temporalCoordinates) {
        final List<Cell> cells = CellHelper.getAllOrganismsCells(getFirstCell());
        int newEnergy = cells.stream().mapToInt(cell -> cell.generateEnergy(terrain)).sum();
        int metaCost = getMetabolismCost();

        addEnergyFromEcosystem(newEnergy);
        removeEnergyFromMetabolism(metaCost);
    }

}
