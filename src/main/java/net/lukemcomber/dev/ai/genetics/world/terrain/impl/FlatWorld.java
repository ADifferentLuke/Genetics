package net.lukemcomber.dev.ai.genetics.world.terrain.impl;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
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

    /**
     * @param
     * @param y
     * @param z
     * @param terrainProperty
     */
    public void setTerrainProperty(int x, int y, int z, TerrainProperty terrainProperty) {
        checkInitialized();

        if( 0 == z ) {
            //we are flat, ignore anything above the z axis
            checkCoordinates(x, y);

            if( true ) {
                System.out.println(String.format("(%d,%d,%d) - Set %s to %d", x, y, z, terrainProperty.getId(),
                        terrainProperty.getValue()));
            }

            //on conflict overwrites
            environmentMap[x][y].put(terrainProperty.getId(), terrainProperty);
        }

    }

    /**
     * @param x
     * @param y
     * @param z
     * @return the TerrainProperty or null if it doesn't exist
     */
    public TerrainProperty getTerrainProperty(int x, int y, int z, final String id) {
        checkInitialized();
        checkCoordinates(x, y);
        return environmentMap[x][y].get(id);
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param id
     */
    @Override
    public void deleteTerrainProperty(int x, int y, int z, String id) {
        checkInitialized();
        checkCoordinates(x, y);
        environmentMap[x][y].remove(id);
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param propertyList
     */
    public void setTerrain(int x, int y, int z, List<TerrainProperty> propertyList) {
        checkInitialized();
        checkCoordinates(x, y);
        environmentMap[x][y] = propertyList.stream().collect(
                Collectors.toMap(TerrainProperty::getId, Function.identity()));
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return
     */
    public List<TerrainProperty> getTerrain(int x, int y, int z) {
        checkInitialized();
        checkCoordinates(x, y);
        return new ArrayList<>(environmentMap[x][y].values());
    }

    /**
     * @param x
     * @param y
     * @param z
     */
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
    public boolean hasCell(final int x, final int y, final int z) {
        checkInitialized();
        return null != organismMap[x][y];
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param cell
     * @return true if the cell was placed, false if the coordicates are already occupied
     */
    public boolean setCell(int x, int y, int z, Cell cell) {
        checkInitialized();
        checkCoordinates(x, y);
        final Cell currentCell = organismMap[x][y];
        if (null == currentCell) {
            organismMap[x][y] = cell;
        }
        return null == currentCell;
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return true if cell deleted, false if there is no cell and no action taken
     */
    public boolean deleteCell(int x, int y, int z) {
        checkInitialized();
        checkCoordinates(x, y);
        final Cell currentCell = organismMap[x][y];
        organismMap[x][y] = null;

        return null != currentCell;
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return the cell at the coordinates or null
     */
    @Override
    public Cell getCell(int x, int y, int z) {
        checkInitialized();
        checkCoordinates(x, y);
        return organismMap[x][y];
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
