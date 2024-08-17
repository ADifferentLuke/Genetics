package net.lukemcomber.genetics.model;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.exception.EvolutionException;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * An abstract class to hold simulation wide constants such as configuration.
 */
public abstract class UniverseConstants {

    private final Logger logger = Logger.getLogger(UniverseConstants.class.getName());

    private final Map<String, Object> constantMap;

    /**
     * Creates a new instance the properties set
     *
     * @param map map of key value pairs to set
     */
    public UniverseConstants(final Map<String, Object> map) {
        constantMap = map;

        logger.info("Using properties ... ");
        final StringBuilder output = new StringBuilder("\n");
        map.forEach((key, value) -> {
            output.append(key);
            output.append(": ");
            output.append(value);
            output.append("\n");
        });
        logger.info(output.toString());
    }

    /**
     * Returns a map of the properties serialized into {@link String}
     *
     * @return map of name value strings
     */
    public Map<String, String> toMap() {
        return constantMap.keySet()
                .stream()
                .collect(
                        Collectors.toMap(key -> key, key -> constantMap.get(key).toString()));
    }

    /**
     * Query for a properties value and return as a type
     *
     * @param key  Property to lookup
     * @param type type to return
     * @param <T>  type of the parameter
     * @return the value or an {@link EvolutionException} if not found.
     */
    public <T> T get(final String key, final Class<T> type) {
        return get(key, type, null);
    }

    /**
     * Query for a properties value and return as a type. If not found, return the default value.
     * If the default value is null, a {@link EvolutionException} is thrown when the key is not found.
     *
     * @param key        key to lookup
     * @param type       type to return
     * @param defaultVal optional default value
     * @param <T>        property type
     * @return the value of the property, or the default value, or {@link EvolutionException}
     */
    public <T> T get(final String key, final Class<T> type, final T defaultVal) {
        T retVal = defaultVal;
        if (StringUtils.isNotEmpty(key) && null != type) {
            if (constantMap.containsKey(key)) {
                retVal = type.cast(constantMap.get(key));
            }
        }
        if (null == defaultVal && null == retVal) {
            throw new EvolutionException("Property " + key + " is not defined.");
        }
        return retVal;
    }
}
