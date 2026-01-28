package io.jmix.plugin.core.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstraction over data access exposed to plugins. Implementations
 * delegate to the host platform (Jmix DataManager in production).
 */
public interface DataService {

    /**
     * Loads entities of the given logical name using the provided query
     * options.
     */
    <T> List<T> query(String entityName, QueryOptions options, Class<T> type);

    /**
     * Loads a single entity by identifier.
     */
    <T> Optional<T> get(String entityName, Object id, Class<T> type);

    /**
     * Creates a new entity instance from a property map.
     */
    <T> T create(String entityName, Map<String, Object> data, Class<T> type);

    /**
     * Updates an existing entity.
     */
    <T> T update(String entityName, Object id, Map<String, Object> data, Class<T> type);

    /**
     * Deletes an entity by identifier. Returns {@code true} when removal
     * actually happened.
     */
    boolean delete(String entityName, Object id);

    /**
     * Executes a named query.
     */
    <T> List<T> executeQuery(String queryName, Map<String, Object> parameters, Class<T> type);

    /**
     * Query parameters supported by {@link #query(String, QueryOptions, Class)}.
     */
    record QueryOptions(
            Map<String, Object> filter,
            List<SortOrder> sort,
            Integer limit,
            Integer offset,
            String fetchPlan) {

        public QueryOptions {
            filter = filter == null ? Map.of() : Map.copyOf(filter);
            sort = sort == null ? List.of() : List.copyOf(sort);
        }

        public static QueryOptions empty() {
            return new QueryOptions(Map.of(), List.of(), null, null, null);
        }
    }

    /**
     * Sort descriptor.
     */
    record SortOrder(String field, Direction direction) {

        public SortOrder {
            if (field == null || field.isBlank()) {
                throw new IllegalArgumentException("field must not be blank");
            }
            if (direction == null) {
                direction = Direction.ASC;
            }
        }

        public enum Direction { ASC, DESC }
    }
}
