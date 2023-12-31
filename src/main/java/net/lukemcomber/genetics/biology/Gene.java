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

    public byte[] toBytes(){
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(nucleotideA);
        buffer.put(nucleotideB);
        buffer.put(nucleotideC);
        buffer.put(nucleotideD);
        return buffer.array();
    }
    public static Gene fromBytes(final byte[] nucleotides ){
        final Gene retVal = new Gene();
        if( null != nucleotides && 4 == nucleotides.length){
            retVal.nucleotideA = nucleotides[0];
            retVal.nucleotideB = nucleotides[1];
            retVal.nucleotideC = nucleotides[2];
            retVal.nucleotideD = nucleotides[3];
        } else {
            throw new EvolutionException("Corrupt Gene.");
        }
        return retVal;
    }

    public String toString(){
        return String.format( "[%d,%d,%d,%d]", nucleotideA, nucleotideB, nucleotideC,nucleotideD);
    }
}
