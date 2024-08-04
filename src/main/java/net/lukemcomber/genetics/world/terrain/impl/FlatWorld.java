package net.lukemcomber.genetics.world.terrain.impl;

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.service.CellHelper;
import net.lukemcomber.genetics.service.GenomeSerDe;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.MetadataStoreFactory;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.world.ResourceManager;
import net.lukemcomber.genetics.world.terrain.Terrain;
import net.lukemcomber.genetics.world.terrain.TerrainProperty;
import net.lukemcomber.genetics.exception.EvolutionException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FlatWorld implements Terrain {

    public final UUID uuid;

    private static final Logger logger = Logger.getLogger(FlatWorld.class.getName());
    private final static boolean debug = false;

    public static final String ID = "FLAT_WORLD";

    private class MatrixCell {
        Cell cell;
        Organism organism;

        boolean isNotEmpty(){
            return null != cell && null != organism;
        }

    }


    private MatrixCell[][] organismMap;
    private Map<String, TerrainProperty>[][] environmentMap;
    private Map<String,Organism> population;
    private final UniverseConstants constants;
    private int worldHeight;
    private int worldWidth;
    private boolean isInitialized = false;

    private long totalOrganisms;

    private ResourceManager resourceManager;

    private final MetadataStoreGroup metadataStoreGroup;

    public FlatWorld(final UniverseConstants constants, final MetadataStoreGroup metadataStoreGroup ){
        this.constants = constants;
        uuid = UUID.randomUUID();
        this.metadataStoreGroup = metadataStoreGroup;
        totalOrganisms = 0;
    }

    public void setTerrainProperty(final SpatialCoordinates spatialCoordinates, final TerrainProperty terrainProperty) {
        checkInitialized();

        if (0 == spatialCoordinates.zAxis) {
            //we are flat, ignore anything above the z axis
            checkCoordinates(spatialCoordinates.xAxis, spatialCoordinates.yAxis);

            if (debug) {
                System.out.println(String.format("(%d,%d,%d) - Set %s to %d", spatialCoordinates.zAxis,
                        spatialCoordinates.yAxis, spatialCoordinates.zAxis, terrainProperty.getId(),
                        terrainProperty.getValue()));
            }

            //on conflict overwrites
            environmentMap[spatialCoordinates.xAxis][spatialCoordinates.yAxis].put(terrainProperty.getId(), terrainProperty);
        }

    }

    public TerrainProperty getTerrainProperty(final SpatialCoordinates spatialCoordinates, final String id) {
        checkInitialized();
        checkCoordinates(spatialCoordinates.xAxis, spatialCoordinates.yAxis);
        return environmentMap[spatialCoordinates.xAxis][spatialCoordinates.yAxis].get(id);
    }

    @Override
    public void deleteTerrainProperty(final SpatialCoordinates spatialCoordinates, final String id) {
        checkInitialized();
        checkCoordinates(spatialCoordinates.xAxis, spatialCoordinates.yAxis);
        environmentMap[spatialCoordinates.xAxis][spatialCoordinates.yAxis].remove(id);
    }

    public void setTerrain(final SpatialCoordinates spatialCoordinates, final List<TerrainProperty> propertyList) {
        checkInitialized();
        checkCoordinates(spatialCoordinates.xAxis, spatialCoordinates.yAxis);
        environmentMap[spatialCoordinates.xAxis][spatialCoordinates.yAxis] = propertyList.stream().collect(
                Collectors.toMap(TerrainProperty::getId, Function.identity()));
    }

    public List<TerrainProperty> getTerrain(final SpatialCoordinates spatialCoordinates) {
        checkInitialized();
        checkCoordinates(spatialCoordinates.xAxis, spatialCoordinates.yAxis);
        return new ArrayList<>(environmentMap[spatialCoordinates.xAxis][spatialCoordinates.yAxis].values());
    }

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
     * @return
     */
    public boolean hasCell(final SpatialCoordinates spatialCoordinates) {
        checkInitialized();
        return null != organismMap[spatialCoordinates.xAxis][spatialCoordinates.yAxis];
    }

    public boolean setCell(final Cell cell,final Organism organism) {
        checkInitialized();
        checkCoordinates(cell.getCoordinates().xAxis, cell.getCoordinates().yAxis);
        final MatrixCell currentCell = organismMap[cell.getCoordinates().xAxis][cell.getCoordinates().yAxis];
        if (null == currentCell) {
            final MatrixCell mCell = new MatrixCell();
            mCell.cell = cell;
            mCell.organism = organism;

            organismMap[cell.getCoordinates().xAxis][cell.getCoordinates().yAxis] = mCell;
            logger.info("Set cell " + cell.getCellType() + " at " + cell.getCoordinates());
        }
        return null == currentCell;
    }

    public boolean deleteCell(final SpatialCoordinates spatialCoordinates) {
        checkInitialized();
        checkCoordinates(spatialCoordinates.xAxis, spatialCoordinates.yAxis);
        final MatrixCell currentCell = organismMap[spatialCoordinates.xAxis][spatialCoordinates.yAxis];
        organismMap[spatialCoordinates.xAxis][spatialCoordinates.yAxis] = null;

        logger.info( "Deleted cell at " + spatialCoordinates + " was " + (null != currentCell));
        return null != currentCell;
    }

    @Override
    public Cell getCell(final SpatialCoordinates spatialCoordinates) {
        checkInitialized();
        checkCoordinates(spatialCoordinates.xAxis, spatialCoordinates.yAxis);
        if( null != organismMap[spatialCoordinates.xAxis][spatialCoordinates.yAxis] ){
            return organismMap[spatialCoordinates.xAxis][spatialCoordinates.yAxis].cell;
        }
        return null;
    }

    @Override
    public Organism getOrganism(SpatialCoordinates spatialCoordinates) {
        checkInitialized();
        checkCoordinates(spatialCoordinates.xAxis, spatialCoordinates.yAxis);
        if( null != organismMap[spatialCoordinates.xAxis][spatialCoordinates.yAxis] ){
            return organismMap[spatialCoordinates.xAxis][spatialCoordinates.yAxis].organism;
        }
        return null;
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
    public boolean hasOrganism(final Organism organism) {
        return null != population && population.containsKey(organism.getUniqueID());
    }

    @Override
    public ResourceManager getResourceManager() {
        checkInitialized();
        return resourceManager;
    }

    @Override
    public UniverseConstants getProperties() {
        /*
         * Does not need to be initialized() first
         */
        return constants;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public long getTotalOrganismCount() {
        return totalOrganisms;
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

    /*
     * z-axis will cause collisions because it doesn't exist, so
     * we need to change the out of bounds check to ignore it
     */
    @Override
    public boolean isOutOfBounds(final SpatialCoordinates spatialCoordinates) {
        return !(getSizeOfXAxis() > spatialCoordinates.xAxis
                && getSizeOfYAxis() > spatialCoordinates.yAxis
                && 0 <= spatialCoordinates.xAxis
                && 0 <= spatialCoordinates.yAxis);
    }

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
                    cells.forEach(c -> setCell(c,organism));
                    population.put(organism.getUniqueID(),organism);
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
     * @param organism
     * @return
     */
    public boolean deleteOrganism(final Organism organism) {
       boolean retVal = false;

       if( null != organism && population.containsKey(organism.getUniqueID())){
           CellHelper.getAllOrganismsCells(organism.getCells())
                   .forEach(cell -> {
                       deleteCell(cell.getCoordinates());
                   });
           retVal = population.remove(organism.getUniqueID()) != null;

       }

       return retVal;
    }

    @Override
    public Organism getOrganism(String oid) {
        return population.get(oid);
    }

    @Override
    public int getOrganismCount() {
        return isInitialized ? population.size() : 0;
    }

    public Iterator<Organism> getOrganisms() {
        return population.values().iterator();
    }



}
