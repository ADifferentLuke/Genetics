package net.lukemcomber.genetics.biology.fitness;

/*
 * (c) 2024 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.store.metadata.Performance;

import java.util.function.Function;

/**
 * Interface for providing fitness calculations.
 */
public interface FitnessFunction extends Function<Performance,Double> {

    String FITNESS_AGE_WEIGHT = "fitness.age.weight";
    String FITNESS_CELLS_WEIGHT = "fitness.numerOfCells.weight";
    String FITNESS_UNUSED_ENERGY_WEIGHT = "fitness.leftOverEnergy.weight";
    String FITNESS_ENERGY_EFFICIENCY_WEIGHT = "fitness.energyEfficiency.weight";
    String FITNESS_CHILDREN_WEIGHT = "fitness.children.weight";
}
