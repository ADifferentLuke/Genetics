package net.lukemcomber.genetics.world.terrain;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.world.ResourceManager;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * A representation of the environment. The stage of life if you will.
 */
public interface Terrain {

    String PROPERTY_TERRAIN_TYPE = "terrain.type";

    /**
     * Set a specific terrain property for the tile at coordinations of (x,y,z)
     *
     * @param terrainProperty the terrain property to set
     */
    void setTerrainProperty(final SpatialCoordinates spatialCoordinates, final TerrainProperty terrainProperty);

    /**
     * Returns the terrain property for the type/key of the supplied id at the supplied (x,y,z) spatialCoordinates
     *
     * @param id the id of the property to return
     * @return the requested property, or null if it doesn't exist or if (x,y,z) is invalid
     */
    TerrainProperty getTerrainProperty(final SpatialCoordinates spatialCoordinates, final String id);

    /**
     * Removes the property keyed by the supplied id at spatialCoordinates (x,y,z). The property itself is not mutated but
     * removed from the environment completely.
     *
     * @param id the id of the property to delete
     */
    void deleteTerrainProperty(final SpatialCoordinates spatialCoordinates, final String id);

    /**
     * Check if spatial coordinates are out of bounds
     *
     * @param spatialCoordinates
     * @return true if the coordinates are out of bounds
     */
    default boolean isOutOfBounds(final SpatialCoordinates spatialCoordinates) {
        return !(getSizeOfXAxis() > spatialCoordinates.xAxis()
                && getSizeOfYAxis() > spatialCoordinates.yAxis()
                && getSizeOfZAxis() > spatialCoordinates.zAxis()
                && 0 <= spatialCoordinates.xAxis()
                && 0 <= spatialCoordinates.yAxis()
                && 0 <= spatialCoordinates.zAxis());
    }

    /**
     * Sets all terrain properties to the values supplied in the arguments. Any previous properties for the
     * respective (x,y,z) spatialCoordinates are overwritten or deleted.
     *
     * @param propertyList the list of properties to set in the environment
     */
    void setTerrain(final SpatialCoordinates spatialCoordinates, final List<TerrainProperty> propertyList);


    /**
     * Returns a full list of all terrain properties for the requested (x,y,z) spatialCoordinates
     *
     * @return list of all terrain properties
     */
    List<TerrainProperty> getTerrain(final SpatialCoordinates spatialCoordinates);

    /**
     * Initialize the terrain to the given dimensions
     *
     * @param x width
     * @param y height
     * @param z depth
     */
    void initialize(final int x, final int y, final int z);

    /**
     * Check if there is a cell at the supplied spatialCoordinates
     *
     * @return true if there is a cell at (x,y,z) otherwise false
     */
    boolean hasCell(final SpatialCoordinates spatialCoordinates);

    /**
     * Attempts to place a cell at the (x,y,z) coordinates. If a cell already exists in that space, nothing
     * is done.
     *
     * @param cell the cell to attempt to place
     * @return true if cell is successfully placed, otherwise false
     */
    boolean setCell(final Cell cell, final Organism organism);

    /**
     * Deletes a cell from the world at position (x,y,z). Die Cell Die!
     *
     * @return true if cell delete otherwise false
     */
    boolean deleteCell(final SpatialCoordinates spatialCoordinates);

    /**
     * Returns a reference to the cell at spatialCoordinates (x,y,z)
     *
     * @return the cell at position (x,y,z) or null if one doesn't exist
     */
    Cell getCell(final SpatialCoordinates spatialCoordinates);


    /**
     * Get the size of the x-axis
     *
     * @return pixel count
     */
    int getSizeOfXAxis();

    /**
     * Get the size of the y-axis
     *
     * @return pixel count
     */
    int getSizeOfYAxis();

    /**
     * Get the size of the z-axis
     *
     * @return pixel count
     */
    int getSizeOfZAxis();

    /**
     * Returns true iff the organism can fit, there are no cell collisions,
     * and the organism is added to the environment.
     *
     * @param organism organism to add
     * @return true if added
     */
    boolean addOrganism(final Organism organism);

    /**
     * Get the organism that has a cell at the given coordinate
     *
     * @param spatialCoordinates location
     * @return an organism or null if one does not exist
     */
    Organism getOrganism(final SpatialCoordinates spatialCoordinates);

    /**
     * Forces a cleanup of all cells in an organism. If a cell has already been cleared, don't error but
     * continue clearing cells.
     *
     * @param organism organism to delete
     * @return true if organism is deleted
     */
    boolean deleteOrganism(final Organism organism);

    /**
     * Get the organism with the provided id
     *
     * @param oid id to lookup
     * @return an organism or null
     */
    Organism getOrganism(final String oid);

    /**
     * Get count of organisms current in the terrain
     *
     * @return count
     */
    int getOrganismCount();

    /**
     * Return an iterator to iterate over the organisms in the terrain
     *
     * @return iterator
     */
    Iterator<Organism> getOrganisms();

    /**
     * Returns true if the organism exists
     *
     * @param organism organism to lookup
     * @return true if the organism exists
     */
    boolean hasOrganism(final Organism organism);

    /**
     * Get count of all organisms that have existed
     *
     * @return count
     */
    long getTotalOrganismCount();


    /**
     * Get the resource manager
     *
     * @return
     */
    ResourceManager getResourceManager();

    /**
     * Get the configuration properties
     *
     * @return
     */
    UniverseConstants getProperties();

    /**
     * Returns the instances unique id
     *
     * @return uuid
     */
    UUID getUUID();


}
