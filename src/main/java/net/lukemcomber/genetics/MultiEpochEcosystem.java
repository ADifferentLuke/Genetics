package net.lukemcomber.genetics;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Genome;
import net.lukemcomber.genetics.biology.GenomeTransciber;
import net.lukemcomber.genetics.biology.transcription.AsexualTransposeAndMutateGeneTranscriber;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.io.GenomeSerDe;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.model.ecosystem.EcosystemDetails;
import net.lukemcomber.genetics.model.ecosystem.impl.EpochEcosystemConfiguration;
import net.lukemcomber.genetics.model.ecosystem.impl.MultiEpochConfiguration;
import net.lukemcomber.genetics.model.ecosystem.impl.MultiEpochDetails;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.MetadataStoreFactory;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.store.SearchableMetadataStore;
import net.lukemcomber.genetics.store.metadata.Performance;
import net.lukemcomber.genetics.utilities.RandomGenomeCreator;
import net.lukemcomber.genetics.world.terrain.Terrain;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.logging.Logger;


public class MultiEpochEcosystem extends Ecosystem implements Runnable {

    public static final String PROPERTY_RNG_SEED = "multi-epoch.population.rng.seed";

    private final Logger logger = Logger.getLogger(MultiEpochEcosystem.class.getName());
    private final MultiEpochConfiguration configuration;
    private final Set<String> organismFilter;
    private final ConcurrentMap<String, Ecosystem> sessions;
    private Callable<Void> cleanupFunction;
    private BufferedWriter bufferedWriter;

    private final Consumer<EpochEcosystem> onEpochStart;
    private final Consumer<EpochEcosystem> onEpochEnd;
    private final Thread ecosystemThread;
    private final long seed;

    public MultiEpochEcosystem(final UniverseConstants universe, final MultiEpochConfiguration configuration) throws IOException {
        this(universe, configuration, null, null);
    }

    public MultiEpochEcosystem(final UniverseConstants universe, final MultiEpochConfiguration configuration,
                               final Consumer<EpochEcosystem> onEpochStart, final Consumer<EpochEcosystem> onEpochEnd) throws IOException {
        super(configuration.getTicksPerDay(), configuration.getSize(), universe, new AsexualTransposeAndMutateGeneTranscriber(universe));
        this.configuration = configuration;

        if (Objects.nonNull(configuration.getStartOrganisms())) {
            setInitialOrganisms(configuration.getStartOrganisms());
        }

        seed = universe.get(PROPERTY_RNG_SEED, Integer.class, 0).longValue();


        organismFilter = new HashSet<>();
        this.onEpochStart = onEpochStart;
        this.onEpochEnd = onEpochEnd;
        sessions = new ConcurrentHashMap<>();

        ecosystemThread = new Thread(this);
        ecosystemThread.setDaemon(true);
        ecosystemThread.setName("master-%s-epoch-runner".formatted(configuration.getName().toLowerCase()));
    }

