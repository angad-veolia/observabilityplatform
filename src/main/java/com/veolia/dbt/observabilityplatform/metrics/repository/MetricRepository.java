package com.veolia.dbt.observabilityplatform.metrics.repository;

import com.veolia.dbt.observabilityplatform.metrics.model.MetricData;

import java.time.Instant;
import java.util.List;

public interface MetricRepository {
    
    /**
     * Save a single metric data point
     */
    void save(MetricData metricData);
    
    /**
     * Save multiple metric data points
     */
    void saveAll(List< MetricData > metricDataList);

    /**
     * Find all metrics stored in the repository
     */
    List< MetricData > findAll();
    
    /**
     * Find metrics by resource ID and metric name within a time range
     */
    List< MetricData > findByResourceIdAndMetricNameAndTimeRange(
            String resourceId, String metricName, Instant startTime, Instant endTime);
    
    /**
     * Find metrics by resource type and metric name within a time range
     */
    List< MetricData > findByResourceTypeAndMetricNameAndTimeRange(
            String resourceType, String metricName, Instant startTime, Instant endTime);
    
    /**
     * Get the latest metric value for a specific resource and metric name
     */
    MetricData findLatestByResourceIdAndMetricName(String resourceId, String metricName);
}
