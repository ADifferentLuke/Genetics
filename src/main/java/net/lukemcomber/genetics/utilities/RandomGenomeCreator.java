package net.lukemcomber.genetics.utilities;


import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class RandomGenomeCreator {

    private static final String dataDir = "tmp";
    public static final String filterFilePath = dataDir + File.separator + "genomes.filter";
    public static final String genomeFilePath = dataDir + File.separator + "genomes.txt";

    private Set<String> filter;

    public RandomGenomeCreator(final Set<String> filter) {
        this.filter = filter;
    }

    public Set<String> generateRandomGenomes(final int count) {

        final Set<String> retVal = new HashSet<>();
        for (int i = 0; i < count; ++i) {
            String dna = "";
            do {
                dna = getRandomHexString(160);
            } while (filter.contains(dna));
            retVal.add(dna);
        }

        if (count != retVal.size()) {
            throw new RuntimeException("Failed to generate adequate organisms.");
        }

        return retVal;
    }

    public Map<SpatialCoordinates, String> generateRandomLocations(final int width, final int height, final Set<String> genomes,
                                                                   final Map<SpatialCoordinates, String> preexisting) {

        final HashSet<String> simpleCollisionDetection = new HashSet<>();
        Random r = new Random();
        final Map<SpatialCoordinates, String> result = new HashMap<>();

        if (null != preexisting) {
            preexisting.forEach((coord, genome) -> {
                final String scdKey = String.format("%d-%d", coord.xAxis, coord.yAxis);
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

            creator.generateRandomLocations(width, height, creator.generateRandomGenomes(count),null).forEach((key, value) -> {

                try {
                    writer.write(String.format("(%03d,%03d,0),%s", key.xAxis, key.yAxis, value));
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
