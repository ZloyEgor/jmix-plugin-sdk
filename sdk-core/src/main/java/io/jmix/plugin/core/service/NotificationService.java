package io.jmix.plugin.core.service;

/**
 * Convenience facade over {@link UIService#showNotification(UIService.NotificationOptions)}
 * with shortcut methods for the common notification levels.
 */
public interface NotificationService {

    void notify(UIService.NotificationOptions options);

    default void success(String message) {
        notify(new UIService.NotificationOptions(
                message, UIService.NotificationType.SUCCESS, null, null));
    }

    default void error(String message) {
        notify(new UIService.NotificationOptions(
                message, UIService.NotificationType.ERROR, null, null));
    }

    default void warning(String message) {
        notify(new UIService.NotificationOptions(
                message, UIService.NotificationType.WARNING, null, null));
    }

    default void info(String message) {
        notify(new UIService.NotificationOptions(
                message, UIService.NotificationType.INFO, null, null));
    }
}
