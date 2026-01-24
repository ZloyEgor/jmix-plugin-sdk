package io.jmix.plugin.core;

/**
 * Lifecycle state of a plugin instance managed by {@link JmixPluginManager}.
 */
public enum PluginState {

    /** Plugin descriptor is known but the plugin is not active. */
    UNLOADED,

    /** Plugin is being loaded. */
    LOADING,

    /** Plugin is fully initialized and ready to serve requests. */
    LOADED,

    /** Plugin is being unloaded. */
    UNLOADING,

    /** Plugin failed to load or unload. */
    ERROR
}
