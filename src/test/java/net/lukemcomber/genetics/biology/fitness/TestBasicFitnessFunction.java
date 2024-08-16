package net.lukemcomber.genetics.biology.fitness;

import net.lukemcomber.genetics.biology.fitness.impl.BasicFitnessFunction;
import net.lukemcomber.genetics.store.TestSearchableMetadataStore;
import net.lukemcomber.genetics.store.metadata.Performance;
import org.testng.annotations.Test;

import java.util.logging.Logger;

public class TestBasicFitnessFunction {

    private static final Logger logger = Logger.getLogger(TestBasicFitnessFunction.class.getName());


    @Test
    public void doFitnessCalculation(){
        final Performance performance = new Performance();
        performance.age = 1010l;
        performance.offspring = 9;
        performance.cells = 1072;
        performance.deathEnergy = 161943;
        performance.totalEnergyHarvested = 831448;
        performance.totalEnergyMetabolized = 482016;
        performance.causeOfDeath = 1;

        final BasicFitnessFunction fitnessFunction = new BasicFitnessFunction();
        performance.fitness = fitnessFunction.apply(performance);

        logger.info("Fitness: " + performance.fitness);

    }
}
