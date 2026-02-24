package io.jmix.plugins.advancedgrid;

import io.jmix.plugin.core.JmixPluginManager;
import io.jmix.plugin.core.descriptor.PluginDescriptor;
import io.jmix.plugin.core.descriptor.PluginDescriptorReader;
import io.jmix.plugin.test.MockPluginContext;
import io.jmix.plugin.test.TestPluginManager;
import io.jmix.plugins.advancedgrid.config.AdvancedGridConfig;
import io.jmix.plugins.advancedgrid.models.ExportConfig;
import io.jmix.plugins.advancedgrid.services.ExportService;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AdvancedGridPluginTest {

    @Test
    void registersComponentTag() throws Exception {
        MockPluginContext context = MockPluginContext.builder("io.jmix.plugins.advanced-grid")
                .config("pageSize", 25)
                .build();
        AdvancedGridPlugin plugin = new AdvancedGridPlugin();
        JmixPluginManager manager = new JmixPluginManager("2.0.0",
                d -> context,
                new PluginDescriptorReader());
        manager.register(plugin, TestPluginManager.descriptorOf(
                "io.jmix.plugins.advanced-grid",
                "Advanced Grid",
                "2.1.0",
                AdvancedGridPlugin.class.getName()));
        manager.load("io.jmix.plugins.advanced-grid");

        assertThat(plugin.getConfigSnapshot().pageSize()).isEqualTo(25);
        assertThat(context.getComponentRegistry().has(AdvancedGridPlugin.COMPONENT_TAG)).isTrue();

        manager.unload("io.jmix.plugins.advanced-grid");
        assertThat(context.getComponentRegistry().has(AdvancedGridPlugin.COMPONENT_TAG)).isFalse();
    }

    @Test
    void exportServiceProducesCsv() {
        ExportService export = new ExportService(AdvancedGridConfig.defaults());
        byte[] payload = export.export(
                List.of(Map.of("id", 1, "name", "Item, A"),
                        Map.of("id", 2, "name", "Item B")),
                new ExportConfig(ExportConfig.Format.CSV, List.of("id", "name"), null, true, "data.csv"));
        String csv = new String(payload);
        assertThat(csv).contains("id,name");
        assertThat(csv).contains("\"Item, A\"");
    }

    @Test
    void descriptorIsReadableFromResources() throws Exception {
        PluginDescriptorReader reader = new PluginDescriptorReader();
        try (InputStream is = AdvancedGridPlugin.class.getResourceAsStream("/META-INF/plugin.json")) {
            assertThat(is).isNotNull();
            PluginDescriptor descriptor = reader.read(is);
            assertThat(descriptor.getEntrypoints().getMain()).isEqualTo(AdvancedGridPlugin.class.getName());
            assertThat(descriptor.getComponents()).isNotEmpty();
        }
    }
}
