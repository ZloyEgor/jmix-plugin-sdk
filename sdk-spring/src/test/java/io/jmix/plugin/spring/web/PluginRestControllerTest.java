package io.jmix.plugin.spring.web;

import io.jmix.plugin.core.JmixPlugin;
import io.jmix.plugin.core.JmixPluginManager;
import io.jmix.plugin.core.PluginContext;
import io.jmix.plugin.core.PluginException;
import io.jmix.plugin.core.descriptor.PluginDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PluginRestControllerTest {

    private JmixPluginManager manager;
    private PluginRestController controller;

    @BeforeEach
    void setUp() throws PluginException {
        manager = new JmixPluginManager("2.0.0");
        controller = new PluginRestController(manager);

        PluginDescriptor descriptor = new PluginDescriptor();
        descriptor.setId("plugin.web");
        descriptor.setName("Web Plugin");
        descriptor.setVersion("1.0.0");
        PluginDescriptor.Entrypoints e = new PluginDescriptor.Entrypoints();
        e.setMain("io.jmix.plugin.spring.web.PluginRestControllerTest$Stub");
        descriptor.setEntrypoints(e);

        manager.register(new Stub(), descriptor);
    }

    @Test
    void listReturnsRegisteredPlugins() {
        var list = controller.list();
        assertThat(list).hasSize(1);
        assertThat(list.get(0).id()).isEqualTo("plugin.web");
    }

    @Test
    void loadAndUnloadTransitionStates() throws Exception {
        controller.load("plugin.web", null);
        assertThat(manager.isLoaded("plugin.web")).isTrue();
        controller.unload("plugin.web");
        assertThat(manager.isLoaded("plugin.web")).isFalse();
    }

    @Test
    void getThrowsForMissingPlugin() {
        assertThatThrownBy(() -> controller.get("missing"))
                .hasMessageContaining("missing");
    }

    static class Stub extends JmixPlugin {
        @Override public void onLoad(PluginContext context) {}
        @Override public void onUnload() {}
    }
}
