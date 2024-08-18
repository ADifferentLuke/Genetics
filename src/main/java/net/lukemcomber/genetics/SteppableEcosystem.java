package net.lukemcomber.genetics;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.ecosystem.EcosystemConfiguration;
import net.lukemcomber.genetics.model.ecosystem.impl.SteppableEcosystemConfiguration;
import net.lukemcomber.genetics.io.LoggerOutputStream;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.metadata.Environment;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An interactive ecosystem that is updated by turns (collections of ticks)
 */
public class SteppableEcosystem extends Ecosystem {

    private final Logger logger = Logger.getLogger(SteppableEcosystem.class.getName());

    private final long ticksPerTurn;

    /**
     * Creates a new instance
     *
     * @param ticksPerTurn number of ticks in a turn
     * @param ticksPerDay  number of ticks in a day
     * @param size         size of the terrain
     * @param type         type of ecosystem
     * @throws IOException
     */
    public SteppableEcosystem(final int ticksPerTurn, final int ticksPerDay, final SpatialCoordinates size, final String type) throws IOException {
        this(ticksPerTurn, ticksPerDay, size, type, null);
    }

    /**
     * Creates a new instance
     *
     * @param ticksPerTurn number of ticks in a turn
     * @param ticksPerDay  number of ticks in a day
     * @param size         size of the terrain
     * @param type         type of ecosystem
     * @param name         name of the simulation
     * @throws IOException
     */
    public SteppableEcosystem(final int ticksPerTurn, final int ticksPerDay, final SpatialCoordinates size, final String type, final String name) throws IOException {

        super(ticksPerDay, size, type, name);
        this.ticksPerTurn = ticksPerTurn;
    }

    /**
     * Get the number of ticks in a turn
     *
     * @return ticks per turn
     */
    public long getTicksPerTurn() {
        return ticksPerTurn;
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
    public EcosystemConfiguration getSetupConfiguration() {
        final SteppableEcosystemConfiguration setupConfiguration = new SteppableEcosystemConfiguration();

        final Terrain terrain = getTerrain();
        if (Objects.nonNull(terrain)) {
            setupConfiguration.setWidth(getTerrain().getSizeOfXAxis());
            setupConfiguration.setHeight(getTerrain().getSizeOfYAxis());
            setupConfiguration.setDepth(getTerrain().getSizeOfZAxis());
        }
        setupConfiguration.setTurnsPerTick(ticksPerTurn);
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
