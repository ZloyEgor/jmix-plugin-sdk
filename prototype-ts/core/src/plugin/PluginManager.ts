import { Plugin, PluginState, PluginInfo } from './Plugin';
import { PluginDescriptor, ValidationResult } from './PluginDescriptor';
import { PluginContext } from './PluginContext';
import * as semver from 'semver';
import { EventEmitter } from 'events';

/**
 * Plugin manager responsible for loading, managing, and unloading plugins
 * 
 * @example
 * ```typescript
 * const manager = new PluginManager(platformContext);
 * 
 * // Register plugin
 * await manager.register(myPlugin, descriptor);
 * 
 * // Load plugin
 * await manager.load('my-plugin-id');
 * 
 * // Get plugin
 * const plugin = manager.get('my-plugin-id');
 * 
 * // Unload plugin
 * await manager.unload('my-plugin-id');
 * ```
 */
export class PluginManager extends EventEmitter {
    private plugins: Map<string, Plugin> = new Map();
    private descriptors: Map<string, PluginDescriptor> = new Map();
    private states: Map<string, PluginState> = new Map();
    private errors: Map<string, string> = new Map();
    private loadOrder: string[] = [];
    private platformContext: PluginContext;

    constructor(platformContext: PluginContext) {
        super();
        this.platformContext = platformContext;
    }

    /**
     * Register a plugin with its descriptor
     */
    public async register(plugin: Plugin, descriptor: PluginDescriptor): Promise<void> {
        // Validate descriptor
        const validation = this.validateDescriptor(descriptor);
        if (!validation.valid) {
            throw new Error(`Invalid plugin descriptor: ${validation.errors?.map(e => e.message).join(', ')}`);
        }

        // Check if plugin already registered
        if (this.plugins.has(descriptor.id)) {
            throw new Error(`Plugin ${descriptor.id} is already registered`);
        }

        // Check compatibility
        this.checkCompatibility(descriptor);

        // Check dependencies
        await this.checkDependencies(descriptor);

        // Store plugin and descriptor
        plugin.setDescriptor(descriptor);
        this.plugins.set(descriptor.id, plugin);
        this.descriptors.set(descriptor.id, descriptor);
        this.states.set(descriptor.id, PluginState.UNLOADED);

        this.emit('plugin:registered', descriptor.id);
    }

    /**
     * Load a plugin by ID
     */
    public async load(pluginId: string): Promise<void> {
        const plugin = this.plugins.get(pluginId);
        if (!plugin) {
            throw new Error(`Plugin ${pluginId} not found`);
        }

        // Check if already loaded
        const currentState = this.states.get(pluginId);
        if (currentState === PluginState.LOADED) {
            return;
        }

        if (currentState === PluginState.LOADING) {
            throw new Error(`Plugin ${pluginId} is already loading`);
        }

        try {
            this.states.set(pluginId, PluginState.LOADING);
            this.emit('plugin:loading', pluginId);

            // Load dependencies first
            await this.loadDependencies(pluginId);

            // Create plugin context
            const context = this.createPluginContext(pluginId);
            plugin.setContext(context);

            // Call onLoad
            await plugin.onLoad(context);

            plugin.markAsLoaded();
            this.states.set(pluginId, PluginState.LOADED);
            this.loadOrder.push(pluginId);
            this.errors.delete(pluginId);

            this.emit('plugin:loaded', pluginId);
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : String(error);
            this.states.set(pluginId, PluginState.ERROR);
            this.errors.set(pluginId, errorMessage);
            this.emit('plugin:error', pluginId, errorMessage);
            throw new Error(`Failed to load plugin ${pluginId}: ${errorMessage}`);
        }
    }

    /**
     * Unload a plugin by ID
     */
    public async unload(pluginId: string): Promise<void> {
        const plugin = this.plugins.get(pluginId);
        if (!plugin) {
            throw new Error(`Plugin ${pluginId} not found`);
        }

        const currentState = this.states.get(pluginId);
        if (currentState !== PluginState.LOADED) {
            return;
        }

        try {
            this.states.set(pluginId, PluginState.UNLOADING);
            this.emit('plugin:unloading', pluginId);

            // Check if other plugins depend on this
            const dependents = this.getDependents(pluginId);
            if (dependents.length > 0) {
                throw new Error(
                    `Cannot unload ${pluginId}: plugins ${dependents.join(', ')} depend on it`
                );
            }

            // Call onUnload
            await plugin.onUnload();

            plugin.markAsUnloaded();
            this.states.set(pluginId, PluginState.UNLOADED);
            this.loadOrder = this.loadOrder.filter(id => id !== pluginId);

            this.emit('plugin:unloaded', pluginId);
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : String(error);
            this.states.set(pluginId, PluginState.ERROR);
            this.errors.set(pluginId, errorMessage);
            throw new Error(`Failed to unload plugin ${pluginId}: ${errorMessage}`);
        }
    }

    /**
     * Get plugin by ID
     */
    public get<T extends Plugin = Plugin>(pluginId: string): T | undefined {
        return this.plugins.get(pluginId) as T | undefined;
    }

    /**
     * Get plugin descriptor
     */
    public getDescriptor(pluginId: string): PluginDescriptor | undefined {
        return this.descriptors.get(pluginId);
    }

    /**
     * Get plugin state
     */
    public getState(pluginId: string): PluginState {
        return this.states.get(pluginId) || PluginState.UNLOADED;
    }

    /**
     * Get plugin info
     */
    public getInfo(pluginId: string): PluginInfo | undefined {
        const descriptor = this.descriptors.get(pluginId);
        if (!descriptor) {
            return undefined;
        }

        return {
            id: descriptor.id,
            name: descriptor.name,
            version: descriptor.version,
            state: this.getState(pluginId),
            error: this.errors.get(pluginId)
        };
    }

