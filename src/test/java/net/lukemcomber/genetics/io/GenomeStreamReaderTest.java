package net.lukemcomber.genetics.io;

import net.lukemcomber.genetics.biology.Genome;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.MetadataStoreFactory;
import net.lukemcomber.genetics.store.MetadataStoreGroup;
import net.lukemcomber.genetics.universes.FlatFloraUniverse;
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
        final String genomeString = "PLANT:78386f1f1d027e2a177d7b3d65543d3a57660773335270726f6b6e58667b5010586557150d2b33741e402e0f26082b740f403515";
        final Genome genome = GenomeSerDe.deserialize(genomeString);

        final String newGenomeString = GenomeSerDe.serialize(genome);
        assert (newGenomeString.equals(genomeString));
        final Genome newGenome = GenomeSerDe.deserialize( genomeString);
        assert (newGenome.getGeneNumber(3).nucleotideA == genome.getGeneNumber(3).nucleotideA);
    }

    public void testReader() throws IOException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(testInitFile);
        final UniverseConstants props = new FlatFloraUniverse();
        final MetadataStoreGroup groupStore = MetadataStoreFactory.getMetadataStore("GenomeSttreamReaderTest::testReader", props);
        final SpatialCoordinates dimensions = new SpatialCoordinates(90,90,90);
        final GenomeStreamReader reader = new GenomeStreamReader( dimensions, props,groupStore );
        final List<Organism> organisms = reader.parse( Files.newInputStream(Paths.get(url.getPath())));

    }
}
