package io.jmix.plugin.core;

/**
 * Runtime context exposed to plugins by {@link JmixPluginManager} during
 * the plugin lifecycle. The interface is intentionally minimal at this
 * stage; platform services (data access, security, UI, notifications,
 * component registry, configuration, storage, metrics, event bus) are
 * introduced incrementally in subsequent modules.
 */
public interface PluginContext {

    /**
     * Identifier of the plugin that owns this context.
     */
    String getPluginId();
}
