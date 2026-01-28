package io.jmix.plugin.core.view;

import java.util.Map;
import java.util.Objects;

/**
 * Minimal description of a navigable view.
 */
public record View(String id, String title, Map<String, Object> params) {

    public View {
        Objects.requireNonNull(id, "id");
        if (title == null) {
            title = id;
        }
        params = params == null ? Map.of() : Map.copyOf(params);
    }
}
