package net.lukemcomber.genetics.store;

import com.google.common.collect.ImmutableMap;
import net.lukemcomber.genetics.store.impl.MetadataStorage;
import net.lukemcomber.genetics.world.terrain.Terrain;
import net.lukemcomber.genetics.world.terrain.impl.FlatWorld;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Test
public class TestExportedMetadataStore {

    private static final Logger logger = Logger.getLogger(TestKryoMetadataStore.class.getName());
    private static final String OUTPUT_PATH = "./output";

    public TestExportedMetadataStore() {
        logger.info("STARTING EXPORT");
    }

    public void test() throws IOException, InterruptedException {
        final TestMetadataStore.TestUniverse testUniverse = new TestMetadataStore.TestUniverse(ImmutableMap.<String,Object>builder()
                .put(Terrain.PROPERTY_TERRAIN_TYPE, FlatWorld.ID)
                .put(TestSearchableMetadata.PROPERTY_ENABLED, true)
                .put(MetadataStore.PROPERTY_DATASTORE_TTL, 1000000L)
                .put(MetadataStore.METADATA_EXPORT, true)
                .put(MetadataStorage.PROPERTY_TYPE_PATH, OUTPUT_PATH)
                .put("metadata.TestSearchableMetadata.export", true)
                .build());


        final int NUM_RECORDS = 75;
        final String simulation = "unit-test-3";

        final MetadataStoreGroup group = MetadataStoreFactory.getMetadataStore(simulation, testUniverse);
        final MetadataStore<TestSearchableMetadata> testMetaStore = group.get(TestSearchableMetadata.class);
        final List<Integer> checkList = new ArrayList<>(NUM_RECORDS);

        logger.info("Generating fake data ...");
        for (int i = 0; i < NUM_RECORDS; i++) {

            final TestSearchableMetadata testSearchableMetadata = new TestSearchableMetadata();
            testSearchableMetadata.str = RandomStringUtils.randomAlphanumeric(1, 1000);
            testSearchableMetadata.intNumber = RandomUtils.nextInt();
            testSearchableMetadata.longNumber = RandomUtils.nextLong();

            checkList.add(testSearchableMetadata.intNumber);

            testMetaStore.store(testSearchableMetadata);

            Thread.sleep(100);
        }

        final String outputFile = MetadataStorage.persist(testMetaStore, simulation, testUniverse);
        if (StringUtils.isEmpty(outputFile)) {
            throw new RuntimeException("Export failed.");
        }
        final File exportFile = new File(outputFile);
        if( !exportFile.exists()) {
            throw new RuntimeException("Output %s does not exist.".formatted(outputFile));
        }

        testMetaStore.expire(true);

        if( !exportFile.exists()) {
            throw new RuntimeException("Output %s deleted during expiration!.".formatted(outputFile));
        }

        exportFile.deleteOnExit();

        logger.info("MetadataStorage thread shutdown.");

    }

}
