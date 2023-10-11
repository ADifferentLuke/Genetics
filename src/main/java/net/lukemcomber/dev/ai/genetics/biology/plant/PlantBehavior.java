package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

public interface PlantBehavior {

    Cell performAction(final Terrain terrain, final Cell rootCell );

    int getEnergyCost();
}
