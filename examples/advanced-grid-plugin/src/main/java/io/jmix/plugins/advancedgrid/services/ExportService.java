package io.jmix.plugins.advancedgrid.services;

import io.jmix.plugins.advancedgrid.config.AdvancedGridConfig;
import io.jmix.plugins.advancedgrid.models.ExportConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Skeleton export service. The actual export backend (Apache POI for
 * XLSX, OpenCSV for CSV, etc.) is not bundled with the SDK example; the
 * service returns CSV-formatted output as a portable default.
 */
public class ExportService {

    private AdvancedGridConfig config;

    public ExportService(AdvancedGridConfig config) {
        this.config = config;
    }

    /**
     * Serialises the rows to a string in the requested format. Returns
     * an empty string when the row collection is empty.
     */
    public byte[] export(List<Map<String, Object>> rows, ExportConfig exportConfig) {
        if (!config.enableExport()) {
            throw new IllegalStateException("Export is disabled by configuration");
        }
        if (rows == null || rows.isEmpty()) {
            return new byte[0];
        }
        return switch (exportConfig.format()) {
            case CSV -> toCsv(rows, exportConfig).getBytes();
            case XLSX, PDF -> toCsv(rows, exportConfig).getBytes();
        };
    }

    public void refreshConfig(AdvancedGridConfig config) {
        this.config = config;
    }

    public AdvancedGridConfig getConfig() {
        return config;
    }

    private String toCsv(List<Map<String, Object>> rows, ExportConfig exportConfig) {
        List<String> columns = exportConfig.columns().isEmpty()
                ? new ArrayList<>(rows.get(0).keySet())
                : exportConfig.columns();

        StringBuilder sb = new StringBuilder();
        if (exportConfig.includeHeaders()) {
            sb.append(String.join(",", columns)).append('\n');
        }
        for (Map<String, Object> row : rows) {
            sb.append(columns.stream()
                    .map(col -> escape(row.get(col)))
                    .collect(Collectors.joining(",")));
            sb.append('\n');
        }
        return sb.toString();
    }

    private static String escape(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        if (str.contains(",") || str.contains("\"")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }
}
