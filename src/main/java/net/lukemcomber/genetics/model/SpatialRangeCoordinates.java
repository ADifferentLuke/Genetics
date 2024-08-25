package net.lukemcomber.genetics.model;

import net.lukemcomber.genetics.exception.EvolutionException;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

public record SpatialRangeCoordinates(Range<Integer> xRange, Range<Integer> yRange, Range<Integer> zRange) {

    /**
     * Returns a string representation of the coordinates
     *
     * @return serialize spatial coordinates
     */
    public String toString() {
        return String.format("(%s,%s,%s)", xRange.toString(), yRange.toString(), zRange.toString());
    }


}
