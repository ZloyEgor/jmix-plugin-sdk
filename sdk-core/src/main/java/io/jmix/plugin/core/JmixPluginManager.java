package io.jmix.plugin.core;

import io.jmix.plugin.core.descriptor.PluginDescriptor;
import io.jmix.plugin.core.descriptor.PluginDescriptorReader;
import io.jmix.plugin.core.lifecycle.PluginErrorEvent;
import io.jmix.plugin.core.lifecycle.PluginLifecycleListener;
import io.jmix.plugin.core.lifecycle.PluginLoadedEvent;
import io.jmix.plugin.core.lifecycle.PluginRegisteredEvent;
import io.jmix.plugin.core.lifecycle.PluginUnloadedEvent;
import io.jmix.plugin.core.version.VersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central registry and lifecycle controller for {@link JmixPlugin}
 * instances. The manager handles registration, dependency resolution,
 * load and unload operations and emits lifecycle events to registered
 * listeners.
 *
 * <p>The class is thread-safe: registration and lifecycle methods are
 * synchronised, while read-only accessors use concurrent collections
 * with weak consistency guarantees.</p>
 */
public class JmixPluginManager {

    private static final Logger LOG = LoggerFactory.getLogger(JmixPluginManager.class);

    private final Map<String, JmixPlugin> plugins = new LinkedHashMap<>();
    private final Map<String, PluginDescriptor> descriptors = new ConcurrentHashMap<>();
    private final Map<String, PluginState> states = new ConcurrentHashMap<>();
    private final Map<String, String> errors = new ConcurrentHashMap<>();
    private final Map<String, Instant> loadedAt = new ConcurrentHashMap<>();
    private final List<String> loadOrder = new ArrayList<>();
    private final List<PluginLifecycleListener> listeners = new CopyOnWriteArrayList<>();

    private final String platformVersion;
    private final PluginContextFactory contextFactory;
    private final PluginDescriptorReader descriptorReader;

    public JmixPluginManager(String platformVersion) {
        this(platformVersion, PluginContextFactory.inMemory(), new PluginDescriptorReader());
    }

    public JmixPluginManager(String platformVersion,
                             PluginContextFactory contextFactory,
                             PluginDescriptorReader descriptorReader) {
        this.platformVersion = platformVersion == null ? "0.0.0" : platformVersion;
        this.contextFactory = contextFactory == null ? PluginContextFactory.inMemory() : contextFactory;
        this.descriptorReader = descriptorReader == null ? new PluginDescriptorReader() : descriptorReader;
    }

    /**
     * Registers a plugin instance together with its descriptor. The
     * plugin remains in {@link PluginState#UNLOADED} state until
     * {@link #load(String)} is invoked.
     */
    public synchronized void register(JmixPlugin plugin, PluginDescriptor descriptor) throws PluginException {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin must not be null");
        }
        if (descriptor == null) {
            throw new IllegalArgumentException("descriptor must not be null");
        }
        descriptorReader.validate(descriptor);
        String id = descriptor.getId();
        if (plugins.containsKey(id)) {
            throw new PluginException("Plugin " + id + " is already registered");
        }
        checkCompatibility(descriptor);
        checkPluginDependenciesDeclared(descriptor);

