package net.lukemcomber.genetics.service;

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

public class GenomeStreamReader extends LGPStreamLineReader<GenomeStreamReader.ContextData,List<Organism>> {

    public static final String DEFAULT_PARENT_ID = "GOD";
    private enum Context {
        DECLARATION,
        EXPLANATION
    }

    class ContextData {
        List<Organism> organisms;
        String currentOrganismType;
        Context context = Context.DECLARATION;
        LGPReader.RangeValueItem item = null;
    }

    public static final String ORGANISM = "ORGANISM ";
    public static final String END = "END";

    private final int sizeOfXAxis;
    private final int sizeOfYAxis;
    private final int sizeOfZAxis;

    private UniverseConstants properties;

    private final MetadataStoreGroup metadataStoreGroup;

    public GenomeStreamReader(final SpatialCoordinates dimensions, final UniverseConstants properties,
                              final MetadataStoreGroup metadataStoreGroup ) {
        super();
        this.sizeOfXAxis = dimensions.xAxis;
        this.sizeOfYAxis = dimensions.yAxis;
        this.sizeOfZAxis = dimensions.zAxis;
        this.metadataStoreGroup = metadataStoreGroup;
        this.properties = properties;
    }

    /**
     * @return
     */
    @Override
    ContextData initPayload() {
        ContextData contextData = new ContextData();
        contextData.organisms = new LinkedList<>();
        return contextData;
    }

    /**
     * @param contextData
     * @return
     */
    @Override
    List<Organism> getResult(final ContextData contextData) {
        return contextData.organisms;
    }

    /**
     * @param line
     * @param contextData
     */
    @Override
    void parse(final String line, final ContextData contextData) {

        switch (contextData.context) {
            case DECLARATION:
                if (line.startsWith(ORGANISM)) {
                    String newLine = line.substring(ORGANISM.length());
                    newLine = newLine.trim();

                    contextData.currentOrganismType = newLine;
                    contextData.context = Context.EXPLANATION;
                }
                break;
            case EXPLANATION:
                if (line.equalsIgnoreCase(END)) {
                    contextData.context = Context.DECLARATION;
                    contextData.currentOrganismType = null;
                } else {
                    contextData.item = parseItem(line, sizeOfXAxis, sizeOfYAxis, sizeOfZAxis);
                    iterateRangeValue(contextData.item, sizeOfXAxis, sizeOfYAxis, sizeOfZAxis,
                            (x, y, z, v) -> {
                                //only support 1 organism per pixel. In the future, ranges should support large organisms?
                                try {
                                    final SpatialCoordinates spatialCoordinates = new SpatialCoordinates(x,y,z);
                                    final TemporalCoordinates temporalCoordinates = new TemporalCoordinates(0,0,0);
                                    final Organism organism = OrganismFactory.create( DEFAULT_PARENT_ID,
                                            GenomeSerDe.deserialize(contextData.currentOrganismType, contextData.item.value), spatialCoordinates,temporalCoordinates,
                                            properties, metadataStoreGroup);
                                    contextData.organisms.add(organism);
                                } catch (DecoderException e) {
                                    throw new RuntimeException(e);
                                }
                                return true;
                            });
                }
                break;
        }
    }
}
