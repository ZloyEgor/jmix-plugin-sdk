package io.jmix.plugin.core;

import io.jmix.plugin.core.descriptor.PluginDescriptor;
import io.jmix.plugin.core.lifecycle.PluginErrorEvent;
import io.jmix.plugin.core.lifecycle.PluginLifecycleListener;
import io.jmix.plugin.core.lifecycle.PluginLoadedEvent;
import io.jmix.plugin.core.lifecycle.PluginUnloadedEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JmixPluginManagerTest {

    @Test
    void registersAndLoadsPlugin() throws Exception {
        JmixPluginManager manager = new JmixPluginManager("2.0.0");
        AtomicInteger loaded = new AtomicInteger();
        AtomicInteger unloaded = new AtomicInteger();
        manager.addListener(new PluginLifecycleListener() {
            @Override public void onLoaded(PluginLoadedEvent event) { loaded.incrementAndGet(); }
            @Override public void onUnloaded(PluginUnloadedEvent event) { unloaded.incrementAndGet(); }
        });

        RecordingPlugin plugin = new RecordingPlugin();
        manager.register(plugin, descriptor("plugin.a", "1.0.0", RecordingPlugin.class.getName(), ">=2.0.0 <3.0.0"));

        assertThat(manager.getState("plugin.a")).isEqualTo(PluginState.UNLOADED);
        manager.load("plugin.a");
        assertThat(manager.getState("plugin.a")).isEqualTo(PluginState.LOADED);
        assertThat(plugin.loadCount).isOne();
        assertThat(plugin.context).isNotNull();

        manager.unload("plugin.a");
        assertThat(manager.getState("plugin.a")).isEqualTo(PluginState.UNLOADED);
        assertThat(plugin.unloadCount).isOne();
        assertThat(loaded.get()).isOne();
        assertThat(unloaded.get()).isOne();
    }

    @Test
    void resolvesDependencyOrder() throws Exception {
        JmixPluginManager manager = new JmixPluginManager("2.0.0");
        RecordingPlugin a = new RecordingPlugin();
        RecordingPlugin b = new RecordingPlugin();

        PluginDescriptor aDesc = descriptor("plugin.a", "1.0.0", RecordingPlugin.class.getName(), null);
        PluginDescriptor bDesc = descriptor("plugin.b", "1.0.0", RecordingPlugin.class.getName(), null);
        bDesc.setDependencies(deps("plugin.a", null));

        manager.register(a, aDesc);
        manager.register(b, bDesc);
        manager.loadAll();

        assertThat(manager.isLoaded("plugin.a")).isTrue();
        assertThat(manager.isLoaded("plugin.b")).isTrue();

        assertThatThrownBy(() -> manager.unload("plugin.a"))
                .isInstanceOf(PluginException.class)
                .hasMessageContaining("plugin.b");

        manager.unload("plugin.b");
        manager.unload("plugin.a");
    }

    @Test
    void rejectsIncompatiblePlatform() {
        JmixPluginManager manager = new JmixPluginManager("1.0.0");
        PluginDescriptor desc = descriptor("plugin.a", "1.0.0", RecordingPlugin.class.getName(), ">=2.0.0 <3.0.0");
        assertThatThrownBy(() -> manager.register(new RecordingPlugin(), desc))
                .isInstanceOf(PluginException.class)
                .hasMessageContaining("Jmix");
    }

    @Test
    void detectsCircularDependency() throws Exception {
        JmixPluginManager manager = new JmixPluginManager("2.0.0");
        PluginDescriptor a = descriptor("a", "1.0.0", RecordingPlugin.class.getName(), null);
        PluginDescriptor b = descriptor("b", "1.0.0", RecordingPlugin.class.getName(), null);
        a.setDependencies(deps("b", null));
        b.setDependencies(deps("a", null));

        manager.register(new RecordingPlugin(), a);
        manager.register(new RecordingPlugin(), b);

        assertThatThrownBy(manager::topologicalSort)
                .isInstanceOf(PluginException.class)
                .hasMessageContaining("Circular dependency");
    }

    @Test
    void capturesLoadFailureAsErrorState() {
        JmixPluginManager manager = new JmixPluginManager("2.0.0");
        FailingPlugin failing = new FailingPlugin();
        List<PluginErrorEvent> errors = new ArrayList<>();
        manager.addListener(new PluginLifecycleListener() {
            @Override public void onError(PluginErrorEvent event) { errors.add(event); }
        });

        assertThatThrownBy(() -> {
            manager.register(failing, descriptor("plugin.fail", "1.0.0", FailingPlugin.class.getName(), null));
            manager.load("plugin.fail");
        }).isInstanceOf(PluginException.class);

        assertThat(manager.getState("plugin.fail")).isEqualTo(PluginState.ERROR);
        assertThat(errors).hasSize(1);
    }

    private static PluginDescriptor descriptor(String id, String version, String main, String jmixRange) {
        PluginDescriptor d = new PluginDescriptor();
        d.setId(id);
        d.setName(id);
        d.setVersion(version);
        PluginDescriptor.Entrypoints e = new PluginDescriptor.Entrypoints();
        e.setMain(main);
        d.setEntrypoints(e);
        if (jmixRange != null) {
            PluginDescriptor.Compatibility c = new PluginDescriptor.Compatibility();
            c.setJmix(jmixRange);
            d.setCompatibility(c);
        }
        return d;
    }

    private static PluginDescriptor.Dependencies deps(String depId, String depVersion) {
        PluginDescriptor.Dependencies d = new PluginDescriptor.Dependencies();
        PluginDescriptor.PluginDependency dep = new PluginDescriptor.PluginDependency();
        dep.setId(depId);
        dep.setVersion(depVersion);
        d.setPlugins(List.of(dep));
        return d;
    }

    static class RecordingPlugin extends JmixPlugin {
        int loadCount;
        int unloadCount;
        PluginContext context;

        @Override
        public void onLoad(PluginContext context) {
            this.context = context;
            this.loadCount++;
        }

        @Override
        public void onUnload() {
            this.unloadCount++;
        }
    }

    static class FailingPlugin extends JmixPlugin {
        @Override
        public void onLoad(PluginContext context) throws PluginException {
            throw new PluginException("boom");
        }

        @Override
        public void onUnload() {
            // never called
        }
    }
}
