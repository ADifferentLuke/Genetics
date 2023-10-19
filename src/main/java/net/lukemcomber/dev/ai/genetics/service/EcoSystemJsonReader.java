package net.lukemcomber.dev.ai.genetics.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.OrganismFactory;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;
import net.lukemcomber.dev.ai.genetics.world.Ecosystem;
import net.lukemcomber.dev.ai.genetics.world.WorldFactory;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import org.apache.commons.codec.DecoderException;

import java.util.LinkedList;
import java.util.List;

public class EcoSystemJsonReader extends LGPReader {

    public Ecosystem read(final JsonNode rootNode) {

        final String worldType = rootNode.path("world").asText("flat");
        final int xMax = rootNode.path("width").asInt(90);
        final int yMax = rootNode.path("height").asInt(90);
        final int zMax = rootNode.path("depth").asInt(90);

        final int ticksPerDay = rootNode.path("ticksPerDay").asInt(10);
        final int ticksPerTurn = rootNode.path("ticksPerTurn").asInt(1);

        final Terrain terrain = WorldFactory.createWorld(worldType);
        terrain.initialize(xMax, yMax, zMax);

        final Ecosystem ecosystem = new Ecosystem( ticksPerTurn, ticksPerDay, terrain);

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
                                final Coordinates coordinates = new Coordinates(x, y, z);
                                final Organism organism = OrganismFactory.create(
                                        GenomeSerDe.deserialize(currentOrganismType, rvi.value), coordinates);
                                ecosystem.addOrganism(organism);
                            } catch (DecoderException e) {
                                throw new RuntimeException(e);
                            }
                            return true;
                        });

            });

        }
        return ecosystem;
    }

}
