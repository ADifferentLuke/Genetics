package net.lukemcomber.genetics.biology.plant;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Genome;
import net.lukemcomber.genetics.biology.GenomeTransciber;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.fitness.FitnessFunction;
import net.lukemcomber.genetics.biology.plant.behavior.EjectSeed;
import net.lukemcomber.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.io.GenomeSerDe;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.store.metadata.Performance;
import net.lukemcomber.genetics.world.terrain.Terrain;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An organism that uses a {@link PlantGenome} and {@link PlantBehavior}
 */
public class PlantOrganism implements Organism {
    private static final Logger logger = Logger.getLogger(PlantOrganism.class.getName());

    public static final String PROPERTY_STARTING_ENERGY = "initial.plant.energy";
    public static final String PROPERTY_STAGNATION_LIMIT = "death.plant.stagnation.limit.days";
    public static final String PROPERTY_STARVATION_LIMIT = "death.plant.starvation.limit.energy";
    public static final String PROPERTY_GERMINATION_LIMIT = "death.plant.germination.limit.ticks";

    public static final String TYPE = "PLANT";
    private final Genome genome;
    private SeedCell cell;
    private List<Cell> activeCells;

    private int energy;
    private int childCount = 0;
    private int seedCount = 0;

    private final String uuid;
    private final String parentUuid;

    private final TemporalCoordinates birthTime;
    private TemporalCoordinates lastUpdateTime;

    private boolean alive;
    private CauseOfDeath causeOfDeath;
    private String deathDetails;

    private int germinationCountDown;
    private int totalResourcesGathered;
    private int totalEnergyMetabolized;
    private int totalMetabolismCost;

    private final UniverseConstants properties;

    private final MetadataStoreGroup metadataStoreGroup;
    private final GenomeTransciber transciber;

    private final FitnessFunction fitnessFunction;

    /**
     * Create a new instance
     *
     * @param parentUuid          the parents id
     * @param seed                source seed
     * @param temporalCoordinates time
     * @param properties          configuration properties
     * @param transcriber          genome transcriber
     * @param fitnessFunction     fitness function
     * @param metadataStoreGroup  metadata store cache
     */
    public PlantOrganism(final String parentUuid, final SeedCell seed, final TemporalCoordinates temporalCoordinates,
                         final UniverseConstants properties, final GenomeTransciber transcriber,
                         final FitnessFunction fitnessFunction,
                         final MetadataStoreGroup metadataStoreGroup) {

        this.genome = seed.getGenome();
        this.parentUuid = parentUuid;
        this.properties = properties;
        this.metadataStoreGroup = metadataStoreGroup;
        this.fitnessFunction = fitnessFunction;

        this.totalResourcesGathered = 0;
        this.totalEnergyMetabolized = 0;
        this.totalMetabolismCost = seed.getMetabolismCost();

        this.cell = seed;
        this.energy = properties.get(PROPERTY_STARTING_ENERGY, Integer.class);
        this.germinationCountDown = properties.get(PROPERTY_GERMINATION_LIMIT, Integer.class, 10);
        this.activeCells = new LinkedList<>();
        this.activeCells.add(seed);

        final String dnaString = GenomeSerDe.serialize(genome);

        this.uuid = DigestUtils.sha1Hex("%d-%d|%d-%s".formatted(temporalCoordinates.totalTicks(),seed.getCoordinates().xAxis(),
                seed.getCoordinates().yAxis(),dnaString ));

        this.birthTime = temporalCoordinates;
        this.lastUpdateTime = temporalCoordinates;
        this.transciber = transcriber;

        alive = true; //It's allliiiiiiiivvvvveeee!
    }

    /**
     * Get the organisms unique id
     *
     * @return unique id
     */
    @Override
    public String getParentId() {
        return parentUuid;
    }

    /**
     * Add energy to the organism from the environment
     *
     * @param energy
     */
    @Override
    public void addEnergyFromEcosystem(final int energy) {
        totalResourcesGathered += energy;
        this.energy += energy;
    }

    /**
     * Remove energy from the organism for metabolism
     *
     * @param energy
     */
    @Override
    public void removeEnergyFromMetabolism(final int energy) {
        spendEnergy(energy);
    }

    /**
     * Spend energy to perform some action
     *
     * @param energy
     */
    @Override
    public void spendEnergy(int energy) {
        totalEnergyMetabolized += energy;
        this.energy -= energy;
    }

