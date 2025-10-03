package net.lukemcomber.genetics.world.terrain.impl;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.io.CellHelper;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.world.ResourceManager;
import net.lukemcomber.genetics.world.terrain.Terrain;
import net.lukemcomber.genetics.world.terrain.TerrainProperty;
import net.lukemcomber.genetics.exception.EvolutionException;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A 2-dimensional implementation of {@link Terrain}
 */
public class FlatWorld extends Terrain {

    private static final Logger logger = Logger.getLogger(FlatWorld.class.getName());

    public static final String ID = "FLAT_WORLD";


    private class MatrixCell {
        Cell cell;
        Organism organism;

        boolean isNotEmpty() {
            return null != cell && null != organism;
        }

    }

    private MatrixCell[][] organismMap;
    private Map<String, TerrainProperty>[][] environmentMap;
    private final ResourceManager resourceManager;

    private final SpatialCoordinates bounds;

    /**
     * Create a new instance from the given configuration properties and a metadata store group
     *
     * @param spatialBounds size of environment
     * @param constants     configuration propertied
     * @param store         metadata store group
     */
    public FlatWorld(final SpatialCoordinates spatialBounds, final UniverseConstants constants, final MetadataStoreGroup store) {
        super(spatialBounds, constants, store);
        resourceManager = new FlatWorldResourceManager(this, constants);
        bounds = spatialBounds;

        organismMap = new MatrixCell[spatialBounds.xAxis()][spatialBounds.yAxis()];
        environmentMap = new HashMap[spatialBounds.xAxis()][spatialBounds.yAxis()];

        //we are at load time, spend extra time now initializing and less time later overall
        for (int i = 0; i < spatialBounds.xAxis(); ++i) {
            for (int j = 0; j < spatialBounds.yAxis(); ++j) {
                environmentMap[i][j] = new HashMap<>();
            }
        }
        logger.info(String.format("World %s initialized to (%d,%d,%d).", ID, spatialBounds.xAxis(), spatialBounds.yAxis(), spatialBounds.zAxis()));
    }

    @Override
    public void clear() {
        organismMap = new MatrixCell[bounds.xAxis()][bounds.yAxis()];
        environmentMap = new HashMap[bounds.xAxis()][bounds.yAxis()];
    }

