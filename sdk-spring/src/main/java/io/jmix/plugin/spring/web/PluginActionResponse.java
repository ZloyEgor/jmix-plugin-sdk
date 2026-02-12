package io.jmix.plugin.spring.web;

/**
 * Generic response for plugin lifecycle actions.
 */
public record PluginActionResponse(String pluginId, String status, String message) {

    public static PluginActionResponse ok(String pluginId, String status) {
        return new PluginActionResponse(pluginId, status, null);
    }
}
