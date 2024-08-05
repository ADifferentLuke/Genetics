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
import net.lukemcomber.genetics.service.GenomeSerDe;
import net.lukemcomber.genetics.service.LoggerOutputStream;
import net.lukemcomber.genetics.store.MetadataStoreFactory;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.universes.PreCannedUniverses;
import net.lukemcomber.genetics.world.ResourceManager;
import net.lukemcomber.genetics.world.terrain.Terrain;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Ecosystem {

    private static final Logger logger = Logger.getLogger(Ecosystem.class.getName());
    private static final LoggerOutputStream loggerOutputStream = new LoggerOutputStream(logger, Level.INFO);
    private Terrain terrain;
    private final int ticksPerDay;
    private final String uuid;
    protected final UniverseConstants properties;
    protected final MetadataStoreGroup metadataStoreGroup;
    private final List<String> initialPopulation;

    private boolean active;
    private boolean initalized;

    public long getTotalTicks() {
        return totalTicks;
    }

    public void setTotalTicks(final long totalTicks) {
        this.totalTicks = totalTicks;
    }

    public long getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(final long totalDays) {
        this.totalDays = totalDays;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(final int currentTick) {
        this.currentTick = currentTick;
    }

    private long totalTicks;
    private long totalDays;
    private int currentTick;

    private final String name;

    public Ecosystem(final int ticksPerDay, final SpatialCoordinates size, final String type) throws IOException {
        this(ticksPerDay,size,type,null);
    }

    public Ecosystem(final int ticksPerDay, final SpatialCoordinates size, final String type, final String name) throws IOException {


        this.ticksPerDay = ticksPerDay;
        this.initialPopulation = new LinkedList<>();

        totalDays = 0;
        totalTicks = 0;
        currentTick = 0;

        properties = PreCannedUniverses.get(type);
        uuid = UUID.randomUUID().toString();
        if(StringUtils.isNotEmpty(name)){
            this.name = name;
        } else {
            this.name = uuid;
        }

        metadataStoreGroup = MetadataStoreFactory.getMetadataStore(uuid, properties);

        terrain = WorldFactory.createWorld(properties, metadataStoreGroup);
        terrain.initialize(size.xAxis, size.yAxis, size.zAxis);

        this.active = true; //TODO should move to initialized?
        this.initalized = false;
    }

    public String getName() {
        return name;
    }

    public UniverseConstants getProperties() {
        return properties;
    }

    public void initialize() {
        if (null != terrain.getResourceManager()) {
            terrain.getResourceManager().initializeAllTerrainResources();
        }
        initalized = true;
    }

    public String getId() {
        return uuid;
    }

    public int getTicksPerDay() {
        return ticksPerDay;
    }

    void refreshResources() {
        if (active) {
            final ResourceManager manager = getTerrain().getResourceManager();
            manager.renewDailyEnvironmentResource();
        }
    }

    public void addOrganismToInitialPopulation(final Organism organism) {
        if (!initalized) {
            initialPopulation.add(GenomeSerDe.serialize(organism.getGenome()));
            terrain.addOrganism(organism);
        } else {
            throw new EvolutionException("Cannot add organism to already started simulation");
        }
    }

    public List<String> getInitialPopulation() {
        return initialPopulation;
    }

    public boolean isActive() {
        return active;
    }

    void isActive(final boolean active) {
        this.active = active;
    }

    private void tick(final int steps) {
        this.totalTicks += steps;
        this.currentTick += steps;

        if (this.currentTick >= ticksPerDay) {
            totalDays++;
            this.currentTick = 0;
        }
    }

    public abstract boolean advance() throws EvolutionException;

    protected void tickEnvironment() {
        final long currentDay = getTotalDays();
        tick(1);

        logger.info("Tick:  " + getTotalTicks());
        // We advanced a day
        if (getTotalDays() > currentDay) {
            refreshResources();
        }
    }

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
            organism.leechResources(getTerrain(), temporalCoordinates);
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

}
