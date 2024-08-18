package net.lukemcomber.genetics.io;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Gene;
import net.lukemcomber.genetics.biology.Genome;
import net.lukemcomber.genetics.biology.OrganismFactory;
import net.lukemcomber.genetics.biology.plant.PlantGenome;
import net.lukemcomber.genetics.biology.plant.PlantOrganism;
import net.lukemcomber.genetics.exception.EvolutionException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.LinkedList;
import java.util.List;

/**
 * Serializes or deserializes {@link Genome} objects
 */
public class GenomeSerDe {

    public static final char GENOME_DELIMITER = ':';

    /**
     * Deserialize a genome string into a Genome object
     *
     * @param genome Serialize genome
     * @return Genome Object
     * @throws DecoderException   thrown if invalid hex code is used
     * @throws EvolutionException thrown if genome is invalid
     */
    public static Genome deserialize(final String genome) throws DecoderException {
        final Genome retVal;
        final int compoundFieldIndex = genome.indexOf(GENOME_DELIMITER);
        if (0 < compoundFieldIndex) {
            final String type = genome.substring(0, compoundFieldIndex);
            final String encodedGenome = genome.substring(compoundFieldIndex + 1);

            final byte[] gArray = Hex.decodeHex(encodedGenome);
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
                retVal = createGenome(type, geneList);
            } else {
                throw new EvolutionException(String.format(
                        "Genome has invalid number of bytes. Must be divisible by 4 but was %d", genome.length()));
            }
        } else {
            throw new EvolutionException(String.format("Malformed Genome [%s]. Missing compound field.", genome));
        }
        return retVal;
    }

    /**
     * Serializes a genome object for later deserialization.
     *
     * @param genome genome object to serialize
     * @return serialized genome
     */
    public static String serialize(final Genome genome) {
        final byte[] gArray = new byte[genome.getNumberOfGenes() * 4]; //All genes have 4 nucleotides
        for (int i = 0; genome.getNumberOfGenes() > i; ++i) {
            gArray[i * 4] = genome.getGeneNumber(i).nucleotideA;
            gArray[i * 4 + 1] = genome.getGeneNumber(i).nucleotideB;
            gArray[i * 4 + 2] = genome.getGeneNumber(i).nucleotideC;
            gArray[i * 4 + 3] = genome.getGeneNumber(i).nucleotideD;
        }
        return String.format("%s:%s", genome.getType(), Hex.encodeHexString(gArray));
    }

    /**
     * Create a new genome from the type and the list of genes
     *
     * @param type  type of genome to create
     * @param genes list of genes for genome
     * @return a new genome
     */
    public static Genome createGenome(final String type, final List<Gene> genes) {
        final Genome retVal;
        switch (type) {
            case PlantOrganism.TYPE:
                retVal = new PlantGenome(genes);
                break;
            default:
                throw new EvolutionException("Unknown species " + type);
        }
        return retVal;
    }

}
