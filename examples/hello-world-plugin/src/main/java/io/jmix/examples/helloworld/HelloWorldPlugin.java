package io.jmix.examples.helloworld;

import io.jmix.plugin.core.JmixPlugin;
import io.jmix.plugin.core.PluginContext;
import io.jmix.plugin.core.PluginException;
import io.jmix.plugin.core.event.EventBus;

/**
 * Demonstration plugin: shows the canonical structure of a Jmix
 * plugin — lifecycle hooks, access to platform services, event
 * subscription, configuration reads, current-user inspection.
 *
 * <p>The implementation mirrors the TypeScript prototype kept in
 * {@code prototype-ts/examples/hello-world-plugin/src/index.ts}.</p>
 */
public class HelloWorldPlugin extends JmixPlugin {

    private static final String DEFAULT_GREETING = "Hello, World!";

    private EventBus.Subscription entityCreatedSubscription;

    @Override
    public void onLoad(PluginContext context) throws PluginException {
        logger().info("{} v{} loading...", getName(), getVersion());

        context.getNotificationService().success("Hello World Plugin loaded!");

        entityCreatedSubscription = context.getEventBus().on("entity:created", payload ->
                logger().info("Entity created: {}", payload));

        String greeting = getConfig("greeting", DEFAULT_GREETING, String.class);
        logger().info("Greeting message: {}", greeting);

        context.getCurrentUser().ifPresent(user ->
                logger().info("Current user: {}", user.username()));

        if (context.hasPermission("admin")) {
            logger().info("User has admin permissions");
        }

        logger().info("{} loaded successfully", getName());
    }

    @Override
    public void onUnload() throws PluginException {
        logger().info("{} unloading...", getName());
        if (entityCreatedSubscription != null) {
            entityCreatedSubscription.close();
            entityCreatedSubscription = null;
        }
        logger().info("{} unloaded", getName());
    }

    @Override
    public void onConfigChange(String key, Object value) throws PluginException {
        logger().info("Configuration changed: {} = {}", key, value);
        if ("greeting".equals(key)) {
            PluginContext context = getContext();
            if (context != null) {
                context.getNotificationService().info("Greeting updated: " + value);
            }
        }
    }
}
