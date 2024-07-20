package net.lukemcomber.genetics.biology.fitness;

import net.lukemcomber.genetics.store.metadata.Performance;

import java.util.function.Function;

public interface FitnessFunction extends Function<Performance,Float> {
}
