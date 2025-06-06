package com.veolia.dbt.observabilityplatform.metrics.repository;

import com.veolia.dbt.observabilityplatform.metrics.model.MetricData;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryMetricRepository implements MetricRepository {

  // Using ConcurrentHashMap for thread safety
    private final ConcurrentHashMap<String, List< MetricData >> metricStore = new ConcurrentHashMap<>();

    @Override
    public void save(MetricData metricData) {
        String key = createKey(metricData.getResource().getId(), metricData.getMetricName());
        metricStore.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(metricData);
    }

    // Add a new method to get all metrics
   public List< MetricData > findAll() {
    return metricStore.values().stream()
            .flatMap(List::stream)
            .sorted(Comparator.comparing(MetricData::getTimestamp))
            .collect(Collectors.toList());
}


    @Override
    public void saveAll(List< MetricData > metricDataList) {
        metricDataList.forEach(this::save);
    }

    @Override
    public List< MetricData > findByResourceIdAndMetricNameAndTimeRange(
            String resourceId, String metricName, Instant startTime, Instant endTime) {
        
        String key = createKey(resourceId, metricName);
        List< MetricData > metrics = metricStore.getOrDefault(key, Collections.emptyList());
        
        return metrics.stream()
                .filter(metric -> !metric.getTimestamp().isBefore(startTime) && 
                                 !metric.getTimestamp().isAfter(endTime))
                .collect(Collectors.toList());
    }

    @Override
    public List< MetricData > findByResourceTypeAndMetricNameAndTimeRange(
            String resourceType, String metricName, Instant startTime, Instant endTime) {
        
        return metricStore.values().stream()
                .flatMap(List::stream)
                .filter(metric -> metric.getResource().getResourceType().equals(resourceType) &&
                                 metric.getMetricName().equals(metricName) &&
                                 !metric.getTimestamp().isBefore(startTime) &&
                                 !metric.getTimestamp().isAfter(endTime))
                .collect(Collectors.toList());
    }

    @Override
    public MetricData findLatestByResourceIdAndMetricName(String resourceId, String metricName) {
        String key = createKey(resourceId, metricName);
        List< MetricData > metrics = metricStore.getOrDefault(key, Collections.emptyList());
        
        return metrics.stream()
                .max((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()))
                .orElse(null);
    }
    
    private String createKey(String resourceId, String metricName) {
        return resourceId + ":" + metricName;
    }
}
