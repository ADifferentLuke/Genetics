package net.lukemcomber.dev.ai.genetics.service;

import net.lukemcomber.dev.ai.genetics.exception.EvolutionException;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//Luke's Genetic Program stream reader ..... yea, I know
public abstract class LGPStreamLineReader<S,T> extends LGPReader{

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
}
