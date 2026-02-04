package io.jmix.plugin.core.lifecycle;

import io.jmix.plugin.core.PluginState;

/**
 * Emitted when a plugin is registered with the manager but not yet loaded.
 */
public final class PluginRegisteredEvent extends PluginLifecycleEvent {

    public PluginRegisteredEvent(String pluginId) {
        super(pluginId, PluginState.UNLOADED);
    }
}
