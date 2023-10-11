package net.lukemcomber.dev.ai.genetics.biology.plant;

import net.lukemcomber.dev.ai.genetics.biology.Gene;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowLeaf;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowRoot;
import net.lukemcomber.dev.ai.genetics.biology.plant.behavior.GrowSeed;
import net.lukemcomber.dev.ai.genetics.model.Coordinates;

import java.util.Iterator;
import java.util.List;

public class PlantGenome extends Genome {

    //perhaps change to array
    public static final int GENE_COUNT = 20;

    private final static byte GROW_LEAF_LEFT =      0b00000;
    private final static byte GROW_LEAF_RIGHT =     0b00001;
    private final static byte GROW_LEAF_UP =        0b00010;
    private final static byte GROW_LEAF_DOWN =      0b00011;
    private final static byte GROW_LEAF_FORWARD =   0b00100;
    private final static byte GROW_LEAF_BACK =      0b00101;

    private final static byte GROW_ROOT_LEFT =      0b00110;
    private final static byte GROW_ROOT_RIGHT =     0b00111;
    private final static byte GROW_ROOT_UP =        0b01000;
    private final static byte GROW_ROOT_DOWN =      0b01001;
    private final static byte GROW_ROOT_FORWARD =   0b01010;
    private final static byte GROW_ROOT_BACK =      0b01011;

    private final static byte GROW_SEED_LEFT =      0b01100;
    private final static byte GROW_SEED_RIGHT =     0b01101;
    private final static byte GROW_SEED_UP =        0b01110;
    private final static byte GROW_SEED_DOWN =      0b01111;
    private final static byte GROW_SEED_FORWARD =   0b10000;
    private final static byte GROW_SEED_BACK =      0b10001;

    private final static int numberOfBits= 5;
    private final Iterator<Byte> iterator;

    public PlantGenome(final List<Gene> genes) {
        super(genes);
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
                plantBehavior = new GrowLeaf(c -> new Coordinates(c.xAxis - 1, c.yAxis, c.zAxis));
                break;
            case GROW_LEAF_RIGHT:
                plantBehavior = new GrowLeaf(c -> new Coordinates(c.xAxis + 1, c.yAxis, c.zAxis));
                break;
            case GROW_LEAF_UP:
                plantBehavior = new GrowLeaf(c -> new Coordinates(c.xAxis, c.yAxis + 1, c.zAxis));
                break;
            case GROW_LEAF_DOWN:
                plantBehavior = new GrowLeaf(c -> new Coordinates(c.xAxis, c.yAxis - 1, c.zAxis));
                break;
            case GROW_LEAF_FORWARD:
                plantBehavior = new GrowLeaf(c -> new Coordinates(c.xAxis, c.yAxis, c.zAxis + 1));
                break;
            case GROW_LEAF_BACK:
                plantBehavior = new GrowLeaf(c -> new Coordinates(c.xAxis, c.yAxis, c.zAxis - 1));
                break;
            case GROW_ROOT_LEFT:
                plantBehavior = new GrowRoot(c -> new Coordinates(c.xAxis - 1, c.yAxis, c.zAxis));
                break;
            case GROW_ROOT_RIGHT:
                plantBehavior = new GrowRoot(c -> new Coordinates(c.xAxis + 1, c.yAxis, c.zAxis));
                break;
            case GROW_ROOT_UP:
                plantBehavior = new GrowRoot(c -> new Coordinates(c.xAxis, c.yAxis + 1, c.zAxis));
                break;
            case GROW_ROOT_DOWN:
                plantBehavior = new GrowRoot(c -> new Coordinates(c.xAxis, c.yAxis - 1, c.zAxis));
                break;
            case GROW_ROOT_FORWARD:
                plantBehavior = new GrowRoot(c -> new Coordinates(c.xAxis, c.yAxis, c.zAxis + 1));
                break;
            case GROW_ROOT_BACK:
                plantBehavior = new GrowRoot(c -> new Coordinates(c.xAxis, c.yAxis, c.zAxis - 1));
                break;
            case GROW_SEED_LEFT:
                plantBehavior = new GrowSeed(c -> new Coordinates(c.xAxis - 1, c.yAxis, c.zAxis));
                break;
            case GROW_SEED_RIGHT:
                plantBehavior = new GrowSeed(c -> new Coordinates(c.xAxis + 1, c.yAxis, c.zAxis));
                break;
            case GROW_SEED_UP:
                plantBehavior = new GrowSeed(c -> new Coordinates(c.xAxis, c.yAxis + 1, c.zAxis));
                break;
            case GROW_SEED_DOWN:
                plantBehavior = new GrowSeed(c -> new Coordinates(c.xAxis, c.yAxis - 1, c.zAxis));
                break;
            case GROW_SEED_FORWARD:
                plantBehavior = new GrowSeed(c -> new Coordinates(c.xAxis, c.yAxis, c.zAxis + 1));
                break;
            case GROW_SEED_BACK:
                plantBehavior = new GrowSeed(c -> new Coordinates(c.xAxis, c.yAxis, c.zAxis - 1));
                break;
            default:
                System.out.println( "No mapping: " + action);
                return null;
        }
        return plantBehavior;
    }
}