        plugins.put(id, plugin);
        descriptors.put(id, descriptor);
        states.put(id, PluginState.UNLOADED);
        publish(new PluginRegisteredEvent(id));
    }

    /**
     * Loads the previously registered plugin and all its dependencies.
     */
    public synchronized void load(String pluginId) throws PluginException {
        JmixPlugin plugin = plugins.get(pluginId);
        if (plugin == null) {
            throw new PluginNotFoundException(pluginId);
        }

        PluginState current = states.get(pluginId);
        if (current == PluginState.LOADED) {
            return;
        }
        if (current == PluginState.LOADING) {
            throw new PluginException("Plugin " + pluginId + " is already loading");
        }

        PluginDescriptor descriptor = descriptors.get(pluginId);
        states.put(pluginId, PluginState.LOADING);

        try {
            loadDependencies(descriptor);

            PluginContext context = contextFactory.create(descriptor);
            plugin.bind(descriptor, context);
            plugin.onLoad(context);
            plugin.markLoaded();

            states.put(pluginId, PluginState.LOADED);
            errors.remove(pluginId);
            loadedAt.put(pluginId, Instant.now());
            loadOrder.add(pluginId);

            publish(new PluginLoadedEvent(pluginId));
        } catch (PluginException ex) {
            recordError(pluginId, ex);
            throw ex;
        } catch (RuntimeException ex) {
            recordError(pluginId, ex);
            throw new PluginException("Failed to load plugin " + pluginId + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Unloads the plugin, refusing to do so when other loaded plugins
     * still depend on it.
     */
    public synchronized void unload(String pluginId) throws PluginException {
        JmixPlugin plugin = plugins.get(pluginId);
        if (plugin == null) {
            throw new PluginNotFoundException(pluginId);
        }
        PluginState current = states.get(pluginId);
        if (current != PluginState.LOADED) {
            return;
        }

        List<String> dependents = getDependents(pluginId);
        if (!dependents.isEmpty()) {
            throw new PluginException("Cannot unload " + pluginId
                    + ": still required by " + dependents);
        }

        states.put(pluginId, PluginState.UNLOADING);
        try {
            plugin.onUnload();
            plugin.markUnloaded();
            states.put(pluginId, PluginState.UNLOADED);
            loadedAt.remove(pluginId);
            loadOrder.remove(pluginId);
            publish(new PluginUnloadedEvent(pluginId));
        } catch (PluginException ex) {
            recordError(pluginId, ex);
            throw ex;
        } catch (RuntimeException ex) {
            recordError(pluginId, ex);
            throw new PluginException("Failed to unload plugin " + pluginId + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Loads every registered plugin in topological order.
     */
    public synchronized void loadAll() throws PluginException {
        for (String id : topologicalSort()) {
            if (states.get(id) != PluginState.LOADED) {
                load(id);
            }
        }
    }

    /**
     * Unloads every loaded plugin in reverse load order.
     */
    public synchronized void unloadAll() throws PluginException {
        List<String> reversed = new ArrayList<>(loadOrder);
        Collections.reverse(reversed);
        for (String id : reversed) {
            unload(id);
        }
    }

    public Optional<JmixPlugin> get(String pluginId) {
        return Optional.ofNullable(plugins.get(pluginId));
    }

    public Optional<PluginDescriptor> getDescriptor(String pluginId) {
        return Optional.ofNullable(descriptors.get(pluginId));
    }

    public PluginState getState(String pluginId) {
        return states.getOrDefault(pluginId, PluginState.UNLOADED);
    }

    public boolean isLoaded(String pluginId) {
        return getState(pluginId) == PluginState.LOADED;
    }

    public boolean isRegistered(String pluginId) {
        return plugins.containsKey(pluginId);
    }

    public Optional<PluginInfo> getInfo(String pluginId) {
        PluginDescriptor d = descriptors.get(pluginId);
        if (d == null) {
            return Optional.empty();
        }
        return Optional.of(new PluginInfo(
                d.getId(),
                d.getName(),
                d.getVersion(),
                getState(pluginId),
                errors.get(pluginId),
                loadedAt.get(pluginId)));
    }

    public List<PluginInfo> list() {
        List<PluginInfo> result = new ArrayList<>(descriptors.size());
        for (String id : descriptors.keySet()) {
            getInfo(id).ifPresent(result::add);
        }
        return Collections.unmodifiableList(result);
    }

    public List<PluginInfo> listLoaded() {
        List<PluginInfo> result = new ArrayList<>();
        for (PluginInfo info : list()) {
            if (info.state() == PluginState.LOADED) {
                result.add(info);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void addListener(PluginLifecycleListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(PluginLifecycleListener listener) {
        listeners.remove(listener);
    }

    private void publish(PluginRegisteredEvent event) {
        for (PluginLifecycleListener l : listeners) {
            safe(() -> l.onRegistered(event));
        }
    }

    private void publish(PluginLoadedEvent event) {
        for (PluginLifecycleListener l : listeners) {
            safe(() -> l.onLoaded(event));
        }
    }

    private void publish(PluginUnloadedEvent event) {
        for (PluginLifecycleListener l : listeners) {
            safe(() -> l.onUnloaded(event));
        }
    }

    private void publish(PluginErrorEvent event) {
        for (PluginLifecycleListener l : listeners) {
            safe(() -> l.onError(event));
        }
    }

    private void recordError(String pluginId, Exception ex) {
        states.put(pluginId, PluginState.ERROR);
        errors.put(pluginId, ex.getMessage());
        publish(new PluginErrorEvent(pluginId, ex.getMessage(), ex));
    }

    private void checkCompatibility(PluginDescriptor descriptor) throws PluginException {
        PluginDescriptor.Compatibility c = descriptor.getCompatibility();
        if (c == null || c.getJmix() == null || c.getJmix().isBlank()) {
            return;
        }
        if (!VersionUtils.satisfies(platformVersion, c.getJmix())) {
            throw new PluginException("Plugin " + descriptor.getId()
                    + " requires Jmix " + c.getJmix()
                    + " but platform version is " + platformVersion);
        }
    }

    private void checkPluginDependenciesDeclared(PluginDescriptor descriptor) throws PluginException {
        PluginDescriptor.Dependencies d = descriptor.getDependencies();
        if (d == null || d.getPlugins() == null) {
            return;
        }
        for (PluginDescriptor.PluginDependency dep : d.getPlugins()) {
            PluginDescriptor depDescriptor = descriptors.get(dep.getId());
            if (depDescriptor == null) {
                continue; // dependency may be registered later
            }
            if (dep.getVersion() != null && !VersionUtils.satisfies(depDescriptor.getVersion(), dep.getVersion())) {
                throw new PluginException("Plugin " + descriptor.getId()
                        + " requires " + dep.getId() + " " + dep.getVersion()
                        + " but registered version is " + depDescriptor.getVersion());
            }
        }
    }

    private void loadDependencies(PluginDescriptor descriptor) throws PluginException {
        PluginDescriptor.Dependencies d = descriptor.getDependencies();
        if (d == null || d.getPlugins() == null) {
            return;
        }
        for (PluginDescriptor.PluginDependency dep : d.getPlugins()) {
            if (!plugins.containsKey(dep.getId())) {
                throw new PluginException("Plugin " + descriptor.getId()
                        + " depends on " + dep.getId() + ", which is not registered");
            }
            if (states.get(dep.getId()) != PluginState.LOADED) {
                load(dep.getId());
            }
        }
    }

    /**
     * Returns plugin ids that have the given plugin among their
     * dependencies and are currently loaded.
     */
    private List<String> getDependents(String pluginId) {
        List<String> dependents = new ArrayList<>();
        for (Map.Entry<String, PluginDescriptor> entry : descriptors.entrySet()) {
            PluginDescriptor.Dependencies d = entry.getValue().getDependencies();
            if (d == null || d.getPlugins() == null) {
                continue;
            }
            for (PluginDescriptor.PluginDependency dep : d.getPlugins()) {
                if (pluginId.equals(dep.getId()) && states.get(entry.getKey()) == PluginState.LOADED) {
                    dependents.add(entry.getKey());
                }
            }
        }
        return dependents;
    }

    /**
     * Topologically sorts all registered descriptors by their plugin
     * dependencies. Throws when a cycle is detected.
     */
    List<String> topologicalSort() throws PluginException {
        List<String> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();
        for (String id : descriptors.keySet()) {
            visit(id, sorted, visited, visiting, new LinkedList<>());
        }
        return sorted;
    }

    private void visit(String pluginId,
                       List<String> sorted,
                       Set<String> visited,
                       Set<String> visiting,
                       LinkedList<String> path) throws PluginException {
        if (visited.contains(pluginId)) {
            return;
        }
        if (visiting.contains(pluginId)) {
            path.add(pluginId);
            throw new PluginException("Circular dependency detected: " + String.join(" -> ", path));
        }
        visiting.add(pluginId);
        path.add(pluginId);

        PluginDescriptor descriptor = descriptors.get(pluginId);
        if (descriptor != null && descriptor.getDependencies() != null
                && descriptor.getDependencies().getPlugins() != null) {
            for (PluginDescriptor.PluginDependency dep : descriptor.getDependencies().getPlugins()) {
                if (descriptors.containsKey(dep.getId())) {
                    visit(dep.getId(), sorted, visited, visiting, path);
                }
            }
        }

        visiting.remove(pluginId);
        path.removeLast();
        visited.add(pluginId);
        sorted.add(pluginId);
    }

    private static void safe(Runnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException ex) {
            LOG.warn("Plugin lifecycle listener threw {}", ex.toString(), ex);
        }
    }

    /**
     * Snapshot of currently registered plugin ids.
     */
    public Collection<String> registeredIds() {
        return new HashMap<>(descriptors).keySet();
    }
}
