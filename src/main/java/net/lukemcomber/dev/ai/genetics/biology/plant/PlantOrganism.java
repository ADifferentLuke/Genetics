package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.model.TemporalCoordinates;
import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;
import net.lukemcomber.dev.ai.genetics.service.GenomeSerDe;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class PlantOrganism implements Organism {

    private static final Logger logger = Logger.getLogger(PlantOrganism.class.getName());

    public static final String PROPERTY_STARTING_ENERGY = "initial.plant.energy";
    public static final String PROPERTY_OLD_AGE_LIMIT = "death.plant.age-limit-days";
    public static final String PROPERTY_STAGNATION_LIMIT  = "death.plant.stagnation-limit-days";
    public static final String PROPERTY_STARVATION_LIMIT = "death.plant.starvation-limit-energy";

    public static final String TYPE = "PLANT";
    private final Genome genome;
    private PlantCell cell;
    private List<Cell> activeCells;

    private int energy;

    private int childCount = 0;

    private final String uuid;
    private final String parentUuid;

    private final TemporalCoordinates birthTime;
    private TemporalCoordinates lastUpdateTime;
    private boolean alive;

    private final UniverseConstants properties;

    public PlantOrganism(final String parentUuid, final SeedCell seed, final TemporalCoordinates temporalCoordinates,
                         final UniverseConstants properties ) {
        this.genome = seed.getGenome();
        this.parentUuid = parentUuid;
        this.cell = seed;
        this.properties = properties;

        this.energy = properties.get(PROPERTY_STARTING_ENERGY, Integer.class);
        this.activeCells = new LinkedList<>();
        this.activeCells.add(seed);
        this.uuid = UUID.randomUUID().toString();
        this.birthTime = temporalCoordinates;
        this.lastUpdateTime = temporalCoordinates;

        seed.activate(); //Allow the seed to grow
        alive = true; //It's allliiiiiiiivvvvveeee!
    }

    @Override
    public String getParentId() {
        return parentUuid;
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
    public void modifyEnergy(int delta) {
        this.energy += delta;
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
                              final BiConsumer<Organism,Cell> onCellDeath) {

        final long mark = temporalCoordinates.totalDays();
        // allow each cell to attempt to perform an action

        if (!alive) {
            performActionOnAllCells((PlantCell) getCells(), cell -> {
                terrain.deleteCell(cell.getCoordinates());

                if (cell instanceof SeedCell) {
                    final SeedCell seed = (SeedCell) cell;
                    if (!seed.isActivated()) {
                        //Remove the cell from the parent organism
                        cell.getParent().removeChild(cell);

                        SeedCell activatedSeed = new SeedCell(null, seed.getGenome(), seed.getCoordinates(), terrain.getProperties());
                        final PlantOrganism plantOrganism = new PlantOrganism( getUniqueID(), activatedSeed, temporalCoordinates, properties );

                        logger.info( "New Organism born: " + plantOrganism.getUniqueID());

                        terrain.addOrganism(plantOrganism);
                    }
                } else {
                    // Trigger any cell death callback
                    if( null != onCellDeath){
                        onCellDeath.accept(this, cell);
                    }
                }
            });
            terrain.deleteOrganism(this);
        } else {
            performActionOnAllCells((PlantCell) getCells(), cell -> {
                logger.info("Actioning cell " + cell);
                final PlantBehavior plantBehavior = genome.getNextAct();
                if (null != plantBehavior) {

                    if (cell.canCellSupport(plantBehavior) && plantBehavior.getEnergyCost(terrain.getProperties()) <= energy) {
                        logger.info("Attempting " + plantBehavior);
                        try {
                            final Cell newCell = plantBehavior.performAction(terrain, cell, this);
                            if (null != newCell) {
                                //Update last updated time
                                lastUpdateTime = temporalCoordinates;
                                childCount++;
                            } else {
                                logger.info("Action " + plantBehavior + " returned no cells");
                            }
                            energy = energy - plantBehavior.getEnergyCost(terrain.getProperties());
                        } catch (final EvolutionException e) {
                            logger.warning(e.getMessage());
                        }
                    } else if (!cell.canCellSupport(plantBehavior)) {
                        logger.info("Cell " + cell + " Behavior not allowed: " + plantBehavior);
                    } else {
                        logger.info( "Not enough energy for " + plantBehavior);
                    }
                }
            });

            //These are optional
            final Integer ageLimit = properties.get(PROPERTY_OLD_AGE_LIMIT,Integer.class,-1);
            final Integer stagnationLimit = properties.get(PROPERTY_STAGNATION_LIMIT,Integer.class,-1);
            final Integer starvationLimit = properties.get(PROPERTY_STARVATION_LIMIT,Integer.class,-1);

            if (0 <= starvationLimit && starvationLimit >= energy) {
                //too exhausted to live
                logger.info("Organism " + uuid + " + died from exhaustion.");
                alive = false;
            }
            if (0 <= stagnationLimit && stagnationLimit < mark - lastUpdateTime.totalDays() ) {
                logger.info("Organism " + uuid + " + died from stagnation.");
                //stagnant
                alive = false;
            }
            if (0 <= ageLimit && ageLimit < temporalCoordinates.totalDays() - birthTime.totalDays() ) {
                logger.info("Organism " + uuid + " + died from old age.");
                alive = false;
            }

        }
        //energy efficiency coefficeint

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
