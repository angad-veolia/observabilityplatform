package com.veolia.dbt.observabilityplatform.metrics.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class NagiosResource extends MonitoredResource {
    private String hostGroup;
    private String serviceGroup;
    private String hostAddress;
    
    // Constructor that sets the monitoring system
    public NagiosResource() {
        setMonitoringSystem("Nagios");
    }
}

// This class extends MonitoredResource and adds Nagios-specific fields such as hostGroup, serviceGroup, and hostAddress.
// It also sets the monitoring system to "Nagios" in the constructor.