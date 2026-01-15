/**
 * Jmix Plugin SDK - Core
 * 
 * @packageDocumentation
 */

// Plugin system
export { Plugin, PluginState, PluginInfo, ValidationResult } from './plugin/Plugin';
export { PluginManager } from './plugin/PluginManager';
export { PluginDescriptor, PluginMetadata } from './plugin/PluginDescriptor';
export {
    PluginContext,
    DataService,
    SecurityService,
    UIService,
    NotificationService,
    ComponentRegistry,
    KeyValueStore,
    User,
    View,
    MetricsService,
    QueryOptions,
    NotificationOptions,
    DialogOptions
} from './plugin/PluginContext';

// Utilities
export { Logger, ConsoleLogger } from './utils/Logger';

// Version
export const SDK_VERSION = '1.0.0-alpha.1';
