package io.jmix.plugin.core;

import io.jmix.plugin.core.descriptor.PluginDescriptor;

/**
 * Strategy for instantiating a {@link PluginContext} for a freshly
 * registered plugin. The default implementation produces a
 * {@link DefaultPluginContext}; host environments (Spring, Jmix) supply
 * their own factory.
 */
@FunctionalInterface
public interface PluginContextFactory {

    PluginContext create(PluginDescriptor descriptor);

    /**
     * Default factory returning an in-memory {@link DefaultPluginContext}.
     */
    static PluginContextFactory inMemory() {
        return descriptor -> DefaultPluginContext.builder(descriptor.getId()).build();
    }
}
