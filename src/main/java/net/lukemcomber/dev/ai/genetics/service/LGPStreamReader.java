package net.lukemcomber.dev.ai.genetics.service;

import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import net.lukemcomber.dev.ai.genetics.model.QuadFunction;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//Luke's Genetic Program stream reader ..... yea, I know
public abstract class LGPStreamReader<T,S> {

    class RangeValueItem {
        public Range<Integer> xCoordinates;
        public Range<Integer> yCoordinates;
        public Range<Integer> zCoordinates;

        public String value;
    }


    private static final int MINIMUM_COORDINATE_LENGTH = "(x,y,z),k".length();

    public T parse(final InputStream initStream) throws IOException {

        S s = initPayload();
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(initStream))){
            String line;
            while ((line = br.readLine()) != null) {
                if (0 < line.indexOf('#')) {
                    line = line.substring(0,line.indexOf('#') );
                }
                line = line.trim();
                if (StringUtils.isNotEmpty(line)) {
                    parse(line,s);
                }
            }
        }

        return getResult(s);
    }


    abstract S initPayload();
    //class private
    abstract T getResult(S s);
    abstract void parse(final String line, final S s);

    Pair<String, String> requireNameValue(final String str) {
        final String[] pair = StringUtils.split(str, '=');
        if (2 == pair.length && StringUtils.isNotEmpty(pair[0]) && StringUtils.isNotEmpty(pair[1])) {
            return new Pair<String, String>() {
                @Override
                public String getLeft() {
                    return pair[0];
                }

                @Override
                public String getRight() {
                    return pair[1];
                }

                @Override
                public String setValue(String value) {
                    throw new NotImplementedException();
                }
            };
        } else {
            throw new EvolutionException("Expect name value syntax but found [" + str + "].");
        }
    }

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
    Range<Integer> parseRange(final String field, final int maxSize) {
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

    void iterateRangeValue (final RangeValueItem item, final int sizeOfXAxis, final int sizeofYAxis, final int sizeofZAxis,
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
