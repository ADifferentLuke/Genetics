package net.lukemcomber.genetics.biology.fitness.impl;

import net.lukemcomber.genetics.biology.fitness.FitnessFunction;
import net.lukemcomber.genetics.store.metadata.Performance;

import java.util.Random;

public class RandomFitnessFunction implements FitnessFunction {

    private final Random rng;

    public RandomFitnessFunction(){
        rng = new Random();
    }


    @Override
    public Double apply(final Performance performance) {
        return rng.nextDouble();
    }
}
