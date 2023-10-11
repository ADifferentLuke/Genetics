package net.lukemcomber.dev.ai.genetics.service;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Genome;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import org.apache.commons.codec.DecoderException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Test
public class GenomeStreamReaderTest {
    private static final String testInitFile = "TestInitialZoology.txt";

    public void testLogic(){
        int actions = 100;
        int bits = actions * 5;
        int bytes = bits / 8;

        while (bits % 8 != 0 || bytes % 4 != 0) {
            actions++;
            bits = actions * 5;
            bytes = bits / 8;
        }
        int bytesDivisibleBy4 = bytes + (4 - (bytes % 4)) % 4;
        System.out.println("Number of actions: " + actions);
        System.out.println("Number of bytes: " + bytesDivisibleBy4);

    }


    public void testGenomeSerDe() throws DecoderException {
        final String genomeString = "78386f1f1d027e2a177d7b3d65543d3a57660773335270726f6b6e58667b5010586557150d2b33741e402e0f26082b740f403515";
        final Genome genome = GenomeSerDe.deserialize("PLANT", genomeString);

        final String newGenomeString = GenomeSerDe.serialize(genome);
        assert (newGenomeString.equals(genomeString));
        final Genome newGenome = GenomeSerDe.deserialize("PLANT", genomeString);
        assert (newGenome.getGeneNumber(3).nucleotideA == genome.getGeneNumber(3).nucleotideA);
    }

    public void testReader() throws IOException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(testInitFile);
        final GenomeStreamReader reader = new GenomeStreamReader( 90,90,90 );
        final List<Organism> organisms = reader.parse( Files.newInputStream(Paths.get(url.getPath())));

    }
}
