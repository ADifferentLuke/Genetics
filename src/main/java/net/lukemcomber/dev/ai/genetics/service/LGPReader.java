package net.lukemcomber.dev.ai.genetics.service;

import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.model.QuadFunction;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

public abstract class LGPReader {

    class RangeValueItem {
        public Range<Integer> xCoordinates;
        public Range<Integer> yCoordinates;
        public Range<Integer> zCoordinates;

        public String value;
    }
    private static final int MINIMUM_COORDINATE_LENGTH = "(x,y,z),k".length();

    RangeValueItem parseItem(final String line, final int xMax, final int yMax, final int zMax) {
        RangeValueItem item = new RangeValueItem();
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
                        item.xCoordinates = parseRange(fields[0], xMax);
                        item.yCoordinates = parseRange(fields[1], yMax);
                        item.zCoordinates = parseRange(fields[2].substring(0,fields[2].length()-1), zMax);

                    } else {
                        throw new EvolutionException(String.format("Unable to parse '%s'.", line));
                    }

                } else {
                    throw new EvolutionException(String.format("Coordinates and value seem incomplete '%s'.", line));
                }
            } else {
                //we have only a value
                item.value = line;
            }
        }
        return item;
    }

    /* Visible for testing */
    protected Range<Integer> parseRange(final String field, final int maxSize) {
        final Range<Integer> retVal;
        if (StringUtils.isBlank(field)) {
            //The entire axis inclusive
            retVal = Range.between(0, maxSize - 1);
        } else {
            switch (field.charAt(0)) {
                case '*':
                    retVal = Range.between(0, maxSize - 1);
                    break;
                case '[':
                    if (']' == field.charAt(field.length() - 1)) {
                        final String newField = field.substring(1, field.length() - 1);
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
                                "Unclosed range found '%s'.", field));
                    }
                    break;
                default: {
                    int coord = Integer.parseInt(field);
                    retVal = Range.between(coord, coord);
                }
            }
        }
        return retVal;
    }

    protected void iterateRangeValue (final RangeValueItem item, final int sizeOfXAxis, final int sizeofYAxis, final int sizeofZAxis,
                            final QuadFunction<Integer,Integer,Integer,String,Boolean> func ){
        if (null != item) {
            final int xUpperBound = null != item.xCoordinates ? item.xCoordinates.getMaximum() : sizeOfXAxis - 1;
            final int xLowerBound = null != item.xCoordinates ? item.xCoordinates.getMinimum() : 0;

            final int yUpperBound = null != item.yCoordinates ? item.yCoordinates.getMaximum() : sizeofYAxis - 1;
            final int yLowerBound = null != item.yCoordinates ? item.yCoordinates.getMinimum() : 0;

            final int zUpperBound = null != item.zCoordinates ? item.zCoordinates.getMaximum() : sizeofZAxis - 1;
            final int zLowerBound = null != item.zCoordinates ? item.zCoordinates.getMinimum() : 0;

            for (int i = xLowerBound; xUpperBound >= i; ++i) {
                for (int j = yLowerBound; yUpperBound >= j; ++j) {
                    for (int k = zLowerBound; zUpperBound >= k; ++k) {
                        func.apply(i,j,k,item.value);
                    }
                }
            }
        }
    }
}
