package net.lukemcomber.genetics.io;

import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.utilities.RandomGenomeCreator;

import java.util.*;

public class SpatialNormalizer extends SpatialCoordinateRangeParser {
    public Map<SpatialCoordinates, String> normalize(final SpatialCoordinates spatialDimensions, final List<String> data) {

        final Map<SpatialCoordinates, String> results = new HashMap<>();
        final Set<String> missingLocations = new HashSet<>();
        for (final String inputStr : data) {
            if( 0 < inputStr.trim().length()) {
                final SpatialCoordinateRangeParser.RangeValueItem rvi = parseItem(inputStr, spatialDimensions);
                if (Objects.isNull(rvi.rangeCoordinates)) {
                    missingLocations.add(rvi.value);
                } else {
                    iterateRangeValue(rvi, spatialDimensions,
                            (coords, v) -> {
                                results.put(coords, v);
                                return true;
                            });
                }
            }
        }
        final RandomGenomeCreator creator = new RandomGenomeCreator(new HashSet<>());
        if(results.isEmpty()){
            return null;
        } else {
            return creator.generateRandomLocations(spatialDimensions.xAxis(), spatialDimensions.yAxis(), missingLocations, results);
        }

    }
}
