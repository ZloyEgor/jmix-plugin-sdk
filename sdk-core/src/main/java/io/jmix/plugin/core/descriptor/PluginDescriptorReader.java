package io.jmix.plugin.core.descriptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jmix.plugin.core.PluginValidationException;
import io.jmix.plugin.core.ValidationResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads {@link PluginDescriptor} instances from {@code plugin.json}
 * payloads. The reader is configured with lenient settings (unknown
 * properties are tolerated) but validates the resulting descriptor for
 * mandatory fields.
 */
public final class PluginDescriptorReader {

    private static final String DESCRIPTOR_ENTRY = "META-INF/plugin.json";

    private final ObjectMapper objectMapper;

    public PluginDescriptorReader() {
        this(defaultObjectMapper());
    }

    public PluginDescriptorReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parses a descriptor from a JSON string.
     */
    public PluginDescriptor read(String json) throws PluginValidationException {
        try {
            PluginDescriptor descriptor = objectMapper.readValue(json, PluginDescriptor.class);
            validate(descriptor);
            return descriptor;
        } catch (JsonProcessingException ex) {
            ValidationResult result = ValidationResult.builder()
                    .error("$", "Invalid plugin.json: " + ex.getOriginalMessage())
                    .build();
            throw new PluginValidationException(result);
        }
    }

    /**
     * Reads a descriptor from a file.
     */
    public PluginDescriptor read(Path file) throws PluginValidationException, IOException {
        return read(Files.readString(file, StandardCharsets.UTF_8));
    }

    /**
     * Reads a descriptor from an {@link InputStream}.
     */
    public PluginDescriptor read(InputStream stream) throws PluginValidationException {
        try {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return read(json);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Validates the descriptor and returns a {@link ValidationResult}.
     */
    public ValidationResult validate(PluginDescriptor descriptor) throws PluginValidationException {
        ValidationResult.Builder builder = ValidationResult.builder();

        if (isBlank(descriptor.getId())) {
            builder.error("id", "Plugin id is required");
        }
        if (isBlank(descriptor.getName())) {
            builder.error("name", "Plugin name is required");
        }
        if (isBlank(descriptor.getVersion())) {
            builder.error("version", "Plugin version is required");
        }
        if (descriptor.getEntrypoints() == null || isBlank(descriptor.getEntrypoints().getMain())) {
            builder.error("entrypoints.main", "Main entry point is required");
        }

        ValidationResult result = builder.build();
        if (!result.valid()) {
            throw new PluginValidationException(result);
        }
        return result;
    }

    /**
     * Returns the entry name used to look up the descriptor inside a JAR.
     */
    public static String descriptorEntry() {
        return DESCRIPTOR_ENTRY;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static ObjectMapper defaultObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }
}
