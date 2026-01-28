package io.jmix.plugin.core.service;

import java.util.Optional;
import java.util.Set;

/**
 * Registry of UI components provided by plugins. The registry is
 * platform-agnostic; the concrete component types are owned by the host
 * application.
 */
public interface ComponentRegistry {

    /**
     * Registers a component class under the given tag.
     */
    void register(String tag, Class<?> componentClass);

    /**
     * Returns the component class registered under the tag.
     */
    Optional<Class<?>> get(String tag);

    /**
     * Returns {@code true} when a component is registered under the tag.
     */
    boolean has(String tag);

    /**
     * Removes the registration for the given tag.
     */
    boolean unregister(String tag);

    /**
     * Returns the set of all registered tags.
     */
    Set<String> getTags();
}
