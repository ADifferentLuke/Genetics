package net.lukemcomber.genetics;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.model.ecosystem.EcosystemConfiguration;
import net.lukemcomber.genetics.io.GenomeSerDe;
import net.lukemcomber.genetics.io.LoggerOutputStream;
import net.lukemcomber.genetics.store.MetadataStoreFactory;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.universes.UniverseFactory;
import net.lukemcomber.genetics.world.ResourceManager;
import net.lukemcomber.genetics.world.TerrainFactory;
import net.lukemcomber.genetics.world.terrain.Terrain;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
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
    private boolean active;
    private boolean initalized;

    /**
     * Creates a new instance of an {@link Ecosystem}
     *
     * @param ticksPerDay count of ticks in a day
     * @param size        scale of terrain
     * @param type        type of the ecosystem
     * @throws IOException
     */
    public Ecosystem(final int ticksPerDay, final SpatialCoordinates size, final String type) throws IOException {
        this(ticksPerDay, size, type, null);
    }

    /**
     * Creates a new instance of an {@link Ecosystem}
     *
     * @param ticksPerDay count of ticks in a day
     * @param size        scale of terrain
     * @param type        type of the ecosystem
     * @param name        human-readable name of the simulation
     * @throws IOException
     */
    public Ecosystem(final int ticksPerDay, final SpatialCoordinates size, final String type, final String name) throws IOException {


        this.ticksPerDay = ticksPerDay;
        this.initialPopulation = new LinkedList<>();

        totalDays = 0;
        totalTicks = 0;
        currentTick = 0;

        properties = UniverseFactory.get(type);
        uuid = UUID.randomUUID().toString();
        if (StringUtils.isNotEmpty(name)) {
            this.name = name;
        } else {
            this.name = uuid;
        }

        metadataStoreGroup = MetadataStoreFactory.getMetadataStore(uuid, properties);

        terrain = TerrainFactory.create(size, properties, metadataStoreGroup);

        this.active = true; //TODO should move to initialized?
        this.initalized = false;
    }

    /**
     * Gets the total ticks of the simulation
     *
     * @return ticks
     */
    public long getTotalTicks() {
        return totalTicks;
    }

    /**
     * Sets the total ticks
     *
     * @param totalTicks
     */
    public void setTotalTicks(final long totalTicks) {
        this.totalTicks = totalTicks;
    }

    /**
     * Get total days
     *
     * @return total days
     */
    public long getTotalDays() {
        return totalDays;
    }

    /**
     * Sets the total days
     *
     * @param totalDays
     */
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
    public int getCurrentTick() {
        return currentTick;
    }

    /**
     * Sets the current tick
     *
     * @param currentTick
     */
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
    public void initialize() {
        if (null != terrain.getResourceManager()) {
            terrain.getResourceManager().initializeAllTerrainResources();
        }
        initalized = true;
    }

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
        if (active) {
            final ResourceManager manager = getTerrain().getResourceManager();
            manager.renewDailyEnvironmentResource();
        }
    }

    /**
     * Add an organism the simulation's seed populations
     *
     * @param organism organism to add
     */
    public void addOrganismToInitialPopulation(final Organism organism) {
        if (!initalized) {
            initialPopulation.add(GenomeSerDe.serialize(organism.getGenome()));
            terrain.addOrganism(organism);
        } else {
            throw new EvolutionException("Cannot add organism to already started simulation");
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
        return active;
    }

    /**
     * Set if the ecosystem is active
     *
     * @param active
     */
    public void isActive(final boolean active) {
        this.active = active;
    }

    /**
     * Advance time by one tick
     *
     * @return true if active
     * @throws EvolutionException
     */
    public abstract boolean advance() throws EvolutionException;

    /**
     * Get the ecosystem configuration used to build this ecosystem
     *
     * @return ecosystem configuration
     */
    public abstract EcosystemConfiguration getSetupConfiguration();

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
