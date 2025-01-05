package net.lukemcomber.genetics;

/*
 * (c) 2024 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.model.ecosystem.EcosystemDetails;
import net.lukemcomber.genetics.model.ecosystem.impl.EpochEcosystemConfiguration;
import net.lukemcomber.genetics.model.ecosystem.impl.EpochEcosystemDetails;
import net.lukemcomber.genetics.store.Metadata;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.impl.MetadataStorage;
import net.lukemcomber.genetics.store.metadata.Environment;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An ecosystem that once started will run until end without input
 */
public class EpochEcosystem extends Ecosystem implements Runnable {

    private final Logger logger = Logger.getLogger(EpochEcosystem.class.getName());

    private final EpochEcosystemConfiguration configuration;
    private final Thread ecosystemThread;

    private Supplier<Boolean> cleanUpFunction;

    public EpochEcosystem(final UniverseConstants universe, final EpochEcosystemConfiguration configuration) throws IOException {
        super(configuration.getTicksPerDay(), configuration.getSize(), universe, configuration.getName());

        this.configuration = configuration;

        ecosystemThread = new Thread(this);
        ecosystemThread.setName("World-" + getId());
        ecosystemThread.setDaemon(true);
    }

    public Thread getEcosystemThread() { //TODO protected
        return ecosystemThread;
    }

    /**
     * Get the maximum days allowed
     *
     * @return maximum days
     */
    public long getMaxDays() {
        return configuration.getMaxDays();
    }

    /**
     * Milliseconds to wait between each tick
     *
     * @return delay in milliseconds
     */
    public long getTickDelayMs() {
        return configuration.getTickDelayMs();
    }

    /**
     * Get the ecosystem configuration used to build this ecosystem
     *
     * @return ecosystem configuration
     */
    @Override
    public EcosystemDetails getSetupConfiguration() {
        final EpochEcosystemDetails setupConfiguration = new EpochEcosystemDetails();

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
    public synchronized void initialize(final Supplier<Boolean> cleanUpFunction) {
        if (getIsInitialized().compareAndSet(false, true)) {
            this.cleanUpFunction = cleanUpFunction;
            if (null != getTerrain().getResourceManager()) {
                getTerrain().getResourceManager().initializeAllTerrainResources();
            }
            isActive(true);
            ecosystemThread.start();
        }
    }

    /**
     * Thread entry point to run ecosystem. Should not be called directly.
     */
    @Override
    public void run() {

        try {
            if (null == getTerrain().getResourceManager()) {
                throw new EvolutionException("Ecosystem must be initialized before running.");
            }
            while (isActive()) {

                final long startTimeMillis = -System.currentTimeMillis();

                logger.info("Ticking world " + getId());
                tickEnvironment();
                tickOrganisms();

                if (getTotalTicks() % 10 == 0) { // TODO make configurable

                    final Environment environmentData = new Environment();
                    environmentData.setTickCount(getTotalTicks());
                    environmentData.setTotalOrganisms((long) getTerrain().getOrganismCount());

                    final MetadataStore<Environment> dataStore = metadataStoreGroup.get(Environment.class);
                    dataStore.store(environmentData);
                }

                if (getTotalDays() >= configuration.getMaxDays()) {
                    isActive(false);
                    killRemainingOrganisms();
                    final Set<Class<? extends Metadata>> activeStores = metadataStoreGroup.getActiveMetadataStores();
                    activeStores.forEach(clazz -> {

                        MetadataStore<?> storage = metadataStoreGroup.get(clazz);
                        final String path = MetadataStorage.persist(storage, getName(), properties);
                        logger.info("Saved data: " + path);
                    });
                    cleanUpFunction.get();
                    metadataStoreGroup.close();
                } else {

                    final long processingTime = System.currentTimeMillis() + startTimeMillis;
                    final long sleepTime = getTickDelayMs() - processingTime;
                    if (0 < sleepTime) {
                        Thread.sleep(sleepTime); //throttle
                    }
                }


            }
        } catch (final InterruptedException e) {
            logger.log(Level.SEVERE, String.format("World id %s failed to delay. Terminating.", getId()), e);
        }
        logger.info("Simulation " + getId() + " finished.");
        isActive(false);

    }

    private void killRemainingOrganisms() {
        final TemporalCoordinates temporalCoordinates = new TemporalCoordinates(getTotalTicks(), getTotalDays(), getCurrentTick());

        for (final Iterator<Organism> it = getTerrain().getOrganisms(); it.hasNext(); ) {
            final Organism organism = it.next();
            organism.kill(temporalCoordinates, Organism.CauseOfDeath.Unknown, "Organism " + organism.getUniqueID() + " died from time ending.");
        }
    }
}
