package net.lukemcomber.genetics.biology.plant.behavior;

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.plant.PlantBehavior;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.util.function.Function;

public class NoOp implements PlantBehavior {
    private final Function<SpatialCoordinates, SpatialCoordinates> function;

    /**
     * Create a new instance with a callback to updated cell location
     *
     * @param func
     */
    public NoOp(final Function<SpatialCoordinates, SpatialCoordinates> func) {
        this.function = func;
    }

    @Override
    public Cell performAction(UniverseConstants properties, Terrain terrain, Organism organism, Cell activeCell, TemporalCoordinates temporalCoordinates, MetadataStoreGroup metadataStoreGroup) {
        return null;
    }

    @Override
    public int getEnergyCost(UniverseConstants properties) {
        return 0;
    }
}
