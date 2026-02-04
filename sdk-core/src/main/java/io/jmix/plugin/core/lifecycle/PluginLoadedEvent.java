package io.jmix.plugin.core.lifecycle;

import io.jmix.plugin.core.PluginState;

/**
 * Emitted after a plugin has been successfully loaded.
 */
public final class PluginLoadedEvent extends PluginLifecycleEvent {

    public PluginLoadedEvent(String pluginId) {
        super(pluginId, PluginState.LOADED);
    }
}
