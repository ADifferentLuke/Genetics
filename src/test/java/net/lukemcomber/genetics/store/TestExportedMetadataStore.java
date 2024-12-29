package net.lukemcomber.genetics.store;

import com.google.common.collect.ImmutableMap;
import net.lukemcomber.genetics.store.impl.MetadataStorage;
import net.lukemcomber.genetics.world.terrain.Terrain;
import net.lukemcomber.genetics.world.terrain.impl.FlatWorld;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Test
public class TestExportedMetadataStore {

    private static final Logger logger = Logger.getLogger(TestKryoMetadataStore.class.getName());
    private static final String OUTPUT_PATH = "./output";

    public TestExportedMetadataStore(){
        logger.info("STARTING EXPORT");
    }

    public void test() throws IOException, InterruptedException {
        final TestMetadataStore.TestUniverse testUniverse = new TestMetadataStore.TestUniverse(ImmutableMap.of(
                Terrain.PROPERTY_TERRAIN_TYPE, FlatWorld.ID,
                TestSearchableMetadata.PROPERTY_ENABLED, true,
                MetadataStore.PROPERTY_DATASTORE_TTL, 1000000L,
                MetadataStore.METADATA_EXPORT, true,
                MetadataStorage.PROPERTY_TYPE_PATH, OUTPUT_PATH
        ));

        final int NUM_RECORDS = 75;
        final String simulation = "unit-test-3";

        final MetadataStoreGroup group = MetadataStoreFactory.getMetadataStore(simulation, testUniverse);
        final MetadataStore<TestSearchableMetadata> testMetaStore = group.get(TestSearchableMetadata.class);
        //final MetadataStorage<TestSearchableMetadata> testMetaStore = (MetadataStorage<TestSearchableMetadata>) group.get(TestSearchableMetadata.class);
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

        // 1 second sleep
        Thread.sleep(1000);

        testMetaStore.expire(true);

        final String path = "%s/%s_%s.txt.gz".formatted(
                OUTPUT_PATH,
                TestSearchableMetadata.class.getSimpleName(), simulation);

        final Path fullOutputPathAndFile = Path.of(path);
        if (fullOutputPathAndFile.toFile().exists()) {

        } else {
            throw new RuntimeException("Export file not found! Check %s.".formatted(path));
        }

        logger.info("MetadataStorage thread shutdown.");

    }

}
