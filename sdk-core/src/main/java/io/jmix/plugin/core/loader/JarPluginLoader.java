package io.jmix.plugin.core.loader;

import io.jmix.plugin.core.JmixPlugin;
import io.jmix.plugin.core.PluginException;
import io.jmix.plugin.core.descriptor.PluginDescriptor;
import io.jmix.plugin.core.descriptor.PluginDescriptorReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Loads a plugin from a JAR file. The loader reads
 * {@code META-INF/plugin.json}, creates an isolated
 * {@link PluginClassLoader} and instantiates the entry-point class
 * declared by the descriptor.
 */
public final class JarPluginLoader {

    private final PluginDescriptorReader descriptorReader;

    public JarPluginLoader() {
        this(new PluginDescriptorReader());
    }

    public JarPluginLoader(PluginDescriptorReader descriptorReader) {
        this.descriptorReader = descriptorReader;
    }

    /**
     * Reads the descriptor from the given JAR file without instantiating
     * the plugin.
     */
    public PluginDescriptor readDescriptor(Path jarFile) throws PluginException, IOException {
        Objects.requireNonNull(jarFile, "jarFile");
        if (!Files.exists(jarFile)) {
            throw new PluginException("Plugin JAR does not exist: " + jarFile);
        }
        try (JarFile jar = new JarFile(jarFile.toFile())) {
            ZipEntry entry = jar.getEntry(PluginDescriptorReader.descriptorEntry());
            if (entry == null) {
                throw new PluginException(
                        "Plugin JAR does not contain " + PluginDescriptorReader.descriptorEntry() + ": " + jarFile);
            }
            try (InputStream is = jar.getInputStream(entry)) {
                return descriptorReader.read(is);
            }
        }
    }

    /**
     * Loads the plugin from the JAR file. Callers must close the returned
     * {@link Loaded#classLoader()} after the plugin has been unloaded.
     *
     * @param jarFile     path to the plugin JAR
     * @param parent      parent class loader; usually
     *                    {@code getClass().getClassLoader()} of the host
     * @return loaded plugin together with its descriptor and isolated
     *         class loader
     * @throws PluginException if the descriptor cannot be read, the
     *                         entry-point class is missing or the plugin
     *                         cannot be instantiated
     */
    public Loaded load(Path jarFile, ClassLoader parent) throws PluginException, IOException {
        PluginDescriptor descriptor = readDescriptor(jarFile);
        if (descriptor.getEntrypoints() == null || descriptor.getEntrypoints().getMain() == null) {
            throw new PluginException(
                    "Plugin descriptor in " + jarFile + " has no entrypoints.main");
        }

        URL jarUrl = jarFile.toUri().toURL();
        PluginClassLoader classLoader = new PluginClassLoader(
                descriptor.getId(), new URL[]{jarUrl}, parent);
        try {
            Class<?> pluginClass = Class.forName(
                    descriptor.getEntrypoints().getMain(), true, classLoader);
            if (!JmixPlugin.class.isAssignableFrom(pluginClass)) {
                throw new PluginException("Entry-point class " + pluginClass.getName()
                        + " does not extend JmixPlugin");
            }
            JmixPlugin plugin = (JmixPlugin) pluginClass.getDeclaredConstructor().newInstance();
            return new Loaded(descriptor, plugin, classLoader);
        } catch (ClassNotFoundException ex) {
            closeQuietly(classLoader);
            throw new PluginException("Plugin entry-point class "
                    + descriptor.getEntrypoints().getMain() + " not found in " + jarFile, ex);
        } catch (ReflectiveOperationException ex) {
            closeQuietly(classLoader);
            throw new PluginException("Failed to instantiate plugin "
                    + descriptor.getId() + ": " + ex.getMessage(), ex);
        }
    }

    private static void closeQuietly(PluginClassLoader classLoader) {
        try {
            classLoader.close();
        } catch (IOException ignored) {
            // best effort on cleanup
        }
    }

    /**
     * Loader output bundling the descriptor, instantiated plugin and
     * its isolated class loader.
     */
    public record Loaded(PluginDescriptor descriptor, JmixPlugin plugin, PluginClassLoader classLoader) {

        public Loaded {
            Objects.requireNonNull(descriptor, "descriptor");
            Objects.requireNonNull(plugin, "plugin");
            Objects.requireNonNull(classLoader, "classLoader");
        }
    }
}
