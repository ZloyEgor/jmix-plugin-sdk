package io.jmix.plugin.spring;

import io.jmix.plugin.core.JmixPlugin;
import io.jmix.plugin.core.JmixPluginManager;
import io.jmix.plugin.core.PluginContextFactory;
import io.jmix.plugin.core.descriptor.PluginDescriptor;
import io.jmix.plugin.core.descriptor.PluginDescriptorReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * Auto-configuration entry point for the Jmix plugin SDK. Activated when
 * {@code jmix.plugin.enabled=true} (default), it exposes a
 * {@link JmixPluginManager} bean wired to the host
 * {@link ApplicationContext} and bridges plugin lifecycle events to the
 * Spring {@link ApplicationEventPublisher}.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "jmix.plugin", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PluginProperties.class)
public class PluginAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(PluginAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public PluginDescriptorReader pluginDescriptorReader() {
        return new PluginDescriptorReader();
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginContextFactory pluginContextFactory(ApplicationContext applicationContext,
                                                     PluginProperties properties) {
        return (PluginDescriptor descriptor) ->
                new SpringPluginContext(applicationContext, descriptor, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginLifecycleEventPublisher pluginLifecycleEventPublisher(ApplicationEventPublisher publisher) {
        return new PluginLifecycleEventPublisher(publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public JmixPluginManager jmixPluginManager(PluginProperties properties,
                                               PluginContextFactory contextFactory,
                                               PluginDescriptorReader descriptorReader,
                                               PluginLifecycleEventPublisher lifecyclePublisher) {
        JmixPluginManager manager = new JmixPluginManager(
                properties.getPlatformVersion(), contextFactory, descriptorReader);
        manager.addListener(lifecyclePublisher);
        return manager;
    }

    /**
     * Background runner that scans the configured plugin locations and
     * eagerly loads every discovered JAR.
     */
    @Bean
    @ConditionalOnProperty(prefix = "jmix.plugin", name = "auto-load", havingValue = "true", matchIfMissing = true)
    public PluginStartupRunner pluginStartupRunner(JmixPluginManager manager,
                                                   PluginProperties properties,
                                                   @Autowired(required = false) List<JmixPlugin> staticPlugins) {
        return new PluginStartupRunner(manager, properties, staticPlugins);
    }

    /**
     * Component that loads JAR plugins on application startup.
     */
    public static class PluginStartupRunner {

        private final JmixPluginManager manager;
        private final PluginProperties properties;
        private final List<JmixPlugin> staticPlugins;

        public PluginStartupRunner(JmixPluginManager manager,
                                   PluginProperties properties,
                                   List<JmixPlugin> staticPlugins) {
            this.manager = manager;
            this.properties = properties;
            this.staticPlugins = staticPlugins;
        }

        @EventListener(ApplicationReadyEvent.class)
        public void onReady() {
            scanLocations();
            try {
                manager.loadAll();
            } catch (Exception ex) {
                LOG.warn("Failed to auto-load registered plugins: {}", ex.getMessage(), ex);
            }
        }

        private void scanLocations() {
            for (String location : properties.getLocations()) {
                Path dir = Paths.get(location);
                if (!Files.isDirectory(dir)) {
                    LOG.info("Plugin location {} is not a directory, skipping", dir);
                    continue;
                }
                try (Stream<Path> stream = Files.list(dir)) {
                    stream.filter(p -> p.toString().endsWith(".jar"))
                            .forEach(this::registerJarSafely);
                } catch (IOException ex) {
                    LOG.warn("Failed to enumerate plugin location {}: {}", dir, ex.toString());
                }
            }
        }

        private void registerJarSafely(Path jar) {
            try {
                manager.registerFromJar(jar);
                LOG.info("Registered plugin from {}", jar);
            } catch (Exception ex) {
                LOG.warn("Failed to register plugin from {}: {}", jar, ex.getMessage(), ex);
            }
        }
    }
}
