package net.lukemcomber.genetics.utilities;


import java.io.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomGenomeCreator {

    private static final String dataDir = "tmp";
    public static final String filterFile = dataDir + File.separator + "genomes.filter";
    public static final String genomeFile = dataDir + File.separator + "genomes.txt";

    public Set<String> getGenomeFilter(final String filterFile) throws IOException {

        final HashSet<String> filter = new HashSet<>();

        final File file = new File(filterFile);
        if( file.exists()) {

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while (null != (line = reader.readLine())) {
                filter.add(line);
            }

            reader.close();
        }

        return filter;
    }

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Usage RandomGenomeCreator <width> <height> <count>");
            return;
        }

        final RandomGenomeCreator creator = new RandomGenomeCreator();

        final Integer width = Integer.valueOf(args[0]);
        final Integer height = Integer.parseInt(args[1]);
        final Integer count = Integer.valueOf(args[2]);

        File directory = new File(dataDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        final File file = new File(genomeFile);
        final HashSet<String> simpleCollisionDetection = new HashSet<>();
        Random r = new Random();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            final Set<String> filter = creator.getGenomeFilter(filterFile);


            for (int i = 0; i < count; i++) {
                int x, y = 0;
                do {
                    x = r.nextInt(width);
                    y = r.nextInt(height);

                } while (simpleCollisionDetection.contains(String.format("%d-%d", x, y)));

                simpleCollisionDetection.add(String.format("%d-%d", x, y));
                String dna = "";
                do {
                    dna = creator.getRandomHexString(160);
                } while (filter.contains(dna));

                String entry = String.format("(%03d,%03d,0),%s", x, y, dna);
                writer.write(entry);
                writer.newLine();
            }
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
