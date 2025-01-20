package net.lukemcomber.genetics;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.exception.EvolutionException;
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

    private final Logger logger = Logger.getLogger(MultiEpochEcosystem.class.getName());
    private final MultiEpochConfiguration configuration;
    private final Set<String> organismFilter;
    private final ConcurrentMap<String, Ecosystem> sessions;
    private Callable<Void> cleanupFunction;
    private BufferedWriter bufferedWriter;

    private final Consumer<EpochEcosystem> onEpochStart;
    private final Consumer<EpochEcosystem> onEpochEnd;
    private final Thread ecosystemThread;
    public MultiEpochEcosystem(final UniverseConstants universe, final MultiEpochConfiguration configuration) throws IOException {
        this( universe, configuration, null,null);
    }

    public MultiEpochEcosystem(final UniverseConstants universe, final MultiEpochConfiguration configuration,
                               final Consumer<EpochEcosystem> onEpochStart, final Consumer<EpochEcosystem> onEpochEnd) throws IOException {
        super(configuration.getTicksPerDay(), configuration.getSize(), universe);
        this.configuration = configuration;

        if (Objects.nonNull(configuration.getStartOrganisms())) {
            setInitialOrganisms(configuration.getStartOrganisms());
        }

        organismFilter = new HashSet<>();
        this.onEpochStart = onEpochStart;
        this.onEpochEnd = onEpochEnd;
        sessions = new ConcurrentHashMap<>();

        ecosystemThread = new Thread(this);
        ecosystemThread.setDaemon(true);
        ecosystemThread.setName( "master-%s-epoch-runner");
    }

    @Override
    public void run() {

        // we have been initialized, we haven't been clean up and we aren't running
        if (getIsInitialized().get() && !getIsCleanedUp().get() && getIsRunning().compareAndSet(false, true)) {
            Map<SpatialCoordinates, String> reincarnates = new HashMap<>();
            if (null != configuration.getStartOrganisms()) {
                reincarnates.putAll(configuration.getStartOrganisms());
                try {
                    addToFilter(new HashSet<>(configuration.getStartOrganisms().values()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            for (int epoch = 0; epoch < configuration.getEpochs(); epoch++) {
                try {

                    logger.info("Beginning epoch " + epoch);

                    final int randomOrganismCount = configuration.getInitialPopulation() - reincarnates.size();

                    final RandomGenomeCreator genomeCreator = new RandomGenomeCreator(organismFilter);
                    final Set<String> initialPopulation = genomeCreator.generateRandomGenomes("PLANT", 0 >= randomOrganismCount ? configuration.getInitialPopulation() : randomOrganismCount);

                    final Map<SpatialCoordinates, String> fauna = genomeCreator.generateRandomLocations(
                            configuration.getSize().xAxis(),
                            configuration.getSize().yAxis(),
                            initialPopulation,
                            reincarnates);

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
                            .build());

                    if( Objects.nonNull(onEpochStart)) {
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
                            ((SearchableMetadataStore<Performance>) metadataStore).page("fitness", 0, configuration.getReusePopulation()).forEach(performance -> {
                                survivingDna.add(performance.getDna());
                            });
                        }
                        return null;
                    });

                    initialPopulation.removeAll(reincarnates.values());
                    addToFilter(initialPopulation);

                    ecosystem.getEcosystemThread().join();

                    reincarnates = genomeCreator.generateRandomLocations(
                            configuration.getSize().xAxis(),
                            configuration.getSize().yAxis(),
                            survivingDna,
                            null);
                    if( Objects.nonNull(this.onEpochEnd)){
                        this.onEpochEnd.accept(ecosystem);
                    }
                } catch (final IOException | InterruptedException e) {
                    //Switch to unchecked because we can't change signature
                    throw new RuntimeException(e);
                }
            }
            if( Objects.nonNull(cleanupFunction)){
                try {
                    cleanupFunction.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new EvolutionException("Clean Up function is null!");
            }
        } else {
            logger.info("Multi Epoch simulation is already running.");
        }
    }

    public ConcurrentMap<String,Ecosystem> getEpochs(){
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
