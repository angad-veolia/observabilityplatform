package com.veolia.dbt.observabilityplatform.metrics.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class MonitoredResource {
    private String id;
    private String name;
    private String resourceType;  // EC2, RDS, S3, Application, Server, etc.
    private String monitoringSystem;  // "AWS", "Nagios", etc.
}
