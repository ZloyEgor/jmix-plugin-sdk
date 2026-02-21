package io.jmix.plugins.advancedgrid.components;

import io.jmix.plugins.advancedgrid.config.AdvancedGridConfig;
import io.jmix.plugins.advancedgrid.models.FilterConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Framework-agnostic skeleton of the {@code advancedGrid} component.
 * The real implementation lives in the Jmix FlowUI extension; this
 * class compiles independently and is registered with the plugin
 * component registry so that the host can resolve the FQCN at runtime.
 */
public class AdvancedGrid {

    private AdvancedGridConfig config = AdvancedGridConfig.defaults();
    private FilterConfig filter;
    private final List<String> columns = new ArrayList<>();
    private final List<Object> rows = new ArrayList<>();

    public AdvancedGrid() {
        // required for reflective registration via plugin.json#components
    }

    public AdvancedGridConfig getConfig() {
        return config;
    }

    public AdvancedGrid setConfig(AdvancedGridConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        return this;
    }

    public FilterConfig getFilter() {
        return filter;
    }

    public AdvancedGrid setFilter(FilterConfig filter) {
        this.filter = filter;
        return this;
    }

    public AdvancedGrid addColumn(String column) {
        if (column != null && !column.isBlank()) {
            columns.add(column);
        }
        return this;
    }

    public List<String> getColumns() {
        return List.copyOf(columns);
    }

    public AdvancedGrid setRows(List<?> rows) {
        this.rows.clear();
        if (rows != null) {
            this.rows.addAll(rows);
        }
        return this;
    }

    public List<Object> getRows() {
        return List.copyOf(rows);
    }
}
