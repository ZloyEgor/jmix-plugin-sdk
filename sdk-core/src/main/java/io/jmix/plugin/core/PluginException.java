package io.jmix.plugin.core;

/**
 * Base checked exception for all plugin-related errors raised by the SDK.
 */
public class PluginException extends Exception {

    private static final long serialVersionUID = 1L;

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
