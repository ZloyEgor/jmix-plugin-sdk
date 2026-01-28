package io.jmix.plugin.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Synchronous publish/subscribe event bus used by plugins to exchange
 * lifecycle and domain events. Implementations are thread-safe and never
 * propagate listener exceptions to the publisher.
 */
public final class EventBus {

    private static final Logger LOG = LoggerFactory.getLogger(EventBus.class);

    private final Map<String, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    /**
     * Subscribes the listener to the given topic.
     *
     * @return a {@link Subscription} that can be used to unregister later
     */
    public Subscription on(String topic, Consumer<Object> listener) {
        requireNonBlank(topic, "topic");
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        listeners.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(listener);
        return () -> off(topic, listener);
    }

    /**
     * Subscribes a listener that fires exactly once.
     */
    public Subscription once(String topic, Consumer<Object> listener) {
        Subscription[] holder = new Subscription[1];
        Consumer<Object> wrapper = payload -> {
            try {
                listener.accept(payload);
            } finally {
                if (holder[0] != null) {
                    holder[0].close();
                }
            }
        };
        holder[0] = on(topic, wrapper);
        return holder[0];
    }

    /**
     * Removes a previously registered listener.
     */
    public void off(String topic, Consumer<Object> listener) {
        List<Consumer<Object>> bucket = listeners.get(topic);
        if (bucket != null) {
            bucket.remove(listener);
        }
    }

    /**
     * Removes all listeners for the given topic.
     */
    public void removeAllListeners(String topic) {
        listeners.remove(topic);
    }

    /**
     * Removes every registered listener.
     */
    public void clear() {
        listeners.clear();
    }

    /**
     * Dispatches the payload to every listener subscribed to the topic.
     * Listener exceptions are logged but do not interrupt dispatching.
     */
    public void emit(String topic, Object payload) {
        List<Consumer<Object>> bucket = listeners.get(topic);
        if (bucket == null) {
            return;
        }
        for (Consumer<Object> listener : bucket) {
            try {
                listener.accept(payload);
            } catch (RuntimeException ex) {
                LOG.warn("Listener for topic '{}' threw {}", topic, ex.toString(), ex);
            }
        }
    }

    private static void requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    /**
     * Unsubscribe handle returned by {@link #on(String, Consumer)}.
     */
    @FunctionalInterface
    public interface Subscription extends AutoCloseable {

        @Override
        void close();
    }
}
