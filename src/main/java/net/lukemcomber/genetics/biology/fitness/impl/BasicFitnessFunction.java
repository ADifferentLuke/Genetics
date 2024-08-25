package net.lukemcomber.genetics.biology.fitness.impl;

import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.fitness.FitnessFunction;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.metadata.Performance;

/**
 * A fitness function based on Organism Age, Number of Cells, Energy Efficiency,
 * Energy Waste, Offspring, and Death to generate a deterministic fitness
 */
public class BasicFitnessFunction implements FitnessFunction {

    public static final String FITNESS_AGE_WEIGHT = "fitness.age.weight";
    public static final String FITNESS_CELLS_WEIGHT = "fitness.numerOfCells.weight";
    public static final String FITNESS_UNUSED_ENERGY_WEIGHT = "fitness.leftOverEnergy.weight";
    public static final String FITNESS_ENERGY_EFFICIENCY_WEIGHT = "fitness.energyEfficiency.weight";
    public static final String FITNESS_CHILDREN_WEIGHT = "fitness.children.weight";

    public static final double DEFAULT_AGE_WEIGHT = 1;
    public static final double DEFAULT_CELLS_WEIGHT = 0.5;
    public static final double DEFAULT_UNUSED_ENERGY_WEIGHT = 0.2;
    public static final double DEFAULT_ENERGY_EFFICIENCY_WEIGHT = 1.5;
    public static final double DEFAULT_CHILDREN_WEIGHT = 2;

    private final double ageWeight;
    private final double cellsWeight;
    private final double unusedEnergyWeight;
    private final double energyEfficiencyWeight;
    private final double childrenWeight;

    /**
     * Creates a new instance with default weights
     */
    public BasicFitnessFunction() {
        this(DEFAULT_AGE_WEIGHT, DEFAULT_CELLS_WEIGHT, DEFAULT_UNUSED_ENERGY_WEIGHT,
                DEFAULT_ENERGY_EFFICIENCY_WEIGHT, DEFAULT_CHILDREN_WEIGHT);
    }

    /**
     * Creates a new instance with weights from configuration
     *
     * @param constants configuration properties
     */
    public BasicFitnessFunction(final UniverseConstants constants) {

        this(
                constants.get(FITNESS_AGE_WEIGHT, Double.class, DEFAULT_AGE_WEIGHT),
                constants.get(FITNESS_CELLS_WEIGHT, Double.class, DEFAULT_CELLS_WEIGHT),
                constants.get(FITNESS_UNUSED_ENERGY_WEIGHT, Double.class, DEFAULT_UNUSED_ENERGY_WEIGHT),
                constants.get(FITNESS_ENERGY_EFFICIENCY_WEIGHT, Double.class, DEFAULT_ENERGY_EFFICIENCY_WEIGHT),
                constants.get(FITNESS_CHILDREN_WEIGHT, Double.class, DEFAULT_CHILDREN_WEIGHT)
        );
    }

    /**
     * Create a new instance by specifying each weight individually
     *
     * @param ageWeight
     * @param numOfCellsWeight
     * @param remainingEnergyWeight
     * @param energyEfficiencyWeight
     * @param childrenWeight
     */
    public BasicFitnessFunction(final double ageWeight, final double numOfCellsWeight, final double remainingEnergyWeight,
                                final double energyEfficiencyWeight, final double childrenWeight) {

        this.ageWeight = ageWeight;
        this.cellsWeight = numOfCellsWeight;
        this.unusedEnergyWeight = remainingEnergyWeight;
        this.energyEfficiencyWeight = energyEfficiencyWeight;
        this.childrenWeight = childrenWeight;
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
        final double cellsValue = cellsWeight * Math.log(Math.sqrt(performance.getCells()));

        // ratio of 1:1 energy usage is the most efficient
        final double energyDifferential = performance.getTotalEnergyHarvested() - performance.getTotalEnergyMetabolized();
        // Let's not blow up the world, check for divide by zero
        final double energyValue = energyEfficiencyWeight * (1 / (0 != energyDifferential ? energyDifferential : 1));

        final double childrenValue = childrenWeight * performance.getOffspring();
        final double deathValue = (double) performance.getCauseOfDeath() / Organism.CauseOfDeath.count;

        return deathValue * (cellsValue +  energyValue) * Math.log(childrenValue + 1);
    }
}
