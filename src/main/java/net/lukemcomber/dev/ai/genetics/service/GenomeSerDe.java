package net.lukemcomber.dev.ai.genetics.service;

import net.lukemcomber.dev.ai.genetics.biology.Gene;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.OrganismFactory;
import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.LinkedList;

public class GenomeSerDe {

    public static Genome deserialize(final String type, final String genome) throws DecoderException {
        final Genome retVal;
        final byte[] gArray = Hex.decodeHex(genome);
        final LinkedList<Gene> geneList = new LinkedList<>();
        if (0 == gArray.length % 4) {
            int genomeSize = gArray.length / 4;
            for (int i = 0; genomeSize > i; ++i) {
                Gene gene = new Gene();
                gene.nucleotideA = gArray[i * 4];
                gene.nucleotideB = gArray[i * 4 + 1];
                gene.nucleotideC = gArray[i * 4 + 2];
                gene.nucleotideD = gArray[i * 4 + 3];
                geneList.addLast(gene);
            }
            retVal = OrganismFactory.createGenome(type, geneList);
        } else {
            throw new EvolutionException("Malformed genome " + genome);
        }
        return retVal;
    }

    public static String serialize(final Genome genome) {
        final byte[] gArray = new byte[genome.getNumberOfGenes() * 4]; //All genes have 4 nucleotides
        for (int i = 0; genome.getNumberOfGenes() > i; ++i) {
            gArray[i * 4] = genome.getGeneNumber(i).nucleotideA;
            gArray[i * 4 + 1] = genome.getGeneNumber(i).nucleotideB;
            gArray[i * 4 + 2] = genome.getGeneNumber(i).nucleotideC;
            gArray[i * 4 + 3] = genome.getGeneNumber(i).nucleotideD;
        }
        return Hex.encodeHexString(gArray);
    }

}