    @Override
    public void run() {

        // we have been initialized, we haven't been clean up and we aren't running
        if (getIsInitialized().get() && !getIsCleanedUp().get() && getIsRunning().compareAndSet(false, true)) {
            Map<SpatialCoordinates, String> firstEpochStartingPopulation = new HashMap<>();
            if (null != configuration.getStartOrganisms()) {
                firstEpochStartingPopulation.putAll(configuration.getStartOrganisms());
            }

            // Just use random genomes for the rest of the initial population
            final int randomOrganismCount = configuration.getInitialPopulation() - firstEpochStartingPopulation.size();
            final RandomGenomeCreator genomeCreator = new RandomGenomeCreator(organismFilter, 0 < seed ? seed : null);
            final Set<String> epochStartPopulation = genomeCreator.generateRandomGenomes("PLANT", 0 >= randomOrganismCount ? configuration.getInitialPopulation() : randomOrganismCount);

            Map<SpatialCoordinates, String> fauna = genomeCreator.generateRandomLocations(
                    configuration.getSize().xAxis(),
                    configuration.getSize().yAxis(),
                    epochStartPopulation,
                    firstEpochStartingPopulation);

            for (int epoch = 0; epoch < configuration.getEpochs(); epoch++) {
                try {

                    logger.info("Beginning epoch " + epoch);

                    final String name;
                    if (StringUtils.isNotEmpty(configuration.getName())) {
                        name = configuration.getName() + "-Epoch-" + epoch;
                    } else {
                        name = null;
                    }

                    final EpochEcosystem ecosystem = new EpochEcosystem(getProperties(), EpochEcosystemConfiguration.builder()
                            .ticksPerDay(configuration.getTicksPerDay())
                            .size(configuration.getSize())
                            .maxDays(configuration.getMaxDays())
                            .tickDelayMs(configuration.getTickDelayMs())
                            .name(name)
                            .startOrganisms(fauna)
                            .build(), getGnomeTranscriber());

                    if (Objects.nonNull(onEpochStart)) {
                        onEpochStart.accept(ecosystem);
                    }

                    final Terrain terrain = ecosystem.getTerrain();
                    final MetadataStoreGroup groupStore = MetadataStoreFactory.getMetadataStore(ecosystem.getId(), terrain.getProperties());

                    logger.info("Epoch started.");
                    sessions.put(ecosystem.getId(), ecosystem);

                    final Set<String> survivingDna = new HashSet<>();
                    final MetadataStore<Performance> metadataStore = groupStore.get(Performance.class);

                    ecosystem.initialize(() -> {

                        if (metadataStore instanceof SearchableMetadataStore<Performance>) {
                            ((SearchableMetadataStore<Performance>) metadataStore).page( 0, configuration.getReusePopulation()).forEach(performance -> {
                                survivingDna.add(performance.getDna());
                            });
                        }
                        return null;
                    });

                    ecosystem.getEcosystemThread().join();
                    if (survivingDna.isEmpty()) {
                        throw new EvolutionException("Timing is wonk");
                    }

                    // Run that shiiiiii
                    final int additionalOrganisms = configuration.getInitialPopulation() - survivingDna.size();
                    final Set<String> baseDna = new HashSet<>(survivingDna);


                    int lcv = 0;
                    final String[] survivingDnaArray = survivingDna.toArray(new String[0]);
                    for (int i = 0; i < additionalOrganisms; ++i) {

                        final String fitGenome = survivingDnaArray[lcv];
                        try {

                            final Genome deserializedGenome = GenomeSerDe.deserialize(fitGenome);
                            final Genome mutatedGenome = getGnomeTranscriber().transcribe(deserializedGenome);

                            baseDna.add(GenomeSerDe.serialize(mutatedGenome));

                        } catch (final DecoderException e) {
                            throw new EvolutionException("Failed to deserialize genome (%s).".formatted(StringUtils.isNotEmpty(fitGenome) ? fitGenome : "null"));
                        }

                        if (survivingDnaArray.length <= ++lcv) {
                            lcv = 0;
                        }
                    }

                    fauna = genomeCreator.generateRandomLocations(
                            configuration.getSize().xAxis(),
                            configuration.getSize().yAxis(),
                            baseDna,
                            null);
                    if (Objects.nonNull(this.onEpochEnd)) {
                        this.onEpochEnd.accept(ecosystem);
                    }
                } catch (final IOException | InterruptedException e) {
                    //Switch to unchecked because we can't change signature
                    throw new RuntimeException(e);
                }
            }

            if (Objects.nonNull(cleanupFunction)) {
                try {
                    cleanupFunction.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            logger.info("Multi Epoch simulation is already running.");
        }
    }

    public ConcurrentMap<String, Ecosystem> getEpochs() {
        return this.sessions;
    }

    @Override
    public void initialize(final Callable<Void> cleanUpHook) {
        try {
            if (getIsInitialized().compareAndSet(false, true)) {
                final String proposedPath;
                if (StringUtils.isNotEmpty(configuration.getFileFilterPath())) {
                    proposedPath = configuration.getFileFilterPath();
                } else {
                    proposedPath = Files.createTempFile("", ".gfl").toString();
                }

                logger.info("Using filter file " + proposedPath);

                final File proposedFile = new File(proposedPath);
                final File proposedDirectory = proposedFile.getParentFile();
                if (!proposedDirectory.exists()) {

                    try {
                        Files.createDirectories(proposedDirectory.toPath());
                    } catch (final IOException e) {
                        throw new EvolutionException("Failed to create output path [%s].".formatted(proposedDirectory.getAbsolutePath()));
                    }
                }

                if (proposedFile.exists() && !proposedFile.isFile()) {
                    throw new IOException("Configured file " + proposedPath + " already exists and is not a file.");
                }
                if (configuration.isDeleteFilterOnExit()) {
                    proposedFile.deleteOnExit();
                }
                if (null != this.bufferedWriter) {
                    throw new EvolutionException("Filter buffer is already open!!");
                }
                this.bufferedWriter = new BufferedWriter(new FileWriter(proposedFile, true));
                this.cleanupFunction = () -> {
                    if (null != bufferedWriter) {
                        try {
                            bufferedWriter.flush();
                            bufferedWriter.close();
                            if (Objects.nonNull(cleanUpHook)) {
                                cleanUpHook.call();
                            }
                        } catch (final Exception e) {
                            throw new RuntimeException(e);
                        }

                    }
                    return null;
                };
                this.ecosystemThread.start();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EcosystemDetails getSetupConfiguration() {
        final MultiEpochDetails setupConfiguration = new MultiEpochDetails();

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

        setupConfiguration.setMaxDays(configuration.getMaxDays());
        setupConfiguration.setTickDelay(configuration.getTickDelayMs());

        return setupConfiguration;
    }

}