    /**
     * Sets the {@link TerrainProperty} at the {@link SpatialCoordinates}
     *
     * @param spatialCoordinates location
     * @param terrainProperty    the terrain property to set
     */
    @Override
    public void setTerrainProperty(final SpatialCoordinates spatialCoordinates, final TerrainProperty terrainProperty) {

        if (0 == spatialCoordinates.zAxis()) {
            //we are flat, ignore anything above the z axis
            checkCoordinates(spatialCoordinates.xAxis(), spatialCoordinates.yAxis());

            //on conflict overwrites
            environmentMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()].put(terrainProperty.getId(), terrainProperty);
        }

    }

    /**
     * Gets a {@link TerrainProperty} at the given {@link SpatialCoordinates}
     *
     * @param spatialCoordinates location
     * @param id                 the id of the property to return
     * @return
     */
    @Override
    public TerrainProperty getTerrainProperty(final SpatialCoordinates spatialCoordinates, final String id) {
        checkCoordinates(spatialCoordinates.xAxis(), spatialCoordinates.yAxis());
        return environmentMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()].get(id);
    }

    /**
     * Delete the respective property from the location
     *
     * @param spatialCoordinates location
     * @param id                 the id of the property to delete
     */
    @Override
    public void deleteTerrainProperty(final SpatialCoordinates spatialCoordinates, final String id) {
        checkCoordinates(spatialCoordinates.xAxis(), spatialCoordinates.yAxis());
        environmentMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()].remove(id);
    }

    /**
     * Get all the {@link TerrainProperty} at a specific location
     *
     * @param spatialCoordinates location
     * @return list of properties
     */
    @Override
    public List<TerrainProperty> getTerrainProperties(final SpatialCoordinates spatialCoordinates) {
        checkCoordinates(spatialCoordinates.xAxis(), spatialCoordinates.yAxis());
        return new ArrayList<>(environmentMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()].values());
    }


    /**
     * Get the organism that has a cell at the given coordinate
     *
     * @param spatialCoordinates location
     * @return an organism or null if one does not exist
     */
    @Override
    public Organism getOrganism(final SpatialCoordinates spatialCoordinates) {
        checkCoordinates(spatialCoordinates.xAxis(), spatialCoordinates.yAxis());
        if (null != organismMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()]) {
            return organismMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()].organism;
        }
        return null;
    }

    /**
     * Initialize the terrain to the given dimensions
     *
     * @param x width
     * @param y height
     * @param z depth
     */
    @Override
    public void initialize(int x, int y, int z) {
    }

    /**
     * Returns true if there is an organism at the given coordinates
     *
     * @return true if an organism exists at the location
     */
    @Override
    public boolean hasCell(final SpatialCoordinates spatialCoordinates) {
        return null != organismMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()];
    }

    /**
     * Attempt to af cell to the terrain. If there is already a cell at the same position,
     * false is returned.
     *
     * @param cell     the cell to attempt to place
     * @param organism cell's organism
     * @return true if set
     */
    @Override
    public boolean setCell(final Cell cell, final Organism organism) {
        checkCoordinates(cell.getCoordinates().xAxis(), cell.getCoordinates().yAxis());
        final MatrixCell currentCell = organismMap[cell.getCoordinates().xAxis()][cell.getCoordinates().yAxis()];
        if (null == currentCell) {
            final MatrixCell mCell = new MatrixCell();
            mCell.cell = cell;
            mCell.organism = organism;

            organismMap[cell.getCoordinates().xAxis()][cell.getCoordinates().yAxis()] = mCell;
        } else {
            throw new EvolutionException("Collision!!!");
        }
        return null == currentCell;
    }

    /**
     * Delete the cell at the given coordinates
     *
     * @param spatialCoordinates
     * @return true if cell is deleted
     */
    @Override
    public boolean deleteCell(final SpatialCoordinates spatialCoordinates, final String id) {
        checkCoordinates(spatialCoordinates.xAxis(), spatialCoordinates.yAxis());
        final MatrixCell currentCell = organismMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()];
        if( currentCell.organism.getUniqueID().equals(id)) {
            organismMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()] = null;
        } else {
            throw new RuntimeException("CRITICAL: Terrain has become corrupted!!");
        }

        return true;
    }

    /**
     * Get the cell at the given coordinates
     *
     * @param spatialCoordinates location
     * @return cell or null if one does not exist
     */
    @Override
    public Cell getCell(final SpatialCoordinates spatialCoordinates) {
        checkCoordinates(spatialCoordinates.xAxis(), spatialCoordinates.yAxis());
        if (null != organismMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()]) {
            return organismMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()].cell;
        }
        return null;
    }

    /**
     * Get the resource manager
     *
     * @return
     */
    @Override
    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    /**
     * Check if spatial coordinates are out of bounds
     *
     * @param spatialCoordinates
     * @return true if the coordinates are out of bounds
     */
    @Override
    public boolean isOutOfBounds(final SpatialCoordinates spatialCoordinates) {
        /*
         * z-axis will cause collisions because it doesn't exist, so
         * we need to change the out of bounds check to ignore it
         */

        return !(getSizeOfXAxis() > spatialCoordinates.xAxis()
                && getSizeOfYAxis() > spatialCoordinates.yAxis()
                && 0 <= spatialCoordinates.xAxis()
                && 0 <= spatialCoordinates.yAxis() );
    }


    private void checkCoordinates(final int x, final int y) {
        if (x >= getSizeOfXAxis() || y >= getSizeOfYAxis()) {
            throw new ArrayIndexOutOfBoundsException("SpatialCoordinates (" + x + "," + y
                    + ") are out of bounds for world size [" + getSizeOfXAxis() + "," + getSizeOfYAxis() + "].");
        }
    }
}
