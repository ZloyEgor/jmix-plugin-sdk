package io.jmix.plugin.core.descriptor;

import io.jmix.plugin.core.PluginValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PluginDescriptorReaderTest {

    private final PluginDescriptorReader reader = new PluginDescriptorReader();

    @Test
    void readsValidDescriptor() throws Exception {
        String json = """
                {
                  "id": "io.jmix.example",
                  "name": "Example",
                  "version": "1.2.3",
                  "entrypoints": { "main": "io.jmix.example.ExamplePlugin" },
                  "compatibility": { "jmix": ">=2.0.0 <3.0.0" },
                  "components": [
                    { "tag": "demo", "class": "io.jmix.example.Demo" }
                  ]
                }
                """;
        PluginDescriptor d = reader.read(json);
        assertThat(d.getId()).isEqualTo("io.jmix.example");
        assertThat(d.getName()).isEqualTo("Example");
        assertThat(d.getVersion()).isEqualTo("1.2.3");
        assertThat(d.getEntrypoints().getMain()).isEqualTo("io.jmix.example.ExamplePlugin");
        assertThat(d.getCompatibility().getJmix()).isEqualTo(">=2.0.0 <3.0.0");
        assertThat(d.getComponents()).hasSize(1);
        assertThat(d.getComponents().get(0).getTag()).isEqualTo("demo");
        assertThat(d.getComponents().get(0).getClassName()).isEqualTo("io.jmix.example.Demo");
    }

    @Test
    void rejectsDescriptorWithoutId() {
        String json = """
                { "name": "no id", "version": "1.0.0",
                  "entrypoints": { "main": "x.Y" } }
                """;
        assertThatThrownBy(() -> reader.read(json))
                .isInstanceOf(PluginValidationException.class)
                .hasMessageContaining("id");
    }

    @Test
    void rejectsDescriptorWithoutEntrypoint() {
        String json = """
                { "id": "x", "name": "x", "version": "1.0.0" }
                """;
        assertThatThrownBy(() -> reader.read(json))
                .isInstanceOf(PluginValidationException.class)
                .hasMessageContaining("entrypoints.main");
    }

    @Test
    void rejectsMalformedJson() {
        assertThatThrownBy(() -> reader.read("not json"))
                .isInstanceOf(PluginValidationException.class);
    }
}
