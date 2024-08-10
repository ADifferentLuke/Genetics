package net.lukemcomber.genetics;

import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.plant.PlantOrganism;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.ecosystem.EcosystemConfiguration;
import net.lukemcomber.genetics.model.ecosystem.impl.SteppableEcosystemConfiguration;
import net.lukemcomber.genetics.service.LoggerOutputStream;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.store.metadata.Environment;
import net.lukemcomber.genetics.world.ResourceManager;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SteppableEcosystem extends Ecosystem {

    private static final Logger logger = Logger.getLogger(SteppableEcosystem.class.getName());

    private static final LoggerOutputStream loggerOutputStream = new LoggerOutputStream(logger, Level.INFO);

    private final long ticksPerTurn;

    public SteppableEcosystem(int ticksPerTurn, int ticksPerDay, SpatialCoordinates size, String type) throws IOException {
        super(ticksPerDay, size, type);
        this.ticksPerTurn = ticksPerTurn;
    }

    public SteppableEcosystem(int ticksPerTurn, int ticksPerDay, SpatialCoordinates size, String type, String name) throws IOException {

        super(ticksPerDay, size, type, name);
        this.ticksPerTurn = ticksPerTurn;
    }

    public long getTicksPerTurn() {
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

    @Override
    public EcosystemConfiguration getSetupConfiguration() {
        final SteppableEcosystemConfiguration setupConfiguration = new SteppableEcosystemConfiguration();

        final Terrain terrain = getTerrain();
        if(Objects.nonNull(terrain)){
            setupConfiguration.width = getTerrain().getSizeOfXAxis();
            setupConfiguration.height = getTerrain().getSizeOfYAxis();
            setupConfiguration.depth = getTerrain().getSizeOfZAxis();
        }
        setupConfiguration.turnsPerTick = ticksPerTurn;
        setupConfiguration.interactive = true;
        setupConfiguration.active = isActive();
        setupConfiguration.name = getName();
        setupConfiguration.id = getId();
        setupConfiguration.totalDays = getTotalDays();
        setupConfiguration.currentTick = getCurrentTick();
        setupConfiguration.totalTicks = getTotalTicks();
        setupConfiguration.currentOrganismCount = getTerrain().getOrganismCount();
        setupConfiguration.totalOrganismCount = getTerrain().getTotalOrganismCount();
        setupConfiguration.properties = getProperties().toMap();
        setupConfiguration.initialPopulation = getInitialPopulation();

        return setupConfiguration;
    }
}
