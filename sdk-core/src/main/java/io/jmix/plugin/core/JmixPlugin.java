package io.jmix.plugin.core;

import io.jmix.plugin.core.descriptor.PluginDescriptor;
import io.jmix.plugin.core.event.EventBus;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Base class for Jmix plugins.
 *
 * <p>A plugin is a self-contained unit of functionality discovered and
 * managed by {@link JmixPluginManager}. Subclasses implement
 * {@link #onLoad(PluginContext)} to register components and event listeners,
 * and {@link #onUnload()} to release allocated resources.</p>
 *
 * <p>Lifecycle and dependency state are owned by the manager: subclasses
 * should not override the package-private setters. The {@code descriptor}
 * and {@code context} fields become available once the manager has
 * registered and bound the plugin.</p>
 */
public abstract class JmixPlugin {

    private volatile PluginDescriptor descriptor;
    private volatile PluginContext context;
    private volatile boolean loaded;

    /**
     * Invoked by the manager when the plugin must initialize.
     * Implementations should register components, event listeners and
     * any background services they need.
     *
     * @param context runtime context for this plugin
     * @throws PluginException if initialization cannot be completed
     */
    public abstract void onLoad(PluginContext context) throws PluginException;

    /**
     * Invoked by the manager during shutdown of the plugin. Implementations
     * must release allocated resources and unregister callbacks.
     *
     * @throws PluginException if shutdown cannot be completed
     */
    public abstract void onUnload() throws PluginException;

    /**
     * Optional hook called when a configuration key has been updated at
     * runtime. The default implementation is a no-op.
     */
    public void onConfigChange(String key, Object value) throws PluginException {
        // default no-op
    }

    /**
     * Optional plugin-specific validation of the configuration map.
     * Default implementation accepts any configuration.
     */
    public ValidationResult validateConfig(Map<String, Object> configuration) {
        return ValidationResult.ok();
    }

    /**
     * Identifier of the plugin, taken from the descriptor when known.
     */
    public String getId() {
        PluginDescriptor d = descriptor;
        return d == null || d.getId() == null ? "unknown" : d.getId();
    }

    /**
     * Human-readable name of the plugin.
     */
    public String getName() {
        PluginDescriptor d = descriptor;
        return d == null || d.getName() == null ? "Unknown Plugin" : d.getName();
    }

    /**
     * Semantic version of the plugin.
     */
    public String getVersion() {
        PluginDescriptor d = descriptor;
        return d == null || d.getVersion() == null ? "0.0.0" : d.getVersion();
    }

    /**
     * Plugin descriptor as bound by the manager. May return {@code null}
     * when the plugin is constructed but not yet registered.
     */
    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Returns {@code true} when the plugin is in the {@link PluginState#LOADED}
     * state from the perspective of the manager.
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Provides the runtime context once the plugin has been bound by the
     * manager. Returns {@code null} for plugins that have not been
     * registered yet.
     */
    protected PluginContext getContext() {
        return context;
    }

    /**
     * Convenience accessor for the plugin-scoped logger.
     */
    protected Logger logger() {
        return requireContext().getLogger();
    }

    /**
     * Convenience accessor for the plugin event bus.
     */
    protected EventBus eventBus() {
        return requireContext().getEventBus();
    }

    /**
     * Emits an event through the plugin event bus.
     */
    protected void emit(String topic, Object payload) {
        eventBus().emit(topic, payload);
    }

    /**
     * Subscribes a listener to a topic on the plugin event bus.
     */
    protected EventBus.Subscription on(String topic, Consumer<Object> listener) {
        return eventBus().on(topic, listener);
    }

    /**
     * Reads a configuration value using the runtime context.
     */
    protected Optional<Object> getConfig(String key) {
        return requireContext().getConfig(key);
    }

    /**
     * Reads a typed configuration value, falling back to {@code defaultValue}
     * when the key is missing or has the wrong type.
     */
    protected <T> T getConfig(String key, T defaultValue, Class<T> type) {
        return requireContext().getConfig(key, type).orElse(defaultValue);
    }

    /**
     * Persists a configuration value via the runtime context.
     */
    protected void setConfig(String key, Object value) {
        requireContext().setConfig(key, value);
    }

    private PluginContext requireContext() {
        return Objects.requireNonNull(context,
                "Plugin context is not available before the plugin has been bound by the manager");
    }

    /**
     * Internal mutator invoked by {@code JmixPluginManager}. Not intended
     * for direct use by plugin code.
     */
    void bind(PluginDescriptor descriptor, PluginContext context) {
        this.descriptor = descriptor;
        this.context = context;
    }

    /**
     * Internal mutator invoked by {@code JmixPluginManager} to mark the
     * plugin as loaded.
     */
    void markLoaded() {
        this.loaded = true;
    }

    /**
     * Internal mutator invoked by {@code JmixPluginManager} to mark the
     * plugin as unloaded.
     */
    void markUnloaded() {
        this.loaded = false;
    }
}
