package io.jmix.plugin.core;

import java.time.Instant;

/**
 * Summary view of a plugin used by management APIs.
 *
 * @param id          identifier of the plugin
 * @param name        human-readable name
 * @param version     semantic version
 * @param state       current lifecycle state
 * @param errorMessage last recorded error, or {@code null}
 * @param loadedAt    timestamp of the last successful load, or {@code null}
 */
public record PluginInfo(
        String id,
        String name,
        String version,
        PluginState state,
        String errorMessage,
        Instant loadedAt) {

    public PluginInfo {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
    }
}
