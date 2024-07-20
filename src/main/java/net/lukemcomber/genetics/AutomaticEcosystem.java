package net.lukemcomber.genetics;

import net.lukemcomber.genetics.model.SpatialCoordinates;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AutomaticEcosystem extends Ecosystem implements Runnable {

    private static final Logger logger = Logger.getLogger(AutomaticEcosystem.class.getName());


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

                logger.info("Ticking world " + getId());
                tickEnvironment();
                tickOrganisms();

                //TODO add catastrophies

                if (getTotalDays() >= maxDays) {
                    isActive(false);
                }

                Thread.sleep(tickDelayMs);


            }
            logger.info("Simulation " + getId() + " finished.");
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, String.format("World id %s failed to delay. Terminating.", getId()), e);
            isActive(false);
        }

    }
}
