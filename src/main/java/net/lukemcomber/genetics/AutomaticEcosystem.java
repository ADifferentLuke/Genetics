package net.lukemcomber.genetics;

/*
 * (c) 2024 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.ecosystem.EcosystemConfiguration;
import net.lukemcomber.genetics.model.ecosystem.impl.AutomatedEcosystemConfiguration;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.metadata.Environment;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An ecosystem that once started will run until end without input
 */
public class AutomaticEcosystem extends Ecosystem implements Runnable {

    private final Logger logger = Logger.getLogger(AutomaticEcosystem.class.getName());

    private final long maxDays;
    private final long tickDelayMs;
    private final AtomicBoolean isStarted;
    private final Thread ecosystemThread;

    /**
     * Create a new instance
     *
     * @param ticksPerDay ticks in a day
     * @param size        size of the terrain
     * @param type        type of the ecosystem
     * @param maxDays     maximum simulation days
     * @param delayMs     delay between ticks in milliseconds
     * @throws IOException
     */
    public AutomaticEcosystem(final int ticksPerDay, final SpatialCoordinates size, final String type,
                              final long maxDays, final long delayMs) throws IOException {
        this(ticksPerDay, size, type, maxDays, delayMs, null);

    }

    /**
     * Create a new instance
     *
     * @param ticksPerDay ticks in a day
     * @param size        size of the terrain
     * @param type        type of the ecosystem
     * @param maxDays     maximum simulation days
     * @param delayMs     delay between ticks in milliseconds
     * @param name        name of the simulation
     * @throws IOException
     */
    public AutomaticEcosystem(final int ticksPerDay, final SpatialCoordinates size, final String type,
                              final long maxDays, final long delayMs, final String name) throws IOException {
        super(ticksPerDay, size, type, name);

        this.maxDays = maxDays;
        this.tickDelayMs = delayMs;
        this.isStarted = new AtomicBoolean(false);

        isActive(true);

        ecosystemThread = new Thread(this);
        ecosystemThread.setName("World-" + getId());
        ecosystemThread.setDaemon(true);

    }

    /**
     * Get the maximum days allowed
     *
     * @return maximum days
     */
    public long getMaxDays() {
        return maxDays;
    }

    /**
     * Milliseconds to wait between each tick
     *
     * @return delay in milliseconds
     */
    public long getTickDelayMs() {
        return tickDelayMs;
    }

    /**
     * Does nothing but return false
     *
     * @return false
     */
    @Override
    public boolean advance() {
        // You can not advance an automatic ecosystem.
        return false;
    }

    /**
     * Get the ecosystem configuration used to build this ecosystem
     *
     * @return ecosystem configuration
     */
    @Override
    public EcosystemConfiguration getSetupConfiguration() {
        final AutomatedEcosystemConfiguration setupConfiguration = new AutomatedEcosystemConfiguration();

        final Terrain terrain = getTerrain();
        if (Objects.nonNull(terrain)) {
            setupConfiguration.setWidth(getTerrain().getSizeOfXAxis());
            setupConfiguration.setHeight(getTerrain().getSizeOfYAxis());
            setupConfiguration.setDepth(getTerrain().getSizeOfZAxis());
        }
        setupConfiguration.setInteractive(false);
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

        setupConfiguration.setMaxDays(getMaxDays());
        setupConfiguration.setTickDelay(getTickDelayMs());


        return setupConfiguration;
    }

    /**
     * Starts the simulation
     */
    @Override
    public synchronized void initialize() {
        super.initialize();
        if (!isStarted.get()) {
            ecosystemThread.start();
            isStarted.set(true);
        }
    }

    /**
     * Thread entry point to run ecosystem. Should not be called directly.
     * <p>
     * Use {@link AutomaticEcosystem#initialize()} to start the simulation.
     */
    @Override
    public void run() {

        try {
            if (null == getTerrain().getResourceManager()) {
                throw new EvolutionException("Ecosystem must be initialized before running.");
            } else if (isStarted.get()) {
                throw new EvolutionException("Ecosystem already started.");
            }
            while (isActive()) {

                final long startTimeMillis = -System.currentTimeMillis();

                logger.info("Ticking world " + getId());
                tickEnvironment();
                tickOrganisms();

                if (getTotalTicks() % 10 == 0) {

                    final Environment environmentData = new Environment();
                    environmentData.setTickCount(getTotalTicks());
                    environmentData.setTotalOrganisms((long) getTerrain().getOrganismCount());

                    final MetadataStore<Environment> dataStore = metadataStoreGroup.get(Environment.class);
                    dataStore.store(environmentData);
                }

                if (getTotalDays() >= maxDays) {
                    isActive(false);
                    killRemainingOrganisms();
                } else {

                    final long processingTime = System.currentTimeMillis() + startTimeMillis;
                    final long sleepTime = tickDelayMs - processingTime;
                    if (0 < sleepTime) {
                        Thread.sleep(tickDelayMs);
                    }
                }


            }
            logger.info("Simulation " + getId() + " finished.");
        } catch (final InterruptedException e) {
            logger.log(Level.SEVERE, String.format("World id %s failed to delay. Terminating.", getId()), e);
            isActive(false);
        }

    }

    private void killRemainingOrganisms() {
        final TemporalCoordinates temporalCoordinates = new TemporalCoordinates(getTotalTicks(), getTotalDays(), getCurrentTick());

        for (final Iterator<Organism> it = getTerrain().getOrganisms(); it.hasNext(); ) {
            final Organism organism = it.next();
            organism.kill(temporalCoordinates, Organism.CauseOfDeath.Unknown, "Organism " + organism.getUniqueID() + " died from time ending.");
        }
    }
}
