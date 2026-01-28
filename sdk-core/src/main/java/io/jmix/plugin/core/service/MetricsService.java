package io.jmix.plugin.core.service;

import java.util.Map;

/**
 * Lightweight metrics façade. Implementations may delegate to Micrometer
 * or any other host-provided metrics infrastructure.
 */
public interface MetricsService {

    /**
     * Records a numeric observation for the given metric name.
     */
    void record(String name, double value);

    /**
     * Increments a counter by the given delta. {@code delta = 1} when omitted.
     */
    default void increment(String name) {
        increment(name, 1.0);
    }

    void increment(String name, double delta);

    /**
     * Aggregated snapshot of all recorded metrics.
     */
    MetricsReport getReport();

    /**
     * Aggregated read-only metrics view.
     */
    interface MetricsReport {

        double avg(String metric);

        double min(String metric);

        double max(String metric);

        double sum(String metric);

        long count(String metric);

        Map<String, Long> counters();
    }
}
