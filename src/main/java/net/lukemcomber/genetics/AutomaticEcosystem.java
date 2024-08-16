package net.lukemcomber.genetics;

import net.lukemcomber.genetics.biology.Organism;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class AutomaticEcosystem extends Ecosystem implements Runnable {

    private final Logger logger = Logger.getLogger(AutomaticEcosystem.class.getName());


    private final long maxDays;
    private final long tickDelayMs;
    private final Thread ecosystemThread;

    public AutomaticEcosystem(final int ticksPerDay, final SpatialCoordinates size, final String type,
                              final long maxDays, final long delayMs) throws IOException {
        this(ticksPerDay,size,type,maxDays,delayMs,null);

    }

    public AutomaticEcosystem(final int ticksPerDay, final SpatialCoordinates size, final String type,
                              final long maxDays, final long delayMs, final String name) throws IOException {
        super(ticksPerDay, size, type, name);

        this.maxDays = maxDays;
        this.tickDelayMs = delayMs;

        isActive(true);

        ecosystemThread = new Thread(this);
        ecosystemThread.setName("World-" + getId());
        ecosystemThread.setDaemon(true);

    }

    public long getMaxDays() {
        return maxDays;
    }

    public long getTickDelayMs() {
        return tickDelayMs;
    }

    @Override
    public boolean advance() {
        // You can not advance an automatic ecosystem.
        return false;
    }

    @Override
    public EcosystemConfiguration getSetupConfiguration() {
        final AutomatedEcosystemConfiguration setupConfiguration = new AutomatedEcosystemConfiguration();

        final Terrain terrain = getTerrain();
        if(Objects.nonNull(terrain)){
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

    @Override
    public void initialize() {
        super.initialize();
        ecosystemThread.start();
    }

    @Override
    public void run() {

        try {
            while (isActive()) {

                final long startTimeMillis = -System.currentTimeMillis();

                logger.info("Ticking world " + getId());
                tickEnvironment();
                tickOrganisms();

                //TODO add catastrophies
                if (getTotalTicks() % 10 == 0) {

                    final Environment environmentData = new Environment();
                    environmentData.tickCount = getTotalTicks();
                    environmentData.totalOrganisms = (long) getTerrain().getOrganismCount();

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
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, String.format("World id %s failed to delay. Terminating.", getId()), e);
            isActive(false);
        }

    }

    private void killRemainingOrganisms() {
        final TemporalCoordinates temporalCoordinates = new TemporalCoordinates(getTotalTicks(), getTotalDays(), getCurrentTick());

        for (final Iterator<Organism> it = getTerrain().getOrganisms(); it.hasNext(); ) {
            final Organism organism = it.next();
            organism.kill(temporalCoordinates, Organism.CauseOfDeath.Unknown,"Organism " + organism.getUniqueID() + " died from time ending.");
        }
    }
}
