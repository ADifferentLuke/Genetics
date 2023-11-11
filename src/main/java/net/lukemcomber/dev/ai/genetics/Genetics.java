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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class Genetics {

    private final static int CLOCK_DELAY_MS = 1000;

    public void run(final String configFile) throws IOException {
        final AutomatedConfigFileReader reader = new AutomatedConfigFileReader();
        final AutomatedConfig config = reader.parse(Files.newInputStream(Paths.get(configFile)));

        run(config.initialEnvironmentFile, config.initialZooFile);
    }

    public void run(final InputStream initEnv, final InputStream zoo) throws IOException {

        //Builds the world
        final Terrain terrain = new TerrainStreamReader().parse(initEnv);

        //reads a genome
        final GenomeStreamReader genomeStreamReader = new GenomeStreamReader(terrain.getSizeOfXAxis(),
                terrain.getSizeOfYAxis(), terrain.getSizeOfZAxis());

        if (null != zoo) {
            final List<Organism> organisms = genomeStreamReader.parse(zoo);

            final List<Cell> cells = organisms.stream()
                    .map(Organism::getCells)
                    .collect(Collectors.toList());

            /*
            cells.forEach(c -> {
                System.out.println("Setting cell on " + c.getCoordinates());
                terrain.setCell(c);
            });
            //the world is set up and the genome is set up. Begin simulation
            final AtomicLong aInt = new AtomicLong(0);
            try {
                while (true) {
                    organisms.stream().forEach( o -> {
                        long i  = aInt.getAndIncrement();
                        o.leechResources(terrain,i);
                        o.performAction(terrain,i);
                        o.prettyPrint(System.out);
                    });
                    Thread.sleep(CLOCK_DELAY_MS);
                }
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
            */
        }
    }

    public void run(final String initFile, final String zooFile) throws IOException {
        run(Files.newInputStream(Paths.get(initFile)), Files.newInputStream(Paths.get(zooFile)));
    }

    public static void main(final String[] args) throws IOException {
        //TODO fix me
        if (2 == args.length) {
            new Genetics().run(args[0],args[1]);
        } else {

            System.err.println("Usage: Genetics <initialization terrain file> [<initialize biology file>]");
            System.exit(-1);
        }
    }
}