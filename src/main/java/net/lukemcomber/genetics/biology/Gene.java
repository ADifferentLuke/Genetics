package net.lukemcomber.genetics.biology;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.exception.EvolutionException;

import java.nio.ByteBuffer;

public final class Gene {

    public byte nucleotideA;
    public byte nucleotideB;
    public byte nucleotideC;
    public byte nucleotideD;

    public Gene() {
        nucleotideA = 0;
        nucleotideB = 0;
        nucleotideC = 0;
        nucleotideD = 0;
    }

    public Gene(final byte nucleotideA, final byte nucleotideB, final byte nucleotideC, final byte nucleotideD) {
        this.nucleotideD = nucleotideD;
        this.nucleotideC = nucleotideC;
        this.nucleotideB = nucleotideB;
        this.nucleotideA = nucleotideA;
    }

    public Gene( final Gene gene ){
        this.nucleotideD = gene.nucleotideD;
        this.nucleotideC = gene.nucleotideC;
        this.nucleotideB = gene.nucleotideB;
        this.nucleotideA = gene.nucleotideA;
    }
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(nucleotideA);
        buffer.put(nucleotideB);
        buffer.put(nucleotideC);
        buffer.put(nucleotideD);
        return buffer.array();
    }

    public static Gene fromBytes(final byte[] nucleotides) {
        final Gene retVal = new Gene();
        if (null != nucleotides && 4 == nucleotides.length) {
            retVal.nucleotideA = nucleotides[0];
            retVal.nucleotideB = nucleotides[1];
            retVal.nucleotideC = nucleotides[2];
            retVal.nucleotideD = nucleotides[3];
        } else {
            throw new EvolutionException("Corrupt Gene.");
        }
        return retVal;
    }

    public String toString() {
        return String.format("[%d,%d,%d,%d]", nucleotideA, nucleotideB, nucleotideC, nucleotideD);
    }
}
