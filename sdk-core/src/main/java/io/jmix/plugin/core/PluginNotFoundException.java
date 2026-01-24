package io.jmix.plugin.core;

/**
 * Raised when a plugin lookup by identifier fails.
 */
public class PluginNotFoundException extends PluginException {

    private static final long serialVersionUID = 1L;

    private final String pluginId;

    public PluginNotFoundException(String pluginId) {
        super("Plugin not found: " + pluginId);
        this.pluginId = pluginId;
    }

    public String getPluginId() {
        return pluginId;
    }
}
