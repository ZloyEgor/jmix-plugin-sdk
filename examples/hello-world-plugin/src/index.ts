import { Plugin, PluginContext } from '@jmix/plugin-sdk-core';

/**
 * Hello World Plugin Example
 * 
 * This plugin demonstrates the basics of plugin development:
 * - Loading and unloading
 * - Using platform services
 * - Registering components
 * - Event handling
 */
export class HelloWorldPlugin extends Plugin {
    /**
     * Called when plugin is loaded
     */
    async onLoad(context: PluginContext): Promise<void> {
        // Log plugin initialization
        this.logger.info(`${this.getName()} v${this.getVersion()} loading...`);

        // Show notification
        context.notificationService.success('Hello World Plugin loaded!');

        // Register event listener
        context.eventBus.on('entity:created', (entity) => {
            this.logger.info('Entity created:', entity);
        });

        // Access configuration
        const greeting = this.getConfig<string>('greeting', 'Hello, World!');
        this.logger.info(`Greeting message: ${greeting}`);

        // Get current user
        const user = context.currentUser;
        if (user) {
            this.logger.info(`Current user: ${user.username}`);
        }

        // Check permissions
        if (context.hasPermission('admin')) {
            this.logger.info('User has admin permissions');
        }

        this.logger.info(`${this.getName()} loaded successfully`);
    }

    /**
     * Called when plugin is unloaded
     */
    async onUnload(): Promise<void> {
        this.logger.info(`${this.getName()} unloading...`);
        
        // Clean up event listeners
        this.eventBus.removeAllListeners();
        
        this.logger.info(`${this.getName()} unloaded`);
    }

    /**
     * React to configuration changes
     */
    async onConfigChange(key: string, value: any): Promise<void> {
        this.logger.info(`Configuration changed: ${key} = ${value}`);
        
        if (key === 'greeting') {
            const context = this.getContext();
            if (context) {
                context.notificationService.info(`Greeting updated: ${value}`);
            }
        }
    }
}

// Export the plugin instance
export default HelloWorldPlugin;
