package io.jmix.plugin.spring;

import io.jmix.plugin.core.JmixPluginManager;
import io.jmix.plugin.core.PluginContextFactory;
import io.jmix.plugin.core.descriptor.PluginDescriptorReader;
import io.jmix.plugin.spring.web.PluginRestController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PluginAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PluginAutoConfiguration.class));

    @Test
    void registersDefaultBeansWhenEnabled() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(JmixPluginManager.class);
            assertThat(ctx).hasSingleBean(PluginContextFactory.class);
            assertThat(ctx).hasSingleBean(PluginDescriptorReader.class);
            assertThat(ctx).hasSingleBean(PluginProperties.class);
            assertThat(ctx).hasSingleBean(PluginLifecycleEventPublisher.class);
        });
    }

    @Test
    void skipsAutoConfigurationWhenDisabled() {
        runner.withPropertyValues("jmix.plugin.enabled=false")
                .run(ctx -> assertThat(ctx).doesNotHaveBean(JmixPluginManager.class));
    }

    @Test
    void picksUpPlatformVersionFromProperties() {
        runner.withPropertyValues("jmix.plugin.platform-version=2.5.0")
                .run(ctx -> {
                    JmixPluginManager manager = ctx.getBean(JmixPluginManager.class);
                    assertThat(manager.getPlatformVersion()).isEqualTo("2.5.0");
                });
    }

    @Test
    void exposesRestControllerInWebContext() {
        WebApplicationContextRunner webRunner = new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(PluginAutoConfiguration.class))
                .withBean(PluginRestController.class);
        webRunner.run(ctx -> assertThat(ctx).hasSingleBean(PluginRestController.class));
    }
}
