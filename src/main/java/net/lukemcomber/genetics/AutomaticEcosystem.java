package net.lukemcomber.genetics;

import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.metadata.Environment;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AutomaticEcosystem extends Ecosystem implements Runnable {

    private final Logger logger = Logger.getLogger(AutomaticEcosystem.class.getName());


    private final long maxDays;
    private final long tickDelayMs;
    private final double catastrophicProbability;
    private final double catastrophicSurvivalRate;

    private final Thread ecosystemThread;

    public AutomaticEcosystem(final int ticksPerDay, final SpatialCoordinates size, final String type,
                              final long maxDays, final long delayMs, final double catastrophicProbability,
                              final double catastrophicSurvivalRate) throws IOException {
        super(ticksPerDay, size, type);

        this.maxDays = maxDays;
        this.tickDelayMs = delayMs;
        this.catastrophicProbability = catastrophicProbability;
        this.catastrophicSurvivalRate = catastrophicSurvivalRate;

        isActive(true);

        ecosystemThread = new Thread(this);
        ecosystemThread.setName("World-" + getId());
        ecosystemThread.setDaemon(true);

    }

    public double getCatastrophicProbability(){
        return catastrophicProbability;
    }

    public double getCatastrophicSurvivalRate(){
        return catastrophicSurvivalRate;
    }

    public long getMaxDays(){
        return maxDays;
    }

    public long getTickDelayMs(){
        return tickDelayMs;
    }

    @Override
    public boolean advance() {
        // You can not advance an automatic ecosystem.
        return false;
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

                final long startTimeMillis = - System.currentTimeMillis();

                logger.info("Ticking world " + getId());
                tickEnvironment();
                tickOrganisms();

                //TODO add catastrophies
                if( getTotalTicks() % 10 == 0 ) {

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
                    if( 0 < sleepTime ) {
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

    private void killRemainingOrganisms(){
        final TemporalCoordinates temporalCoordinates = new TemporalCoordinates(getTotalTicks(), getTotalDays(), getCurrentTick());

        for (final Iterator<Organism> it = getTerrain().getOrganisms(); it.hasNext(); ) {
            final Organism organism = it.next();
            organism.kill(temporalCoordinates,"Organism " + organism.getUniqueID() + " died from time ending.");
        }
    }
}
