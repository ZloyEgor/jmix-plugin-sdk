package io.jmix.plugin.core.service;

import io.jmix.plugin.core.user.User;

import java.util.Optional;

/**
 * Authorization and identity facade.
 */
public interface SecurityService {

    boolean hasPermission(String permission);

    boolean hasRole(String role);

    Optional<User> getCurrentUser();

    boolean isAuthenticated();

    /**
     * Returns {@code true} when the current user is allowed to perform
     * the given operation on the given entity.
     */
    boolean canAccess(String entityName, Operation operation);

    enum Operation { CREATE, READ, UPDATE, DELETE }
}
