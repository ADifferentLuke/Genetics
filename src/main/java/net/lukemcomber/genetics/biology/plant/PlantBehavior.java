package net.lukemcomber.genetics.biology.plant;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.world.terrain.Terrain;

public interface PlantBehavior {

    Cell performAction(final Terrain terrain, final Cell activeCell, final Organism organism);

    int getEnergyCost(final UniverseConstants properties);
}
