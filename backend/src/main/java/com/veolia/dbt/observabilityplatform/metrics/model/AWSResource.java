package com.veolia.dbt.observabilityplatform.metrics.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class AWSResource extends MonitoredResource {
    private String awsRegion;
    private String accountId;
    private String arnId;
    
    // Constructor that sets the monitoring system
    public AWSResource() {
        setMonitoringSystem("AWS");
    }
}
// This class extends MonitoredResource and adds AWS-specific fields such as awsRegion, accountId, and arnId.
// It also sets the monitoring system to "AWS" in the constructor.