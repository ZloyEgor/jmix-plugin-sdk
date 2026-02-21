package io.jmix.plugins.advancedgrid.components;

import io.jmix.plugins.advancedgrid.config.AdvancedGridConfig;

import java.util.Map;
import java.util.Optional;

/**
 * Stub loader that materialises an {@link AdvancedGrid} instance from
 * a property map declared in a Jmix descriptor file. The full Jmix
 * loader integration is out of scope for the SDK example.
 */
public final class AdvancedGridLoader {

    private AdvancedGridLoader() {
    }

    public static AdvancedGrid load(Map<String, Object> properties) {
        AdvancedGrid grid = new AdvancedGrid();
        AdvancedGridConfig defaults = AdvancedGridConfig.defaults();
        AdvancedGridConfig config = new AdvancedGridConfig(
                asInt(properties.get("pageSize"), defaults.pageSize()),
                asBool(properties.get("enableVirtualScroll"), defaults.enableVirtualScroll()),
                asBool(properties.get("enableExport"), defaults.enableExport()),
                defaults.exportFormats());
        grid.setConfig(config);
        Optional.ofNullable(properties.get("columns"))
                .filter(v -> v instanceof Iterable<?>)
                .ifPresent(v -> ((Iterable<?>) v).forEach(c -> grid.addColumn(String.valueOf(c))));
        return grid;
    }

    private static int asInt(Object value, int fallback) {
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

    private static boolean asBool(Object value, boolean fallback) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return fallback;
    }
}
