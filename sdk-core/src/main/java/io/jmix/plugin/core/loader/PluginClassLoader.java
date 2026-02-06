package io.jmix.plugin.core.loader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Isolated class loader used to load a plugin's classes from its JAR.
 *
 * <p>The loader uses the standard parent-first delegation model so that
 * platform classes (JmixPlugin, PluginContext, java.util.*) come from
 * the parent loader, while plugin-specific classes are sourced from the
 * provided URLs. Implementations of plugin SDK extension points must not
 * be shadowed inside the plugin JAR.</p>
 */
public final class PluginClassLoader extends URLClassLoader {

    private final String pluginId;

    public PluginClassLoader(String pluginId, URL[] urls, ClassLoader parent) {
        super("plugin:" + pluginId, urls, parent);
        this.pluginId = pluginId;
    }

    public String getPluginId() {
        return pluginId;
    }
}
