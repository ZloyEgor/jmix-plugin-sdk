package io.jmix.plugin.core.lifecycle;

import io.jmix.plugin.core.PluginState;

/**
 * Emitted after a plugin has been unloaded by the manager.
 */
public final class PluginUnloadedEvent extends PluginLifecycleEvent {

    public PluginUnloadedEvent(String pluginId) {
        super(pluginId, PluginState.UNLOADED);
    }
}
