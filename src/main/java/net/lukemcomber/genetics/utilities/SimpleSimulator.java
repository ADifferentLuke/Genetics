package net.lukemcomber.genetics.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lukemcomber.genetics.AutomaticEcosystem;
import net.lukemcomber.genetics.Ecosystem;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.OrganismFactory;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.io.GenomeSerDe;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.MetadataStoreFactory;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.store.SearchableMetadataStore;
import net.lukemcomber.genetics.store.metadata.Performance;
import net.lukemcomber.genetics.universes.FlatFloraUniverse;
import net.lukemcomber.genetics.utilities.model.SimpleSimulation;
import net.lukemcomber.genetics.world.terrain.Terrain;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


/**
 * A utility class for running multiple simulations in serial
 */
public class SimpleSimulator {

    private final Logger logger = Logger.getLogger(SimpleSimulator.class.getName());
    private final SimpleSimulation simulation;
    private final Set<String> organismFilter;
    private final BufferedWriter bufferedWriter;
    private final ConcurrentMap<String, Ecosystem> sessions;


    /**
     * Create a new instance
     *
     * @param simulation configuration of the simulation
     * @param filterFile file to use to prevent duplicate organisms
     * @throws IOException
     */
    public SimpleSimulator(final SimpleSimulation simulation, final File filterFile) throws IOException {
        this.simulation = simulation;
        organismFilter = new HashSet<>();

        bufferedWriter = new BufferedWriter(new FileWriter(filterFile, true));
        sessions = new ConcurrentHashMap<>();
    }

    /**
     * Get a map of simulation names to ecosystems
     *
     * @return map
     */
    public ConcurrentMap<String, Ecosystem> getSessions() {
        return sessions;
    }


    /**
     * Run the simulation and all epochs starting with the startingPopulation for the first epoch
     *
     * @param startingPopulation initial simulation population
     * @throws IOException
     * @throws InterruptedException
     */
    public void run(final Map<SpatialCoordinates, String> startingPopulation) throws IOException, InterruptedException {

        Map<SpatialCoordinates, String> reincarnates = new HashMap<>();
        if (null != startingPopulation) {
            reincarnates.putAll(startingPopulation);
            addToFilter(new HashSet<>(startingPopulation.values()));
        }
        for (int epoch = 0; epoch < simulation.getEpochs(); epoch++) {

            logger.info("Beginning epoch " + epoch);

            final int randomOrganismCount = simulation.getInitialPopulation() - reincarnates.size();

            final RandomGenomeCreator genomeCreator = new RandomGenomeCreator(organismFilter);
            final Set<String> initialPopulation = genomeCreator.generateRandomGenomes(0 >= randomOrganismCount ? simulation.getInitialPopulation() : randomOrganismCount);

            final Map<SpatialCoordinates, String> fauna = genomeCreator.generateRandomLocations(simulation.getWidth(), simulation.getHeight(), initialPopulation, reincarnates);

            final String name;
            if (StringUtils.isNotEmpty(simulation.getName())) {
                name = simulation.getName() + "-Epoch-" + epoch;
            } else {
                name = null;
            }


            final SpatialCoordinates spatialCoordinates = new SpatialCoordinates(simulation.getWidth(), simulation.getHeight(), 0);
            final AutomaticEcosystem ecosystem = new AutomaticEcosystem(simulation.getTicksPerDay(), spatialCoordinates, FlatFloraUniverse.ID,
                    simulation.getMaxDays(), simulation.getTickDelayMs(), name);

            final Terrain terrain = ecosystem.getTerrain();
            final TemporalCoordinates temporalCoordinates = new TemporalCoordinates(0, 0, 0);
            final MetadataStoreGroup groupStore = MetadataStoreFactory.getMetadataStore(ecosystem.getId(), terrain.getProperties());

            fauna.forEach(((coordinates, genome) -> {
                final Organism organism;
                try {
                    organism = OrganismFactory.create(Organism.DEFAULT_PARENT,
                            GenomeSerDe.deserialize(genome), coordinates, temporalCoordinates,
                            terrain.getProperties(), groupStore);
                } catch (final DecoderException e) {
                    throw new RuntimeException(e);
                }
                ecosystem.addOrganismToInitialPopulation(organism);
            }));
            logger.info("Epoch started.");
            sessions.put(ecosystem.getId(), ecosystem);
            ecosystem.initialize();

            initialPopulation.removeAll(reincarnates.values());
            addToFilter(initialPopulation);
            final Set<String> bestOfSim = monitorSimulation(ecosystem, simulation.getReusePopulation());
            reincarnates = genomeCreator.generateRandomLocations(simulation.getWidth(), simulation.getHeight(), bestOfSim, null);

        }
    }


