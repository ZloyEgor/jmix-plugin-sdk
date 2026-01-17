import { PluginContext } from './PluginContext';
import { PluginDescriptor } from './PluginDescriptor';

/**
 * Base class for all Jmix plugins
 * 
 * @example
 * ```typescript
 * export class MyPlugin extends Plugin {
 *     async onLoad(context: PluginContext): Promise<void> {
 *         console.log('Plugin loaded!');
 *         context.logger.info(`${this.getName()} initialized`);
 *     }
 * 
 *     async onUnload(): Promise<void> {
 *         console.log('Plugin unloaded');
 *     }
 * }
 * ```
 */
export abstract class Plugin {
    private _descriptor?: PluginDescriptor;
    private _context?: PluginContext;
    private _loaded: boolean = false;

    /**
     * Get plugin descriptor
     */
    public getDescriptor(): PluginDescriptor | undefined {
        return this._descriptor;
    }

    /**
     * Set plugin descriptor (called by PluginManager)
     */
    public setDescriptor(descriptor: PluginDescriptor): void {
        this._descriptor = descriptor;
    }

    /**
     * Get plugin context
     */
    public getContext(): PluginContext | undefined {
        return this._context;
    }

    /**
     * Set plugin context (called by PluginManager)
     */
    public setContext(context: PluginContext): void {
        this._context = context;
    }

    /**
     * Get plugin ID
     */
    public getId(): string {
        return this._descriptor?.id || 'unknown';
    }

    /**
     * Get plugin name
     */
    public getName(): string {
        return this._descriptor?.name || 'Unknown Plugin';
    }

    /**
     * Get plugin version
     */
    public getVersion(): string {
        return this._descriptor?.version || '0.0.0';
    }

    /**
     * Check if plugin is loaded
     */
    public isLoaded(): boolean {
        return this._loaded;
    }

    /**
     * Mark plugin as loaded
     */
    protected markAsLoaded(): void {
        this._loaded = true;
    }

    /**
     * Mark plugin as unloaded
     */
    protected markAsUnloaded(): void {
        this._loaded = false;
    }

    /**
     * Called when plugin is loaded
     * Override this method to initialize your plugin
     */
    abstract onLoad(context: PluginContext): Promise<void>;

    /**
     * Called when plugin is unloaded
     * Override this method to clean up resources
     */
    abstract onUnload(): Promise<void>;

    /**
     * Called when plugin configuration changes
     * Override to react to config changes
     */
    public async onConfigChange?(key: string, value: any): Promise<void>;

    /**
     * Called to validate plugin configuration
     * Override to add custom validation
     */
    public async validateConfig?(config: Record<string, any>): Promise<ValidationResult>;

    /**
     * Get plugin configuration
     */
    protected getConfig<T = any>(key: string, defaultValue?: T): T | undefined {
        if (!this._context) {
            return defaultValue;
        }
        return this._context.getConfig(key) ?? defaultValue;
    }

    /**
     * Set plugin configuration
     */
    protected setConfig(key: string, value: any): void {
        if (!this._context) {
            throw new Error('Plugin context not available');
        }
        this._context.setConfig(key, value);
        if (this.onConfigChange) {
            this.onConfigChange(key, value);
        }
    }

    /**
     * Get logger
     */
    protected get logger() {
        if (!this._context) {
            throw new Error('Plugin context not available');
        }
        return this._context.logger;
    }

    /**
     * Get event bus
     */
    protected get eventBus() {
        if (!this._context) {
            throw new Error('Plugin context not available');
        }
        return this._context.eventBus;
    }

    /**
     * Emit event
     */
    protected emit(event: string, ...args: any[]): void {
        this.eventBus.emit(event, ...args);
    }

    /**
     * Listen to event
     */
    protected on(event: string, handler: (...args: any[]) => void): void {
        this.eventBus.on(event, handler);
    }

    /**
     * Listen to event once
     */
    protected once(event: string, handler: (...args: any[]) => void): void {
        this.eventBus.once(event, handler);
    }

    /**
     * Remove event listener
     */
    protected off(event: string, handler: (...args: any[]) => void): void {
        this.eventBus.off(event, handler);
    }
}

/**
 * Validation result interface
 */
export interface ValidationResult {
    valid: boolean;
    errors?: Array<{
        field: string;
        message: string;
    }>;
}

/**
 * Plugin state enum
 */
export enum PluginState {
    UNLOADED = 'unloaded',
    LOADING = 'loading',
    LOADED = 'loaded',
    UNLOADING = 'unloading',
    ERROR = 'error'
}

/**
 * Plugin info for status queries
 */
export interface PluginInfo {
    id: string;
    name: string;
    version: string;
    state: PluginState;
    error?: string;
    loadedAt?: Date;
}
