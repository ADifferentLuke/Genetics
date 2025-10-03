package net.lukemcomber.genetics.biology.fitness;

/*
 * (c) 2024 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.io.GenomeSerDe;
import net.lukemcomber.genetics.store.metadata.Performance;

import java.util.function.Function;

/**
 * Interface for providing fitness calculations.
 */
public interface FitnessFunction extends Function<Organism, Performance> {

    String FITNESS_AGE_WEIGHT = "fitness.age.weight";
    String FITNESS_CELLS_WEIGHT = "fitness.numerOfCells.weight";
    String FITNESS_UNUSED_ENERGY_WEIGHT = "fitness.leftOverEnergy.weight";
    String FITNESS_ENERGY_EFFICIENCY_WEIGHT = "fitness.energyEfficiency.weight";
    String FITNESS_CHILDREN_WEIGHT = "fitness.children.weight";

    default Performance apply(final Organism organism) {
        Performance performance = new Performance();
        performance.setName(organism.getUniqueID());
        performance.setParentId(organism.getParentId());
        performance.setDna(GenomeSerDe.serialize(organism.getGenome()));

        performance.setOffspring(organism.getOffspringCount());

        performance.setBirthTick(organism.getBirthTick());
        performance.setDeathEnergy(organism.getEnergy());
        performance.setDeathTick(organism.getLastUpdatedTick());

        performance.setCauseOfDeathStr(organism.getDeathDetails());
        performance.setCauseOfDeath(organism.getCauseOfDeath().ordinal());
        performance.setAge(performance.getDeathTick() - performance.getBirthTick());

        performance.setTotalEnergyHarvested(organism.getTotalEnergyHarvested());
        performance.setTotalEnergyMetabolized(organism.getTotalEnergyMetabolized());

        performance.setCells(organism.getCellCount());
        performance.setFitness(calculate(performance, organism));
        return performance;
    }

    double calculate(final Performance performance, final Organism organism);
}
