package net.lukemcomber.genetics.biology.fitness;

import com.google.common.collect.ImmutableMap;
import net.lukemcomber.genetics.TestUniverse;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.fitness.impl.BasicFitnessFunction;
import net.lukemcomber.genetics.store.metadata.Performance;
import org.testng.annotations.Test;

import java.util.logging.Logger;

public class TestBasicFitnessFunction {

    private static final Logger logger = Logger.getLogger(TestBasicFitnessFunction.class.getName());


    @Test
    public void doFitnessCalculation() {
        final TestUniverse testUniverse = new TestUniverse(ImmutableMap.of(
                Organism.PROPERTY_OLD_AGE_LIMIT, 1000
        ));
        final Performance performance = new Performance();
        performance.setAge(1010l);
        performance.setOffspring(9);
        performance.setCells(1072);
        performance.setDeathEnergy(161943);
        performance.setTotalEnergyHarvested(831448);
        performance.setTotalEnergyMetabolized(482016);
        performance.setCauseOfDeath(1);

        final BasicFitnessFunction fitnessFunction = new BasicFitnessFunction(testUniverse);
        performance.setFitness(fitnessFunction.calculate(performance,null));

        logger.info("Fitness: " + performance.getFitness());

    }
}
