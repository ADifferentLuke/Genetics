package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.dev.ai.genetics.service.GenomeSerDe;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

public class PlantOrganism implements Organism {

    public static final String TYPE = "PLANT";
    private final Genome genome;
    private PlantCell cell;
    private List<Cell> activeCells;

    private int energy;
    private int age;

    private int debugSize = 0;

    public PlantOrganism(final Genome genome, final SeedCell cell) {
        this.genome = genome;
        this.cell = cell;
        this.energy = 5;
        activeCells = new LinkedList<>();
        activeCells.add(cell);
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

    /**
     * @param terrain
     * @return
     */
    @Override
    public Cell performAction(final Terrain terrain) {

        debugSize = dfs( cell, terrain, 0);
        /*
        //TODO for each cell, get behavior, can execute, do execute, pay price
        //TODO how does a cell tell if the behavior is acceptable
        //TODO who converts leaves to stems?

        // get position in genome
        // get next sequence
        final PlantBehavior plantBehavior = genome.getNextAct();
        if( null != plantBehavior){
            System.out.println( plantBehavior.toString() );
            //TODO we need to figure out a way to
            if( plantBehavior.getEnergyCost() <= energy ) {
                final Cell newCell = plantBehavior.performAction(terrain, cell);
                if( null != newCell){
                    activeCells.add(newCell);
                }

                energy = energy - plantBehavior.getEnergyCost();
            }
        } else {
            System.out.println( "Behavior is null");
        }

        //TODO increment age?
        */

        return null;
    }
    private int dfs( final PlantCell rootCell, final Terrain terrain, int cCount){
        final List<Cell> children = rootCell.getChildren();
        if( 0 < children.size()){
           for( final Cell cell : children ){
               cCount += dfs((PlantCell) cell,terrain, cCount);
           }
        }
        cCount++;
        // try behavior on rootCell
        final PlantBehavior plantBehavior = genome.getNextAct();
        if( null != plantBehavior){
            //TODO can cell execute behavior?
            if( rootCell.canCellSupport(plantBehavior) && plantBehavior.getEnergyCost() <= energy ){
                System.out.println( "Attempting " + plantBehavior );
                final Cell newCell = plantBehavior.performAction(terrain, cell);
                if( null != newCell){
                    cell.addChild(newCell);
                    cCount++;
                }

                energy = energy - plantBehavior.getEnergyCost();
            }
        }
        return cCount;
    }

    /**
     * @param terrain
     */
    @Override
    public void leechResources(Terrain terrain) {
        //noop - we're impotent
    }

    /**
     * @param out
     */
    @Override
    public void prettyPrint(final OutputStream out) {
        final PrintStream pout = new PrintStream(out);
        pout.println( String.format("Organism: %s", GenomeSerDe.serialize(genome)));
        pout.println( String.format("Age: %d" ,age ));
        pout.println( String.format("Energy: %d", energy ));
        pout.println( String.format( "Cells: %s", debugSize));
        pout.println();
    }

}
