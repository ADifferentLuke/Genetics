package net.lukemcomber.genetics.store;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import com.google.common.collect.ImmutableMap;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.world.terrain.Terrain;
import net.lukemcomber.genetics.world.terrain.impl.FlatWorld;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertTrue;

@Test
public class TestKryoMetadataStore {

    private static final Logger logger = Logger.getLogger(TestKryoMetadataStore.class.getName());

    public static class TestUniverse extends UniverseConstants {
        public TestUniverse(Map<String, Object> map) {
            super(map);
        }
    }

    public void test() throws IOException, InterruptedException {
        final TestUniverse testUniverse = new TestUniverse(ImmutableMap.of(
                Terrain.PROPERTY_TERRAIN_TYPE, FlatWorld.ID,
                "metadata.TestSearchableMetadata.enabled", true,
                MetadataStore.PROPERTY_DATASTORE_TTL, 1000000
        ));

        final int NUM_RECORDS = 75;
        final int RECORD_SELECT_NUMBER = 21; //just randomly picked

        final MetadataStoreGroup group = MetadataStoreFactory.getMetadataStore("unit-test-2", testUniverse);
        final SearchableMetadataStore<TestSearchableMetadata> testMetaStore = (SearchableMetadataStore<TestSearchableMetadata>) group.get(TestSearchableMetadata.class);
        final List<Integer> checkList = new ArrayList<>(NUM_RECORDS);
        TestSearchableMetadata cacheRecord = null;


        logger.info("Generating fake data ...");
        for (int i = 0; i < NUM_RECORDS; i++) {

            final TestSearchableMetadata testSearchableMetadata = new TestSearchableMetadata();
            testSearchableMetadata.str = RandomStringUtils.randomAlphanumeric(1, 1000);
            testSearchableMetadata.intNumber = RandomUtils.nextInt();
            testSearchableMetadata.longNumber = RandomUtils.nextLong();

            checkList.add(testSearchableMetadata.intNumber);


            testMetaStore.store(testSearchableMetadata);

            if (RECORD_SELECT_NUMBER == i) {
                cacheRecord = testSearchableMetadata;
            }

            Thread.sleep(100);
        }

        // 1 second sleep
        Thread.sleep(1000);

        Collections.sort(checkList, Collections.reverseOrder());

        logger.info("Validating sorted data order.");
        int pageNo = 1;
        int countPerPage = 12;

        //Get top 25
        final List<TestSearchableMetadata> page = testMetaStore.page("int", pageNo, countPerPage);
        logger.severe("List size: " + page.size());
        logger.severe( "Total size: " + testMetaStore.count());
        for (int i = 0; i < page.size(); ++i) {
            try {
                assertEquals(page.get(i).intNumber, checkList.get((pageNo * countPerPage) + i));
            } catch( final Exception e ){
                logger.severe(e.getMessage());
                throw e;
            }
        }

        if (null != cacheRecord) {
            final List<TestSearchableMetadata> recordStrLookupResults = testMetaStore.find(cacheRecord.str, 5);
            assertEquals(1, recordStrLookupResults.size());

            assertEquals(cacheRecord.str, recordStrLookupResults.get(0).str);

            final List<TestSearchableMetadata> recordIntLookupResults = testMetaStore.find(cacheRecord.intNumber, 5);
            if (0 >= recordIntLookupResults.size()) {
                throw new RuntimeException("Find by named type failed");
            }

            assertEquals(cacheRecord.intNumber, recordIntLookupResults.get(0).intNumber);

        } else if (RECORD_SELECT_NUMBER < NUM_RECORDS) {
            throw new RuntimeException("Should have gotten a record but didn't?");
        }
        testMetaStore.expire(true);

        //final List<TestSearchableMetadata> leakedData = testMetaStore.retrieve();
        //assertNull(leakedData, "Data leaked after expiration.");
    }
}
