package io.jmix.plugins.advancedgrid.services;

import io.jmix.plugin.core.service.DataService;
import io.jmix.plugins.advancedgrid.config.AdvancedGridConfig;
import io.jmix.plugins.advancedgrid.models.FilterConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Skeleton data access service. Delegates to the platform-provided
 * {@link DataService} once it is wired by the host application.
 */
public class GridDataService {

    private final DataService dataService;
    private AdvancedGridConfig config;

    public GridDataService(DataService dataService, AdvancedGridConfig config) {
        this.dataService = dataService;
        this.config = config;
    }

    public <T> List<T> load(String entityName, FilterConfig filter, int page, Class<T> type) {
        DataService.QueryOptions options = new DataService.QueryOptions(
                toFilterMap(filter),
                List.of(),
                config.pageSize(),
                Math.max(page, 0) * config.pageSize(),
                null);
        return dataService.query(entityName, options, type);
    }

    public void refreshConfig(AdvancedGridConfig config) {
        this.config = config;
    }

    public AdvancedGridConfig getConfig() {
        return config;
    }

    private Map<String, Object> toFilterMap(FilterConfig filter) {
        if (filter == null || filter.conditions().isEmpty()) {
            return Map.of();
        }
        Map<String, Object> map = new HashMap<>();
        for (FilterConfig.Condition c : filter.conditions()) {
            map.put(c.field(), c.value());
        }
        return map;
    }
}
