package io.jmix.plugin.core.service.support;

import io.jmix.plugin.core.service.ComponentRegistry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe {@link ComponentRegistry} backed by an in-memory map.
 */
public final class InMemoryComponentRegistry implements ComponentRegistry {

    private final ConcurrentHashMap<String, Class<?>> components = new ConcurrentHashMap<>();

    @Override
    public void register(String tag, Class<?> componentClass) {
        requireNonBlank(tag);
        if (componentClass == null) {
            throw new IllegalArgumentException("componentClass must not be null");
        }
        components.put(tag, componentClass);
    }

    @Override
    public Optional<Class<?>> get(String tag) {
        return Optional.ofNullable(components.get(tag));
    }

    @Override
    public boolean has(String tag) {
        return components.containsKey(tag);
    }

    @Override
    public boolean unregister(String tag) {
        return components.remove(tag) != null;
    }

    @Override
    public Set<String> getTags() {
        return Collections.unmodifiableSet(new HashSet<>(components.keySet()));
    }

    private static void requireNonBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tag must not be blank");
        }
    }
}
