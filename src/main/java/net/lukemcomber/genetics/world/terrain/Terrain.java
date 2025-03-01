package net.lukemcomber.genetics.world.terrain;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.biology.plant.cells.EjectedSeedCell;
import net.lukemcomber.genetics.io.CellHelper;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.world.ResourceManager;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

/**
 * A representation of the environment. The stage of life if you will.
 */
public abstract class Terrain {

    public static final String PROPERTY_TERRAIN_TYPE = "terrain.type";
    private static final Logger logger = Logger.getLogger(Terrain.class.getName());

    private final SpatialCoordinates spatialBounds;
    private final UniverseConstants constants;
    public final UUID uuid;
    private final Map<String, Organism> population;
    private final MetadataStoreGroup metadataStoreGroup;
    private long totalOrganisms;




    public Terrain(final SpatialCoordinates spatialBounds, final UniverseConstants constants, final MetadataStoreGroup store ){
        this.spatialBounds = spatialBounds;
        this.constants = constants;
        this.population = new ConcurrentSkipListMap<>();
        this.uuid = UUID.randomUUID();
        this.totalOrganisms = 0;
        this.metadataStoreGroup = store;

    }

    public abstract void clear();

    /**
     * Set a specific terrain property for the tile at coordinations of (x,y,z)
     *
     * @param terrainProperty the terrain property to set
     */
    public abstract void setTerrainProperty(final SpatialCoordinates spatialCoordinates, final TerrainProperty terrainProperty);

    /**
     * Returns the terrain property for the type/key of the supplied id at the supplied (x,y,z) spatialCoordinates
     *
     * @param id the id of the property to return
     * @return the requested property, or null if it doesn't exist or if (x,y,z) is invalid
     */
    public abstract TerrainProperty<?> getTerrainProperty(final SpatialCoordinates spatialCoordinates, final String id);

    /**
     * Removes the property keyed by the supplied id at spatialCoordinates (x,y,z). The property itself is not mutated but
     * removed from the environment completely.
     *
     * @param id the id of the property to delete
     */
    public abstract void deleteTerrainProperty(final SpatialCoordinates spatialCoordinates, final String id);

    /**
     * Get all the {@link TerrainProperty} at a specific location
     *
     * @param spatialCoordinates location
     * @return list of properties
     */
    public abstract List<TerrainProperty> getTerrainProperties(final SpatialCoordinates spatialCoordinates);
    /**
     * Initialize the terrain to the given dimensions
     *
     * @param x width
     * @param y height
     * @param z depth
     */
    public abstract void initialize(final int x, final int y, final int z);

    /**
     * Check if there is a cell at the supplied spatialCoordinates
     *
     * @return true if there is a cell at (x,y,z) otherwise false
     */
    public abstract boolean hasCell(final SpatialCoordinates spatialCoordinates);

    /**
     * Attempts to place a cell at the (x,y,z) coordinates. If a cell already exists in that space, nothing
     * is done.
     *
     * @param cell the cell to attempt to place
     * @return true if cell is successfully placed, otherwise false
     */
    public abstract boolean setCell(final Cell cell, final Organism organism);

    /**
     * Deletes a cell from the world at position (x,y,z). Die Cell Die!
     *
     * @return true if cell delete otherwise false
     */
    public abstract boolean deleteCell(final SpatialCoordinates spatialCoordinates, final String id);

    /**
     * Returns a reference to the cell at spatialCoordinates (x,y,z)
     *
     * @return the cell at position (x,y,z) or null if one doesn't exist
     */
    public abstract Cell getCell(final SpatialCoordinates spatialCoordinates);

    /**
     * Check if spatial coordinates are out of bounds
     *
     * @param spatialCoordinates
     * @return true if the coordinates are out of bounds
     */
    public boolean isOutOfBounds(final SpatialCoordinates spatialCoordinates) {
        return !(getSizeOfXAxis() > spatialCoordinates.xAxis()
                && getSizeOfYAxis() > spatialCoordinates.yAxis()
                && getSizeOfZAxis() > spatialCoordinates.zAxis()
                && 0 <= spatialCoordinates.xAxis()
                && 0 <= spatialCoordinates.yAxis()
                && 0 <= spatialCoordinates.zAxis());
    }
    /**
     * Get the size of the x-axis
     *
     * @return pixel count
     */
    public int getSizeOfXAxis(){
        return spatialBounds.xAxis();
    }

    /**
     * Get the size of the y-axis
     *
     * @return pixel count
     */
    public int getSizeOfYAxis(){
        return spatialBounds.yAxis();
    }

    /**
     * Get the size of the z-axis
     *
     * @return pixel count
     */
    public int getSizeOfZAxis(){
        return spatialBounds.zAxis();
    }

    /**
     * Attempt to add the organism to the terrain. The organism must fit and
     * not collide with other cells.
     *
     * @param organism organism to add
     * @return true if organism added
     */
    public boolean addOrganism(final Organism organism) {
        boolean retVal = false;
        if (null != organism) {
            if (!population.containsKey(organism.getUniqueID())) {
                final List<Cell> cells = CellHelper.getAllOrganismsCells(organism.getFirstCell());
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
     * Get the organism that has a cell at the given coordinate
     *
     * @param spatialCoordinates location
     * @return an organism or null if one does not exist
     */
    public abstract Organism getOrganism(final SpatialCoordinates spatialCoordinates);

    /**
     * Get the organism with the provided id
     *
     * @param oid id to lookup
     * @return an organism or null
     */
    public Organism getOrganism(final String oid) {
        return population.get(oid);
    }

    /**
     * Get count of organisms current in the terrain
     *
     * @return count
     */
    public int getOrganismCount() {
        return  population.size();
    }

    /**
     * Return an iterator to iterate over the organisms in the terrain
     *
     * @return iterator
     */
    public Iterator<Organism> getOrganisms() {
        return population.values().iterator();
    }
    /**
     * Returns true if the organism exists
     *
     * @param organism organism to lookup
     * @return true if the organism exists
     */
    public boolean hasOrganism(final Organism organism) {
        return null != population && population.containsKey(organism.getUniqueID());
    }

    /**
     * Forces a cleanup of all cells in an organism. If a cell has already been cleared, don't error but
     * continue clearing cells.
     *
     * @param organism organism to delete
     * @return true if organism is deleted
     */
    public boolean deleteOrganism(final Organism organism) {
        boolean retVal = false;

        if (null != organism && population.containsKey(organism.getUniqueID())) {
            CellHelper.getAllOrganismsCells(organism.getFirstCell())
                    .forEach(cell -> {
                        deleteCell(cell.getCoordinates(), organism.getUniqueID());
                    });
            retVal = population.remove(organism.getUniqueID()) != null;

        }

        return retVal;
    }

    /**
     * Get count of all organisms that have existed
     *
     * @return count
     */
    public long getTotalOrganismCount() {
        return totalOrganisms;
    }



    /**
     * Get the resource manager
     *
     * @return
     */
    public abstract ResourceManager getResourceManager();

    /**
     * Get the configuration properties
     *
     * @return
     */
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
    public UUID getUUID(){
        return uuid;
    }


}
