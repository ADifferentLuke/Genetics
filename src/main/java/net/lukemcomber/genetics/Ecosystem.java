package net.lukemcomber.genetics;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.OrganismFactory;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.model.ecosystem.EcosystemDetails;
import net.lukemcomber.genetics.io.GenomeSerDe;
import net.lukemcomber.genetics.io.LoggerOutputStream;
import net.lukemcomber.genetics.store.MetadataStoreFactory;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.world.ResourceManager;
import net.lukemcomber.genetics.world.TerrainFactory;
import net.lukemcomber.genetics.world.terrain.Terrain;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ecosystem the simulation will run in. An ecosystem comprises Terrain, Organisms, and Resources
 */
public abstract class Ecosystem {

    private static final Logger logger = Logger.getLogger(Ecosystem.class.getName());
    private static final LoggerOutputStream loggerOutputStream = new LoggerOutputStream(logger, Level.INFO);

    protected final UniverseConstants properties;
    protected final MetadataStoreGroup metadataStoreGroup;
    private Terrain terrain;
    private final int ticksPerDay;
    private final String uuid;
    private final List<String> initialPopulation;
    private long totalTicks;
    private long totalDays;
    private int currentTick;
    private final String name;
    private final AtomicBoolean isRunning;
    private final AtomicBoolean isInitialized;
    private final AtomicBoolean isCleanedUp;
    private final SpatialCoordinates worldSize;

    public Ecosystem(final int ticksPerDay, final SpatialCoordinates size, final UniverseConstants universe) throws IOException {
        this(ticksPerDay, size, universe, null);
    }

    public Ecosystem(final int ticksPerDay, final SpatialCoordinates size, final UniverseConstants universe, final String name) throws IOException {


        this.ticksPerDay = ticksPerDay;
        this.initialPopulation = new LinkedList<>();
        this.worldSize = size;

        totalDays = 0;
        totalTicks = 0;
        currentTick = 0;

        properties = universe;
        uuid = UUID.randomUUID().toString();
        if (StringUtils.isNotEmpty(name)) {
            this.name = name;
        } else {
            this.name = uuid;
        }

        metadataStoreGroup = MetadataStoreFactory.getMetadataStore(uuid, properties);

        terrain = TerrainFactory.create(size, properties, metadataStoreGroup);

        isRunning = new AtomicBoolean(false);
        isInitialized = new AtomicBoolean(false);
        isCleanedUp = new AtomicBoolean(false);
    }

    public TemporalCoordinates getTime() {
        return new TemporalCoordinates(totalTicks, totalDays, currentTick);
    }

    protected AtomicBoolean getIsRunning() {
        return isRunning;
    }

    protected AtomicBoolean getIsInitialized() {
        return isInitialized;
    }

    protected AtomicBoolean getIsCleanedUp() {
        return isCleanedUp;
    }

    /**
     * Gets the total ticks of the simulation
     *
     * @return ticks
     */
    @Deprecated
    public long getTotalTicks() {
        return totalTicks;
    }

    /**
     * Sets the total ticks
     *
     * @param totalTicks
     */
    @Deprecated
    public void setTotalTicks(final long totalTicks) {
        this.totalTicks = totalTicks;
    }

    /**
     * Get total days
     *
     * @return total days
     */
    @Deprecated
    public long getTotalDays() {
        return totalDays;
    }

    /**
     * Sets the total days
     *
     * @param totalDays
     */
    @Deprecated
    public void setTotalDays(final long totalDays) {
        this.totalDays = totalDays;
    }

    /**
     * Get the terrain
     *
     * @return terrain
     */
    public Terrain getTerrain() {
        return terrain;
    }

    /**
     * Get the current tick
     *
     * @return current tick
     */
    @Deprecated
    public int getCurrentTick() {
        return currentTick;
    }

    /**
     * Sets the current tick
     *
     * @param currentTick
     */
    @Deprecated
    public void setCurrentTick(final int currentTick) {
        this.currentTick = currentTick;
    }

