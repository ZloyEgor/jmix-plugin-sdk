/**
 * Plugin descriptor interface matching the plugin.json schema
 */
export interface PluginDescriptor {
    /**
     * Unique plugin identifier (reverse domain notation recommended)
     * Example: io.jmix.plugins.advanced-grid
     */
    id: string;

    /**
     * Human-readable plugin name
     */
    name: string;

    /**
     * Semantic version (MAJOR.MINOR.PATCH)
     */
    version: string;

    /**
     * Plugin description
     */
    description: string;

    /**
     * Plugin author information
     */
    author: {
        name: string;
        email?: string;
        url?: string;
    };

    /**
     * License information
     */
    license: {
        type: string;
        url?: string;
    };

    /**
     * Platform compatibility requirements
     */
    compatibility: {
        jmix: string; // Version range (e.g., ">=2.0.0 <3.0.0")
        java?: string;
        springBoot?: string;
    };

    /**
     * Plugin dependencies
     */
    dependencies?: {
        plugins?: Array<{
            id: string;
            version: string; // Version range
        }>;
        maven?: Array<{
            groupId: string;
            artifactId: string;
            version: string;
        }>;
    };

    /**
     * UI components provided by this plugin
     */
    components?: Array<{
        tag: string; // XML tag name
        class: string; // Fully qualified class name
        icon?: string; // Icon path
    }>;

    /**
     * Permissions required by this plugin
     */
    permissions?: string[];

    /**
     * Entry points
     */
    entrypoints: {
        main: string; // Main plugin class or module
        config?: string; // Configuration schema path
    };

    /**
     * Plugin configuration
     */
    configuration?: {
        schema?: string; // JSON Schema path
        defaults?: Record<string, any>;
    };

    /**
     * Marketplace-specific metadata
     */
    marketplace?: {
        categories: string[];
        keywords: string[];
        icon: string;
        screenshots?: string[];
        demo?: string;
    };

    /**
     * Repository information
     */
    repository?: {
        type: string;
        url: string;
    };

    /**
     * Documentation URL
     */
    documentation?: string;

    /**
     * Changelog file path
     */
    changelog?: string;

    /**
     * Support information
     */
    support?: {
        email?: string;
        forum?: string;
        issues?: string;
    };
}

/**
 * Validation result for plugin descriptor
 */
export interface ValidationResult {
    valid: boolean;
    errors?: Array<{
        path: string;
        message: string;
    }>;
}

/**
 * Plugin metadata extracted from descriptor
 */
export interface PluginMetadata {
    id: string;
    name: string;
    version: string;
    description: string;
    author: string;
    license: string;
}