    /**
     * Run the {@link SimpleSimulator} from the configuration supplied and using the provided
     * filter file.
     *
     * Usage: SimpleSimulator &lt;file&gt; &lt;filter&gt;
     *
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(final String[] args) throws IOException, InterruptedException {

        if (2 != args.length) {
            System.err.println("Usage: SimpleSimulator <file> <filter>");
            return;
        }

        final File inputFile = new File(args[0]);
        final File filterFile = new File(args[1]);
        final ObjectMapper objectMapper = new ObjectMapper();

        final InputStream configFile = SimpleSimulator.class.getResourceAsStream("/logging/logging.properties");
        LogManager.getLogManager().readConfiguration(configFile);


        if (inputFile.exists()) {
            final FileInputStream inputStream = new FileInputStream(inputFile);
            final SimpleSimulation simpleSimulation = objectMapper.readValue(inputStream, SimpleSimulation.class);
            final SimpleSimulator simulation = new SimpleSimulator(simpleSimulation, filterFile);

            simulation.run(null);
        } else {
            System.err.println("File [" + args[0] + "] does not exist.");
        }
    }

    private void addToFilter(final Set<String> organisms) throws IOException {
        organismFilter.addAll(organisms);

        organisms.forEach(organism -> {
            try {
                bufferedWriter.write(organism);
                bufferedWriter.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        bufferedWriter.flush();

    }
    private Set<String> monitorSimulation(final AutomaticEcosystem ecosystem, final int reuseCount) throws InterruptedException, IOException {

        logger.info("Beginning monitor of " + ecosystem.getId());
        do {
            Thread.sleep(60000);
            logger.info(String.format("Day %d Tick %d Organisms %d Total Organisms %d", ecosystem.getTotalDays(), ecosystem.getCurrentTick(),
                    ecosystem.getTerrain().getOrganismCount(), ecosystem.getTerrain().getTotalOrganismCount()));
        } while (ecosystem.isActive());
        logger.info(String.format("%s ended on day %d tick %d", ecosystem.getId(), ecosystem.getTotalDays(), ecosystem.getCurrentTick()));
        final MetadataStoreGroup groupStore = MetadataStoreFactory.getMetadataStore(ecosystem.getId(), ecosystem.getProperties());

        final MetadataStore<Performance> metadataStore = groupStore.get(Performance.class);
        final Set<String> reincarnate = new HashSet<>();
        if (metadataStore instanceof SearchableMetadataStore<Performance>) {
            if (logger.getLevel().intValue() <= Level.INFO.intValue()) {
                final List<Performance> bestOrganisms = ((SearchableMetadataStore<Performance>) metadataStore).page("fitness", 0, 2);
                bestOrganisms.forEach(organism -> {
                    logger.info("Organism " + organism.getDna() + " - fitness " + organism.getFitness());
                });
            }
            ((SearchableMetadataStore<Performance>) metadataStore).page("fitness", 0, reuseCount).forEach(performance -> {
                reincarnate.add(performance.getDna());
            });
        }
        return reincarnate;
    }
}
