package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.model.UniverseConstants;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;

public interface PlantBehavior {

    Cell performAction(final Terrain terrain, final Cell activeCell, final Organism organism);

    int getEnergyCost(final UniverseConstants properties);
}
