package io.jmix.plugin.spring;

import io.jmix.plugin.core.lifecycle.PluginErrorEvent;
import io.jmix.plugin.core.lifecycle.PluginLifecycleListener;
import io.jmix.plugin.core.lifecycle.PluginLoadedEvent;
import io.jmix.plugin.core.lifecycle.PluginRegisteredEvent;
import io.jmix.plugin.core.lifecycle.PluginUnloadedEvent;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Bridge that forwards plugin lifecycle events from the SDK manager to
 * the Spring {@link ApplicationEventPublisher}. Applications can react
 * to plugin state changes with regular {@code @EventListener} methods.
 */
public class PluginLifecycleEventPublisher implements PluginLifecycleListener {

    private final ApplicationEventPublisher publisher;

    public PluginLifecycleEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void onRegistered(PluginRegisteredEvent event) {
        publisher.publishEvent(event);
    }

    @Override
    public void onLoaded(PluginLoadedEvent event) {
        publisher.publishEvent(event);
    }

    @Override
    public void onUnloaded(PluginUnloadedEvent event) {
        publisher.publishEvent(event);
    }

    @Override
    public void onError(PluginErrorEvent event) {
        publisher.publishEvent(event);
    }
}
