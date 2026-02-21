package io.jmix.plugins.advancedgrid.models;

import java.util.List;

/**
 * Export job descriptor used by {@link io.jmix.plugins.advancedgrid.services.ExportService}.
 */
public record ExportConfig(
        Format format,
        List<String> columns,
        FilterConfig filter,
        boolean includeHeaders,
        String fileName) {

    public ExportConfig {
        if (format == null) {
            format = Format.XLSX;
        }
        columns = columns == null ? List.of() : List.copyOf(columns);
        if (fileName == null || fileName.isBlank()) {
            fileName = "export." + format.name().toLowerCase();
        }
    }

    public enum Format { XLSX, CSV, PDF }
}
