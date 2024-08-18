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
}
