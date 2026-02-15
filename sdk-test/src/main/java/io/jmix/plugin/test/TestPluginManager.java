package io.jmix.plugin.test;

import io.jmix.plugin.core.JmixPlugin;
import io.jmix.plugin.core.JmixPluginManager;
import io.jmix.plugin.core.PluginContext;
import io.jmix.plugin.core.PluginContextFactory;
import io.jmix.plugin.core.PluginException;
import io.jmix.plugin.core.descriptor.PluginDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link JmixPluginManager} subclass that allows tests to swap the
 * {@link PluginContext} per plugin and pre-register plugins quickly.
 */
public final class TestPluginManager extends JmixPluginManager {

    private final Map<String, PluginContext> contextOverrides = new HashMap<>();

    public TestPluginManager(String platformVersion) {
        this(platformVersion, MockPluginContext.builder("default").build());
    }

    public TestPluginManager(String platformVersion, PluginContext defaultContext) {
        super(platformVersion,
                (PluginContextFactory) descriptor -> defaultContext,
                new io.jmix.plugin.core.descriptor.PluginDescriptorReader());
    }

    /**
     * Registers a context to be returned for a specific plugin id.
     */
    public void registerContext(String pluginId, PluginContext context) {
        contextOverrides.put(pluginId, context);
    }

    /**
     * Registers a plugin instance with a hand-crafted descriptor.
     */
    public void preRegister(JmixPlugin plugin, PluginDescriptor descriptor) throws PluginException {
        register(plugin, descriptor);
    }

    /**
     * Builds a minimal {@link PluginDescriptor} for tests.
     */
    public static PluginDescriptor descriptorOf(String id, String name, String version, String entryFqn) {
        PluginDescriptor d = new PluginDescriptor();
        d.setId(id);
        d.setName(name);
        d.setVersion(version);
        PluginDescriptor.Entrypoints e = new PluginDescriptor.Entrypoints();
        e.setMain(entryFqn);
        d.setEntrypoints(e);
        return d;
    }

    public Map<String, PluginContext> getContextOverrides() {
        return contextOverrides;
    }
}
