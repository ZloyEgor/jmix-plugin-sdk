package io.jmix.plugin.core.service.support;

import io.jmix.plugin.core.service.SecurityService;
import io.jmix.plugin.core.user.User;

import java.util.Optional;

/**
 * Permissive default {@link SecurityService} that grants nothing. Plugin
 * code should treat the absence of a real security backend as a deny by
 * default.
 */
public final class NoopSecurityService implements SecurityService {

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }

    @Override
    public boolean hasRole(String role) {
        return false;
    }

    @Override
    public Optional<User> getCurrentUser() {
        return Optional.empty();
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public boolean canAccess(String entityName, Operation operation) {
        return false;
    }
}
