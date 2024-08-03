package net.lukemcomber.genetics;

import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.plant.PlantOrganism;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.service.LoggerOutputStream;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.store.metadata.Environment;
import net.lukemcomber.genetics.world.ResourceManager;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SteppableEcosystem extends Ecosystem{

    private static final Logger logger = Logger.getLogger(SteppableEcosystem.class.getName());

    private static final LoggerOutputStream loggerOutputStream = new LoggerOutputStream(logger, Level.INFO);

    private final long ticksPerTurn;

    public SteppableEcosystem(int ticksPerTurn, int ticksPerDay, SpatialCoordinates size, String type) throws IOException {
        super(ticksPerDay, size, type);
        this.ticksPerTurn = ticksPerTurn;
    }

    public long getTicksPerTurn(){
        return ticksPerTurn;
    }

    public boolean advance() throws EvolutionException {
        if (isActive()) {
            for (int i = 0; i < getTicksPerTurn(); ++i) {
                tickEnvironment();
                tickOrganisms();
            }

            final Environment environmentData = new Environment();
            environmentData.tickCount = getTotalTicks();
            environmentData.totalOrganisms = (long) getTerrain().getOrganismCount();

           final MetadataStore<Environment> dataStore = metadataStoreGroup.get(Environment.class);
           dataStore.store(environmentData);

        }
        return isActive();
    }
}
