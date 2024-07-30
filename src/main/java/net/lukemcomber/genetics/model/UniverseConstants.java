package net.lukemcomber.genetics.model;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.exception.EvolutionException;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.logging.Logger;

public abstract class UniverseConstants {

    private final Logger logger = Logger.getLogger(UniverseConstants.class.getName());

    private final Map<String,Object> constantMap;

    public UniverseConstants(final Map<String,Object> map){
        constantMap = map;

        logger.info("Using properties ... ");
        final StringBuilder output = new StringBuilder("\n");
        map.forEach( (key,value) -> {
            output.append(key);
            output.append(": " );
            output.append(value);
            output.append("\n");
        });
        logger.info(output.toString());
    }

    public <T> T get(final String key, final Class<T> type ){
        return get(key,type,null);
    }
    public <T> T get(final String key, final Class<T> type, final T defaultVal){
        T retVal = defaultVal;
        if(StringUtils.isNotEmpty(key) && null != type ){
            if( constantMap.containsKey(key)){
                retVal = type.cast(constantMap.get(key));
            }
        }
        if( null == defaultVal && null == retVal){
            throw new EvolutionException( "Property " + key + " is not defined.");
        }
        return retVal;
    }
}
