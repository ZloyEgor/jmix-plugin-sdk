package io.jmix.plugins.advancedgrid.config;

import io.jmix.plugin.core.ValidationResult;

import java.util.List;
import java.util.Map;

/**
 * Configuration snapshot for the advanced grid plugin. The record is
 * immutable; callers create a modified copy with {@link #with(String, Object)}.
 */
public record AdvancedGridConfig(
        int pageSize,
        boolean enableVirtualScroll,
        boolean enableExport,
        List<String> exportFormats) {

    public AdvancedGridConfig {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be positive");
        }
        exportFormats = exportFormats == null ? List.of() : List.copyOf(exportFormats);
    }

    public static AdvancedGridConfig defaults() {
        return new AdvancedGridConfig(20, true, true, List.of("xlsx", "csv", "pdf"));
    }

    /**
     * Returns a copy of this configuration with a single property
     * overridden. Unknown keys are ignored.
     */
    public AdvancedGridConfig with(String key, Object value) {
        return switch (key) {
            case "pageSize" -> new AdvancedGridConfig(toInt(value, pageSize),
                    enableVirtualScroll, enableExport, exportFormats);
            case "enableVirtualScroll" -> new AdvancedGridConfig(pageSize,
                    toBool(value, enableVirtualScroll), enableExport, exportFormats);
            case "enableExport" -> new AdvancedGridConfig(pageSize, enableVirtualScroll,
                    toBool(value, enableExport), exportFormats);
            default -> this;
        };
    }

    /**
     * Validates a configuration map without instantiating the record.
     */
    public static ValidationResult validate(Map<String, Object> configuration) {
        if (configuration == null) {
            return ValidationResult.ok();
        }
        ValidationResult.Builder builder = ValidationResult.builder();
        Object pageSize = configuration.get("pageSize");
        if (pageSize instanceof Number n && n.intValue() <= 0) {
            builder.error("pageSize", "pageSize must be positive");
        }
        return builder.build();
    }

    private static int toInt(Object value, int fallback) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private static boolean toBool(Object value, boolean fallback) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return fallback;
    }
}
