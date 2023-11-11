package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.model.TemporalCoordinates;
import net.lukemcomber.dev.ai.genetics.service.GenomeSerDe;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class PlantOrganism implements Organism {

    private static final Logger logger = Logger.getLogger(PlantOrganism.class.getName());

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

    public PlantOrganism(final String parentUuid, final SeedCell seed, final TemporalCoordinates temporalCoordinates) {
        this.genome = seed.getGenome();
        this.parentUuid = parentUuid;
        this.cell = seed;

        this.energy = 5;
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

    /**
     * @param terrain
     * @return
     */
    @Override
    public Cell performAction(final Terrain terrain, final TemporalCoordinates temporalCoordinates) {

        final long mark = lastUpdateTime.totalDays();
        // allow each cell to attempt to perform an action

        if (!alive) {
            performActionOnAllCells((PlantCell) getCells(), cell -> {
                terrain.deleteCell(cell.getCoordinates());

                if (cell instanceof SeedCell) {
                    final SeedCell seed = (SeedCell) cell;
                    if (!seed.isActivated()) {

                        SeedCell activatedSeed = new SeedCell(null, seed.getGenome(), seed.getCoordinates());
                        final PlantOrganism plantOrganism = new PlantOrganism( getUniqueID(), activatedSeed, temporalCoordinates );

                        logger.info( "New Organism born: " + plantOrganism.getUniqueID());

                        terrain.addOrganism(plantOrganism);
                    }
                } else {
                    //call a func interface for any reclaimation
                }
            });
            //Spawn dem eggs
            //get all seeds
            //remove seeds from this organism
            // create new organism and add to terrain
        } else {
            performActionOnAllCells((PlantCell) getCells(), cell -> {
                final PlantBehavior plantBehavior = genome.getNextAct();
                if (null != plantBehavior) {

                    if (cell.canCellSupport(plantBehavior) && plantBehavior.getEnergyCost() <= energy) {
                        logger.info("Attempting " + plantBehavior);
                        try {
                            final Cell newCell = plantBehavior.performAction(terrain, cell, this);
                            if (null != newCell) {
                                //Update last updated time
                                lastUpdateTime = temporalCoordinates;
                                childCount++;
                            }
                            energy = energy - plantBehavior.getEnergyCost();
                        } catch (final EvolutionException e) {
                            logger.warning(e.getMessage());
                        }
                    } else if (!cell.canCellSupport(plantBehavior)) {
                        logger.info("Cell " + cell + " Behavior not allowed: " + plantBehavior);
                    }
                }
            });

            if (0 > energy) {
                //too exhausted to live
                logger.info("Organism " + uuid + " + died from exhaustion.");
                alive = false;
            }
            if (10 < lastUpdateTime.totalDays() - mark) {
                logger.info("Organism " + uuid + " + died from stagnation.");
                //stagnant
                alive = false;
            }
            if (100 < birthTime.totalDays()) {
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
        pout.println(String.format("Energy: %d", energy));
        pout.println(String.format("Cells: %s", childCount));
        pout.println();
    }

}
