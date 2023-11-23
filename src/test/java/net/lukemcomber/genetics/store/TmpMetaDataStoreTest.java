package net.lukemcomber.genetics.store;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import com.google.common.collect.ImmutableMap;
import net.lukemcomber.genetics.model.UniverseConstants;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.testng.AssertJUnit.*;

@Test
public class TmpMetaDataStoreTest {

    private static final Logger logger = Logger.getLogger( TmpMetaDataStoreTest.class.getName());

    public static class TestUniverse extends UniverseConstants {
        public TestUniverse(Map<String, Object> map) {
            super(map);
        }
    }

    public void test() throws IOException, InterruptedException {
        final TestUniverse testUniverse = new TestUniverse(ImmutableMap.of(
                "metadata.TestMetadata.enabled", true,
                MetadataStore.PROPERTY_DATASTORE_TTL, 1l
        ));
        final MetadataStore<TestMetadata> testMetaStore = MetadataStoreFactory.getMetadataStore("test",
                TestMetadata.class, testUniverse);

        final TestMetadata testMetadata = new TestMetadata();
        testMetadata.str = "Hello World!";

        testMetaStore.store(testMetadata);

        Thread.sleep(500l);
        final List<TestMetadata> results = testMetaStore.retrieve();

        assertEquals(1, results.size());

        assertTrue(testMetadata.str.equals(results.get(0).str));
        assertFalse(testMetadata.str == results.get(0).str);

        logger.info("Waiting for cache cleanup .... ");
        for( int loopCounter = 0; 1000 > loopCounter && !testMetaStore.expire(); ++loopCounter){
            Thread.sleep(30l);
        }

        assertTrue(testMetaStore.expire(true));

        final List<TestMetadata> closedResults = testMetaStore.retrieve();
        assertNull(closedResults);
    }
}
