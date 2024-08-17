package net.lukemcomber.genetics.io;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Abstract class to generically support Json parsers
 *
 * @param <S> parsing context data
 * @param <T> results of the parsing
 */
public abstract class LGPStreamLineReader<S, T> extends SpatialCoordinateRangeParser {


    /**
     * Parse the input stream and return new object. Entry point for Json readers
     *
     * @param initStream the input stream to read from
     * @return the results of the parsing
     * @throws IOException - unable to create local tmp files
     */
    public T parse(final InputStream initStream) throws IOException {

        S s = initPayload();
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(initStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (0 < line.indexOf('#')) {
                    line = line.substring(0, line.indexOf('#'));
                }
                line = line.trim();
                if (StringUtils.isNotEmpty(line)) {
                    parse(line, s);
                }
            }
        }

        return getResult(s);
    }


    /**
     * Initializes the output parameter and any reader setup
     *
     * @return context data
     * @throws IOException
     */
    abstract S initPayload() throws IOException;

    /**
     * Returns the results of parsing
     *
     * @param s context object
     * @return the result
     */
    abstract T getResult(S s);

    /**
     * Parse the provided line using the context data provided
     *
     * @param line string to parse
     * @param s    context data
     */
    abstract void parse(final String line, final S s);

}
