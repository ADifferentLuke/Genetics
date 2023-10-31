package net.lukemcomber.dev.ai.genetics.service;

import net.lukemcomber.dev.ai.genetics.biology.Gene;
import net.lukemcomber.dev.ai.genetics.biology.Genome;

import net.lukemcomber.dev.ai.genetics.biology.plant.PlantGenome;


public class GenomeStdOutWriter {

    public static void prettyPrintGenomeToStdOut(final Genome genome) {

        System.out.println("========================= GENOME=================================");
        System.out.println("|                                                               |");
        for (int i = 0; genome.getNumberOfGenes() > i; ++i) {
            final Gene gene = genome.getGeneNumber(i);
            System.out.println(String.format("|    Gene %02d  -----------------------------------------------   |", i + 1));
            System.out.println(String.format("|                         %03d                                   |", gene.nucleotideA));
            System.out.println(String.format("|                 %03d               %03d                         |", gene.nucleotideB, gene.nucleotideC));
            System.out.println(String.format("|                         %03d                                   |", gene.nucleotideD));
            System.out.println("|                                                               |");
        }

        System.out.println("=================================================================");
    }

    public static void hexPrintGenomeToStdOut(final Genome genome) {
        System.out.println("Encoded genome: " + GenomeSerDe.serialize(genome));
    }

    public static void binaryPrintGenomeToStdOut(final Genome genome) {
        System.out.println("Raw binary genome: ");
        for (int i = 0; genome.getNumberOfGenes() > i; ++i) {
            final Gene gene = genome.getGeneNumber(i);
            //When we change the byte to an int, the bits become two-compliment. Handle that.
            System.out.print("\t" + Integer.toBinaryString((gene.nucleotideA & 0xFF) + 0x100).substring(1));
            System.out.println("  " + Integer.toBinaryString((gene.nucleotideB & 0xFF) + 0x100).substring(1));
            System.out.print("\t" + Integer.toBinaryString((gene.nucleotideC & 0xFF) + 0x100).substring(1));
            System.out.println("  " + Integer.toBinaryString((gene.nucleotideD & 0xFF) + 0x100).substring(1));
            System.out.println();

        }
    }
}
