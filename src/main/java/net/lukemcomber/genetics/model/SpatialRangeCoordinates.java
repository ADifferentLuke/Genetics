package net.lukemcomber.genetics.model;

import net.lukemcomber.genetics.exception.EvolutionException;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

public record SpatialRangeCoordinates(Range xRange, Range yRange, Range zRange) {

    /**
     * Returns a string representation of the coordinates
     *
     * @return serialize spatial coordinates
     */
    public String toString() {
        return String.format("(%d,%d,%d)", xRange, yRange, zRange);
    }

    /**
     * Deserialize a string representation of spatial coordinates into a {@link SpatialCoordinates}
     *
     * @param coordinates string to deserialize
     * @return new coordinates or null
     */
    public static SpatialCoordinates fromString(final String coordinates) {
        SpatialCoordinates retVal = null;
        if (StringUtils.isNotEmpty(coordinates)) {
            if ('(' == coordinates.charAt(0) && ')' == coordinates.charAt(coordinates.length() - 1)) {
                final String unwrapped = coordinates.substring(1, coordinates.length() - 1);
                final String[] segmentedCoords = StringUtils.splitPreserveAllTokens(unwrapped, ',');
                if (3 == segmentedCoords.length) {
                    retVal = new SpatialCoordinates(Integer.parseInt(segmentedCoords[0]),
                            Integer.parseInt(segmentedCoords[1]), Integer.parseInt(segmentedCoords[2]));
                } else {
                    throw new EvolutionException(coordinates + " is not a valid SpatialCoordinate.");
                }
            } else {
                throw new EvolutionException(coordinates + " is malformed.");
            }
        }

        return retVal;
    }
}
