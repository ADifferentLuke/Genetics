package net.lukemcomber.genetics.biology.fitness.impl;

import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.fitness.FitnessFunction;
import net.lukemcomber.genetics.store.metadata.Performance;

import java.util.Random;

/**
 * Assigns random numbers as fitness
 */
public class RandomFitnessFunction implements FitnessFunction {

    private final Random rng;

    /**
     * Create a new instance
     */
    public RandomFitnessFunction(){
        rng = new Random();
    }


    /**
     * Calculate the fitness from a {@link Performance}
     * @param performance the function argument
     * @return fitness
     */
    @Override
    public double calculate(final Performance performance, final Organism organism) {
        return rng.nextDouble();
    }
}
