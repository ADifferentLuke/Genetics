package net.lukemcomber.genetics.service;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import net.lukemcomber.genetics.AutomaticEcosystem;
import net.lukemcomber.genetics.SteppableEcosystem;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.OrganismFactory;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.Ecosystem;
import net.lukemcomber.genetics.store.MetadataStoreFactory;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.world.terrain.Terrain;
import org.apache.commons.codec.DecoderException;

import java.io.IOException;

public class EcoSystemJsonReader extends LGPReader {

    //TODO this is in two places, we should condense to single source of truth
    public static final String DEFAULT_PARENT_ID = "GOD";


    public Ecosystem read(final JsonNode rootNode) throws IOException {

        final String worldType = rootNode.path("world").asText("flat");
        final String name = rootNode.path("name").asText(null);
        final int xMax = rootNode.path("width").asInt(90);
        final int yMax = rootNode.path("height").asInt(90);
        final int zMax = rootNode.path("depth").asInt(90);

        final int ticksPerDay = rootNode.path("ticksPerDay").asInt(10);
        final JsonNode ticksPerTurnNode = rootNode.path("ticksPerTurn");

        final SpatialCoordinates gridSize = new SpatialCoordinates(xMax,yMax,zMax);

        final Ecosystem ecosystem;
        if( ! ticksPerTurnNode.isMissingNode() ) {
            final int ticksPerTurn = ticksPerTurnNode.asInt();
            if( 0 < ticksPerTurn ){
                ecosystem = new SteppableEcosystem(ticksPerTurn, ticksPerDay, gridSize, worldType, name);
            } else {
                throw new RuntimeException("False not acceptable as steppable parameter. Weird right?");
            }
        } else {
            final long maxDays = rootNode.path("maxDays").asLong(1);
            final long tickDelay = rootNode.path("tickDelay").asLong(1000);
            final double cataclysmProbability = rootNode.path("cataclysmProbability").asDouble(0);
            final double cataclysmSurvivalRate = rootNode.path("cataclysmSurvivalRate").asDouble(0);

            ecosystem = new AutomaticEcosystem(ticksPerDay, gridSize, worldType,maxDays, tickDelay, name);
        }
        final Terrain terrain = ecosystem.getTerrain();
        final MetadataStoreGroup groupStore = MetadataStoreFactory.getMetadataStore(ecosystem.getId(),terrain.getProperties());
        final TemporalCoordinates temporalCoordinates = new TemporalCoordinates(0,0,0);

        final ArrayNode zooArray = (ArrayNode) rootNode.path("zoo");
        if (!zooArray.isMissingNode() && 0 < zooArray.size()) {
            zooArray.forEach(node -> {
                final String line = node.asText();

                String currentOrganismType = "PLANT";
                final LGPReader.RangeValueItem rvi = parseItem(line,xMax,yMax,zMax);
                iterateRangeValue(rvi, xMax, yMax, zMax,
                        (x, y, z, v) -> {
                            //only support 1 organism per pixel. In the future, ranges should support large organisms?
                            try {
                                final SpatialCoordinates spatialCoordinates = new SpatialCoordinates(x, y, z);
                                final Organism organism = OrganismFactory.create( DEFAULT_PARENT_ID,
                                        GenomeSerDe.deserialize(currentOrganismType, rvi.value), spatialCoordinates,temporalCoordinates,
                                        terrain.getProperties(),groupStore );
                                ecosystem.addOrganismToInitialPopulation(organism);
                            } catch (DecoderException e) {
                                throw new RuntimeException(e);
                            }
                            return true;
                        });

            });

        }
        ecosystem.initialize();
        return ecosystem;
    }

}
