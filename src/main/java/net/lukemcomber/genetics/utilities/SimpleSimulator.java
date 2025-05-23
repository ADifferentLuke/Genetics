package net.lukemcomber.genetics.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lukemcomber.genetics.MultiEpochEcosystem;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.model.ecosystem.impl.MultiEpochConfiguration;
import net.lukemcomber.genetics.store.MetadataStoreFactory;
import net.lukemcomber.genetics.universes.CustomUniverse;
import net.lukemcomber.genetics.universes.FlatFloraUniverse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;


/**
 * A utility class for running multiple simulations in serial
 */
public class SimpleSimulator {

    /*
   {

        "epochs": 5,
        "height": 400,
        "width": 800,
        "ticksPerDay": 10,
        "maxDays": 10,
        "initialPopulation": 100,
        "reusePopulation": 50,
        "filter": "misc/genome.filter",
        "name": "simsim",
        "tickDelayMs": 0
    }
     */

    /**
     * Run the {@link SimpleSimulator} from the configuration supplied and using the provided
     * filter file.
     * <p>
     * Usage: SimpleSimulator &lt;file&gt; &lt;filter&gt;
     *
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(final String[] args) throws IOException, InterruptedException {

        if (!(1 <= args.length && 2 >= args.length)) {
            System.err.println("Usage: SimpleSimulator <file> [<universe>]");
            return;
        }

        final File inputFile = new File(args[0]);
        final File universeFile = args.length == 2 ? new File(args[1]) : null;
        final ObjectMapper objectMapper = new ObjectMapper();

        final InputStream configFile = SimpleSimulator.class.getResourceAsStream("/logging/logging.properties");
        LogManager.getLogManager().readConfiguration(configFile);

        UniverseConstants universe = null;
        if( null != universeFile){
            universe = new CustomUniverse(universeFile);
        } else {
            universe = new FlatFloraUniverse();
        }


        if (inputFile.exists()) {
            final FileInputStream inputStream = new FileInputStream(inputFile);
            final JsonNode inputJson = objectMapper.readTree(inputStream);
            final MultiEpochConfiguration configuration = MultiEpochConfiguration.builder()
                    .name(inputJson.path("name").asText(""))
                    .size(new SpatialCoordinates(inputJson.path("width").asInt(),
                            inputJson.path("height").asInt(), 0))
                    .epochs(inputJson.path("epochs").asInt(1))
                    .fileFilterPath(inputJson.path("filter").asText(null))
                    .initialPopulation(inputJson.path("initialPopulation").asInt())
                    .maxDays(inputJson.path("maxDays").asInt())
                    .reusePopulation(inputJson.path("reusePopulation").asInt())
                    .startOrganisms(null)
                    .tickDelayMs(inputJson.path("tickDelayMs").asInt())
                    .ticksPerDay(inputJson.path("ticksPerDay").asInt())
                    .deleteFilterOnExit(inputJson.path("deleteFilterOnExit").asBoolean(false))
                    .build();
            final MultiEpochEcosystem ecosystem = new MultiEpochEcosystem(universe, configuration);
            ecosystem.initialize(null);
            final Thread simulation = new Thread(ecosystem);
            simulation.setName("SimpleSimulatorEcosystem");
            simulation.start();
            simulation.join();

            // kill all
            MetadataStoreFactory.freeResourcesAndTerminate();

            System.out.println("Finished.");
        } else {
            System.err.println("File [" + args[0] + "] does not exist.");
        }
    }

}
