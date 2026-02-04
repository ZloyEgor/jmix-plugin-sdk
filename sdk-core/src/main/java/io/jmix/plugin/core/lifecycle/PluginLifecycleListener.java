package io.jmix.plugin.core.lifecycle;

/**
 * Callback receiver registered on the plugin manager.
 */
public interface PluginLifecycleListener {

    default void onRegistered(PluginRegisteredEvent event) {
    }

    default void onLoaded(PluginLoadedEvent event) {
    }

    default void onUnloaded(PluginUnloadedEvent event) {
    }

    default void onError(PluginErrorEvent event) {
    }
}
