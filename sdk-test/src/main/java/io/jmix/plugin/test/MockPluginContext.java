package io.jmix.plugin.test;

import io.jmix.plugin.core.DefaultPluginContext;
import io.jmix.plugin.core.PluginContext;
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
import java.util.Map;
import java.util.Optional;

/**
 * In-memory {@link PluginContext} pre-configured for unit tests.
 * Combines the default in-memory service implementations with helper
 * methods for stubbing the current user and platform metadata.
 */
public final class MockPluginContext implements PluginContext {

    private final DefaultPluginContext delegate;

    private MockPluginContext(DefaultPluginContext delegate) {
        this.delegate = delegate;
    }

    public static Builder builder(String pluginId) {
        return new Builder(pluginId);
    }

    public static MockPluginContext create(String pluginId) {
        return builder(pluginId).build();
    }

    @Override
    public String getPluginId() {
        return delegate.getPluginId();
    }

    @Override
    public String getPlatformVersion() {
        return delegate.getPlatformVersion();
    }

    @Override
    public String getEnvironment() {
        return delegate.getEnvironment();
    }

    @Override
    public Logger getLogger() {
        return delegate.getLogger();
    }

    @Override
    public EventBus getEventBus() {
        return delegate.getEventBus();
    }

    @Override
    public DataService getDataService() {
        return delegate.getDataService();
    }

    @Override
    public SecurityService getSecurityService() {
        return delegate.getSecurityService();
    }

    @Override
    public UIService getUIService() {
        return delegate.getUIService();
    }

    @Override
    public NotificationService getNotificationService() {
        return delegate.getNotificationService();
    }

    @Override
    public ComponentRegistry getComponentRegistry() {
        return delegate.getComponentRegistry();
    }

    @Override
    public KeyValueStore getLocalStorage() {
        return delegate.getLocalStorage();
    }

    @Override
    public KeyValueStore getSessionStorage() {
        return delegate.getSessionStorage();
    }

    @Override
    public MetricsService getMetrics() {
        return delegate.getMetrics();
    }

    @Override
    public Optional<Object> getConfig(String key) {
        return delegate.getConfig(key);
    }

    @Override
    public void setConfig(String key, Object value) {
        delegate.setConfig(key, value);
    }

    @Override
    public boolean hasConfig(String key) {
        return delegate.hasConfig(key);
    }

    @Override
    public Optional<User> getCurrentUser() {
        return delegate.getCurrentUser();
    }

    @Override
    public List<String> getPermissions() {
        return delegate.getPermissions();
    }

    /**
     * Convenient fluent builder for tests.
     */
    public static final class Builder {

        private final DefaultPluginContext.Builder delegateBuilder;

        private Builder(String pluginId) {
            this.delegateBuilder = DefaultPluginContext.builder(pluginId)
                    .platformVersion("2.0.0")
                    .environment("test");
        }

        public Builder platformVersion(String value) {
            delegateBuilder.platformVersion(value);
            return this;
        }

        public Builder environment(String value) {
            delegateBuilder.environment(value);
            return this;
        }

        public Builder config(String key, Object value) {
            delegateBuilder.config(key, value);
            return this;
        }

        public Builder configuration(Map<String, Object> values) {
            delegateBuilder.configuration(values);
            return this;
        }

        public Builder currentUser(User user) {
            delegateBuilder.currentUser(user);
            return this;
        }

        public Builder permissions(List<String> permissions) {
            delegateBuilder.permissions(permissions);
            return this;
        }

        public Builder dataService(DataService service) {
            delegateBuilder.dataService(service);
            return this;
        }

        public Builder securityService(SecurityService service) {
            delegateBuilder.securityService(service);
            return this;
        }

        public Builder uiService(UIService service) {
            delegateBuilder.uiService(service);
            return this;
        }

        public Builder notificationService(NotificationService service) {
            delegateBuilder.notificationService(service);
            return this;
        }

        public Builder metrics(MetricsService service) {
            delegateBuilder.metrics(service);
            return this;
        }

        public MockPluginContext build() {
            return new MockPluginContext(delegateBuilder.build());
        }
    }
}
