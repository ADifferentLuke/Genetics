package net.lukemcomber.genetics.store;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import com.google.common.collect.ImmutableMap;
import net.lukemcomber.genetics.world.terrain.Terrain;
import net.lukemcomber.genetics.world.terrain.impl.FlatWorld;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static org.testng.AssertJUnit.*;

@Test
public class TestTmpMetadataStore {

    private static final Logger logger = Logger.getLogger(TestTmpMetadataStore.class.getName());

    public void test() throws IOException, InterruptedException {
        final TestMetadataStore.TestUniverse testUniverse = new TestMetadataStore.TestUniverse(ImmutableMap.of(
                Terrain.PROPERTY_TERRAIN_TYPE, FlatWorld.ID,
                "metadata.TestMetadata.enabled", true,
                MetadataStore.PROPERTY_DATASTORE_TTL, 1000000l
        ));
        final MetadataStoreGroup group = MetadataStoreFactory.getMetadataStore("tmp-datastore-test-1", testUniverse);
        final MetadataStore<TestMetadata> testMetaStore = group.get(TestMetadata.class);

        final TestMetadata testMetadata = new TestMetadata();
        testMetadata.str = "Hello World!";

        testMetaStore.store(testMetadata);

        /*
        sleep(500l);
        final List<TestMetadata> results = testMetaStore.retrieve();

        assertEquals(1, results.size());

        assertEquals(testMetadata.str, results.get(0).str);
        assertNotSame(testMetadata.str, results.get(0).str);

        logger.info("Waiting for cache cleanup .... ");
        testMetaStore.expire();

        assertTrue(testMetaStore.expire(true));
        testMetaStore.expire(true);

        final List<TestMetadata> closedResults = testMetaStore.retrieve();
        assertNull(closedResults);
        */
    }
}
