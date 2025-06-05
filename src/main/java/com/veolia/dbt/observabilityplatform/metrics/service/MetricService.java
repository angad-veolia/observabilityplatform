package com.veolia.dbt.observabilityplatform.metrics.service;

import com.veolia.dbt.observabilityplatform.metrics.model.MetricData;
import com.veolia.dbt.observabilityplatform.metrics.repository.MetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricService {

    private final MetricRepository metricRepository;
    
    @Autowired
    public MetricService(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }
    
    /**
     * Get metrics for a specific resource and metric name within a time range
     */
    public List< MetricData > getMetricsForResource(String resourceId, String metricName, 
                                                 Instant startTime, Instant endTime) {
        return metricRepository.findByResourceIdAndMetricNameAndTimeRange(
                resourceId, metricName, startTime, endTime);
    }
    
    /**
     * Get metrics for a resource type and metric name within a time range
     */
    public List< MetricData > getMetricsForResourceType(String resourceType, String metricName, 
                                                    Instant startTime, Instant endTime) {
        return metricRepository.findByResourceTypeAndMetricNameAndTimeRange(
                resourceType, metricName, startTime, endTime);
    }

    public List< MetricData > getAllMetrics() {
        return metricRepository.findAll();
    }
    
    /**
     * Get the latest metrics for all resources
     * This is a simplified implementation - in a real app, you'd want to optimize this
     */
    public Map<String, MetricData> getLatestMetricsForAllResources() {
        // This is a dummy implementation that would be replaced with a more efficient query
        // in a real time-series database
        Map<String, MetricData> latestMetrics = new HashMap<>();
        
        // For demo purposes, we'll just return the latest metrics for our dummy resources
        // In a real implementation, you'd query the database for the latest metrics
        
        // EC2 CPU Utilization
        MetricData ec2Cpu = metricRepository.findLatestByResourceIdAndMetricName(
                "i-1234567890abcdef0", "CPUUtilization");
        if (ec2Cpu != null) {
            latestMetrics.put("ec2-cpu", ec2Cpu);
        }
        
        // RDS CPU Utilization
        MetricData rdsCpu = metricRepository.findLatestByResourceIdAndMetricName(
                "db-1234567890abcdef0", "CPUUtilization");
        if (rdsCpu != null) {
            latestMetrics.put("rds-cpu", rdsCpu);
        }
        
        // S3 Bucket Size
        MetricData s3Size = metricRepository.findLatestByResourceIdAndMetricName(
                "my-production-bucket", "BucketSizeBytes");
        if (s3Size != null) {
            latestMetrics.put("s3-size", s3Size);
        }
        
        // Nagios CPU Load
        MetricData nagiosCpu = metricRepository.findLatestByResourceIdAndMetricName(
                "app-server-01", "CPULoad");
        if (nagiosCpu != null) {
            latestMetrics.put("nagios-cpu", nagiosCpu);
        }
        
        return latestMetrics;
    }


}
