package net.lukemcomber.genetics.biology.fitness.impl;

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.fitness.FitnessFunction;
import net.lukemcomber.genetics.biology.plant.cells.LeafCell;
import net.lukemcomber.genetics.biology.plant.cells.RootCell;
import net.lukemcomber.genetics.biology.plant.cells.StemCell;
import net.lukemcomber.genetics.io.CellHelper;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.metadata.Performance;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * A fitness function based on Organism Age, Number of Cells, Energy Efficiency,
 * Energy Waste, Offspring, and Death to generate a deterministic fitness
 */
public class BasicV2FitnessFunction implements FitnessFunction {

    private static final Logger logger = Logger.getLogger(BasicV2FitnessFunction.class.getName());
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

    private final double BETA = 3;
    private final int k = 50;
    private final double SYMMETRY_WEIGHT = 1;
    private final double SIZE_WEIGHT = 1;
    private final double AGE_WEIGHT = 1;
    private final double OFFSPRING_WEIGHT = 1;


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
    public double calculate(final Performance performance, final Organism organism) {

        // larger is better, but not a huge advantage.
        double fitness = 0;

        final List<Cell> flattenBody = CellHelper.getAllOrganismsCells(organism.getFirstCell());

        long size = performance.getCells();
        double sizeScore = (double) size / (size + k);

        int underGroundCell = 1;
        int aboveGroundCell = 1;
        double symmetryScore = 0;

        for (final Cell cell : flattenBody) {
            if (Objects.nonNull(cell)) {
                switch (cell.getCellType()) {
                    case StemCell.TYPE, LeafCell.TYPE -> aboveGroundCell++;
                    case RootCell.TYPE -> underGroundCell++;
                }
            }
        }

        if (1 < flattenBody.size()) {
            if (underGroundCell < aboveGroundCell) {
                symmetryScore = (double) underGroundCell / aboveGroundCell;
            } else {
                symmetryScore = (double) aboveGroundCell / underGroundCell;
            }
        }

        // relative age calculation
        long birth = performance.getBirthTick();
        long now = Math.min(performance.getDeathTick(), maximumAge); // or sim clock
        long denom = Math.max(1, (maximumAge - birth));       // avoid /0 if born at end
        double ageScore = Math.max(0.0, Math.min(1.0, (now - birth) / (double) denom));

        int offspringCount = performance.getOffspring();
        double offspringScore;
        if (offspringCount < 1) {
            offspringScore = Math.exp(-BETA * (1 - offspringCount));
        } else if (offspringCount <= 2) {
            offspringScore = 1.0;
        } else {
            offspringScore = Math.exp(-BETA * (offspringCount - 2));
        }

        logger.info("%s - Symmetry Factor: %f Age Factor: %f Fitness: %f".formatted(performance.getName(), symmetryScore, ageScore, fitness));

        return Math.pow(symmetryScore, SYMMETRY_WEIGHT) * Math.pow(ageScore, AGE_WEIGHT) * Math.pow(offspringScore, OFFSPRING_WEIGHT) * Math.pow(sizeScore, SIZE_WEIGHT);
    }
}
