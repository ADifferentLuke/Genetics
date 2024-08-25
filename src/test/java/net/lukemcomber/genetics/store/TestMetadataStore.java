package net.lukemcomber.genetics.store;

import net.lukemcomber.genetics.model.UniverseConstants;

import java.util.Map;

public class TestMetadataStore {
    public static class TestUniverse extends UniverseConstants {
        public TestUniverse(Map<String, Object> map) {
            super(map);
        }
    }
}
