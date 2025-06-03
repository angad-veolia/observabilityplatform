package com.veolia.dbt.observabilityplatform.metrics.repository;

import com.veolia.dbt.observabilityplatform.metrics.model.MetricData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MetricRepository extends JpaRepository<MetricData, Long> {
    
    // Custom query methods
    List< MetricData > findByMetricNameAndTimestampBetween(
        String metricName, Instant startTime, Instant endTime);
        
    List< MetricData > findByResourceIdAndMetricNameOrderByTimestampDesc(
        String resourceId, String metricName);
}
