package net.lukemcomber.dev.ai.genetics;

import net.lukemcomber.dev.ai.genetics.biology.Cell;
import net.lukemcomber.dev.ai.genetics.biology.Organism;
import net.lukemcomber.dev.ai.genetics.model.AutomatedConfig;
import net.lukemcomber.dev.ai.genetics.service.AutomatedConfigFileReader;
import net.lukemcomber.dev.ai.genetics.service.GenomeStreamReader;
import net.lukemcomber.dev.ai.genetics.service.TerrainStreamReader;
import net.lukemcomber.dev.ai.genetics.world.terrain.Terrain;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class Genetics {

    public void run(final String configFile) throws IOException {
        final AutomatedConfigFileReader reader = new AutomatedConfigFileReader();
        final AutomatedConfig config = reader.parse(Files.newInputStream(Paths.get(configFile)));

        run( config.initialEnvironmentFile, config.initialZooFile );
    }

    public void run(final InputStream initEnv, final InputStream zoo) throws IOException {

        final Terrain terrain = new TerrainStreamReader().parse(initEnv);
        final GenomeStreamReader genomeStreamReader = new GenomeStreamReader(terrain.getSizeOfXAxis(),
                terrain.getSizeOfYAxis(), terrain.getSizeOfZAxis());
        if (null != zoo) {
            final List<Organism> organisms = genomeStreamReader.parse(zoo);

            final List<Cell> cells = organisms.stream()
                    .flatMap(o -> o.getCells().stream())
                    .collect(Collectors.toList());

            cells.forEach(c -> {
                System.out.println("Setting cell on " + c.getCoordinates());
                terrain.setCell(c);
            });
        }
    }

    public void run(final String initFile, final String zooFile) throws IOException {
        run(Files.newInputStream(Paths.get(initFile)), Files.newInputStream(Paths.get(zooFile)));
    }

    public static void main(final String[] args) throws IOException {
        if (1 == args.length) {
            new Genetics().run(args[0]);
        } else {

            System.err.println("Usage: Genetics <initialization terrain file> [<initialize biology file>]");
            System.exit(-1);
        }
    }
}