package com.veolia.dbt.observabilityplatform.metrics.collection;

import com.veolia.dbt.observabilityplatform.metrics.model.MetricData;
import java.util.List;

public interface MetricCollector {
    /**
     * Collects metrics from a specific source
     * @return List of collected metric data points
     */
    List< MetricData > collectMetrics();
    
    /**
     * Returns the name of this collector
     * @return collector name
     */
    String getCollectorName();
}
