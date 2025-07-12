package net.lukemcomber.genetics.biology.fitness.impl;

import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.fitness.FitnessFunction;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.metadata.Performance;

import java.util.logging.Logger;

/**
 * A fitness function based on Organism Age, Number of Cells, Energy Efficiency,
 * Energy Waste, Offspring, and Death to generate a deterministic fitness
 */
public class BasicV2FitnessFunction implements FitnessFunction {

    private static final Logger logger = Logger.getLogger( BasicV2FitnessFunction.class.getName());
    public static final double DEFAULT_AGE_WEIGHT = 1;
    public static final double DEFAULT_CELLS_WEIGHT = 0.5;
    public static final double DEFAULT_UNUSED_ENERGY_WEIGHT = 0.2;
    public static final double DEFAULT_ENERGY_EFFICIENCY_WEIGHT = 1.5;
    public static final double DEFAULT_CHILDREN_WEIGHT = 1;

    public static final int DEFAULT_MAX_AGE = 1000;

    private final double ageWeight;
    private final double cellsWeight;
    private final double unusedEnergyWeight;
    private final double energyEfficiencyWeight;
    private final double childrenWeight;
    private final int maximumAge;


    /**
     * Creates a new instance with weights from configuration
     *
     * @param constants configuration properties
     */
    public BasicV2FitnessFunction(final UniverseConstants constants) {

        this.ageWeight = constants.get(FITNESS_AGE_WEIGHT, Double.class, DEFAULT_AGE_WEIGHT);
        this.cellsWeight = constants.get(FITNESS_CELLS_WEIGHT, Double.class, DEFAULT_CELLS_WEIGHT);
        this.unusedEnergyWeight = constants.get(FITNESS_UNUSED_ENERGY_WEIGHT, Double.class, DEFAULT_UNUSED_ENERGY_WEIGHT);
        this.energyEfficiencyWeight = constants.get(FITNESS_ENERGY_EFFICIENCY_WEIGHT, Double.class, DEFAULT_ENERGY_EFFICIENCY_WEIGHT);
        this.childrenWeight = constants.get(FITNESS_CHILDREN_WEIGHT, Double.class, DEFAULT_CHILDREN_WEIGHT);
        this.maximumAge = constants.get(Organism.PROPERTY_OLD_AGE_LIMIT, Integer.class, DEFAULT_MAX_AGE);

    }


    /**
     * Calculate the fitness from a {@link Performance}
     *
     * @param performance the function argument
     * @return fitness
     */
    @Override
    public Double apply(final Performance performance) {

        // larger is better, but not a huge advantage.
        double fitness = 0;

        final double ageFactor = ageWeight * ( (double) performance.getAge() / maximumAge );
        //final double reproductionFactor = (1- Math.pow(0.5, performance.getOffspring()));
        final double reproductionFactor = 1 - Math.exp( -performance.getOffspring() );
        final double sizeFactor = 1 - Math.exp( -performance.getCells() );

        final double deathValue = (double) performance.getCauseOfDeath() / Organism.CauseOfDeath.count;

        if( 0 < performance.getOffspring()){
            fitness = sizeFactor + reproductionFactor;
        }

        logger.info( "%s - Reproduction Factor: %f Size Factor: %f Fitness: %f".formatted( performance.getName(), reproductionFactor, sizeFactor, fitness));

        //TODO how to determine organism symmatry

        return fitness;

    }
}
