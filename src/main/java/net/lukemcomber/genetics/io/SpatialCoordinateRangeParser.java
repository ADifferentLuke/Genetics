package net.lukemcomber.genetics.io;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.model.SpatialRangeCoordinates;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

import java.util.function.BiFunction;

/**
 * An abstract class for adding support for parsing coordinate ranges. <br/>
 * Format: <br/>
 * (RANGE,RANGE,RANGE) <br/>
 * RANGE := SUBRANGE | INTEGER | WILDCARD <br/>
 * SUBRANGE := [INTEGER-INTEGER] <br/>
 * WILDCARD := *
 */
public abstract class SpatialCoordinateRangeParser {

    class RangeValueItem {
        public SpatialRangeCoordinates rangeCoordinates;

        public String value;
    }

    private static final int MINIMUM_COORDINATE_LENGTH = "(x,y,z),k".length();

    /**
     * Parses the input line and return
     *
     * @param line the line to parse
     * @param size the maximum size of the simulation space
     * @return object containing left over string and parsed ranges
     */
    RangeValueItem parseItem(final String line, final SpatialCoordinates size) {
        final RangeValueItem item = new RangeValueItem();
        if (StringUtils.isNotEmpty(line)) {
            if ('(' == line.charAt(0)) {
                //We have some coords
                if (MINIMUM_COORDINATE_LENGTH <= line.length() && 1 < line.indexOf(')')) {
                    final String[] fields = StringUtils.splitPreserveAllTokens(line.substring(1), ',');
                    if (4 == fields.length) {
                        /*
                         * NOTE: the 3 field will still have the closing param. We'll need to remove it
                         *
                         * Field [0]: x || [x1-x2]
                         * Field [1]: y || [y1-y2]
                         * Field [2]: z || [z1-z2])
                         * Field [3]: value
                         */
                        item.value = fields[3];
                        item.rangeCoordinates = new SpatialRangeCoordinates(
                                parseRange(fields[0], size.xAxis()),
                                parseRange(fields[1], size.yAxis()),
                                parseRange(fields[2].substring(0, fields[2].length() - 1), size.zAxis())
                        );

                    } else {
                        throw new EvolutionException(String.format("Unable to parse '%s'.", line));
                    }

                } else {
                    throw new EvolutionException(String.format("SpatialCoordinates and value seem incomplete '%s'.", line));
                }
            } else {
                //we have only a value
                item.value = line;
            }
        }
        return item;
    }

    /**
     * Parses a range across one axis using the max size as a boundary. If the range
     * extends beyond the maximum size, an {@link EvolutionException} is thrown.
     *
     * @param possibleRange string of the possible range
     * @param maxSize       maximum size
     * @return Range object
     */
    /* Visible for testing */
    protected Range<Integer> parseRange(final String possibleRange, final int maxSize) {
        final Range<Integer> retVal;
        if (StringUtils.isBlank(possibleRange)) {
            //The entire axis inclusive
            retVal = Range.between(0, maxSize - 1);
        } else {
            switch (possibleRange.charAt(0)) {
                case '*':
                    retVal = Range.between(0, maxSize - 1);
                    break;
                case '[':
                    if (']' == possibleRange.charAt(possibleRange.length() - 1)) {
                        final String newField = possibleRange.substring(1, possibleRange.length() - 1);
                        final String[] parsedRange = StringUtils.split(newField, '-');
                        if (2 == parsedRange.length) {

                            final Integer min = Integer.parseInt(parsedRange[0]);
                            final Integer max = Integer.parseInt(parsedRange[1]);
                            if (0 <= min && 0 <= max && maxSize > max && maxSize > min) {
                                retVal = Range.between(min, max);
                            } else {
                                throw new EvolutionException(String.format(
                                        "Invalid terrain coordinate. Values (%d,%d) must be between 0 and %d.", min, max, maxSize));
                            }
                        } else {
                            throw new EvolutionException((String.format("Invalid number of bounderies for range '%s'.", newField)));
                        }
                    } else {
                        throw new EvolutionException(String.format(
                                "Unclosed range found '%s'.", possibleRange));
                    }
                    break;
                default: {
                    final int coord = Integer.parseInt(possibleRange);
                    retVal = Range.between(coord, coord);
                }
            }
        }
        return retVal;
    }

    /**
     * Utility method of executing a function over a range of coordinates
     * @param item Range and Item mappings
     * @param spatialMaxAxis size of simulation
     * @param func function to call on each spatial coordinate
     */
    protected void iterateRangeValue(final RangeValueItem item, final SpatialCoordinates spatialMaxAxis,
                                     final BiFunction<SpatialCoordinates, String, Boolean> func) {
        if (null != item) {
            final int xUpperBound = null != item.rangeCoordinates ? item.rangeCoordinates.xRange().getMaximum() : spatialMaxAxis.xAxis() - 1;
            final int xLowerBound = null != item.rangeCoordinates ? item.rangeCoordinates.xRange().getMinimum() : 0;

            final int yUpperBound = null != item.rangeCoordinates ? item.rangeCoordinates.yRange().getMaximum() : spatialMaxAxis.yAxis() - 1;
            final int yLowerBound = null != item.rangeCoordinates ? item.rangeCoordinates.yRange().getMinimum() : 0;

            final int zUpperBound = null != item.rangeCoordinates ? item.rangeCoordinates.zRange().getMaximum() : spatialMaxAxis.zAxis() - 1;
            final int zLowerBound = null != item.rangeCoordinates ? item.rangeCoordinates.zRange().getMinimum() : 0;

            for (int i = xLowerBound; xUpperBound >= i; ++i) {
                for (int j = yLowerBound; yUpperBound >= j; ++j) {
                    for (int k = zLowerBound; zUpperBound >= k; ++k) {

                        func.apply(new SpatialCoordinates(i, j, k), item.value);
                    }
                }
            }
        }
    }
}
