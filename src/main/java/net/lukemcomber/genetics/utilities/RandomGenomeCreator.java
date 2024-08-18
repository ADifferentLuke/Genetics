package net.lukemcomber.genetics.utilities;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A utility class to generate random genomes.
 */
public class RandomGenomeCreator {

    private static final String dataDir = "tmp";
    public static final String filterFilePath = dataDir + File.separator + "genomes.filter";
    public static final String genomeFilePath = dataDir + File.separator + "genomes.txt";

    private Set<String> filter;

    /**
     * Create a new instance with a genome filter
     * @param filter set of genomes to avoid
     */
    public RandomGenomeCreator(final Set<String> filter) {
        this.filter = filter;
    }

    /**
     * Generates random genomes without repeating
     * @param type type of genome
     * @param count number of genomes to create
     * @return set of genome strings
     */
    public Set<String> generateRandomGenomes(final String type, final int count) {

        final Set<String> retVal = new HashSet<>();
        for (int i = 0; i < count; ++i) {
            String dna = "";
            do {
                dna = getRandomHexString(160);
            } while (filter.contains(dna));
            retVal.add(String.format("%s:%s", type, dna));
        }

        if (count != retVal.size()) {
            throw new RuntimeException("Failed to generate adequate organisms.");
        }

        return retVal;
    }

    /**
     * Generates random coordinates for a set of genomes.
     *
     * @param width width of simulation
     * @param height height of simulation
     * @param genomes set of genomes to generate locations for
     * @param preexisting any pre-existing genomes and coordinates
     * @return map of coordinates and genomes
     */
    public Map<SpatialCoordinates, String> generateRandomLocations(final int width, final int height, final Set<String> genomes,
                                                                   final Map<SpatialCoordinates, String> preexisting) {

        final HashSet<String> simpleCollisionDetection = new HashSet<>();
        final Random r = new Random();
        final Map<SpatialCoordinates, String> result = new HashMap<>();

        if (null != preexisting) {
            preexisting.forEach((coord, genome) -> {
                final String scdKey = String.format("%d-%d", coord.xAxis(), coord.yAxis());
                if (simpleCollisionDetection.contains(scdKey)) {
                    throw new EvolutionException("Collision detected at " + coord);
                }

                simpleCollisionDetection.add(scdKey);
                result.put(coord,genome);

            });
        }

        result.putAll(genomes.stream().collect(Collectors.toMap(genome -> {
            int x, y = 0;
            do {
                x = r.nextInt(width);
                y = r.nextInt(height);
            } while (simpleCollisionDetection.contains(String.format("%d-%d", x, y)));
            simpleCollisionDetection.add(String.format("%d-%d", x, y));

            return new SpatialCoordinates(x, y, 0);
        }, genome -> genome)));

        return result;
    }


    /**
     * Generates random genomes and write to ./tmp/genomes.txt.
     *
     * A filter file at ./tmp/genomes.filter is used to avoid repeats across invokations
     *
     * Usage: RandomGenomeCreator &lt;width&gt; &lt;height&gt; &lt;count&gt;
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.err.println("Usage RandomGenomeCreator <width> <height> <count>");
            return;
        }


        final Integer width = Integer.valueOf(args[0]);
        final Integer height = Integer.parseInt(args[1]);
        final Integer count = Integer.valueOf(args[2]);

        File directory = new File(dataDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        final HashSet<String> filter = new HashSet<>();
        final File filterFile = new File(filterFilePath);
        if (filterFile.exists()) {

            BufferedReader reader = new BufferedReader(new FileReader(filterFile));
            String line;
            while (null != (line = reader.readLine())) {
                filter.add(line);
            }

            reader.close();
        }

        final RandomGenomeCreator creator = new RandomGenomeCreator(filter);

        final File file = new File(genomeFilePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {

            creator.generateRandomLocations(width, height, creator.generateRandomGenomes("PLANT",count),null).forEach((key, value) -> {

                try {
                    writer.write(String.format("(%03d,%03d,0),%s", key.xAxis(), key.yAxis(), value));
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String getRandomHexString(int numchars) {
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while (sb.length() < (numchars)) {
            sb.append(Integer.toHexString(r.nextInt(255)));
        }

        return sb.toString().substring(0, numchars);
    }
}
