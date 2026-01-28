package io.jmix.plugin.core.service.support;

import io.jmix.plugin.core.service.UIService;
import io.jmix.plugin.core.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * {@link UIService} stub that logs interactions instead of rendering
 * them. Intended for headless contexts.
 */
public final class NoopUIService implements UIService {

    private static final Logger LOG = LoggerFactory.getLogger(NoopUIService.class);

    @Override
    public void showNotification(NotificationOptions options) {
        LOG.info("[notification] {}: {}", options.type(), options.message());
    }

    @Override
    public CompletableFuture<Object> showDialog(DialogOptions options) {
        LOG.info("[dialog] {}: {}", options.title(), options.content());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void navigateTo(String viewId, Map<String, Object> params) {
        LOG.info("[navigation] viewId={} params={}", viewId, params);
    }

    @Override
    public Optional<View> getCurrentView() {
        return Optional.empty();
    }

    @Override
    public void refreshCurrentView() {
        // no-op
    }
}
