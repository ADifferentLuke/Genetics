package net.lukemcomber.genetics.universes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.lukemcomber.genetics.model.UniverseConstants;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CustomUniverse extends UniverseConstants {
    public CustomUniverse(final File inputFile) {
        super(CustomUniverse.read(inputFile));
    }

    public CustomUniverse(final Map<String,Object> properties){
        super(properties);
    }

    private static Map<String, Object> read(final File file) {
        final ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> retVal = new HashMap<>();
        if (file.exists()) {
            try (final InputStream inputStream = new FileInputStream(file)) {
                retVal = mapper.readValue(inputStream, new TypeReference<Map<String, Object>>(){});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return retVal;
    }
}
