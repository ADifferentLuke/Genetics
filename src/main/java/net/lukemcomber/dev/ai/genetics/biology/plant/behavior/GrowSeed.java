package net.lukemcomber.dev.ai.genetics.biology.plant.behavior;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.LeafCell;
import net.lukemcomber.dev.ai.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

import java.util.function.Function;

public class GrowSeed implements PlantBehavior {

    private final Function<Coordinates,Coordinates> function;

    public GrowSeed(final Function<Coordinates,Coordinates> func){
        this.function = func;
    }
    /**
     * @param terrain
     * @param cell
     * @return
     */
    @Override
    public Cell performAction(final Terrain terrain,final Cell cell) {
        Cell retVal = null;
        /*
        final Coordinates newCoordinates = function.apply(cell.getCoordinates());


        //The boolean logic looks weird, but we need to use AND for short circuit
        if((!terrain.isOutOfBounds(newCoordinates)) && (!terrain.hasCell(newCoordinates))){
            //TODO MUTATION NEEDED HERE
            final Organism organism = cell.getOrganism();
            final SeedCell newCell = new SeedCell(organism.getEcoSystem(),newCoordinates,organism.getGenome());
            terrain.setCell(newCell);
            System.out.println( "Created new Organism: " + organism.getUniqueID());

            retVal = null;
        } else {
            throw new EvolutionException("Seed growth failed. Collision detected.");
        }
*/
        return retVal;
        //create new seed cell
        //create new organism
        //change stem to dead end?
    }


    /**
     * @return
     */
    @Override
    public int getEnergyCost() {
        return 10;
    }
}
