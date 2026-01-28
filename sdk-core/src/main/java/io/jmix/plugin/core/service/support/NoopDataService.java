package io.jmix.plugin.core.service.support;

import io.jmix.plugin.core.service.DataService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pass-through {@link DataService} returning empty results. Used when no
 * data backend has been wired into the plugin context.
 */
public final class NoopDataService implements DataService {

    @Override
    public <T> List<T> query(String entityName, QueryOptions options, Class<T> type) {
        return List.of();
    }

    @Override
    public <T> Optional<T> get(String entityName, Object id, Class<T> type) {
        return Optional.empty();
    }

    @Override
    public <T> T create(String entityName, Map<String, Object> data, Class<T> type) {
        throw new UnsupportedOperationException("No data backend is configured for entity " + entityName);
    }

    @Override
    public <T> T update(String entityName, Object id, Map<String, Object> data, Class<T> type) {
        throw new UnsupportedOperationException("No data backend is configured for entity " + entityName);
    }

    @Override
    public boolean delete(String entityName, Object id) {
        return false;
    }

    @Override
    public <T> List<T> executeQuery(String queryName, Map<String, Object> parameters, Class<T> type) {
        return List.of();
    }
}
