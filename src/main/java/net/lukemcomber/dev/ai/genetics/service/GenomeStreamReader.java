package net.lukemcomber.dev.ai.genetics.service;

import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.biology.OrganismFactory;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import org.apache.commons.codec.DecoderException;

import java.util.LinkedList;
import java.util.List;

public class GenomeStreamReader extends LGPStreamReader<List<GenomeStreamReader.OrganismLocation>, GenomeStreamReader.ContextData> {

    private enum Context {
        DECLARATION,
        EXPLANATION
    }

    class ContextData {
        List<OrganismLocation> organisms;
        String currentOrganismType;
        Context context = Context.DECLARATION;
        RangeValueItem item = null;
    }

    public class OrganismLocation {
        int x = 0;
        int y = 0;
        int z = 0;

        Organism organism = null;
    }

    public static final String ORGANISM = "ORGANISM ";
    public static final String END = "END";

    final int sizeOfXAxis;
    final int sizeOfYAxis;
    final int sizeOfZAxis;


    public GenomeStreamReader(int sizeOfXAxis, int sizeOfYAxis, int sizeOfZAxis) {
        super();
        this.sizeOfXAxis = sizeOfXAxis;
        this.sizeOfYAxis = sizeOfYAxis;
        this.sizeOfZAxis = sizeOfZAxis;
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
    List<OrganismLocation> getResult(final ContextData contextData) {
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
                                //create genome from factory?
                                try {
                                    final Organism organism = OrganismFactory.create(contextData.currentOrganismType,
                                            GenomeSerDe.deserialize(contextData.currentOrganismType, contextData.item.value));
                                    final OrganismLocation location = new OrganismLocation();
                                    location.x = x;
                                    location.y = y;
                                    location.z = z;
                                    location.organism = organism;
                                    contextData.organisms.add(location);
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
