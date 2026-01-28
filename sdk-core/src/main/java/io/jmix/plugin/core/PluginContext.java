package io.jmix.plugin.core;

import io.jmix.plugin.core.event.EventBus;
import io.jmix.plugin.core.service.ComponentRegistry;
import io.jmix.plugin.core.service.DataService;
import io.jmix.plugin.core.service.KeyValueStore;
import io.jmix.plugin.core.service.MetricsService;
import io.jmix.plugin.core.service.NotificationService;
import io.jmix.plugin.core.service.SecurityService;
import io.jmix.plugin.core.service.UIService;
import io.jmix.plugin.core.user.User;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Runtime context exposed to plugins by {@link JmixPluginManager} during
 * the plugin lifecycle. Provides access to platform services, plugin
 * configuration, the event bus and the current user. The interface is
 * deliberately decoupled from any particular platform implementation; the
 * Jmix-aware variant lives in the {@code sdk-spring} module.
 */
public interface PluginContext {

    /**
     * Identifier of the plugin that owns this context.
     */
    String getPluginId();

    /**
     * Reported platform version (semver-compatible). Used by the manager
     * to verify plugin compatibility constraints.
     */
    String getPlatformVersion();

    /**
     * Logical environment label, typically {@code development},
     * {@code production} or {@code test}.
     */
    String getEnvironment();

    /**
     * Logger preconfigured with plugin identifier bindings.
     */
    Logger getLogger();

    /**
     * Event bus dedicated to inter-plugin communication.
     */
    EventBus getEventBus();

    /**
     * Data access facade.
     */
    DataService getDataService();

    /**
     * Security and authorization facade.
     */
    SecurityService getSecurityService();

    /**
     * UI integration facade.
     */
    UIService getUIService();

    /**
     * Notification facade.
     */
    NotificationService getNotificationService();

    /**
     * Component registry for UI components provided by plugins.
     */
    ComponentRegistry getComponentRegistry();

    /**
     * Persistent key/value storage scoped to the plugin.
     */
    KeyValueStore getLocalStorage();

    /**
     * Volatile key/value storage tied to the user session.
     */
    KeyValueStore getSessionStorage();

    /**
     * Metrics reporting facade.
     */
    MetricsService getMetrics();

    /**
     * Resolves an arbitrary configuration key. Implementations are
     * expected to namespace the key with the plugin identifier.
     */
    Optional<Object> getConfig(String key);

    /**
     * Typed variant of {@link #getConfig(String)}.
     */
    @SuppressWarnings("unchecked")
    default <T> Optional<T> getConfig(String key, Class<T> type) {
        return getConfig(key)
                .filter(type::isInstance)
                .map(value -> (T) value);
    }

    /**
     * Stores a configuration value. Implementations may persist or
     * propagate the change to consumers.
     */
    void setConfig(String key, Object value);

    /**
     * Returns {@code true} when a configuration value for the given
     * key is available.
     */
    boolean hasConfig(String key);

    /**
     * Currently authenticated user, or empty when none is available.
     */
    Optional<User> getCurrentUser();

    /**
     * Permissions granted to the current user.
     */
    List<String> getPermissions();

    /**
     * Permission check shortcut.
     */
    default boolean hasPermission(String permission) {
        return getPermissions().contains(permission);
    }
}
