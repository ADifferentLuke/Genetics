package net.lukemcomber.dev.ai.genetics.world.terrain.impl;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;
import net.lukemcomber.dev.ai.genetics.service.CellHelper;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import net.lukemcomber.dev.ai.genetics.world.terrain.TerrainProperty;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FlatWorld implements Terrain {

    private final static boolean debug = false;

    public static final String ID = "FLAT_WORLD";

    private Cell[][] organismMap;
    private Map<String, TerrainProperty>[][] environmentMap;
    private List<Organism> population;
    private int worldHeight;
    private int worldWidth;
    private int ticksPerDay;
    private int ticksPerTurn;
    private boolean isInitialized = false;


    public void setTerrainProperty(final Coordinates coordinates, final TerrainProperty terrainProperty) {
        checkInitialized();

        if (0 == coordinates.zAxis) {
            //we are flat, ignore anything above the z axis
            checkCoordinates(coordinates.xAxis, coordinates.yAxis);

            if (debug) {
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
    public void deleteTerrainProperty(final Coordinates coordinates, final String id) {
        checkInitialized();
        checkCoordinates(coordinates.xAxis, coordinates.yAxis);
        environmentMap[coordinates.xAxis][coordinates.yAxis].remove(id);
    }

    public void setTerrain(final Coordinates coordinates, final List<TerrainProperty> propertyList) {
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
        population = new ArrayList<>();

        ticksPerDay = 10; //defaults
        ticksPerTurn = 1;

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

    @Override
    public boolean hasOrganism(Organism organism) {
        return null != population && population.contains(organism);
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

    /*
     * z-axis will cause collisions because it doesn't exist, so
     * we need to change the out of bounds check to ignore it
     */
    @Override
    public boolean isOutOfBounds(final Coordinates coordinates) {
        return !(getSizeOfXAxis() > coordinates.xAxis
                && getSizeOfYAxis() > coordinates.yAxis
                && 0 <= coordinates.xAxis
                && 0 <= coordinates.yAxis);
    }

    public boolean addOrganism(final Organism organism) {
        boolean retVal = false;
        if (null != organism) {
            if (!population.contains(organism)) {
                final List<Cell> cells = CellHelper.getAllOrganismsCells(organism.getCells());
                // Before setting the cells, make sure there are no conflicts
                boolean doesOrganismFit = true;
                for (final Cell cell : cells) {
                    if (hasCell(cell.getCoordinates())) {
                        final Cell currentCell = getCell(cell.getCoordinates());
                        if (currentCell != cell) {
                            doesOrganismFit = false;
                        }
                    }
                }
                if (doesOrganismFit) {
                    cells.forEach(c -> setCell(c));
                    population.add(organism);
                    retVal = true;
                } else {
                    throw new RuntimeException("Failed to create terrain. Organisms physically conflict.");
                }
            }
        }
        return retVal;
    }

    public boolean deleteOrganism(final Organism organism) {
        throw new NotImplementedException();
    }

    public Iterator<Organism> getOrganisms() {
         /*
          *   To prevent concurrent modification, create iterator from a shallow copy
          *    so any changes to the underlying list does not invalidate the iterator
          *
          */
        return new ArrayList<>(population).iterator();
    }



}