    /**
     * Get the current fitness function
     *
     * @return fitness function
     */
    @Override
    public FitnessFunction getFitnessFunction() {
        return fitnessFunction;
    }

    /**
     * Kill the organism
     *
     * @param temporalCoordinates time
     * @param causeOfDeath        cause of death
     * @param reason              human-readable message
     */
    @Override
    public void kill(final TemporalCoordinates temporalCoordinates, final CauseOfDeath causeOfDeath, final String reason) {

        alive = false;
        this.causeOfDeath = causeOfDeath;
        this.deathDetails = reason;


        final Performance performance = fitnessFunction.apply(this);
        try {
            final MetadataStore<Performance> performanceStore = metadataStoreGroup.get(Performance.class);
            performanceStore.store(performance);
        } catch (EvolutionException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        logger.info(reason);
    }

    /**
     * Get the current genome transcriber
     *
     * @return transcriber
     */
    @Override
    public GenomeTransciber getTranscriber() {
        return transciber;
    }

    /**
     * Get the organisms genome
     *
     * @return genome
     */
    public Genome getGenome() {
        return genome;
    }

    /**
     * Get the organism's type
     *
     * @return type
     */
    @Override
    public String getOrganismType() {
        return TYPE;
    }

    /**
     * Get the first cell of the organism's life
     *
     * @return cell
     */
    @Override
    public Cell getFirstCell() {
        return cell;
    }

    /**
     * Get the organisms current energy
     *
     * @return energy
     */
    @Override
    public int getEnergy() {
        return this.energy;
    }

    /**
     * Check if organism is alive
     *
     * @return true if alive
     */
    @Override
    public boolean isAlive() {
        return alive;
    }

    /**
     * Get the organisms birth tick
     *
     * @return tick
     */
    @Override
    public long getBirthTick() {
        return birthTime.totalTicks();
    }

    @Override
    public int getOffspringCount() {
        return seedCount;
    }

    @Override
    public int getTotalEnergyMetabolized() {
        return totalEnergyMetabolized;
    }

    @Override
    public int getTotalEnergyHarvested() {
        return totalResourcesGathered;
    }

    @Override
    public int getCellCount() {
        return childCount;
    }

    @Override
    public CauseOfDeath getCauseOfDeath() {
        return causeOfDeath;
    }

    @Override
    public String getDeathDetails() {
        return deathDetails;
    }

    /**
     * Get the last updated tick of the organism
     *
     * @return tick
     */
    @Override
    public long getLastUpdatedTick() {
        return lastUpdateTime.totalTicks();
    }

    /**
     * Allow the organism to perform it's next action
     *
     * @param terrain             the terrain
     * @param temporalCoordinates time
     * @param onCellDeath         callback if a cell dies
     */
    @Override
    public void performAction(final Terrain terrain, final TemporalCoordinates temporalCoordinates,
                              final BiConsumer<Organism, Cell> onCellDeath) {

        final long mark = temporalCoordinates.totalDays();
        // allow each cell to attempt to perform an action

        if (!alive) {
            performActionOnAllCells((PlantCell) getFirstCell(), cell -> {

                if (cell instanceof SeedCell && cell != getFirstCell()) {
                    final SeedCell seed = (SeedCell) cell;
                    //Remove the cell from the parent organism
                    cell.getParent().removeChild(cell);

                    /*
                     * To prevent collisions, delete the cell before re-adding the organism. This is so that
                     *   the seed cell does not collide with itself.
                     */
                    terrain.deleteCell(seed.getCoordinates(), getUniqueID());

                    SeedCell activatedSeed = new SeedCell(null, seed.getGenome(), seed.getCoordinates(), terrain.getProperties());
                    final PlantOrganism plantOrganism = new PlantOrganism(getUniqueID(), activatedSeed,
                            temporalCoordinates, properties, transciber, fitnessFunction, metadataStoreGroup);

                    logger.info(String.format("Created %s at %s from Seed", plantOrganism.getUniqueID(), seed.getCoordinates()));


                    logger.info("New Organism born: " + plantOrganism.getUniqueID());

                    terrain.addOrganism(plantOrganism);
                } else {
                    // Trigger any cell death callback
                    if (null != onCellDeath) {
                        onCellDeath.accept(this, cell);
                    }
                }
            });
            terrain.deleteOrganism(this);
        } else if (!cell.isActivated()) {
            //Do whatever the seed needs to do to activate
            if (cell instanceof PlantBehavior) {
                ((PlantBehavior) cell).performAction(properties, terrain, this, cell, temporalCoordinates, metadataStoreGroup);
            }
        } else {
            performActionOnAllCells((PlantCell) getFirstCell(), cell -> {

                logger.info("Burning calories for cell " + cell);
                removeEnergyFromMetabolism(cell.getMetabolismCost());
                logger.info("Leeching resources..");
                addEnergyFromEcosystem(cell.generateEnergy(terrain));
                logger.info("Actioning cell " + cell);

                final PlantBehavior plantBehavior = genome.getNextAct();
                if (null != plantBehavior) {


                    if (cell.canCellSupport(plantBehavior) && plantBehavior.getEnergyCost(terrain.getProperties()) <= energy) {
                        try {
                            final Cell newCell = plantBehavior.performAction(properties, terrain, this,
                                    cell, temporalCoordinates, metadataStoreGroup);


                            if (null != newCell) {
                                //Update last updated time
                                lastUpdateTime = temporalCoordinates;
                                childCount++;
                                totalMetabolismCost += newCell.getMetabolismCost();
                                if (newCell instanceof SeedCell) {
                                    seedCount++;
                                }
                            } else {
                                logger.info("Action " + plantBehavior + " returned no cells");
                            }
                        } catch (final EvolutionException e) {
                            // Collisions
                             //logger.warning(e.getMessage());
                        }
                    } else if (!cell.canCellSupport(plantBehavior)) {
                        logger.info("Cell " + cell + " Behavior not allowed: " + plantBehavior);
                    } else {
                        logger.info("Not enough energy for " + plantBehavior);
                    }
                }
            });

            //These are optional
            final Integer ageLimit = properties.get(PROPERTY_OLD_AGE_LIMIT, Integer.class, -1);
            final Integer stagnationLimit = properties.get(PROPERTY_STAGNATION_LIMIT, Integer.class, -1);
            final Integer starvationLimit = properties.get(PROPERTY_STARVATION_LIMIT, Integer.class, -1);

            String deathLogStr = "";
            if (0 <= starvationLimit && starvationLimit >= energy) {
                kill(temporalCoordinates, CauseOfDeath.Exhaustion, "Organism " + uuid + " died from exhaustion.");
            }
            if (0 <= stagnationLimit && stagnationLimit < mark - lastUpdateTime.totalDays()) {
                kill(temporalCoordinates, CauseOfDeath.Stagnation, "Organism " + uuid + " died from stagnation.");
            }
            if (0 <= ageLimit && ageLimit < temporalCoordinates.totalDays() - birthTime.totalDays()) {
                kill(temporalCoordinates, CauseOfDeath.OldAge, "Organism " + uuid + " died from old age.");
            }
            if (1 == cell.getChildren().size() && 0 >= germinationCountDown--) {
                kill(temporalCoordinates, CauseOfDeath.Stagnation, "Organism " + uuid + " failed to germinate.");
            }
        }

    }

    /**
     * Clean up all cells from the terrain
     *
     * @param terrain
     */
    @Override
    public void cleanup(final Terrain terrain) {

    }

    /**
     * Get the organisms unique id
     *
     * @return unique id
     */
    @Override
    public String getUniqueID() {
        return uuid;
    }

    private void performActionOnAllCells(final PlantCell cell, final Consumer<PlantCell> func) {

        if (null != cell && func != null) {

            final List<Cell> children = cell.getChildren();
            if (0 < children.size()) {
                final Cell[] childList = children.toArray(new Cell[0]);
                for (final Cell childCell : childList) {
                    performActionOnAllCells((PlantCell) childCell, func);
                }
            }
            func.accept(cell);
        }
    }

    /**
     * Utility method for nicely formatting organism information into the provided {@link OutputStream}
     *
     * @param out
     */
    @Override
    public void prettyPrint(final OutputStream out) {
        final PrintStream pout = new PrintStream(out);
        pout.println(String.format("Organism: %s", GenomeSerDe.serialize(genome)));
        pout.println(String.format("Birth Tick: %d", birthTime.totalTicks()));
        pout.println(String.format("Last Updated: %d", lastUpdateTime.totalTicks()));
        pout.println(String.format("Energy: %d", energy));
        pout.println(String.format("Cells: %s", childCount));
        pout.println();
    }

    @Override
    public int getMetabolismCost() {
        return totalMetabolismCost;
    }

}
