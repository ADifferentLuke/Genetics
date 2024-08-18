package net.lukemcomber.genetics.io;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.OrganismFactory;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.TemporalCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import org.apache.commons.codec.DecoderException;

import java.util.LinkedList;
import java.util.List;

/**
 * Parses a serialized genome and coordinates
 */
public class GenomeStreamReader extends LGPStreamLineReader<GenomeStreamReader.ContextData, List<Organism>> {

    class ContextData {
        List<Organism> organisms;
        SpatialCoordinateRangeParser.RangeValueItem item = null;
    }

    private UniverseConstants properties;

    private final MetadataStoreGroup metadataStoreGroup;
    private final SpatialCoordinates simulationSize;

    /**
     * Creates a new genome stream reader
     *
     * @param dimensions         spatial size of simulation
     * @param properties         simulation parameters
     * @param metadataStoreGroup meta data tracking store
     */
    public GenomeStreamReader(final SpatialCoordinates dimensions, final UniverseConstants properties,
                              final MetadataStoreGroup metadataStoreGroup) {
        super();
        this.simulationSize = dimensions;
        this.metadataStoreGroup = metadataStoreGroup;
        this.properties = properties;
    }

    /**
     * Initializes the reader.
     *
     * @return reader context data
     */
    @Override
    ContextData initPayload() {
        ContextData contextData = new ContextData();
        contextData.organisms = new LinkedList<>();
        return contextData;
    }

    /**
     * Returns a list of organisms
     *
     * @param contextData reader context data
     * @return list of built organisms
     */
    @Override
    List<Organism> getResult(final ContextData contextData) {
        return contextData.organisms;
    }

    /**
     * Parse coordinates or a range of coordinates and deserializes organisms
     *
     * @param line        string to parse
     * @param contextData reader context data
     */
    @Override
    void parse(final String line, final ContextData contextData) {

        // Expand the range if it is one
        contextData.item = parseItem(line, simulationSize);

        // Iterate the range or point
        iterateRangeValue(contextData.item, simulationSize,
                (spatialCoordinates, v) -> {
                    //only support 1 organism per pixel. In the future, ranges should support large organisms?
                    try {
                        final TemporalCoordinates temporalCoordinates = new TemporalCoordinates(0, 0, 0);
                        final Organism organism = OrganismFactory.create(Organism.DEFAULT_PARENT,
                                GenomeSerDe.deserialize(v), spatialCoordinates, temporalCoordinates,
                                properties, metadataStoreGroup);
                        contextData.organisms.add(organism);
                    } catch (final DecoderException e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                });
    }
}
