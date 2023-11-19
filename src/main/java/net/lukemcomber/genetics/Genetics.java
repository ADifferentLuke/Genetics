package net.lukemcomber.genetics;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;
import net.lukemcomber.genetics.biology.Organism;
import net.lukemcomber.genetics.model.AutomatedConfig;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.service.AutomatedConfigFileReader;
import net.lukemcomber.genetics.service.GenomeStreamReader;
import net.lukemcomber.genetics.service.TerrainStreamReader;
import net.lukemcomber.genetics.universes.FlatFloraUniverse;
import net.lukemcomber.genetics.world.terrain.Terrain;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Genetics {

    private final static int CLOCK_DELAY_MS = 1000;

    public void run(final String configFile) throws IOException {
        final AutomatedConfigFileReader reader = new AutomatedConfigFileReader();
        final AutomatedConfig config = reader.parse(Files.newInputStream(Paths.get(configFile)));

        run(config.initialEnvironmentFile, config.initialZooFile);
    }

    public void run(final InputStream initEnv, final InputStream zoo) throws IOException {

        final UniverseConstants properties = new FlatFloraUniverse();
        //Builds the world
        final Terrain terrain = new TerrainStreamReader().parse(initEnv);

        //reads a genome
        final GenomeStreamReader genomeStreamReader = new GenomeStreamReader(terrain.getSizeOfXAxis(),
                terrain.getSizeOfYAxis(), terrain.getSizeOfZAxis(), properties);

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
        System.out.println( "Thank you for using my Genetics library.");
    }
}