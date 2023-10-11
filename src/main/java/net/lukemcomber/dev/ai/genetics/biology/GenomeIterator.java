package net.lukemcomber.dev.ai.genetics.biology;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Stream;

public class GenomeIterator implements Iterator<Byte> {

    private int index;
    private int numBits;
    private final byte[] binaryGenome;

    protected GenomeIterator(final LinkedList<Gene> geneList, final int numberOfBits) {

        this.index = 0;
        this.numBits = numberOfBits;

        final int[] byteArray = geneList.stream()
                .flatMap(gene -> Stream.of(gene.nucleotideA, gene.nucleotideB, gene.nucleotideC, gene.nucleotideD))
                .mapToInt(Byte::intValue)
                .map(value -> (byte) value)
                .toArray();

        binaryGenome = new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            binaryGenome[i] = (byte) byteArray[i];
        }

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
