/**
 * Logger interface for plugins
 */
export interface Logger {
    debug(message: string, ...meta: any[]): void;
    info(message: string, ...meta: any[]): void;
    warn(message: string, ...meta: any[]): void;
    error(message: string, ...meta: any[]): void;
    child(bindings: Record<string, any>): Logger;
}

/**
 * Console-based logger implementation
 */
export class ConsoleLogger implements Logger {
    constructor(private bindings: Record<string, any> = {}) {}

    private format(level: string, message: string, meta: any[]): string {
        const timestamp = new Date().toISOString();
        const bindingsStr = Object.keys(this.bindings).length > 0
            ? ` ${JSON.stringify(this.bindings)}`
            : '';
        const metaStr = meta.length > 0 ? ` ${JSON.stringify(meta)}` : '';
        return `[${timestamp}] ${level}:${bindingsStr} ${message}${metaStr}`;
    }

    debug(message: string, ...meta: any[]): void {
        console.debug(this.format('DEBUG', message, meta));
    }

    info(message: string, ...meta: any[]): void {
        console.info(this.format('INFO', message, meta));
    }

    warn(message: string, ...meta: any[]): void {
        console.warn(this.format('WARN', message, meta));
    }

    error(message: string, ...meta: any[]): void {
        console.error(this.format('ERROR', message, meta));
    }

    child(bindings: Record<string, any>): Logger {
        return new ConsoleLogger({ ...this.bindings, ...bindings });
    }
}
