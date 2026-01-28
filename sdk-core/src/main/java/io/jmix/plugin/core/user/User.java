package io.jmix.plugin.core.user;

import java.util.List;
import java.util.Objects;

/**
 * Plain projection of the currently authenticated user.
 */
public record User(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        List<String> roles,
        List<String> permissions) {

    public User {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(username, "username");
        roles = roles == null ? List.of() : List.copyOf(roles);
        permissions = permissions == null ? List.of() : List.copyOf(permissions);
    }
}
