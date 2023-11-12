package net.lukemcomber.dev.ai.genetics.service;

import net.lukemcomber.dev.ai.genetics.model.SpatialCoordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import net.lukemcomber.dev.ai.genetics.world.terrain.TerrainProperty;
import net.lukemcomber.dev.ai.genetics.world.terrain.TerrainPropertyFactory;
import net.lukemcomber.dev.ai.genetics.WorldFactory;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;


public class TerrainStreamReader extends LGPStreamLineReader<TerrainStreamReader.ContextData,Terrain> {

    final static boolean debug = false;

    private enum Context {
        TYPE,
        INIT,
        BODY,
        PROPERTY

    }

    class ContextData {

        Terrain terrain = null;
        TerrainProperty property = null;
        int xMax = 0, yMax = 0, zMax = 0;
        Context context = Context.TYPE;
        LGPReader.RangeValueItem item = null;
    }

    public static final String WORLD = "WORLD";
    public static final String GRID = "GRID";
    public static final String START = "START ";
    public static final String END = "END";

    /**
     * @return
     */
    @Override
    ContextData initPayload() {
        ContextData data = new ContextData();
        return data;
    }

    /**
     * @param contextData
     * @return
     */
    @Override
    Terrain getResult(final ContextData contextData) {
        return contextData.terrain;
    }

    /**
     * @param line
     */
    @Override
    void parse(final String line, final ContextData data) {

        switch (data.context) {
            case TYPE: {
                final Pair<String, String> pair = requireNameValue(line);
                if (WORLD.equalsIgnoreCase(pair.getLeft())) {
                    data.terrain = WorldFactory.createWorld(pair.getRight());
                    data.context = Context.INIT;
                }
                break;
            }
            case INIT: {
                final Pair<String, String> pair = requireNameValue(line);
                if (GRID.equalsIgnoreCase(pair.getLeft())) {
                    final String[] dimensions = StringUtils.split(pair.getRight(), 'x');
                    if (3 == dimensions.length) {
                        data.xMax = Integer.parseInt(dimensions[0]);
                        data.yMax = Integer.parseInt(dimensions[1]);
                        data.zMax = Integer.parseInt(dimensions[2]);
                        data.terrain.initialize(data.xMax, data.yMax, data.zMax);
                        data.context = Context.BODY;
                    } else {
                        throw new EvolutionException("Invalid syntax for grid [" + pair.getRight() + "].");
                    }
                }
                break;
            }
            case BODY: {
                if (line.startsWith(START)) {
                    String newLine = line.substring(START.length());
                    newLine = newLine.trim();

                    data.property = TerrainPropertyFactory.createTerrainProperty(newLine);
                    data.context = Context.PROPERTY;
                    if( debug ) {
                        System.out.println("Setting " + data.property.getId());
                    }
                }
                break;
            }
            case PROPERTY: {
                if (line.equalsIgnoreCase(END)) {
                    data.context = Context.BODY;
                } else {
                    data.item = parseItem(line, data.xMax, data.yMax, data.zMax);
                    iterateRangeValue(data.item, data.xMax, data.yMax, data.zMax,
                            (x, y, z, v) -> {
                                final TerrainProperty tp = data.property.clone();
                                tp.setValue(v);
                                final SpatialCoordinates spatialCoordinates = new SpatialCoordinates(x,y,z);
                                data.terrain.setTerrainProperty(spatialCoordinates, tp);
                                return true;
                            });
                }
                break;
            }
        }
    }

// Line := ID : entry
// ID := Terrain Identifier
// value := integer
// entry := (X,Y,Z), value : value
// X := x : [x1-x2]
// Y := y : [y1-y2]
// Z := z : [z1-z2]


// 3x + 2z + 4y = 0

}
