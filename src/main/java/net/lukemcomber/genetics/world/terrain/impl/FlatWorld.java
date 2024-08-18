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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A 2-dimensional implementation of {@link Terrain}
 */
public class FlatWorld implements Terrain {

    private static final Logger logger = Logger.getLogger(FlatWorld.class.getName());

    public static final String ID = "FLAT_WORLD";
    public final UUID uuid;


    private class MatrixCell {
        Cell cell;
        Organism organism;

        boolean isNotEmpty() {
            return null != cell && null != organism;
        }

    }

    private final static boolean debug = false;
    private MatrixCell[][] organismMap;
    private Map<String, TerrainProperty>[][] environmentMap;
    private Map<String, Organism> population;
    private final UniverseConstants constants;
    private int worldHeight;
    private int worldWidth;
    private boolean isInitialized = false;
    private long totalOrganisms;
    private ResourceManager resourceManager;
    private final MetadataStoreGroup metadataStoreGroup;

    /**
     * Create a new instance from the given configuration properties and a metadata store group
     *
     * @param constants          configuration propertied
     * @param metadataStoreGroup metadata store group
     */
    public FlatWorld(final UniverseConstants constants, final MetadataStoreGroup metadataStoreGroup) {
        this.constants = constants;
        uuid = UUID.randomUUID();
        this.metadataStoreGroup = metadataStoreGroup;
        totalOrganisms = 0;
    }

    /**
     * Sets the {@link TerrainProperty} at the {@link SpatialCoordinates}
     *
     * @param spatialCoordinates location
     * @param terrainProperty    the terrain property to set
     */
    @Override
    public void setTerrainProperty(final SpatialCoordinates spatialCoordinates, final TerrainProperty terrainProperty) {
        checkInitialized();

        if (0 == spatialCoordinates.zAxis()) {
            //we are flat, ignore anything above the z axis
            checkCoordinates(spatialCoordinates.xAxis(), spatialCoordinates.yAxis());

            if (debug) {
                System.out.println(String.format("(%d,%d,%d) - Set %s to %d", spatialCoordinates.xAxis(),
                        spatialCoordinates.yAxis(), spatialCoordinates.zAxis(), terrainProperty.getId(),
                        terrainProperty.getValue()));
            }

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
        checkInitialized();
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
        checkInitialized();
        checkCoordinates(spatialCoordinates.xAxis(), spatialCoordinates.yAxis());
        environmentMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()].remove(id);
    }

