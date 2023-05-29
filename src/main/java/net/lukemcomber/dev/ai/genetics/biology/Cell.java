package net.lukemcomber.dev.ai.genetics.biology;

import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;

/**
 * Interface for representing cells and providing a uniform interface for manipulating them
 */
public interface Cell {

    /**
     * Returns a reference to the parent organism
     * @return reference to parent organism
     */
    Organism getOrganism();

    Coordinates getCoordinates();
}