    /**
     * Get simulation name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the simulation's configuration property
     *
     * @return properties
     */
    public UniverseConstants getProperties() {
        return properties;
    }

    /**
     * Initializes the ecosystem
     */
    public abstract void initialize(final Callable<Void> cleanUpHook);

    /**
     * Get the ecosystems unique id
     *
     * @return
     */
    public String getId() {
        return uuid;
    }

    /**
     * Get the count of ticks in a day
     *
     * @return ticks in a day
     */
    public int getTicksPerDay() {
        return ticksPerDay;
    }

    /**
     * Refresh the ecosystem's resources
     */
    void refreshResources() {
        if (isRunning.get()) {
            final ResourceManager manager = getTerrain().getResourceManager();
            manager.renewDailyEnvironmentResource();
        }
    }

    void setInitialOrganisms(final Map<SpatialCoordinates, String> generation) {

        try {
            final MetadataStoreGroup groupStore = MetadataStoreFactory.getMetadataStore(getId(), getProperties());

            for (final Map.Entry<SpatialCoordinates, String> record : generation.entrySet()) {

                final Organism organism = OrganismFactory.create(Organism.DEFAULT_PARENT,
                        GenomeSerDe.deserialize(record.getValue()), record.getKey(), getTime(),
                        getProperties(), groupStore);

                initialPopulation.add(record.getValue());
                terrain.addOrganism(organism);
            }
        } catch (final DecoderException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the list of organisms that seeded the ecosystem
     *
     * @return list of genome strings
     */
    public List<String> getInitialPopulation() {
        return initialPopulation;
    }

    /**
     * Is the simulation active
     *
     * @return true if currently running
     */
    public boolean isActive() {
        return isRunning.get();
    }

    /**
     * Set if the ecosystem is active
     *
     * @param active
     */
    public void isActive(final boolean active) {
        isRunning.set(active);
    }

    public SpatialCoordinates getWorldSize() {
        return worldSize;
    }


    /**
     * Get the ecosystem configuration used to build this ecosystem
     *
     * @return ecosystem configuration
     */
    public abstract EcosystemDetails getSetupConfiguration();

    /**
     * Advance the environment by one tick
     */
    protected void tickEnvironment() {
        final long currentDay = getTotalDays();
        tick(1);

        logger.info("Tick:  " + getTotalTicks());
        // We advanced a day
        if (getTotalDays() > currentDay) {
            refreshResources();
        }
    }

    /**
     * Advance all organisms by one tick
     */
    protected void tickOrganisms() {
        final TemporalCoordinates temporalCoordinates = new TemporalCoordinates(getTotalTicks(), getTotalDays(), getCurrentTick());
        logger.info("Organism count " + getTerrain().getOrganismCount());

        /*
         * Organisms remove themselves (or rather the terrain removes them) from
         *  the world which can cause a Concurrency problem. We can solve this
         *  by creating a new reference to the collection.
         *
         * More thought should be given to making this purely asynchronous
         */
        for (final Iterator<Organism> it = getTerrain().getOrganisms(); it.hasNext(); ) {
            Organism organism = it.next();
            logger.info("Ticking Organism: " + organism.getUniqueID());
            organism.performAction(getTerrain(), temporalCoordinates, ((organism1, cell) -> {
                final ResourceManager manager = getTerrain().getResourceManager();
                manager.renewEnvironmentResourceFromCellDeath(organism, cell);
                logger.info("Organism " + organism.getUniqueID() + " decayed.");
            }));
            organism.prettyPrint(loggerOutputStream);

        }
        if (0 == getTerrain().getOrganismCount()) {
            isActive(false);
        }
    }

    private void tick(final int steps) {
        this.totalTicks += steps;
        this.currentTick += steps;

        if (this.currentTick >= ticksPerDay) {
            totalDays++;
            this.currentTick = 0;
        }
    }
}
