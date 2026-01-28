package io.jmix.plugin.core.service;

import io.jmix.plugin.core.view.View;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * User interface integration facade.
 */
public interface UIService {

    void showNotification(NotificationOptions options);

    CompletableFuture<Object> showDialog(DialogOptions options);

    void navigateTo(String viewId, Map<String, Object> params);

    Optional<View> getCurrentView();

    void refreshCurrentView();

    /**
     * Notification rendering parameters.
     */
    record NotificationOptions(
            String message,
            NotificationType type,
            Integer durationMs,
            Position position) {

        public NotificationOptions {
            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("message must not be blank");
            }
            if (type == null) {
                type = NotificationType.INFO;
            }
        }
    }

    enum NotificationType { SUCCESS, ERROR, WARNING, INFO }

    enum Position { TOP, BOTTOM, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    /**
     * Dialog rendering parameters.
     */
    record DialogOptions(
            String title,
            String content,
            List<Button> buttons,
            String width,
            String height) {

        public DialogOptions {
            if (title == null) {
                title = "";
            }
            if (content == null) {
                content = "";
            }
            buttons = buttons == null ? List.of() : List.copyOf(buttons);
        }
    }

    /**
     * Dialog button definition.
     */
    record Button(String text, boolean primary, Runnable action) { }
}
