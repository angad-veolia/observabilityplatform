package com.veolia.dbt.observabilityplatform.metrics.service;

import com.veolia.dbt.observabilityplatform.metrics.model.MetricData;
import com.veolia.dbt.observabilityplatform.metrics.repository.MetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    //For Nagios Implementation!!
    
        /**
         * Get metrics for a specific application
         */
        public List< MetricData > getMetricsForApplication(String appName) {
            // Get all metrics
            List< MetricData > allMetrics = metricRepository.findAll();
            
            // Filter by application name (stored in labels)
            return allMetrics.stream()
                .filter(metric -> appName.equals(metric.getTags().get("application")))
                .collect(Collectors.toList());
        }

        /**
         * Get latest metrics for a specific application
         */
        public Map<String, MetricData> getLatestMetricsForApplication(String appName) {
            Map<String, MetricData> result = new HashMap<>();
            
            // Get all metrics for the application
            List< MetricData > appMetrics = getMetricsForApplication(appName);
            
            // Group by resource ID and metric name, keeping only the latest
            Map<String, Map<String, MetricData>> byResource = new HashMap<>();
            
            for (MetricData metric : appMetrics) {
                String resourceId = metric.getTags().get("host");
                String metricName = metric.getMetricName();
                String key = resourceId + ":" + metricName;
                
                if (!result.containsKey(key) || 
                    result.get(key).getTimestamp().isBefore(metric.getTimestamp())) {
                    result.put(key, metric);
                }
            }
            
            return result;
        }

        /**
         * Get application health status (green, amber, critical)
         */
        public Map<String, Object> getApplicationHealthStatus(String appName) {
            Map<String, Object> result = new HashMap<>();
            Map<String, String> statusByMetric = new HashMap<>();
            String overallStatus = "green";
            
            // Get latest metrics for the application
            Map<String, MetricData> latestMetrics = getLatestMetricsForApplication(appName);
            
            // Process all metrics for this application
            for (MetricData metric : latestMetrics.values()) {
                String metricName = metric.getMetricName();
                Double value = metric.getValue();
                
                // Get thresholds from labels
                Double warningThreshold = null;
                Double criticalThreshold = null;
                
                if (metric.getTags().containsKey("warning_threshold")) {
                    warningThreshold = Double.parseDouble(metric.getTags().get("warning_threshold"));
                }
                
                if (metric.getTags().containsKey("critical_threshold")) {
                    criticalThreshold = Double.parseDouble(metric.getTags().get("critical_threshold"));
                }
                
                // Use default thresholds if not available in labels
                if (warningThreshold == null) {
                    if ("CPU Usage".equals(metricName)) warningThreshold = 80.0;
                    else if ("Memory Usage".equals(metricName)) warningThreshold = 80.0;
                    else if ("Disk Usage on /".equals(metricName)) warningThreshold = 70.0;
                    else warningThreshold = 80.0; // Default
                }
                
                if (criticalThreshold == null) {
                    if ("CPU Usage".equals(metricName)) criticalThreshold = 95.0;
                    else if ("Memory Usage".equals(metricName)) criticalThreshold = 95.0;
                    else if ("Disk Usage on /".equals(metricName)) criticalThreshold = 90.0;
                    else criticalThreshold = 95.0; // Default
                }
                
                // Evaluate status based on thresholds
                String status = evaluateStatus(value, warningThreshold, criticalThreshold);
                statusByMetric.put(metricName, status);
                
                // Update overall status (worst case wins)
                overallStatus = worseStatus(overallStatus, status);
            }
            
            // Build result
            result.put("application", appName);
            result.put("overallStatus", overallStatus);
            result.put("metrics", statusByMetric);
            result.put("timestamp", Instant.now());
            
            return result;
        }

        /**
         * Evaluate status based on value and thresholds
         */
        private String evaluateStatus(Double value, Double warningThreshold, Double criticalThreshold) {
            if (value >= criticalThreshold) return "critical";
            if (value >= warningThreshold) return "amber";
            return "green";
        }

        /**
         * Return the worse of two statuses
         */
        private String worseStatus(String status1, String status2) {
            if ("critical".equals(status1) || "critical".equals(status2)) return "critical";
            if ("amber".equals(status1) || "amber".equals(status2)) return "amber";
            return "green";
        }


}
