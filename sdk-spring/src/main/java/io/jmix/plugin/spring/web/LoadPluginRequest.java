package io.jmix.plugin.spring.web;

import java.util.Map;

/**
 * Optional payload accepted by the {@code POST /api/plugins/{id}/load}
 * endpoint. Allows callers to update plugin configuration as part of
 * the load operation.
 */
public record LoadPluginRequest(Map<String, Object> configuration) {

    public LoadPluginRequest {
        configuration = configuration == null ? Map.of() : Map.copyOf(configuration);
    }
}
