package io.jmix.plugin.core.service.support;

import io.jmix.plugin.core.service.MetricsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.LongAdder;

/**
 * Thread-safe in-memory {@link MetricsService} aggregating observations
 * and counters.
 */
public final class InMemoryMetricsService implements MetricsService {

    private final ConcurrentHashMap<String, List<Double>> observations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LongAdder> counters = new ConcurrentHashMap<>();

    @Override
    public void record(String name, double value) {
        observations.computeIfAbsent(name, k -> new CopyOnWriteArrayList<>()).add(value);
    }

    @Override
    public void increment(String name, double delta) {
        counters.computeIfAbsent(name, k -> new LongAdder()).add(Math.round(delta));
    }

    @Override
    public MetricsReport getReport() {
        Map<String, List<Double>> snapshot = new HashMap<>();
        observations.forEach((k, v) -> snapshot.put(k, List.copyOf(v)));
        Map<String, Long> countersSnapshot = new HashMap<>();
        counters.forEach((k, v) -> countersSnapshot.put(k, v.longValue()));
        return new SnapshotReport(snapshot, countersSnapshot);
    }

    private record SnapshotReport(Map<String, List<Double>> observations, Map<String, Long> counters)
            implements MetricsReport {

        @Override
        public double avg(String metric) {
            List<Double> values = observations.getOrDefault(metric, List.of());
            if (values.isEmpty()) {
                return 0.0;
            }
            double sum = 0;
            for (double v : values) {
                sum += v;
            }
            return sum / values.size();
        }

        @Override
        public double min(String metric) {
            return observations.getOrDefault(metric, List.of()).stream()
                    .mapToDouble(Double::doubleValue)
                    .min()
                    .orElse(0.0);
        }

        @Override
        public double max(String metric) {
            return observations.getOrDefault(metric, List.of()).stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0);
        }

        @Override
        public double sum(String metric) {
            return observations.getOrDefault(metric, List.of()).stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
        }

        @Override
        public long count(String metric) {
            return observations.getOrDefault(metric, List.of()).size();
        }

        @Override
        public Map<String, Long> counters() {
            return counters;
        }
    }
}
