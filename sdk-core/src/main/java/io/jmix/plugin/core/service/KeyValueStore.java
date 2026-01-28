package io.jmix.plugin.core.service;

import java.util.Optional;
import java.util.Set;

/**
 * Generic key/value store interface used for plugin-local persistent
 * storage and session storage.
 */
public interface KeyValueStore {

    <T> Optional<T> get(String key, Class<T> type);

    void set(String key, Object value);

    boolean delete(String key);

    boolean has(String key);

    Set<String> keys();

    void clear();
}
