package io.jmix.plugin.core.lifecycle;

import io.jmix.plugin.core.PluginState;

/**
 * Emitted when a plugin transitions to the {@link PluginState#ERROR}
 * state during load or unload.
 */
public final class PluginErrorEvent extends PluginLifecycleEvent {

    private final String errorMessage;
    private final transient Throwable cause;

    public PluginErrorEvent(String pluginId, String errorMessage, Throwable cause) {
        super(pluginId, PluginState.ERROR);
        this.errorMessage = errorMessage;
        this.cause = cause;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public Throwable cause() {
        return cause;
    }
}
