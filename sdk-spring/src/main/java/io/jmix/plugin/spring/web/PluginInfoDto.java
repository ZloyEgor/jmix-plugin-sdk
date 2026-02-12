package io.jmix.plugin.spring.web;

import io.jmix.plugin.core.PluginInfo;
import io.jmix.plugin.core.PluginState;

import java.time.Instant;

/**
 * Public projection of a {@link PluginInfo} for the REST API.
 *
 * @param id           plugin identifier
 * @param name         human-readable name
 * @param version      semantic version
 * @param state        current lifecycle state
 * @param errorMessage last recorded error, when applicable
 * @param loadedAt     timestamp of the last successful load, when applicable
 */
public record PluginInfoDto(
        String id,
        String name,
        String version,
        PluginState state,
        String errorMessage,
        Instant loadedAt) {

    public static PluginInfoDto from(PluginInfo info) {
        return new PluginInfoDto(
                info.id(),
                info.name(),
                info.version(),
                info.state(),
                info.errorMessage(),
                info.loadedAt());
    }
}
