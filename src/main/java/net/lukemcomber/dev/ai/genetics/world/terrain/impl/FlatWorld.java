package net.lukemcomber.dev.ai.genetics.world.terrain.impl;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import net.lukemcomber.dev.ai.genetics.world.terrain.TerrainProperty;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FlatWorld implements Terrain {

    public static final String ID = "FLAT_WORLD";

    private Cell[][] organismMap;
    private Map<String, TerrainProperty>[][] environmentMap;
    private int worldHeight;
    private int worldWidth;
    private boolean isInitialized = false;

    public void setTerrainProperty(final Coordinates coordinates, final  TerrainProperty terrainProperty) {
        checkInitialized();

        if( 0 == coordinates.zAxis ) {
            //we are flat, ignore anything above the z axis
            checkCoordinates(coordinates.xAxis, coordinates.yAxis);

            if( true ) {
                System.out.println(String.format("(%d,%d,%d) - Set %s to %d", coordinates.zAxis,
                        coordinates.yAxis, coordinates.zAxis, terrainProperty.getId(),
                        terrainProperty.getValue()));
            }

            //on conflict overwrites
            environmentMap[coordinates.xAxis][coordinates.yAxis].put(terrainProperty.getId(), terrainProperty);
        }

    }

    public TerrainProperty getTerrainProperty(final Coordinates coordinates, final String id) {
        checkInitialized();
        checkCoordinates(coordinates.xAxis, coordinates.yAxis);
        return environmentMap[coordinates.xAxis][coordinates.yAxis].get(id);
    }

    @Override
    public void deleteTerrainProperty(final Coordinates coordinates,final String id) {
        checkInitialized();
        checkCoordinates(coordinates.xAxis, coordinates.yAxis);
        environmentMap[coordinates.xAxis][coordinates.yAxis].remove(id);
    }

    public void setTerrain(final Coordinates coordinates,final List<TerrainProperty> propertyList) {
        checkInitialized();
        checkCoordinates(coordinates.xAxis, coordinates.yAxis);
        environmentMap[coordinates.xAxis][coordinates.yAxis] = propertyList.stream().collect(
                Collectors.toMap(TerrainProperty::getId, Function.identity()));
    }

    public List<TerrainProperty> getTerrain(final Coordinates coordinates) {
        checkInitialized();
        checkCoordinates(coordinates.xAxis, coordinates.yAxis);
        return new ArrayList<>(environmentMap[coordinates.xAxis][coordinates.yAxis].values());
    }

    @Override
    public void initialize(int x, int y, int z) {
        worldHeight = y;
        worldWidth = x;
        organismMap = new Cell[x][y];
        environmentMap = new HashMap[x][y];

        //we are at load time, spend extra time now initializing and less time later overall
        for (int i = 0; i < x; ++i) {
            for (int j = 0; j < y; ++j) {
                environmentMap[i][j] = new HashMap<>();
            }
        }
        System.out.println(String.format("World %s initialized to (%d,%d,%d).", ID, x, y, z));
        isInitialized = true;
    }

    /**
     * @return
     */
    public boolean hasCell(final Coordinates coordinates) {
        checkInitialized();
        return null != organismMap[coordinates.xAxis][coordinates.yAxis];
    }

    public boolean setCell(final Cell cell) {
        checkInitialized();
        checkCoordinates(cell.getCoordinates().xAxis, cell.getCoordinates().yAxis);
        final Cell currentCell = organismMap[cell.getCoordinates().xAxis][cell.getCoordinates().yAxis];
        if (null == currentCell) {
            organismMap[cell.getCoordinates().xAxis][cell.getCoordinates().yAxis] = cell;
        }
        return null == currentCell;
    }

    public boolean deleteCell(final Coordinates coordinates) {
        checkInitialized();
        checkCoordinates(coordinates.xAxis, coordinates.yAxis);
        final Cell currentCell = organismMap[coordinates.xAxis][coordinates.yAxis];
        organismMap[coordinates.xAxis][coordinates.yAxis] = null;

        return null != currentCell;
    }

    @Override
    public Cell getCell(final Coordinates coordinates) {
        checkInitialized();
        checkCoordinates(coordinates.xAxis, coordinates.yAxis);
        return organismMap[coordinates.xAxis][coordinates.yAxis];
    }

    /**
     * @return
     */
    @Override
    public int getSizeOfXAxis() {
        return worldWidth;
    }


    /**
     * @return
     */
    @Override
    public int getSizeOfYAxis() {
        return worldHeight;
    }

    /**
     * @return
     */
    @Override
    public int getSizeOfZAxis() {
        return 0;
    }

    private void checkCoordinates(final int x, final int y) {
        if (x >= worldWidth || y >= worldHeight) {
            throw new ArrayIndexOutOfBoundsException("Coordinates (" + x + "," + y
                    + ") are out of bounds for world size [" + worldWidth + "," + worldHeight + "].");
        }
    }

    private void checkInitialized() {
        if (!isInitialized) {
            throw new EvolutionException("FlatWorld has not yet been initialized.");
        }
    }
}
