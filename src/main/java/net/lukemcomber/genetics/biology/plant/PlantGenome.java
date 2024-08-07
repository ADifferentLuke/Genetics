package net.lukemcomber.genetics.biology.plant;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Gene;
import net.lukemcomber.genetics.biology.Genome;
import net.lukemcomber.genetics.biology.plant.behavior.EjectSeed;
import net.lukemcomber.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.genetics.biology.plant.behavior.GrowRoot;
import net.lukemcomber.genetics.biology.plant.behavior.GrowSeed;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.service.GenomeSerDe;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class PlantGenome extends Genome {

    private static final Logger logger = Logger.getLogger(PlantGenome.class.getName());

    //perhaps change to array
    public static final int GENE_COUNT = 20;

    public final static byte GROW_LEAF_LEFT = 0b00000;
    public final static byte GROW_LEAF_RIGHT = 0b00001;
    public final static byte GROW_LEAF_UP = 0b00010;
    public final static byte GROW_LEAF_DOWN = 0b00011;
    public final static byte GROW_LEAF_FORWARD = 0b00100;
    public final static byte GROW_LEAF_BACK = 0b00101;

    public final static byte GROW_ROOT_LEFT = 0b00110;
    public final static byte GROW_ROOT_RIGHT = 0b00111;
    public final static byte GROW_ROOT_UP = 0b01000;
    public final static byte GROW_ROOT_DOWN = 0b01001;
    public final static byte GROW_ROOT_FORWARD = 0b01010;
    public final static byte GROW_ROOT_BACK = 0b01011;

    public final static byte GROW_SEED_LEFT = 0b01100;
    public final static byte GROW_SEED_RIGHT = 0b01101;
    public final static byte GROW_SEED_UP = 0b01110;
    public final static byte GROW_SEED_DOWN = 0b01111;
    public final static byte GROW_SEED_FORWARD = 0b10000;
    public final static byte GROW_SEED_BACK = 0b10001;
    public final static byte EJECT_SEED_LEFT = 0b10010;
    public final static byte EJECT_SEED_RIGHT = 0b10011;
    public final static byte EJECT_SEED_UP = 0b10100;
    public final static byte EJECT_SEED_DOWN = 0b10101;
    public final static byte EJECT_SEED_FORWARD = 0b10110;
    public final static byte EJECT_SEED_BACK = 0b10111;

    //private final static int numberOfBits= 5;
    public final static int numberOfBits = 8;
    private final Iterator<Byte> iterator;

    public PlantGenome(final List<Gene> genes) {
        super(genes, PlantOrganism.TYPE);
        iterator = iterator(numberOfBits);
    }

    /**
     * @return
     */
    @Override
    public PlantBehavior getNextAct() {

        final PlantBehavior plantBehavior;

        final byte action = iterator.next();

        //yuck
        switch (action) {
            case GROW_LEAF_LEFT:
                logger.info("GROW_LEAF_LEFT");
                plantBehavior = new GrowLeaf(c -> new SpatialCoordinates(c.xAxis - 1, c.yAxis, c.zAxis));
                break;
            case GROW_LEAF_RIGHT:
                logger.info("GROW_LEAF_RIGHT");
                plantBehavior = new GrowLeaf(c -> new SpatialCoordinates(c.xAxis + 1, c.yAxis, c.zAxis));
                break;
            case GROW_LEAF_UP:
                logger.info("GROW_LEAF_UP");
                plantBehavior = new GrowLeaf(c -> new SpatialCoordinates(c.xAxis, c.yAxis + 1, c.zAxis));
                break;
            case GROW_LEAF_DOWN:
                logger.info("GROW_LEAF_DOWN");
                plantBehavior = new GrowLeaf(c -> new SpatialCoordinates(c.xAxis, c.yAxis - 1, c.zAxis));
                break;
            case GROW_LEAF_FORWARD:
                logger.info("GROW_LEAF_FORWARD");
                plantBehavior = new GrowLeaf(c -> new SpatialCoordinates(c.xAxis, c.yAxis, c.zAxis + 1));
                break;
            case GROW_LEAF_BACK:
                logger.info("GROW_LEAF_BACK");
                plantBehavior = new GrowLeaf(c -> new SpatialCoordinates(c.xAxis, c.yAxis, c.zAxis - 1));
                break;
            case GROW_ROOT_LEFT:
                logger.info("GROW_ROOT_LEFT");
                plantBehavior = new GrowRoot(c -> new SpatialCoordinates(c.xAxis - 1, c.yAxis, c.zAxis));
                break;
            case GROW_ROOT_RIGHT:
                logger.info("GROW_ROOT_RIGHT");
                plantBehavior = new GrowRoot(c -> new SpatialCoordinates(c.xAxis + 1, c.yAxis, c.zAxis));
                break;
            case GROW_ROOT_UP:
                logger.info("GROW_ROOT_UP");
                plantBehavior = new GrowRoot(c -> new SpatialCoordinates(c.xAxis, c.yAxis + 1, c.zAxis));
                break;
            case GROW_ROOT_DOWN:
                logger.info("GROW_ROOT_DOWN");
                plantBehavior = new GrowRoot(c -> new SpatialCoordinates(c.xAxis, c.yAxis - 1, c.zAxis));
                break;
            case GROW_ROOT_FORWARD:
                logger.info("GROW_ROOT_FORWARD");
                plantBehavior = new GrowRoot(c -> new SpatialCoordinates(c.xAxis, c.yAxis, c.zAxis + 1));
                break;
            case GROW_ROOT_BACK:
                logger.info("GROW_ROOT_BACK");
                plantBehavior = new GrowRoot(c -> new SpatialCoordinates(c.xAxis, c.yAxis, c.zAxis - 1));
                break;
            case GROW_SEED_LEFT:
                logger.info("GROW_SEED_LEFT");
                plantBehavior = new GrowSeed(c -> new SpatialCoordinates(c.xAxis - 1, c.yAxis, c.zAxis));
                break;
            case GROW_SEED_RIGHT:
                logger.info("GROW_SEED_RIGHT");
                plantBehavior = new GrowSeed(c -> new SpatialCoordinates(c.xAxis + 1, c.yAxis, c.zAxis));
                break;
            case GROW_SEED_UP:
                logger.info("GROW_SEED_UP");
                plantBehavior = new GrowSeed(c -> new SpatialCoordinates(c.xAxis, c.yAxis + 1, c.zAxis));
                break;
            case GROW_SEED_DOWN:
                logger.info("GROW_SEED_DOWN");
                plantBehavior = new GrowSeed(c -> new SpatialCoordinates(c.xAxis, c.yAxis - 1, c.zAxis));
                break;
            case GROW_SEED_FORWARD:
                logger.info("GROW_SEED_FORWARD");
                plantBehavior = new GrowSeed(c -> new SpatialCoordinates(c.xAxis, c.yAxis, c.zAxis + 1));
                break;
            case GROW_SEED_BACK:
                logger.info("GROW_SEED_BACK");
                plantBehavior = new GrowSeed(c -> new SpatialCoordinates(c.xAxis, c.yAxis, c.zAxis - 1));
                break;
            case EJECT_SEED_LEFT:
                logger.info("EJECT_SEED_LEFT");
                plantBehavior = new EjectSeed(c -> new SpatialCoordinates(c.xAxis - 1, c.yAxis, c.zAxis));
                break;
            case EJECT_SEED_RIGHT:
                logger.info("EJECT_SEED_RIGHT");
                plantBehavior = new EjectSeed(c -> new SpatialCoordinates(c.xAxis + 1, c.yAxis, c.zAxis));
                break;
            case EJECT_SEED_UP:
                logger.info("EJECT_SEED_UP");
                plantBehavior = new EjectSeed(c -> new SpatialCoordinates(c.xAxis, c.yAxis + 1, c.zAxis));
                break;
            case EJECT_SEED_DOWN:
                logger.info("EJECT_SEED_UP");
                plantBehavior = new EjectSeed(c -> new SpatialCoordinates(c.xAxis, c.yAxis - 1, c.zAxis));
                break;
            case EJECT_SEED_FORWARD:
                logger.info("EJECT_SEED_FORWARD");
                plantBehavior = new EjectSeed(c -> new SpatialCoordinates(c.xAxis, c.yAxis, c.zAxis + 1));
                break;
            case EJECT_SEED_BACK:
                logger.info("EJECT_SEED_BACK");
                plantBehavior = new EjectSeed(c -> new SpatialCoordinates(c.xAxis, c.yAxis, c.zAxis - 1));
                break;
            default:
                logger.info("Junk DNA: " + action);
                return null;
        }
        return plantBehavior;
    }

    @Override
    public Genome clone() {
        final List<Gene> geneCopy = new LinkedList<>();
        for( int i =0; i < getNumberOfGenes(); ++i ) {
            geneCopy.add(new Gene(getGeneNumber(i)));
        }
        return new PlantGenome( geneCopy );
    }
}
