package net.lukemcomber.genetics.store.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.UniverseConstants;
import net.lukemcomber.genetics.store.Metadata;
import net.lukemcomber.genetics.store.MetadataStore;
import net.lukemcomber.genetics.store.Primary;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class MetadataStorage {

    private static final Logger logger = Logger.getLogger(MetadataStorage.class.getName());

    public static final String PROPERTY_METADATA_EXPORT_TEMPLATE = "metadata.%s.export";
    public static final String PROPERTY_TYPE_PATH = "metadata.export.path";
    public static final String PROPERTY_FILE_POSTFIX = "metadata.export.postfix";
    public static final String PROPERTY_EXPORT_CHUNK_SIZE = "metadata.export.chunk.size";

    public static String persist(final MetadataStore<? extends Metadata> store, final String simulation, final UniverseConstants properties) {

        final ObjectMapper mapper = new ObjectMapper();
        String resultPath = null;
        final String property = PROPERTY_METADATA_EXPORT_TEMPLATE.formatted(store.type().getSimpleName());
        if (properties.get(property, Boolean.class, false)) {

            final String basePath = properties.get(PROPERTY_TYPE_PATH, String.class);
            final String fullPath = (basePath.endsWith(File.separator) ? basePath.substring(0, basePath.length() - 1) : basePath) +
                    File.separator +
                    (simulation.endsWith(File.separator) ? simulation.substring(0, simulation.length() - 1) : simulation);
            final String filePostfix = properties.get(PROPERTY_FILE_POSTFIX, String.class, "");

            final String fullOutputPathString = "%s/%s%s.txt.gz".formatted(
                    fullPath,
                    store.type().getSimpleName(),
                    StringUtils.isNotBlank(filePostfix) ? "_" + filePostfix : "");

            final Path fullOutputPathAndFile = Path.of(fullOutputPathString);

            try {
                Files.createDirectories(Paths.get(fullPath));
            } catch (final IOException e) {
                throw new EvolutionException("Failed to create output path [%s].".formatted(fullPath));
            }

            logger.info("Exporting data to %s".formatted(fullOutputPathString));


            try (final FileOutputStream output = new FileOutputStream(fullOutputPathAndFile.toFile());
                 final Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), StandardCharsets.UTF_8)) {

                // Get the Primary index
                final Class<?> clazz = store.type();
                String primaryIndex = null;
                // sequential search ... nice
                for (final Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Primary.class)) {
                        primaryIndex = field.getAnnotation(Primary.class).name();
                        break;
                    }
                }
                if (StringUtils.isEmpty(primaryIndex)) {
                    writer.close();
                    throw new EvolutionException("Type %s has no primary index. Data cannot be exported.".formatted(store.type().getSimpleName()));
                } else {
                    final int chunkSize = properties.get(PROPERTY_EXPORT_CHUNK_SIZE, int.class, 1000);
                    int pageNumber = 0;
                    List<? extends Metadata> page;

                    do {
                        // we got the records, now write them out!!!
                        page = store.page(pageNumber++, chunkSize);
                        if(Objects.nonNull(page)) {
                            page.forEach(metadata -> {
                                try {
                                    final String json = mapper.writeValueAsString(metadata);
                                    writer.write(json);
                                    writer.write("\n");
                                } catch (final IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    } while (0 < page.size());
                    writer.flush();
                    writer.close();
                }
                resultPath = fullOutputPathString;
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        return resultPath;
    }

}