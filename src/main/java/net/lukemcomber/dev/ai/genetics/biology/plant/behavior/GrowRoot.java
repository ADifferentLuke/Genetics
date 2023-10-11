package net.lukemcomber.dev.ai.genetics.biology.plant.behavior;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

import java.util.function.Function;

public class GrowRoot implements PlantBehavior {

    private final Function<Coordinates,Coordinates> function;

    public GrowRoot(final Function<Coordinates,Coordinates> func){
        this.function = func;
    }
    /**
     * @param terrain
     * @param rootCell
     * @return
     */
    @Override
    public Cell performAction(Terrain terrain, Cell rootCell) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public int getEnergyCost() {
        return 2;
    }
}
