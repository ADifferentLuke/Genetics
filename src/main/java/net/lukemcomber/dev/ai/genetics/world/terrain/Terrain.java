package net.lukemcomber.dev.ai.genetics.world.terrain;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;

import java.util.List;

/**
 * A representation of the environment. The stage of life if you will.
 */
public interface Terrain {

    /**
     * Set a specific terrain property for the tile at coordinations of (x,y,z)
     * @param x the x axis coordinate
     * @param y the y axis coordinate
     * @param z the z axis coordinate
     * @param terrainProperty the terrain property to set
     */
    void setTerrainProperty(final Coordinates coordinates, final TerrainProperty terrainProperty );

    /**
     * Returns the terrain property for the type/key of the supplied id at the supplied (x,y,z) coordinates
     * @param x the x axis coordinate
     * @param y the y axis coordinate
     * @param z the z axis coordinate
     * @param id the id of the property to return
     * @return the requested property, or null if it doesn't exist or if (x,y,z) is invalid
     */
    TerrainProperty getTerrainProperty(final Coordinates coordinates, final String id );

    /**
     * Removes the property keyed by the supplied id at coordinates (x,y,z). The property itself is not mutated but
     * removed from the environment completely.
     * @param x the x axis coordinate
     * @param y the y axis coordinate
     * @param z the z axit coordinate
     * @param id the id of the property to delete
     */
    void deleteTerrainProperty( final Coordinates coordinates, final String id );

    /**
     * Sets all terrain properties to the values supplied in the arguments. Any previous properties for the
     * respective (x,y,z) coordinates are overwritten or deleted.
     * @param x the x axis coordinate
     * @param y the y axis coordinate
     * @param z the z axis coordinate
     * @param propertyList the list of properties to set in the environment
     */
    void setTerrain(final Coordinates coordinates, final List<TerrainProperty> propertyList);


    /**
     * Returns a full list of all terrain properties for the requested (x,y,z) coordinates
     * @param x the x axis coordinate
     * @param y the y axis coordinate
     * @param z the z axis coordinate
     * @return list of all terrain properties
     */
    List<TerrainProperty> getTerrain(final Coordinates coordinates );

    void initialize( final int x, final int y, final int z );

    /**
     * Check if there is a cell at the supplied coordinates
     * @param x the x axis coordinate
     * @param y the y axis coordinate
     * @param z the z axis coordinate
     * @return true if there is a cell at (x,y,z) otherwise false
     */
    boolean hasCell( final Coordinates coordinates );

    /**
     * Attempts to place a cell at the (x,y,z) coordinates. If a cell already exists in that space, nothing
     * is done.
     * @param x the x axis coordinate
     * @param y the y axis coordinate
     * @param z the z axis coordinate
     * @param cell the cell to attempt to place
     * @return true if cell is successfully placed, otherwise false
     */
    boolean setCell( final Cell cell );

    /**
     * Deletes a cell from the world at position (x,y,z). Die Cell Die!
     * @param x the x axis coordinate
     * @param y the y axis coordinate
     * @param z the z axis coordinate
     * @return true if cell delete otherwise false
     */
    boolean deleteCell( final Coordinates coordinates );

    /**
     * Returns a reference to the cell at coordinates (x,y,z)
     * @param x the x axis coordinate
     * @param y the y axis coordinate
     * @param z the z axis coordinate
     * @return the cell at position (x,y,z) or null if one doesn't exist
     */
    Cell getCell( final Coordinates coordinates);

    int getSizeOfXAxis();
    int getSizeOfYAxis();
    int getSizeOfZAxis();

}
