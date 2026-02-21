package io.jmix.plugins.advancedgrid;

import io.jmix.plugin.core.JmixPlugin;
import io.jmix.plugin.core.PluginContext;
import io.jmix.plugin.core.PluginException;
import io.jmix.plugin.core.ValidationResult;
import io.jmix.plugin.core.event.EventBus;
import io.jmix.plugins.advancedgrid.components.AdvancedGrid;
import io.jmix.plugins.advancedgrid.config.AdvancedGridConfig;
import io.jmix.plugins.advancedgrid.services.ExportService;
import io.jmix.plugins.advancedgrid.services.GridDataService;

import java.util.Map;

/**
 * Skeleton implementation of the advanced grid plugin described in
 * {@code prototype.md}. The class wires the component registry and
 * exports a configurable {@link GridDataService} and
 * {@link ExportService}.
 *
 * <p>The skeleton intentionally does not depend on Jmix FlowUI; the
 * concrete UI integration is delivered separately, while this module
 * stays framework-agnostic and compiles on its own.</p>
 */
public class AdvancedGridPlugin extends JmixPlugin {

    public static final String COMPONENT_TAG = "advancedGrid";

    private AdvancedGridConfig config = AdvancedGridConfig.defaults();
    private GridDataService dataService;
    private ExportService exportService;
    private EventBus.Subscription dataChangedSubscription;

    @Override
    public void onLoad(PluginContext context) throws PluginException {
        logger().info("Loading {} v{}", getName(), getVersion());

        config = readConfig(context);
        dataService = new GridDataService(context.getDataService(), config);
        exportService = new ExportService(config);

        context.getComponentRegistry().register(COMPONENT_TAG, AdvancedGrid.class);

        dataChangedSubscription = context.getEventBus().on("grid:data-changed", payload ->
                logger().debug("Grid data changed: {}", payload));

        context.getNotificationService().success(getName() + " loaded");
    }

    @Override
    public void onUnload() throws PluginException {
        logger().info("Unloading {}", getName());
        if (dataChangedSubscription != null) {
            dataChangedSubscription.close();
            dataChangedSubscription = null;
        }
        PluginContext context = getContext();
        if (context != null) {
            context.getComponentRegistry().unregister(COMPONENT_TAG);
        }
    }

    @Override
    public ValidationResult validateConfig(Map<String, Object> configuration) {
        return AdvancedGridConfig.validate(configuration);
    }

    @Override
    public void onConfigChange(String key, Object value) throws PluginException {
        logger().info("AdvancedGrid configuration changed: {} = {}", key, value);
        if (config != null) {
            config = config.with(key, value);
        }
        if (dataService != null) {
            dataService.refreshConfig(config);
        }
        if (exportService != null) {
            exportService.refreshConfig(config);
        }
    }

    public AdvancedGridConfig getConfigSnapshot() {
        return config;
    }

    public GridDataService getDataService() {
        return dataService;
    }

    public ExportService getExportService() {
        return exportService;
    }

    private AdvancedGridConfig readConfig(PluginContext context) {
        AdvancedGridConfig defaults = AdvancedGridConfig.defaults();
        return new AdvancedGridConfig(
                context.getConfig("pageSize", Integer.class).orElse(defaults.pageSize()),
                context.getConfig("enableVirtualScroll", Boolean.class).orElse(defaults.enableVirtualScroll()),
                context.getConfig("enableExport", Boolean.class).orElse(defaults.enableExport()),
                defaults.exportFormats());
    }
}
