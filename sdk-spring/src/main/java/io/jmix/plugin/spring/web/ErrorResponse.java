package io.jmix.plugin.spring.web;

/**
 * Generic error payload returned by {@link PluginRestController}.
 */
public record ErrorResponse(String code, String message, String pluginId) { }
