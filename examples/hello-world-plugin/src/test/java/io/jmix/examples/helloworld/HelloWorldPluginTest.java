package io.jmix.examples.helloworld;

import io.jmix.plugin.core.JmixPluginManager;
import io.jmix.plugin.core.PluginException;
import io.jmix.plugin.core.descriptor.PluginDescriptor;
import io.jmix.plugin.core.descriptor.PluginDescriptorReader;
import io.jmix.plugin.test.JmixPluginTest;
import io.jmix.plugin.test.MockPluginContext;
import io.jmix.plugin.test.TestPluginManager;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@JmixPluginTest(pluginId = "io.jmix.examples.hello-world")
class HelloWorldPluginTest {

    @Test
    void contextIsInjected(MockPluginContext context) {
        assertThat(context.getPluginId()).isEqualTo("io.jmix.examples.hello-world");
        assertThat(context.getEnvironment()).isEqualTo("test");
    }

    @Test
    void pluginLoadsViaTestManager(MockPluginContext context, TestPluginManager manager) throws Exception {
        HelloWorldPlugin plugin = new HelloWorldPlugin();
        PluginDescriptor descriptor = TestPluginManager.descriptorOf(
                "io.jmix.examples.hello-world",
                "Hello World Plugin",
                "1.0.0",
                HelloWorldPlugin.class.getName());

        manager.registerContext("io.jmix.examples.hello-world", context);
        manager.preRegister(plugin, descriptor);
        manager.load("io.jmix.examples.hello-world");

        assertThat(plugin.isLoaded()).isTrue();
        assertThat(plugin.getId()).isEqualTo("io.jmix.examples.hello-world");

        manager.unload("io.jmix.examples.hello-world");
        assertThat(plugin.isLoaded()).isFalse();
    }

    @Test
    void descriptorIsReadableFromResources() throws Exception {
        PluginDescriptorReader reader = new PluginDescriptorReader();
        try (InputStream is = HelloWorldPlugin.class.getResourceAsStream("/META-INF/plugin.json")) {
            assertThat(is).isNotNull();
            PluginDescriptor descriptor = reader.read(is);
            assertThat(descriptor.getId()).isEqualTo("io.jmix.examples.hello-world");
            assertThat(descriptor.getEntrypoints().getMain()).isEqualTo(HelloWorldPlugin.class.getName());
        }
    }

    @Test
    void respondsToConfigChange(MockPluginContext context) throws PluginException {
        HelloWorldPlugin plugin = new HelloWorldPlugin();
        JmixPluginManager manager = new JmixPluginManager("2.0.0",
                d -> context,
                new PluginDescriptorReader());
        manager.register(plugin, TestPluginManager.descriptorOf(
                "io.jmix.examples.hello-world",
                "Hello World Plugin",
                "1.0.0",
                HelloWorldPlugin.class.getName()));
        manager.load("io.jmix.examples.hello-world");
        plugin.onConfigChange("greeting", "Привет!");
        assertThat(plugin.isLoaded()).isTrue();
    }
}
