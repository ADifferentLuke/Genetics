package net.lukemcomber.genetics.biology.fitness;

import net.lukemcomber.genetics.biology.fitness.impl.BasicFitnessFunction;
import net.lukemcomber.genetics.store.TestSearchableMetadataStore;
import net.lukemcomber.genetics.store.metadata.Performance;
import org.testng.annotations.Test;

import java.util.logging.Logger;

public class TestBasicFitnessFunction {

    private static final Logger logger = Logger.getLogger(TestBasicFitnessFunction.class.getName());


    @Test
    public void doFitnessCalculation() {
        final Performance performance = new Performance();
        performance.setAge(1010l);
        performance.setOffspring(9);
        performance.setCells(1072);
        performance.setDeathEnergy(161943);
        performance.setTotalEnergyHarvested(831448);
        performance.setTotalEnergyMetabolized(482016);
        performance.setCauseOfDeath(1);

        final BasicFitnessFunction fitnessFunction = new BasicFitnessFunction();
        performance.setFitness(fitnessFunction.apply(performance));

        logger.info("Fitness: " + performance.getFitness());

    }
}
