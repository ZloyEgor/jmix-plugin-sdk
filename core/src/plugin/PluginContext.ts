import { EventEmitter } from 'events';
import { Logger } from '../utils/Logger';

/**
 * Plugin context provided to plugins at runtime
 * Contains access to platform services and APIs
 */
export interface PluginContext {
    /**
     * Core services
     */
    readonly dataService: DataService;
    readonly securityService: SecurityService;
    readonly uiService: UIService;
    readonly notificationService: NotificationService;

    /**
     * Component registry
     */
    readonly componentRegistry: ComponentRegistry;

    /**
     * Platform information
     */
    readonly platformVersion: string;
    readonly environment: 'development' | 'production' | 'test';

    /**
     * Configuration access
     */
    getConfig<T = any>(key: string): T | undefined;
    setConfig(key: string, value: any): void;
    hasConfig(key: string): boolean;

    /**
     * Storage
     */
    readonly localStorage: KeyValueStore;
    readonly sessionStorage: KeyValueStore;

    /**
     * Logging
     */
    readonly logger: Logger;

    /**
     * Event bus for pub/sub
     */
    readonly eventBus: EventEmitter;

    /**
     * Current user and session
     */
    readonly currentUser: User | null;
    readonly permissions: string[];

    /**
     * Permission checking
     */
    hasPermission(permission: string): boolean;

    /**
     * Metrics
     */
    readonly metrics: MetricsService;
}

/**
 * Data service interface
 */
export interface DataService {
    /**
     * Query entities
     */
    query<T = any>(entityName: string, options?: QueryOptions): Promise<T[]>;

    /**
     * Get single entity by ID
     */
    get<T = any>(entityName: string, id: string | number): Promise<T | null>;

    /**
     * Create new entity
     */
    create<T = any>(entityName: string, data: Partial<T>): Promise<T>;

    /**
     * Update existing entity
     */
    update<T = any>(entityName: string, id: string | number, data: Partial<T>): Promise<T>;

    /**
     * Delete entity
     */
    delete(entityName: string, id: string | number): Promise<boolean>;

    /**
     * Execute custom query
     */
    executeQuery<T = any>(queryName: string, params?: Record<string, any>): Promise<T[]>;
}

/**
 * Query options for data service
 */
export interface QueryOptions {
    filter?: Record<string, any>;
    sort?: Array<{ field: string; order: 'asc' | 'desc' }>;
    limit?: number;
    offset?: number;
    fetchPlan?: string;
}

/**
 * Security service interface
 */
export interface SecurityService {
    /**
     * Check if current user has permission
     */
    hasPermission(permission: string): boolean;

    /**
     * Check if current user has role
     */
    hasRole(role: string): boolean;

    /**
     * Get current user
     */
    getCurrentUser(): User | null;

    /**
     * Check if authenticated
     */
    isAuthenticated(): boolean;

    /**
     * Validate entity access
     */
    canAccess(entityName: string, operation: 'create' | 'read' | 'update' | 'delete'): boolean;
}

/**
 * UI service interface
 */
export interface UIService {
    /**
     * Show notification
     */
    showNotification(options: NotificationOptions): void;

    /**
     * Show dialog
     */
    showDialog(options: DialogOptions): Promise<any>;

    /**
     * Navigate to view
     */
    navigateTo(viewId: string, params?: Record<string, any>): void;

    /**
     * Get current view
     */
    getCurrentView(): View | null;

    /**
     * Refresh view
     */
    refreshView(): void;
}

/**
 * Notification service
 */
export interface NotificationService {
    /**
     * Send notification
     */
    notify(options: NotificationOptions): void;

    /**
     * Success notification
     */
    success(message: string): void;

    /**
     * Error notification
     */
    error(message: string): void;

    /**
     * Warning notification
     */
    warning(message: string): void;

    /**
     * Info notification
     */
    info(message: string): void;
}

/**
 * Notification options
 */
export interface NotificationOptions {
    message: string;
    type: 'success' | 'error' | 'warning' | 'info';
    duration?: number;
    position?: 'top' | 'bottom' | 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right';
}

/**
 * Dialog options
 */
export interface DialogOptions {
    title: string;
    content: string | any;
    buttons?: Array<{
        text: string;
        action: () => void;
        primary?: boolean;
    }>;
    width?: string;
    height?: string;
}

/**
 * Component registry
 */
export interface ComponentRegistry {
    /**
     * Register component
     */
    register(tag: string, component: any): void;

    /**
     * Get component by tag
     */
    get(tag: string): any;

    /**
     * Check if component exists
     */
    has(tag: string): boolean;

    /**
     * Unregister component
     */
    unregister(tag: string): void;

    /**
     * Get all registered tags
     */
    getTags(): string[];
}

/**
 * Key-value store interface
 */
export interface KeyValueStore {
    get<T = any>(key: string): Promise<T | null>;
    set(key: string, value: any): Promise<void>;
    delete(key: string): Promise<void>;
    has(key: string): Promise<boolean>;
    keys(): Promise<string[]>;
    clear(): Promise<void>;
}

/**
 * User interface
 */
export interface User {
    id: string;
    username: string;
    email?: string;
    firstName?: string;
    lastName?: string;
    roles: string[];
    permissions: string[];
}

/**
 * View interface
 */
export interface View {
    id: string;
    title: string;
    params: Record<string, any>;
}

/**
 * Metrics service
 */
export interface MetricsService {
    /**
     * Record a metric value
     */
    record(name: string, value: number): void;

    /**
     * Increment counter
     */
    increment(name: string, delta?: number): void;

    /**
     * Get metric report
     */
    getReport(): Promise<MetricsReport>;
}

/**
 * Metrics report
 */
export interface MetricsReport {
    avg(metric: string): number;
    min(metric: string): number;
    max(metric: string): number;
    sum(metric: string): number;
    count(metric: string): number;
}
