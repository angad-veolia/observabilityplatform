package com.veolia.dbt.observabilityplatform.metrics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricData {
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private MonitoredResource resource;
    private String metricName;
    private Double value;
    private Instant timestamp;
    private String unit;
    @Builder.Default
    private Map<String, String> labels = new HashMap<>();
    
}
