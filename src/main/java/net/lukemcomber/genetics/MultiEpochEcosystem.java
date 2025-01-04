package net.lukemcomber.genetics;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.OrganismFactory;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.io.GenomeSerDe;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.ecosystem.EcosystemDetails;
import net.lukemcomber.genetics.model.ecosystem.impl.EpochEcosystemConfiguration;
import net.lukemcomber.genetics.model.ecosystem.impl.MultiEpochConfiguration;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.MetadataStoreFactory;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.store.SearchableMetadataStore;
import net.lukemcomber.genetics.store.metadata.Performance;
import net.lukemcomber.genetics.universes.FlatFloraUniverse;
import net.lukemcomber.genetics.utilities.RandomGenomeCreator;
import net.lukemcomber.genetics.world.terrain.Terrain;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class MultiEpochEcosystem extends Ecosystem implements Runnable {

    private final Logger logger = Logger.getLogger(MultiEpochEcosystem.class.getName());
    private final MultiEpochConfiguration configuration;
    private final Set<String> organismFilter;
    private final ConcurrentMap<String, Ecosystem> sessions;
    private final Map<SpatialCoordinates, String> initialPopulation;
    private Supplier<Boolean> cleanupFunction;

    private BufferedWriter bufferedWriter;

    public MultiEpochEcosystem(final MultiEpochConfiguration configuration) throws IOException {
        this(configuration, null);
    }

    public MultiEpochEcosystem(final MultiEpochConfiguration configuration, final Map<SpatialCoordinates, String> startingPopulation) throws IOException {
        super(configuration.getTicksPerDay(), configuration.getSize(), configuration.getType());
        this.configuration = configuration;

        organismFilter = new HashSet<>();

        sessions = new ConcurrentHashMap<>();
        this.initialPopulation = startingPopulation;
    }

    @Override
    public void run() {
        // we have been initialized, we haven't been clean up and we aren't running
        if (getIsInitialized().get() && !getIsCleanedUp().get() && getIsRunning().compareAndSet(false, true)) {
            Map<SpatialCoordinates, String> reincarnates = new HashMap<>();
            if (null != initialPopulation) {
                reincarnates.putAll(initialPopulation);
                try {
                    addToFilter(new HashSet<>(initialPopulation.values()));
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


                    final EpochEcosystem ecosystem = new EpochEcosystem(EpochEcosystemConfiguration.builder()
                            .ticksPerDay(configuration.getTicksPerDay())
                            .size(configuration.getSize())
                            .type(FlatFloraUniverse.ID)
                            .maxDays(configuration.getMaxDays())
                            .tickDelayMs(configuration.getTickDelayMs())
                            .name(name)
                            .build());

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

                    final Set<String> survivingDna = new HashSet<>();
                    final MetadataStore<Performance> metadataStore = groupStore.get(Performance.class);

                    ecosystem.initialize(() -> {

                        if (metadataStore instanceof SearchableMetadataStore<Performance>) {
                            ((SearchableMetadataStore<Performance>) metadataStore).page("fitness", 0, configuration.getReusePopulation()).forEach(performance -> {
                                survivingDna.add(performance.getDna());
                            });
                        }
                        return true;
                    });

                    initialPopulation.removeAll(reincarnates.values());
                    addToFilter(initialPopulation);

                    ecosystem.getEcosystemThread().join();

                    reincarnates = genomeCreator.generateRandomLocations(
                            configuration.getSize().xAxis(),
                            configuration.getSize().yAxis(),
                            survivingDna,
                            null);

                } catch (final IOException | InterruptedException e) {
                    //Switch to unchecked because we can't change signature
                    throw new RuntimeException(e);
                }
            }
            if (!cleanupFunction.get()) {
                logger.severe("Clean Up function is null!!");
                throw new EvolutionException("Clean Up function is null!");
            }
        } else {
            logger.info("Multi Epoch simulation is already running.");
        }
    }

    @Override
    public void initialize(final Supplier<Boolean> cleanUpHook) {
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
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (null != cleanUpHook) {
                            return cleanUpHook.get();
                        }
                        return true;
                    }
                    return false;
                };
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
        return null;
    }

}
