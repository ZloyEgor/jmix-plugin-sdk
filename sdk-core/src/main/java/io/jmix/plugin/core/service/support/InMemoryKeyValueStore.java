package io.jmix.plugin.core.service.support;

import io.jmix.plugin.core.service.KeyValueStore;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory {@link KeyValueStore} useful for tests and
 * standalone runs.
 */
public final class InMemoryKeyValueStore implements KeyValueStore {

    private final ConcurrentHashMap<String, Object> values = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = values.get(key);
        if (value == null || !type.isInstance(value)) {
            return Optional.empty();
        }
        return Optional.of((T) value);
    }

    @Override
    public void set(String key, Object value) {
        if (value == null) {
            values.remove(key);
        } else {
            values.put(key, value);
        }
    }

    @Override
    public boolean delete(String key) {
        return values.remove(key) != null;
    }

    @Override
    public boolean has(String key) {
        return values.containsKey(key);
    }

    @Override
    public Set<String> keys() {
        return Collections.unmodifiableSet(new HashSet<>(values.keySet()));
    }

    @Override
    public void clear() {
        values.clear();
    }
}
