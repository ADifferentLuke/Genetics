package net.lukemcomber.genetics.io;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Gene;
import net.lukemcomber.genetics.biology.Genome;

import java.io.OutputStream;
import java.io.PrintStream;


/**
 * Writes the genome to an {@link OutputStream}
 */
public class GenomeStreamWriter {

    /**
     * Writes the Genome values in a format showing each nucleotide.
     *
     * @param genome       genome to write
     * @param outputStream stream to write into
     */
    public static void prettyPrintGenomeToStream(final Genome genome, final OutputStream outputStream) {

        final PrintStream printStream = new PrintStream(outputStream);

        printStream.println("========================= GENOME=================================");
        printStream.println("|                                                               |");
        for (int i = 0; genome.getNumberOfGenes() > i; ++i) {
            final Gene gene = genome.getGeneNumber(i);
            printStream.println(String.format("|    Gene %02d  -----------------------------------------------   |", i + 1));
            printStream.println(String.format("|                         %03d                                   |", gene.nucleotideA));
            printStream.println(String.format("|                 %03d               %03d                         |", gene.nucleotideB, gene.nucleotideC));
            printStream.println(String.format("|                         %03d                                   |", gene.nucleotideD));
            printStream.println("|                                                               |");
        }

        printStream.println("=================================================================");
    }

    /**
     * Write the genome into the {@link OutputStream} as a binary string
     *
     * @param genome       genome to write
     * @param outputStream stream to write into
     */
    public static void binaryPrintGenomeToStream(final Genome genome, final OutputStream outputStream) {

        final PrintStream printStream = new PrintStream(outputStream);


        printStream.println("Raw binary genome: ");
        for (int i = 0; genome.getNumberOfGenes() > i; ++i) {
            final Gene gene = genome.getGeneNumber(i);
            //When we change the byte to an int, the bits become two-compliment. Handle that.
            printStream.print("\t" + Integer.toBinaryString((gene.nucleotideA & 0xFF) + 0x100).substring(1));
            printStream.println("  " + Integer.toBinaryString((gene.nucleotideB & 0xFF) + 0x100).substring(1));
            printStream.print("\t" + Integer.toBinaryString((gene.nucleotideC & 0xFF) + 0x100).substring(1));
            printStream.println("  " + Integer.toBinaryString((gene.nucleotideD & 0xFF) + 0x100).substring(1));
            printStream.println();

        }
    }

    /**
     * Write the genome into the {@link OutputStream} as a hex string
     *
     * @param genome       genome to write
     * @param outputStream output stream to write into
     */
    public static void hexPrintGenomeToStream(final Genome genome, final OutputStream outputStream) {
        final PrintStream printStream = new PrintStream(outputStream);
        printStream.println("Raw hex genome: " + GenomeSerDe.serialize(genome));
    }
}
