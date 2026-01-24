package io.jmix.plugin.core;

import java.util.List;

/**
 * Raised when a plugin descriptor or configuration fails validation.
 */
public class PluginValidationException extends PluginException {

    private static final long serialVersionUID = 1L;

    private final transient ValidationResult result;

    public PluginValidationException(ValidationResult result) {
        super(formatMessage(result));
        this.result = result;
    }

    public PluginValidationException(String message, ValidationResult result) {
        super(message);
        this.result = result;
    }

    /**
     * Validation result describing the failure in detail.
     */
    public ValidationResult getResult() {
        return result;
    }

    private static String formatMessage(ValidationResult result) {
        List<ValidationResult.Error> errors = result.errors();
        if (errors.isEmpty()) {
            return "Plugin validation failed";
        }
        StringBuilder sb = new StringBuilder("Plugin validation failed: ");
        for (int i = 0; i < errors.size(); i++) {
            ValidationResult.Error err = errors.get(i);
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(err.path()).append(": ").append(err.message());
        }
        return sb.toString();
    }
}
