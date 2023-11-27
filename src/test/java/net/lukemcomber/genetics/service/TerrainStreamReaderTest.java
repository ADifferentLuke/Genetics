package net.lukemcomber.genetics.service;

import net.lukemcomber.genetics.world.terrain.Terrain;
import org.apache.commons.lang3.Range;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@Test
public class TerrainStreamReaderTest {

    private static final String testInitFile = "TestInitialEnvironment.txt";

    public void testReader() throws IOException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(testInitFile);
        final TerrainStreamReader fileReader = new TerrainStreamReader("unit-test");

        assert url != null;
        final Terrain terrain = fileReader.parse(Files.newInputStream(Paths.get(url.getPath())));


    }

    public void testRangeParsingLogic(){
        final String range1 = "[0-99]";
        final String range2 = "[6-7]";
        final String range3 = "55";
        final String range4 = "*";
        final String range5 = "";

        final Range<Integer> resultRange = Range.between(0,99);
        final Range<Integer> resultRange2 = Range.between(6,7);
        final Range<Integer> resultRange3 = Range.between(55,55);

        final int max = 100;

        final TerrainStreamReader reader = new TerrainStreamReader("unit-test");
        final Range<Integer> testRange1 = reader.parseRange(range1,max);
        final Range<Integer> testRange2 = reader.parseRange(range2,max);
        final Range<Integer> testRange3 = reader.parseRange(range3,max);
        final Range<Integer> testRange4 = reader.parseRange(range4,max);
        final Range<Integer> testRange5 = reader.parseRange(range5,max);

        assert( testRange1.equals(resultRange));
        assert( testRange2.equals(resultRange2));
        assert( testRange3.equals(resultRange3));
        assert( testRange4.equals(resultRange));
        assert( testRange5.equals(resultRange));


    }
}
