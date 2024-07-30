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
import static org.testng.AssertJUnit.assertTrue;

@Test
public class TestSearchableMetadataStore {

    private static final Logger logger = Logger.getLogger(TestSearchableMetadataStore.class.getName());

    public static class TestUniverse extends UniverseConstants {
        public TestUniverse(Map<String, Object> map) {
            super(map);
        }
    }

    public void test() throws IOException, InterruptedException {
        final TmpMetadataStoreTest.TestUniverse testUniverse = new TmpMetadataStoreTest.TestUniverse(ImmutableMap.of(
                Terrain.PROPERTY_TERRAIN_TYPE, FlatWorld.ID,
                "metadata.TestMetadata.enabled", true,
                MetadataStore.PROPERTY_DATASTORE_TTL, 1000000l
        ));

        final int NUM_RECORDS = 75;
        final int RECORD_SELECT_NUMBER = 21; //just randomly picked

        final MetadataStoreGroup group = MetadataStoreFactory.getMetadataStore("unit-test-2", testUniverse);
        final SearchableMetadataStore<TestMetadata> testMetaStore = (SearchableMetadataStore<TestMetadata>) group.get(TestMetadata.class);
        final List<Integer> checkList = new ArrayList<>(NUM_RECORDS);
        TestMetadata cacheRecord = null;


        logger.info("Generating fake data ...");
        for (int i = 0; i < NUM_RECORDS; i++) {

            final TestMetadata testMetadata = new TestMetadata();
            testMetadata.str = RandomStringUtils.randomAlphanumeric(1, 1000);
            testMetadata.intNumber = RandomUtils.nextInt();
            testMetadata.longNumber = RandomUtils.nextLong();

            checkList.add(testMetadata.intNumber);


            testMetaStore.store(testMetadata);

            if (RECORD_SELECT_NUMBER == i) {
                cacheRecord = testMetadata;
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
        final List<TestMetadata> page = testMetaStore.page("int", pageNo, countPerPage);
        for (int i = 0; i < page.size(); ++i) {
            assertEquals(page.get(i).intNumber, checkList.get((pageNo * countPerPage) + i));
        }

        if (null != cacheRecord) {
            final List<TestMetadata> recordStrLookupResults = testMetaStore.find( cacheRecord.str, 5);
            assertEquals(1, recordStrLookupResults.size());

            assertEquals( cacheRecord.str, recordStrLookupResults.get(0).str);

            final List<TestMetadata> recordIntLookupResults = testMetaStore.find( cacheRecord.intNumber, 5 );
            if( 0 >= recordIntLookupResults.size()){
               throw new RuntimeException("Find by named type failed");
            }

            assertEquals( cacheRecord.intNumber,recordIntLookupResults.get(0).intNumber );

        } else if (RECORD_SELECT_NUMBER < NUM_RECORDS) {
            throw new RuntimeException("Should have gotten a record but didn't?");
        }
        testMetaStore.expire(true);

    }
}