    /**
     * List all registered plugins
     */
    public list(): PluginInfo[] {
        return Array.from(this.descriptors.keys()).map(id => this.getInfo(id)!);
    }

    /**
     * List loaded plugins
     */
    public listLoaded(): PluginInfo[] {
        return this.list().filter(info => info.state === PluginState.LOADED);
    }

    /**
     * Check if plugin is loaded
     */
    public isLoaded(pluginId: string): boolean {
        return this.getState(pluginId) === PluginState.LOADED;
    }

    /**
     * Load all registered plugins
     */
    public async loadAll(): Promise<void> {
        const sorted = this.topologicalSort();
        for (const pluginId of sorted) {
            if (!this.isLoaded(pluginId)) {
                await this.load(pluginId);
            }
        }
    }

    /**
     * Unload all plugins
     */
    public async unloadAll(): Promise<void> {
        // Unload in reverse order
        for (const pluginId of [...this.loadOrder].reverse()) {
            await this.unload(pluginId);
        }
    }

    /**
     * Validate plugin descriptor
     */
    private validateDescriptor(descriptor: PluginDescriptor): ValidationResult {
        const errors: Array<{ path: string; message: string }> = [];

        if (!descriptor.id) {
            errors.push({ path: 'id', message: 'Plugin ID is required' });
        }

        if (!descriptor.name) {
            errors.push({ path: 'name', message: 'Plugin name is required' });
        }

        if (!descriptor.version || !semver.valid(descriptor.version)) {
            errors.push({ path: 'version', message: 'Invalid semantic version' });
        }

        if (!descriptor.entrypoints?.main) {
            errors.push({ path: 'entrypoints.main', message: 'Main entry point is required' });
        }

        return {
            valid: errors.length === 0,
            errors: errors.length > 0 ? errors : undefined
        };
    }

    /**
     * Check platform compatibility
     */
    private checkCompatibility(descriptor: PluginDescriptor): void {
        const platformVersion = this.platformContext.platformVersion;

        if (descriptor.compatibility?.jmix) {
            if (!semver.satisfies(platformVersion, descriptor.compatibility.jmix)) {
                throw new Error(
                    `Plugin ${descriptor.id} requires Jmix ${descriptor.compatibility.jmix}, ` +
                    `but platform version is ${platformVersion}`
                );
            }
        }
    }

    /**
     * Check plugin dependencies
     */
    private async checkDependencies(descriptor: PluginDescriptor): Promise<void> {
        const pluginDeps = descriptor.dependencies?.plugins || [];

        for (const dep of pluginDeps) {
            const depDescriptor = this.descriptors.get(dep.id);
            if (!depDescriptor) {
                throw new Error(
                    `Plugin ${descriptor.id} depends on ${dep.id}, which is not registered`
                );
            }

            if (!semver.satisfies(depDescriptor.version, dep.version)) {
                throw new Error(
                    `Plugin ${descriptor.id} requires ${dep.id} ${dep.version}, ` +
                    `but version ${depDescriptor.version} is registered`
                );
            }
        }
    }

    /**
     * Load plugin dependencies
     */
    private async loadDependencies(pluginId: string): Promise<void> {
        const descriptor = this.descriptors.get(pluginId);
        if (!descriptor) {
            return;
        }

        const pluginDeps = descriptor.dependencies?.plugins || [];
        for (const dep of pluginDeps) {
            if (!this.isLoaded(dep.id)) {
                await this.load(dep.id);
            }
        }
    }

    /**
     * Get plugins that depend on the given plugin
     */
    private getDependents(pluginId: string): string[] {
        const dependents: string[] = [];

        for (const [id, descriptor] of this.descriptors.entries()) {
            const deps = descriptor.dependencies?.plugins || [];
            if (deps.some(dep => dep.id === pluginId)) {
                if (this.isLoaded(id)) {
                    dependents.push(id);
                }
            }
        }

        return dependents;
    }

    /**
     * Topological sort for dependency order
     */
    private topologicalSort(): string[] {
        const sorted: string[] = [];
        const visited = new Set<string>();
        const visiting = new Set<string>();

        const visit = (pluginId: string) => {
            if (visited.has(pluginId)) {
                return;
            }

            if (visiting.has(pluginId)) {
                throw new Error(`Circular dependency detected: ${pluginId}`);
            }

            visiting.add(pluginId);

            const descriptor = this.descriptors.get(pluginId);
            const deps = descriptor?.dependencies?.plugins || [];

            for (const dep of deps) {
                visit(dep.id);
            }

            visiting.delete(pluginId);
            visited.add(pluginId);
            sorted.push(pluginId);
        };

        for (const pluginId of this.descriptors.keys()) {
            visit(pluginId);
        }

        return sorted;
    }

    /**
     * Create plugin-specific context
     */
    private createPluginContext(pluginId: string): PluginContext {
        // Create context with plugin-specific prefix for config, storage, etc.
        const descriptor = this.descriptors.get(pluginId)!;

        return {
            ...this.platformContext,
            // Plugin-specific logger
            logger: this.platformContext.logger.child({ plugin: pluginId }),
            
            // Plugin-specific config namespace
            getConfig: <T = any>(key: string) => {
                return this.platformContext.getConfig(`plugins.${pluginId}.${key}`);
            },
            setConfig: (key: string, value: any) => {
                this.platformContext.setConfig(`plugins.${pluginId}.${key}`, value);
            },
            hasConfig: (key: string) => {
                return this.platformContext.hasConfig(`plugins.${pluginId}.${key}`);
            }
        };
    }
}
