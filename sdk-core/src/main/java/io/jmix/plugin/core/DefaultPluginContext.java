package io.jmix.plugin.core;

import io.jmix.plugin.core.event.EventBus;
import io.jmix.plugin.core.service.ComponentRegistry;
import io.jmix.plugin.core.service.DataService;
import io.jmix.plugin.core.service.KeyValueStore;
import io.jmix.plugin.core.service.MetricsService;
import io.jmix.plugin.core.service.NotificationService;
import io.jmix.plugin.core.service.SecurityService;
import io.jmix.plugin.core.service.UIService;
import io.jmix.plugin.core.service.support.InMemoryComponentRegistry;
import io.jmix.plugin.core.service.support.InMemoryKeyValueStore;
import io.jmix.plugin.core.service.support.InMemoryMetricsService;
import io.jmix.plugin.core.service.support.NoopDataService;
import io.jmix.plugin.core.service.support.NoopSecurityService;
import io.jmix.plugin.core.service.support.NoopUIService;
import io.jmix.plugin.core.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default in-memory {@link PluginContext} implementation. Intended for
 * environments where Jmix runtime services are unavailable (standalone
 * loading of a plugin JAR, test harnesses). Production deployments use
 * the Spring-integrated variant defined in the {@code sdk-spring} module.
 */
public final class DefaultPluginContext implements PluginContext {

    private final String pluginId;
    private final String platformVersion;
    private final String environment;
    private final Logger logger;
    private final EventBus eventBus;
    private final DataService dataService;
    private final SecurityService securityService;
    private final UIService uiService;
    private final NotificationService notificationService;
    private final ComponentRegistry componentRegistry;
    private final KeyValueStore localStorage;
    private final KeyValueStore sessionStorage;
    private final MetricsService metrics;
    private final Map<String, Object> configuration;
    private final User currentUser;
    private final List<String> permissions;

    private DefaultPluginContext(Builder b) {
        this.pluginId = Objects.requireNonNull(b.pluginId, "pluginId");
        this.platformVersion = b.platformVersion;
        this.environment = b.environment;
        this.logger = LoggerFactory.getLogger("plugin." + pluginId);
        this.eventBus = b.eventBus;
        this.dataService = b.dataService;
        this.securityService = b.securityService;
        this.uiService = b.uiService;
        this.notificationService = b.notificationService;
        this.componentRegistry = b.componentRegistry;
        this.localStorage = b.localStorage;
        this.sessionStorage = b.sessionStorage;
        this.metrics = b.metrics;
        this.configuration = new ConcurrentHashMap<>(b.configuration);
        this.currentUser = b.currentUser;
        this.permissions = List.copyOf(b.permissions);
    }

    public static Builder builder(String pluginId) {
        return new Builder(pluginId);
    }

    @Override
    public String getPluginId() {
        return pluginId;
    }

    @Override
    public String getPlatformVersion() {
        return platformVersion;
    }

    @Override
    public String getEnvironment() {
        return environment;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public DataService getDataService() {
        return dataService;
    }

    @Override
    public SecurityService getSecurityService() {
        return securityService;
    }

    @Override
    public UIService getUIService() {
        return uiService;
    }

    @Override
    public NotificationService getNotificationService() {
        return notificationService;
    }

    @Override
    public ComponentRegistry getComponentRegistry() {
        return componentRegistry;
    }

    @Override
    public KeyValueStore getLocalStorage() {
        return localStorage;
    }

    @Override
    public KeyValueStore getSessionStorage() {
        return sessionStorage;
    }

    @Override
    public MetricsService getMetrics() {
        return metrics;
    }

    @Override
    public Optional<Object> getConfig(String key) {
        return Optional.ofNullable(configuration.get(key));
    }

    @Override
    public void setConfig(String key, Object value) {
        if (value == null) {
            configuration.remove(key);
        } else {
            configuration.put(key, value);
        }
    }

    @Override
    public boolean hasConfig(String key) {
        return configuration.containsKey(key);
    }

    @Override
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    @Override
    public List<String> getPermissions() {
        return permissions;
    }

    /**
     * Fluent builder for {@link DefaultPluginContext}. Unspecified
     * services fall back to in-memory or no-op implementations.
     */
    public static final class Builder {

        private final String pluginId;
        private String platformVersion = "0.0.0";
        private String environment = "development";
        private EventBus eventBus = new EventBus();
        private DataService dataService = new NoopDataService();
        private SecurityService securityService = new NoopSecurityService();
        private UIService uiService = new NoopUIService();
        private NotificationService notificationService;
        private ComponentRegistry componentRegistry = new InMemoryComponentRegistry();
        private KeyValueStore localStorage = new InMemoryKeyValueStore();
        private KeyValueStore sessionStorage = new InMemoryKeyValueStore();
        private MetricsService metrics = new InMemoryMetricsService();
        private final Map<String, Object> configuration = new ConcurrentHashMap<>();
        private User currentUser;
        private List<String> permissions = List.of();

        private Builder(String pluginId) {
            this.pluginId = pluginId;
        }

        public Builder platformVersion(String value) {
            this.platformVersion = value;
            return this;
        }

        public Builder environment(String value) {
            this.environment = value;
            return this;
        }

        public Builder eventBus(EventBus value) {
            this.eventBus = value;
            return this;
        }

        public Builder dataService(DataService value) {
            this.dataService = value;
            return this;
        }

        public Builder securityService(SecurityService value) {
            this.securityService = value;
            return this;
        }

        public Builder uiService(UIService value) {
            this.uiService = value;
            return this;
        }

        public Builder notificationService(NotificationService value) {
            this.notificationService = value;
            return this;
        }

        public Builder componentRegistry(ComponentRegistry value) {
            this.componentRegistry = value;
            return this;
        }

        public Builder localStorage(KeyValueStore value) {
            this.localStorage = value;
            return this;
        }

        public Builder sessionStorage(KeyValueStore value) {
            this.sessionStorage = value;
            return this;
        }

        public Builder metrics(MetricsService value) {
            this.metrics = value;
            return this;
        }

        public Builder config(String key, Object value) {
            this.configuration.put(key, value);
            return this;
        }

        public Builder configuration(Map<String, Object> values) {
            this.configuration.putAll(values);
            return this;
        }

        public Builder currentUser(User user) {
            this.currentUser = user;
            return this;
        }

        public Builder permissions(List<String> values) {
            this.permissions = values == null ? List.of() : List.copyOf(values);
            return this;
        }

        public DefaultPluginContext build() {
            if (notificationService == null) {
                notificationService = uiService::showNotification;
            }
            return new DefaultPluginContext(this);
        }
    }
}
