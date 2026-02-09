package io.jmix.plugin.spring;

import io.jmix.plugin.core.DefaultPluginContext;
import io.jmix.plugin.core.PluginContext;
import io.jmix.plugin.core.descriptor.PluginDescriptor;
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
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Optional;

/**
 * {@link PluginContext} backed by the Spring {@link ApplicationContext}.
 * Services exposed to plugins are resolved as Spring beans when present,
 * otherwise the SDK's in-memory defaults are used.
 */
public class SpringPluginContext implements PluginContext {

    private final ApplicationContext applicationContext;
    private final DefaultPluginContext delegate;

    public SpringPluginContext(ApplicationContext applicationContext, PluginDescriptor descriptor,
                               PluginProperties properties) {
        this.applicationContext = applicationContext;
        this.delegate = DefaultPluginContext.builder(descriptor.getId())
                .platformVersion(properties.getPlatformVersion())
                .environment(detectEnvironment(applicationContext))
                .eventBus(beanOrDefault(EventBus.class, EventBus::new))
                .dataService(beanOrDefault(DataService.class, () -> new io.jmix.plugin.core.service.support.NoopDataService()))
                .securityService(beanOrDefault(SecurityService.class, () -> new io.jmix.plugin.core.service.support.NoopSecurityService()))
                .uiService(beanOrDefault(UIService.class, () -> new io.jmix.plugin.core.service.support.NoopUIService()))
                .componentRegistry(beanOrDefault(ComponentRegistry.class, () -> new io.jmix.plugin.core.service.support.InMemoryComponentRegistry()))
                .metrics(beanOrDefault(MetricsService.class, () -> new io.jmix.plugin.core.service.support.InMemoryMetricsService()))
                .localStorage(beanOrDefault(KeyValueStore.class, () -> new io.jmix.plugin.core.service.support.InMemoryKeyValueStore()))
                .sessionStorage(beanOrDefault(KeyValueStore.class, () -> new io.jmix.plugin.core.service.support.InMemoryKeyValueStore()))
                .build();
    }

    private static String detectEnvironment(ApplicationContext ctx) {
        String[] profiles = ctx.getEnvironment().getActiveProfiles();
        if (profiles.length > 0) {
            return profiles[0];
        }
        return "default";
    }

    private <T> T beanOrDefault(Class<T> type, java.util.function.Supplier<T> fallback) {
        try {
            return applicationContext.getBean(type);
        } catch (BeansException ex) {
            return fallback.get();
        }
    }

    /**
     * Direct access to the underlying {@link ApplicationContext} so
     * plugins can look up Spring beans not exposed through the SDK
     * service interfaces.
     */
    public <T> T getBean(Class<T> type) {
        return applicationContext.getBean(type);
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
        return delegate.getConfig(key)
                .or(() -> Optional.ofNullable(applicationContext.getEnvironment().getProperty(key)));
    }

    @Override
    public void setConfig(String key, Object value) {
        delegate.setConfig(key, value);
    }

    @Override
    public boolean hasConfig(String key) {
        return delegate.hasConfig(key) || applicationContext.getEnvironment().containsProperty(key);
    }

    @Override
    public Optional<User> getCurrentUser() {
        return delegate.getCurrentUser();
    }

    @Override
    public List<String> getPermissions() {
        return delegate.getPermissions();
    }
}
