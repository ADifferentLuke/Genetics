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
import net.lukemcomber.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.service.GenomeSerDe;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.store.metadata.Performance;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlantOrganism implements Organism {



    private static final Logger logger = Logger.getLogger(PlantOrganism.class.getName());

    public static final String PROPERTY_STARTING_ENERGY = "initial.plant.energy";
    public static final String PROPERTY_OLD_AGE_LIMIT = "death.plant.age.limit.days";
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

    private int germinationCountDown;
    private int totalResourcesGathered;
    private int totalEnergyMetabolized;

    private final UniverseConstants properties;

    private final MetadataStoreGroup metadataStoreGroup;
    private final GenomeTransciber transciber;

    private final FitnessFunction fitnessFunction;


    public PlantOrganism(final String parentUuid, final SeedCell seed, final TemporalCoordinates temporalCoordinates,
                         final UniverseConstants properties, final GenomeTransciber transciber,
                         final FitnessFunction fitnessFunction,
                         final MetadataStoreGroup metadataStoreGroup) {

        this.genome = seed.getGenome();
        this.parentUuid = parentUuid;
        this.cell = seed;
        this.properties = properties;
        this.metadataStoreGroup = metadataStoreGroup;
        this.fitnessFunction = fitnessFunction;
        this.totalResourcesGathered = 0;
        this.totalEnergyMetabolized = 0;

        this.energy = properties.get(PROPERTY_STARTING_ENERGY, Integer.class);
        this.germinationCountDown = properties.get(PROPERTY_GERMINATION_LIMIT,Integer.class,10);
        this.activeCells = new LinkedList<>();
        this.activeCells.add(seed);
        this.uuid = UUID.randomUUID().toString();
        this.birthTime = temporalCoordinates;
        this.lastUpdateTime = temporalCoordinates;
        this.transciber = transciber;

        alive = true; //It's allliiiiiiiivvvvveeee!
    }

    @Override
    public String getParentId() {
        return parentUuid;
    }

    @Override
    public void addEnergyFromEcosystem(int energy) {
        totalResourcesGathered += energy;
        this.energy += energy;
    }

    @Override
    public void removeEnergyFromMetabolism(int energy) {
        spendEnergy(energy);
    }

    @Override
    public void spendEnergy(int energy) {
        totalEnergyMetabolized += energy;
        this.energy -= energy;
    }

    @Override
    public FitnessFunction getFitnessFunction() {
        return fitnessFunction;
    }

    @Override
    public void kill(final TemporalCoordinates temporalCoordinates, final CauseOfDeath causeOfDeath, final String reason) {
        alive = false;
        final Performance performance = new Performance();
        performance.name = this.uuid;
        performance.parentId = this.parentUuid;
        performance.dna = GenomeSerDe.serialize(getGenome());
        performance.offspring = seedCount;
        performance.birthTick = this.birthTime.totalTicks();
        performance.deathEnergy = this.energy;
        performance.deathTick = temporalCoordinates.totalTicks();
        performance.causeOfDeathStr = reason;
        performance.causeOfDeath = causeOfDeath.ordinal();
        performance.age = performance.deathTick - performance.birthTick;
        performance.totalEnergyHarvested = totalResourcesGathered;
        performance.totalEnergyMetabolized = totalEnergyMetabolized;

        performance.cells = childCount + 1; // Added 1 for current cell that's not a child

        if (null != fitnessFunction) {
            performance.fitness = fitnessFunction.apply(performance);
        } else {
            performance.fitness = 0d;
        }

        try {
            final MetadataStore<Performance> performanceStore = metadataStoreGroup.get(Performance.class);
            performanceStore.store(performance);
        } catch (EvolutionException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        logger.info(reason);
    }

    @Override
    public GenomeTransciber getTranscriber() {
        return transciber;
    }

    public Genome getGenome() {
        return genome;
    }

    /**
     * @return
     */
    @Override
    public String getOrganismType() {
        return TYPE;
    }

    /**
     * @return
     */
    @Override
    public Cell getCells() {
        return cell;
    }

    @Override
    public int getEnergy() {
        return this.energy;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public long getBirthTick() {
        return birthTime.totalTicks();
    }

    @Override
    public long getLastUpdatedTick() {
        return lastUpdateTime.totalTicks();
    }

    /**
     * @param terrain
     * @return
     */
    @Override
    public Cell performAction(final Terrain terrain, final TemporalCoordinates temporalCoordinates,
                              final BiConsumer<Organism, Cell> onCellDeath) {

        final long mark = temporalCoordinates.totalDays();
        // allow each cell to attempt to perform an action

        if (!alive) {
            performActionOnAllCells((PlantCell) getCells(), cell -> {
                terrain.deleteCell(cell.getCoordinates());

                if (cell instanceof SeedCell && cell != getCells()) {
                    final SeedCell seed = (SeedCell) cell;
                    //Remove the cell from the parent organism
                    cell.getParent().removeChild(cell);

                    SeedCell activatedSeed = new SeedCell(null, seed.getGenome(), seed.getCoordinates(), terrain.getProperties());
                    final PlantOrganism plantOrganism = new PlantOrganism(getUniqueID(), activatedSeed,
                            temporalCoordinates, properties, transciber, fitnessFunction, metadataStoreGroup);

                    logger.info(String.format("Created %s at %s from Seed", plantOrganism.getUniqueID(), seed.getCoordinates()));


                    logger.info("New Organism born: " + plantOrganism.getUniqueID() );

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
            performActionOnAllCells((PlantCell) getCells(), cell -> {
                logger.info("Actioning cell " + cell );
                final PlantBehavior plantBehavior = genome.getNextAct();
                if (null != plantBehavior) {

                    if (cell.canCellSupport(plantBehavior) && plantBehavior.getEnergyCost(terrain.getProperties()) <= energy) {
                        logger.info("Attempting " + plantBehavior);
                        try {
                            logger.info("Start newCell");
                            final Cell newCell = plantBehavior.performAction(properties, terrain, this,
                                    cell, temporalCoordinates, metadataStoreGroup);

                            logger.info("Start endCell");
                            if (null != newCell) {
                                //Update last updated time
                                lastUpdateTime = temporalCoordinates;
                                childCount++;
                                if (newCell instanceof SeedCell) {
                                    seedCount++;
                                }
                            } else {
                                logger.info("Action " + plantBehavior + " returned no cells");
                            }
                        } catch (final EvolutionException e) {
                            // logger.warning(e.getMessage());
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
                kill( temporalCoordinates,CauseOfDeath.Exhaustion,"Organism " + uuid + " died from exhaustion.");
            }
            if (0 <= stagnationLimit && stagnationLimit < mark - lastUpdateTime.totalDays()) {
                kill(temporalCoordinates,CauseOfDeath.Stagnation,"Organism " + uuid + " died from stagnation.");
            }
            if (0 <= ageLimit && ageLimit < temporalCoordinates.totalDays() - birthTime.totalDays()) {
                kill(temporalCoordinates,CauseOfDeath.OldAge,"Organism " + uuid + " died from old age.");
            }
            if( 1 == cell.getChildren().size() && 0 >= germinationCountDown-- ){
                kill(temporalCoordinates,CauseOfDeath.Stagnation,"Organism " + uuid + " failed to germinate.");
            }
        }

        return null;
    }

    @Override
    public void cleanup(final Terrain terrain) {

    }

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

}