    /**
     * Sets a list of {@link TerrainProperty} at a specific coordinate
     *
     * @param spatialCoordinates location
     * @param propertyList       the list of properties to set in the environment
     */
    @Override
    public void setTerrain(final SpatialCoordinates spatialCoordinates, final List<TerrainProperty> propertyList) {
        checkInitialized();
        checkCoordinates(spatialCoordinates.xAxis(), spatialCoordinates.yAxis());
        environmentMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()] = propertyList.stream().collect(
                Collectors.toMap(TerrainProperty::getId, Function.identity()));
    }

    /**
     * Get all the {@link TerrainProperty} at a specific location
     *
     * @param spatialCoordinates location
     * @return list of properties
     */
    @Override
    public List<TerrainProperty> getTerrain(final SpatialCoordinates spatialCoordinates) {
        checkInitialized();
        checkCoordinates(spatialCoordinates.xAxis(), spatialCoordinates.yAxis());
        return new ArrayList<>(environmentMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()].values());
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
        worldHeight = y;
        worldWidth = x;
        organismMap = new MatrixCell[x][y];
        environmentMap = new HashMap[x][y];
        /*
         * Because we only allow iterator access to the organism data structure and the
         * organisms themselves can remove themselves from terrain, we have a concurrency problem.
         *
         * Dirty fix is to use a Concurrent map, but does not solve the underlying design problem
         * that essentially guarantees concurrent access in a single thread context. Bad design maybe?
         *
         * TODO think more on this
         */
        population = new ConcurrentHashMap<>();

        //we are at load time, spend extra time now initializing and less time later overall
        for (int i = 0; i < x; ++i) {
            for (int j = 0; j < y; ++j) {
                environmentMap[i][j] = new HashMap<>();
            }
        }
        logger.info(String.format("World %s initialized to (%d,%d,%d).", ID, x, y, z));

        isInitialized = true;
        resourceManager = new FlatWorldResourceManager(this, constants);
    }

    /**
     * Returns true if there is an organism at the given coordinates
     *
     * @return true if an organism exists at the location
     */
    @Override
    public boolean hasCell(final SpatialCoordinates spatialCoordinates) {
        checkInitialized();
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
        checkInitialized();
        checkCoordinates(cell.getCoordinates().xAxis(), cell.getCoordinates().yAxis());
        final MatrixCell currentCell = organismMap[cell.getCoordinates().xAxis()][cell.getCoordinates().yAxis()];
        if (null == currentCell) {
            final MatrixCell mCell = new MatrixCell();
            mCell.cell = cell;
            mCell.organism = organism;

            organismMap[cell.getCoordinates().xAxis()][cell.getCoordinates().yAxis()] = mCell;
            logger.info("Set cell " + cell.getCellType() + " at " + cell.getCoordinates());
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
    public boolean deleteCell(final SpatialCoordinates spatialCoordinates) {
        checkInitialized();
        checkCoordinates(spatialCoordinates.xAxis(), spatialCoordinates.yAxis());
        final MatrixCell currentCell = organismMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()];
        organismMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()] = null;

        logger.info("Deleted cell at " + spatialCoordinates + " was " + (null != currentCell));
        return null != currentCell;
    }

    /**
     * Get the cell at the given coordinates
     *
     * @param spatialCoordinates location
     * @return cell or null if one does not exist
     */
    @Override
    public Cell getCell(final SpatialCoordinates spatialCoordinates) {
        checkInitialized();
        checkCoordinates(spatialCoordinates.xAxis(), spatialCoordinates.yAxis());
        if (null != organismMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()]) {
            return organismMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()].cell;
        }
        return null;
    }

    /**
     * Get the organism that has a cell at the given coordinate
     *
     * @param spatialCoordinates location
     * @return an organism or null if one does not exist
     */
    @Override
    public Organism getOrganism(final SpatialCoordinates spatialCoordinates) {
        checkInitialized();
        checkCoordinates(spatialCoordinates.xAxis(), spatialCoordinates.yAxis());
        if (null != organismMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()]) {
            return organismMap[spatialCoordinates.xAxis()][spatialCoordinates.yAxis()].organism;
        }
        return null;
    }

    /**
     * Get the size of the x-axis
     *
     * @return pixel count
     */
    @Override
    public int getSizeOfXAxis() {
        return worldWidth;
    }


    /**
     * Get the size of the y-axis
     *
     * @return pixel count
     */
    @Override
    public int getSizeOfYAxis() {
        return worldHeight;
    }

    /**
     * Get the size of the z-axis
     *
     * @return 0
     */
    @Override
    public int getSizeOfZAxis() {
        return 0;
    }

    /**
     * Returns true if the organism exists
     *
     * @param organism organism to lookup
     * @return true if the organism exists
     */
    @Override
    public boolean hasOrganism(final Organism organism) {
        return null != population && population.containsKey(organism.getUniqueID());
    }

    /**
     * Get the resource manager
     *
     * @return
     */
    @Override
    public ResourceManager getResourceManager() {
        checkInitialized();
        return resourceManager;
    }

    /**
     * Get the configuration properties
     *
     * @return
     */
    @Override
    public UniverseConstants getProperties() {
        /*
         * Does not need to be initialized() first
         */
        return constants;
    }

    /**
     * Returns the instances unique id
     *
     * @return uuid
     */
    @Override
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Get count of all organisms that have existed
     *
     * @return count
     */
    @Override
    public long getTotalOrganismCount() {
        return totalOrganisms;
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
                && 0 <= spatialCoordinates.yAxis());
    }

    /**
     * Attempt to add the organism to the terrain. The organism must fit and
     * not collide with other cells.
     *
     * @param organism organism to add
     * @return true if organism added
     */
    @Override
    public boolean addOrganism(final Organism organism) {
        boolean retVal = false;
        if (null != organism) {
            if (!population.containsKey(organism.getUniqueID())) {
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
                    cells.forEach(c -> setCell(c, organism));
                    population.put(organism.getUniqueID(), organism);
                    retVal = true;
                    totalOrganisms++;

                } else {
                    throw new RuntimeException("Failed to create terrain. Organisms physically conflict.");
                }
            }
        }
        return retVal;
    }

    /**
     * Forces a cleanup of all cells in an organism. If a cell has already been cleared, don't error but
     * continue clearing cells.
     *
     * @param organism organism to delete
     * @return true if organism is deleted
     */
    @Override
    public boolean deleteOrganism(final Organism organism) {
        boolean retVal = false;

        if (null != organism && population.containsKey(organism.getUniqueID())) {
            CellHelper.getAllOrganismsCells(organism.getCells())
                    .forEach(cell -> {
                        deleteCell(cell.getCoordinates());
                    });
            retVal = population.remove(organism.getUniqueID()) != null;

        }

        return retVal;
    }

    /**
     * Get the organism with the provided id
     *
     * @param oid id to lookup
     * @return an organism or null
     */
    @Override
    public Organism getOrganism(final String oid) {
        return population.get(oid);
    }

    /**
     * Get count of organisms current in the terrain
     *
     * @return count
     */
    @Override
    public int getOrganismCount() {
        return isInitialized ? population.size() : 0;
    }

    /**
     * Return an iterator to iterate over the organisms in the terrain
     *
     * @return iterator
     */
    @Override
    public Iterator<Organism> getOrganisms() {
        return population.values().iterator();
    }

    private void checkCoordinates(final int x, final int y) {
        if (x >= worldWidth || y >= worldHeight) {
            throw new ArrayIndexOutOfBoundsException("SpatialCoordinates (" + x + "," + y
                    + ") are out of bounds for world size [" + worldWidth + "," + worldHeight + "].");
        }
    }

    private void checkInitialized() {
        if (!isInitialized) {
            throw new EvolutionException("FlatWorld has not yet been initialized.");
        }
    }

}
