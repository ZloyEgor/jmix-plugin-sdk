package io.jmix.plugin.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of a validation operation. Carries either an empty list of errors
 * (valid) or the structured list of issues that prevented validation from
 * succeeding.
 */
public record ValidationResult(boolean valid, List<Error> errors) {

    public ValidationResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    /**
     * Convenience factory for a successful validation.
     */
    public static ValidationResult ok() {
        return new ValidationResult(true, List.of());
    }

    /**
     * Convenience factory for a failed validation with the provided errors.
     */
    public static ValidationResult invalid(List<Error> errors) {
        return new ValidationResult(false, errors);
    }

    /**
     * Builder accumulating errors and producing a {@link ValidationResult}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Single validation failure descriptor.
     */
    public record Error(String path, String message) {

        public Error {
            if (path == null) {
                throw new IllegalArgumentException("path must not be null");
            }
            if (message == null) {
                throw new IllegalArgumentException("message must not be null");
            }
        }
    }

    /**
     * Mutable accumulator for validation errors.
     */
    public static final class Builder {
        private final List<Error> errors = new ArrayList<>();

        private Builder() {
        }

        public Builder error(String path, String message) {
            errors.add(new Error(path, message));
            return this;
        }

        public ValidationResult build() {
            if (errors.isEmpty()) {
                return ok();
            }
            return new ValidationResult(false, Collections.unmodifiableList(new ArrayList<>(errors)));
        }
    }
}
