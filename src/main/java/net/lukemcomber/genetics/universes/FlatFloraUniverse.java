package net.lukemcomber.genetics.universes;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import com.google.common.collect.ImmutableMap;
import net.lukemcomber.genetics.biology.plant.PlantOrganism;
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
import net.lukemcomber.genetics.world.terrain.impl.FlatWorldResourceManager;

import java.util.Map;

public final class FlatFloraUniverse extends UniverseConstants {

    public static final String ID = "flat-floral-universe";

    private static final Map<String, Object> CONSTANTS;

    static {
        CONSTANTS = new ImmutableMap.Builder<String, Object>()
                /* World Properties */
                .put(FlatWorldResourceManager.DAILY_SOLAR_PROPERTY, 10)
                .put(FlatWorldResourceManager.INITIAL_SOIL_PROPERTY, 100)
                /* Plant Properties */
                .put(PlantOrganism.PROPERTY_STARTING_ENERGY, 5)
                .put(PlantOrganism.PROPERTY_OLD_AGE_LIMIT, 100)
                .put(PlantOrganism.PROPERTY_STARVATION_LIMIT, 0)
                .put(PlantOrganism.PROPERTY_STAGNATION_LIMIT, 10)
                /* Cell Properties */
                .put(LeafCell.PROPERTY_ENERGY, 3)
                .put(LeafCell.PROPERTY_METACOST, 1)
                .put(SeedCell.PROPERTY_ENERGY, 0)
                .put(SeedCell.PROPERTY_METACOST, 0)
                .put(StemCell.PROPERTY_ENERGY, 0)
                .put(StemCell.PROPERTY_METACOST, 1)
                .put(RootCell.PROPERTY_ENERGY, 2)
                .put(RootCell.PROPERTY_METACOST, 1)
                /* Action Costs */
                .put(GrowLeaf.PROPERTY_GROW_LEAF_COST, 1)
                .put(GrowRoot.PROPERTY_GROW_ROOT_COST, 2)
                .put(GrowSeed.PROPERTY_GROW_SEED_COST, 10)
                /* Genome Modification / Reproduction */
                .put(AsexualTransposGenomeTranscriber.GENOME_TRANSPOSE_PROBABILITY,5)
                .put(MutationGenomeTranscriber.GENOME_MUTATE_PROBABILITY,5)
                .build();
    }

    public FlatFloraUniverse() {
        super(CONSTANTS);
    }
}
