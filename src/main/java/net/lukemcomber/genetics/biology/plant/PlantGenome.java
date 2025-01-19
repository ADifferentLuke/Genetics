package net.lukemcomber.genetics.biology.plant;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Gene;
import net.lukemcomber.genetics.biology.Genome;
import net.lukemcomber.genetics.biology.plant.behavior.*;
import net.lukemcomber.genetics.model.SpatialCoordinates;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import static net.lukemcomber.genetics.biology.Genome.SpatialTransformation.*;

/**
 * Provides genome interpretation and expression. This class binds the gene bit values
 * to actionable behavior. It will loop iterate over 0&gt;N&lt;9 bits over the genome.
 */
public class PlantGenome extends Genome {

    private static final Logger logger = Logger.getLogger(PlantGenome.class.getName());

    /**
     * The enum that binds the raw binary values to gene expressions for cells
     */
    public enum GeneExpression {
        GROW_LEAF_LEFT((byte)  /*   */ 0b00000, GrowLeaf.class, LEFT),
        GROW_LEAF_RIGHT((byte) /*   */ 0b00001, GrowLeaf.class, RIGHT),
        GROW_LEAF_UP((byte) /*      */ 0b00010, NoOp.class, IDENTITY),
        GROW_LEAF_DOWN((byte) /*    */ 0b00011, NoOp.class, IDENTITY),
        GROW_LEAF_FORWARD((byte) /* */ 0b00100, GrowLeaf.class, FORWARD),
        GROW_LEAF_BACK((byte) /*    */ 0b00101, GrowLeaf.class, BACK),
        GROW_ROOT_LEFT((byte) /*    */ 0b00110, GrowRoot.class, LEFT),
        GROW_ROOT_RIGHT((byte) /*   */ 0b00111, GrowRoot.class, RIGHT),
        GROW_ROOT_UP((byte)    /*   */ 0b01000, NoOp.class, IDENTITY),
        GROW_ROOT_DOWN((byte)  /*   */ 0b01001, NoOp.class, IDENTITY),
        GROW_ROOT_FORWARD((byte) /* */ 0b01010, GrowRoot.class, FORWARD),
        GROW_ROOT_BACK((byte) /*    */ 0b01011, GrowRoot.class, BACK),
        GROW_SEED_LEFT((byte) /*    */ 0b01100, GrowSeed.class, LEFT),
        GROW_SEED_RIGHT((byte) /*   */ 0b01101, GrowSeed.class, RIGHT),
        GROW_SEED_UP((byte) /*      */ 0b01110, NoOp.class, IDENTITY),
        GROW_SEED_DOWN((byte) /*    */ 0b01111, NoOp.class, IDENTITY),
        GROW_SEED_FORWARD((byte) /* */ 0b10000, GrowSeed.class, FORWARD),
        GROW_SEED_BACK((byte) /*    */ 0b10001, GrowSeed.class, BACK),
        EJECT_SEED_LEFT((byte) /*   */ 0b10010, EjectSeed.class, LEFT),
        EJECT_SEED_RIGHT((byte) /*  */ 0b10011, EjectSeed.class, RIGHT),
        EJECT_SEED_UP((byte) /*     */ 0b10100, NoOp.class, IDENTITY),
        EJECT_SEED_DOWN((byte) /*   */ 0b10101, NoOp.class, IDENTITY),
        EJECT_SEED_FORWARD((byte) /**/ 0b10110, EjectSeed.class, FORWARD),
        EJECT_SEED_BACK((byte) /*   */ 0b10111, EjectSeed.class, BACK);

        private final Function<SpatialCoordinates, SpatialCoordinates> spatialConversionFunction;
        private final byte value;
        private final Class<? extends PlantBehavior> klass;

        GeneExpression(final byte value, final Class<? extends PlantBehavior> klass,
                       final Genome.SpatialTransformation transformation) {
            this.spatialConversionFunction = transformation.value();
            this.value = value;
            this.klass = klass;
        }

        private PlantBehavior behavior() {

            final Constructor<? extends PlantBehavior> constructor;
            try {
                constructor = klass
                        .getDeclaredConstructor(Function.class );
                return constructor.newInstance(spatialConversionFunction);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Returns the raw binary value of this gene expression
         *
         * @return a byte with bits set for gene
         */
        public byte value() {
            return value;
        }

        /**
         * Generates a new actionable behavior given the raw binary value
         *
         * @param b raw binary value of gene
         * @return an actionable behavior object or null if there's no mapping
         */
        public static PlantBehavior express(final byte b) {
            final int expressionIndex = b & 0xFF; // we're unsigned
            if (expressionIndex < lookupTable.length) {

                /*
                 * We are lucky. Our genome is a contiguous value beginning with 0. As a result,
                 *   we can use it as an array index.
                 */
                final GeneExpression geneExpression = lookupTable[expressionIndex];
                if (expressionIndex != geneExpression.value) {
                    throw new RuntimeException("HOW????");
                } else {
                    return geneExpression.behavior();
                }
            } else {
                logger.info("Junk DNA: " + b);
            }
            return null;
        }

        private static final GeneExpression[] lookupTable = GeneExpression.values();
    }

    public final static int numberOfBits = 8;
    private final Iterator<Byte> iterator;

    /**
     * Builds a new genome for expression from the list of given genes
     *
     * @param genes genes to use
     */
    public PlantGenome(final List<Gene> genes) {
        super(genes, PlantOrganism.TYPE);
        iterator = iterator(numberOfBits);
    }

    /**
     * Build and return an actionable behavior from the next gene in the genome
     *
     * @return a behavior object or null
     */
    @Override
    public PlantBehavior getNextAct() {


        final byte action = iterator.next();
        final PlantBehavior plantBehavior = GeneExpression.express(action);
        return plantBehavior;
    }

    /**
     * Provide a deep clone of the current genome
     *
     * @return genome clone
     */
    @Override
    public Genome clone() {
        final List<Gene> geneCopy = new LinkedList<>();
        for (int i = 0; i < getNumberOfGenes(); ++i) {
            geneCopy.add(new Gene(getGeneNumber(i)));
        }
        return new PlantGenome(geneCopy);
    }
}
