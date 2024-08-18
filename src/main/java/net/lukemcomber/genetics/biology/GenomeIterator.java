package net.lukemcomber.genetics.biology;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import java.util.Iterator;
import java.util.LinkedList;

/**
 * An iterator over the bits in a genome
 */
public class GenomeIterator implements Iterator<Byte> {

    private int index;
    private int numBits;
    private final byte[] binaryGenome;

    /**
     * The genes in the genome and the number of bits in gene interpretation. Currently only suppors a max of 8 (1 byte)
     *
     * @param geneList     list of genes
     * @param numberOfBits dna window size into genome
     */
    protected GenomeIterator(final LinkedList<Gene> geneList, final int numberOfBits) {

        this.index = 0;
        this.numBits = numberOfBits;
        binaryGenome = Genome.toBytes(geneList);
    }

    /**
     * We'll allow you to loop over the genome forever!
     *
     * @return true
     */
    @Override
    public boolean hasNext() {
        return true;
    }

    /**
     * Get the next byte in the genome
     *
     * @return next byte
     */
    @Override
    public Byte next() {
        byte result = extractBits(binaryGenome[index], numBits);
        index = (index + 1) % binaryGenome.length;
        return result;
    }

    private byte extractBits(byte value, int numBits) {
        if (numBits < 1 || numBits > 8) {
            throw new IllegalArgumentException("Number of bits should be between 1 and 8");
        }

        int mask = (1 << numBits) - 1;
        return (byte) (value & mask);
    }
}
