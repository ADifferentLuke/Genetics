package net.lukemcomber.genetics;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.model.ecosystem.EcosystemDetails;
import net.lukemcomber.genetics.model.ecosystem.impl.SteppableEcosystemConfiguration;
import net.lukemcomber.genetics.model.ecosystem.impl.SteppableEcosystemDetails;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.metadata.Environment;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * An interactive ecosystem that is updated by turns (collections of ticks)
 */
public class SteppableEcosystem extends Ecosystem {

    private final Logger logger = Logger.getLogger(SteppableEcosystem.class.getName());
    private final SteppableEcosystemConfiguration configuration;

    public SteppableEcosystem(final UniverseConstants universe, final SteppableEcosystemConfiguration configuration) throws IOException {
        super(configuration.getTicksPerDay(), configuration.getSize(), universe, configuration.getName());
        this.configuration = configuration;
    }

    /**
     * Get the number of ticks in a turn
     *
     * @return ticks per turn
     */
    public long getTicksPerTurn() {
        return configuration.getTicksPerTurn();
    }

    @Override
    public void initialize(final Supplier<Boolean> cleanUpHook) {
        if (getIsInitialized().compareAndSet(false, true)) {
            if (null != getTerrain().getResourceManager()) {
                getTerrain().getResourceManager().initializeAllTerrainResources();
            }
            isActive(true);
        }
    }

    /**
     * Advance time by one tick
     *
     * @return true if active
     * @throws EvolutionException
     */
    public boolean advance() throws EvolutionException {
        if (isActive()) {
            for (int i = 0; i < getTicksPerTurn(); ++i) {
                tickEnvironment();
                tickOrganisms();
            }

            final Environment environmentData = new Environment();
            environmentData.setTickCount(getTotalTicks());
            environmentData.setTotalOrganisms((long) getTerrain().getOrganismCount());

            final MetadataStore<Environment> dataStore = metadataStoreGroup.get(Environment.class);
            dataStore.store(environmentData);

        }
        return isActive();
    }

    /**
     * Get the ecosystem configuration used to build this ecosystem
     *
     * @return ecosystem configuration
     */
    @Override
    public EcosystemDetails getSetupConfiguration() {
        final SteppableEcosystemDetails setupConfiguration = new SteppableEcosystemDetails();

        final Terrain terrain = getTerrain();
        if (Objects.nonNull(terrain)) {
            setupConfiguration.setWidth(getTerrain().getSizeOfXAxis());
            setupConfiguration.setHeight(getTerrain().getSizeOfYAxis());
            setupConfiguration.setDepth(getTerrain().getSizeOfZAxis());
        }
        setupConfiguration.setTurnsPerTick(configuration.getTicksPerTurn());
        setupConfiguration.setInteractive(true);
        setupConfiguration.setActive(isActive());
        setupConfiguration.setName(getName());
        setupConfiguration.setId(getId());
        setupConfiguration.setTotalDays(getTotalDays());
        setupConfiguration.setCurrentTick(getCurrentTick());
        setupConfiguration.setTotalTicks(getTotalTicks());
        setupConfiguration.setCurrentOrganismCount(getTerrain().getOrganismCount());
        setupConfiguration.setTotalOrganismCount(getTerrain().getTotalOrganismCount());
        setupConfiguration.setProperties(getProperties().toMap());
        setupConfiguration.setInitialPopulation(getInitialPopulation());

        return setupConfiguration;
    }
}
