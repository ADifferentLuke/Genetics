package net.lukemcomber.genetics.universes;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import net.lukemcomber.genetics.biology.plant.PlantOrganism;
import net.lukemcomber.genetics.biology.plant.behavior.EjectSeed;
import net.lukemcomber.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.genetics.biology.plant.behavior.GrowRoot;
import net.lukemcomber.genetics.biology.plant.behavior.GrowSeed;
import net.lukemcomber.genetics.biology.plant.cells.LeafCell;
import net.lukemcomber.genetics.biology.plant.cells.RootCell;
import net.lukemcomber.genetics.biology.plant.cells.SeedCell;
import net.lukemcomber.genetics.biology.plant.cells.StemCell;
import net.lukemcomber.genetics.biology.transcription.AsexualTransposGenomeTranscriber;
import net.lukemcomber.genetics.biology.transcription.MutationGenomeTranscriber;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.impl.MetadataStorage;
import net.lukemcomber.genetics.store.metadata.Environment;
import net.lukemcomber.genetics.store.metadata.Performance;
import net.lukemcomber.genetics.world.terrain.Terrain;
import net.lukemcomber.genetics.world.terrain.impl.FlatWorld;
import net.lukemcomber.genetics.world.terrain.impl.FlatWorldResourceManager;

import java.util.Map;
import java.util.logging.Logger;

/**
 * A 2-dimensional universe for plant life
 */
public final class FlatFloraUniverse extends UniverseConstants {

    private static final Logger logger = Logger.getLogger(FlatFloraUniverse.class.getName());


    public static final String ID = "flat-floral-universe";

    private static final Map<String, Object> CONSTANTS;

    static {
        CONSTANTS = new ImmutableMap.Builder<String, Object>()
                /* World Type */
                .put(Terrain.PROPERTY_TERRAIN_TYPE, FlatWorld.ID)
                /* World Properties */
                .put(FlatWorldResourceManager.DAILY_SOLAR_PROPERTY, 10)
                .put(FlatWorldResourceManager.INITIAL_SOIL_PROPERTY, 100)
                /* Plant Properties */
                .put(PlantOrganism.PROPERTY_STARTING_ENERGY, 5)
                .put(PlantOrganism.PROPERTY_OLD_AGE_LIMIT, 100)
                .put(PlantOrganism.PROPERTY_STARVATION_LIMIT, 0)
                .put(PlantOrganism.PROPERTY_STAGNATION_LIMIT, 10)
                .put(PlantOrganism.PROPERTY_GERMINATION_LIMIT, 10)
                /* Cell Properties */
                .put(LeafCell.PROPERTY_ENERGY, 2)
                .put(LeafCell.PROPERTY_METACOST, 1)
                .put(SeedCell.PROPERTY_ENERGY, 0)
                .put(SeedCell.PROPERTY_METACOST, 0)
                .put(StemCell.PROPERTY_ENERGY, 1)
                .put(StemCell.PROPERTY_METACOST, 1)
                .put(RootCell.PROPERTY_ENERGY, 2)
                .put(RootCell.PROPERTY_METACOST, 1)
                /* Action Costs */
                .put(GrowLeaf.PROPERTY_GROW_LEAF_COST, 2)
                .put(GrowRoot.PROPERTY_GROW_ROOT_COST, 2)
                .put(GrowSeed.PROPERTY_GROW_SEED_COST, 20)
                .put(EjectSeed.PROPERTY_EJECT_SEED_COST, 25)
                /* Genome Modification / Reproduction */
                .put(AsexualTransposGenomeTranscriber.GENOME_TRANSPOSE_PROBABILITY, 5)
                .put(MutationGenomeTranscriber.GENOME_MUTATE_PROBABILITY, 5)
                /* Metadata Store */
                .put(MetadataStore.PROPERTY_DATASTORE_TTL, 86400L) // One day in seconds
                .put(MetadataStore.METADATA_EXPORT, true)
                .put(MetadataStorage.PROPERTY_TYPE_PATH, "./output/")
                /* Metadata */
                .put(Performance.PROPERTY_PERFORMANCE_ENABLE, true)
                .put(Environment.PROPERTY_ENVIRONMENT_ENABLE, true)
                .put(MetadataStorage.PROPERTY_METADATA_EXPORT_TEMPLATE.formatted(Performance.class.getSimpleName()), true)
                .put(MetadataStorage.PROPERTY_METADATA_EXPORT_TEMPLATE.formatted(Environment.class.getSimpleName()), true)
                .build();
    }

    public FlatFloraUniverse() {
        super(CONSTANTS);
    }
}
