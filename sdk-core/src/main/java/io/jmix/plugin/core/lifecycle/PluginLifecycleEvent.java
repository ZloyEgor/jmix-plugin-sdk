package io.jmix.plugin.core.lifecycle;

import io.jmix.plugin.core.PluginState;

import java.time.Instant;
import java.util.Objects;

/**
 * Base type for plugin lifecycle events emitted by the plugin manager.
 */
public sealed abstract class PluginLifecycleEvent
        permits PluginRegisteredEvent, PluginLoadedEvent, PluginUnloadedEvent, PluginErrorEvent {

    private final String pluginId;
    private final PluginState state;
    private final Instant timestamp;

    protected PluginLifecycleEvent(String pluginId, PluginState state) {
        this.pluginId = Objects.requireNonNull(pluginId, "pluginId");
        this.state = Objects.requireNonNull(state, "state");
        this.timestamp = Instant.now();
    }

    public String pluginId() {
        return pluginId;
    }

    public PluginState state() {
        return state;
    }

    public Instant timestamp() {
        return timestamp;
    }
}
