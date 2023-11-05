package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.service.GenomeSerDe;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class PlantOrganism implements Organism {

    private static final Logger logger = Logger.getLogger(PlantOrganism.class.getName());

    public static final String TYPE = "PLANT";
    private final Genome genome;
    private PlantCell cell;
    private List<Cell> activeCells;

    private int energy;
    private int age;

    private int debugSize = 0;

    private final String uuid;
    private final String parentUuid;

    private final long birthTick;

    public PlantOrganism(final String parentUuid, final SeedCell seed, final long currentTick) {
        this.genome = seed.getGenome();
        this.parentUuid = parentUuid;
        this.cell = seed;

        this.energy = 5;
        this.activeCells = new LinkedList<>();
        this.activeCells.add(seed);
        this.uuid = UUID.randomUUID().toString();
        this.birthTick = currentTick;

        seed.activate(); //Allow the seed to grow
    }

    @Override
    public String getParentId(){
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
    public void modifyEnergy(int delta) {
        this.energy += delta;
    }

    /**
     * @param terrain
     * @return
     */
    @Override
    public Cell performAction(final Terrain terrain, final long currentTick) {

        debugSize = dfs(cell, terrain, 0);

        return null;
    }

    @Override
    public String getUniqueID() {
        return uuid;
    }

    private int dfs(final PlantCell rootCell, final Terrain terrain, final int cCount) {
        final List<Cell> children = rootCell.getChildren();
        int childCount = 0;
        if (0 < children.size()) {
            final Cell[] childList = children.toArray(new Cell[0]);
            for (final Cell cell : childList) {
                int c = dfs((PlantCell) cell, terrain, cCount);
                childCount += c;
            }
        }
        childCount++;
        // try behavior on rootCell
        final PlantBehavior plantBehavior = genome.getNextAct();
        if (null != plantBehavior) {
            //TODO can cell execute behavior?
            if (rootCell.canCellSupport(plantBehavior) && plantBehavior.getEnergyCost() <= energy) {
                logger.info("Attempting " + plantBehavior);
                try {
                    final Cell newCell = plantBehavior.performAction(terrain, rootCell,this);
                    if (null != newCell) {
                        childCount++;
                    }
                    energy = energy - plantBehavior.getEnergyCost();
                } catch (final EvolutionException e) {
                    logger.warning(e.getMessage());
                }
            } else if (!rootCell.canCellSupport(plantBehavior)) {
                logger.info("Cell " + rootCell + " Behavior not allowed: " + plantBehavior);
            }
        }
        return childCount;
    }


    /**
     * @param out
     */
    @Override
    public void prettyPrint(final OutputStream out) {
        final PrintStream pout = new PrintStream(out);
        pout.println(String.format("Organism: %s", GenomeSerDe.serialize(genome)));
        pout.println(String.format("Age: %d", age));
        pout.println(String.format("Energy: %d", energy));
        pout.println(String.format("Cells: %s", debugSize));
        pout.println();
    }

}
