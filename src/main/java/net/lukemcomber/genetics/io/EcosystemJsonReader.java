package net.lukemcomber.genetics.io;

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
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.Ecosystem;
import net.lukemcomber.genetics.store.MetadataStoreFactory;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.world.terrain.Terrain;
import org.apache.commons.codec.DecoderException;

import java.io.IOException;

/**
 * This class reads in a json document and constructs an {@link Ecosystem}
 */
public class EcosystemJsonReader extends SpatialCoordinateRangeParser {

    /**
     * /world
     */
    public static final String WORLD_PROPERTY_PATH = "world";
    /**
     * /name
     */
    public static final String NAME_PROPERTY_PATH = "name";
    /**
     * /width
     */
    public static final String WIDTH_PROPERTY_PATH = "width";
    /**
     * /height
     */
    public static final String HEIGHT_PROPERTY_PATH = "height";
    /**
     * /depth
     */
    public static final String DEPTH_PROPERTY_PATH = "depth";
    /**
     * /tickPerDay
     */
    public static final String TICKS_PER_DAY_PROPERTY_PATH = "ticksPerDay";
    /**
     * /ticksPerTurn
     */
    public static final String TICKS_PER_TURN_PROPERTY_PATH = "ticksPerTurn";
    /**
     * /maxDays
     */
    public static final String MAX_DAYS_PROPERTY_PATH = "maxDays";
    /**
     * /tickDelay
     */
    public static final String TICK_DELAY_PROPERTY_PATH = "tickDelay";
    /**
     * /zoo
     */
    public static final String ZOO_PROPERTY_PATH = "zoo";

    /**
     * Creates an Ecosystem object from a Jackson {@link JsonNode} and initializes it. Currently
     * hard coded for PLANT cells.
     *
     * @param rootNode json object containing ecosystem parameters
     * @return a new Ecosystem
     * @throws IOException      - Unable to create filesystem cache
     * @throws RuntimeException - if json is missing parameters or incomplete
     */
    public Ecosystem read(final JsonNode rootNode) throws IOException {

        /* Simulation ecosystem */
        final JsonNode worldTypeNode = getPropertyJsonNode(rootNode, WORLD_PROPERTY_PATH, true);

        /* Simulation human readable name */
        final JsonNode nameNode = getPropertyJsonNode(rootNode, NAME_PROPERTY_PATH, false);

        /* Dimensions */
        final JsonNode xMaxNode = getPropertyJsonNode(rootNode, WIDTH_PROPERTY_PATH, true);
        final JsonNode yMaxNode = getPropertyJsonNode(rootNode, HEIGHT_PROPERTY_PATH, true);
        final JsonNode zMaxNode = getPropertyJsonNode(rootNode, DEPTH_PROPERTY_PATH, true);

        /* Global time settings */
        final JsonNode ticksPerDayNode = getPropertyJsonNode(rootNode, TICKS_PER_DAY_PROPERTY_PATH, true);
        final JsonNode ticksPerTurnNode = getPropertyJsonNode(rootNode, TICKS_PER_TURN_PROPERTY_PATH, false);

        /* Pull out values from JSON */
        final String worldType = worldTypeNode.asText();

        final String name = nameNode.asText(null);

        final int xMax = xMaxNode.asInt();
        final int yMax = yMaxNode.asInt();
        final int zMax = zMaxNode.asInt();

        final int ticksPerDay = ticksPerDayNode.asInt();

        /* Build our simulation space */
        final SpatialCoordinates gridSize = new SpatialCoordinates(xMax, yMax, zMax);

        final Ecosystem ecosystem;

        /* Determine type of simulation */
        if (!ticksPerTurnNode.isMissingNode()) {
            final int ticksPerTurn = ticksPerTurnNode.asInt();
            if (0 < ticksPerTurn) {
                ecosystem = new SteppableEcosystem(ticksPerTurn, ticksPerDay, gridSize, worldType, name);
            } else {
                throw new RuntimeException("Ticks per turn cannot be a negative value. You can't time travel!");
            }
        } else {
            final JsonNode maxDaysNode = getPropertyJsonNode(rootNode, MAX_DAYS_PROPERTY_PATH, true);
            final JsonNode tickDelayNode = getPropertyJsonNode(rootNode, TICK_DELAY_PROPERTY_PATH, true);

            final long maxDays = maxDaysNode.asLong();
            final long tickDelay = tickDelayNode.asLong();

            ecosystem = new AutomaticEcosystem(ticksPerDay, gridSize, worldType, maxDays, tickDelay, name);
        }

        /* Build the initial organisms */

        final Terrain terrain = ecosystem.getTerrain();
        final MetadataStoreGroup groupStore = MetadataStoreFactory.getMetadataStore(ecosystem.getId(), terrain.getProperties());
        final TemporalCoordinates temporalCoordinates = new TemporalCoordinates(0, 0, 0);

        final JsonNode zooNode = getPropertyJsonNode(rootNode, ZOO_PROPERTY_PATH, true);
        final ArrayNode zooArray = (ArrayNode) zooNode;
        if (!zooArray.isMissingNode() && 0 < zooArray.size()) {
            zooArray.forEach(node -> {
                final String line = node.asText();

                final SpatialCoordinateRangeParser.RangeValueItem rvi = parseItem(line, gridSize );
                iterateRangeValue(rvi, gridSize,
                        (spatialCoordinates, v) -> {
                            //only support 1 organism per pixel. In the future, ranges should support large organisms?
                            try {
                                final Organism organism = OrganismFactory.create(Organism.DEFAULT_PARENT,
                                        GenomeSerDe.deserialize(rvi.value), spatialCoordinates, temporalCoordinates,
                                        terrain.getProperties(), groupStore);
                                ecosystem.addOrganismToInitialPopulation(organism);
                            } catch (DecoderException e) {
                                throw new RuntimeException(e);
                            }
                            return true;
                        });

            });

        }
        ecosystem.initialize(null);
        return ecosystem;
    }

    /**
     * Returns the JsonNode for field or MissingNode if required is false. If required is true
     * and the JsonNode does not contain the requested field, an {@link EvolutionException} is thrown
     *
     * @param node     json node
     * @param field    field to pull out
     * @param required throw exception if not found
     * @return the JsonNode requirest or MissingNode if required is false
     */
    private JsonNode getPropertyJsonNode(final JsonNode node, final String field, final boolean required) {
        final JsonNode newNode = node.path(field);
        if (required && (null == newNode || newNode.isMissingNode())) {
            throw new EvolutionException(String.format("%s is a required field. Please specify", field));
        }
        return newNode;

    }

}
