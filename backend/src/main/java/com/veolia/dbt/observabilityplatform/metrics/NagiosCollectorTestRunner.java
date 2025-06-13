package com.veolia.dbt.observabilityplatform.metrics;

import com.veolia.dbt.observabilityplatform.metrics.collection.NagiosMetricCollector;
import com.veolia.dbt.observabilityplatform.metrics.collection.NagiosApiConfig;
import com.veolia.dbt.observabilityplatform.metrics.model.MetricData;
import com.veolia.dbt.observabilityplatform.metrics.repository.MetricRepository;
import com.veolia.dbt.observabilityplatform.metrics.service.MetricService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;



import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Component
@Profile("test") // Only run this in the test profile
public class NagiosCollectorTestRunner implements CommandLineRunner {

    private final NagiosMetricCollector nagiosCollector;
    private final MetricRepository repository;
    private final MetricService metricService;
    private final NagiosApiConfig apiConfig;

    public NagiosCollectorTestRunner(NagiosMetricCollector nagiosCollector, 
                               MetricRepository repository,
                               MetricService metricService,
                               NagiosApiConfig apiConfig) {
    this.nagiosCollector = nagiosCollector;
    this.repository = repository;
    this.metricService = metricService;
    this.apiConfig = apiConfig;
    }


    @Override
    public void run(String... args) throws Exception {

        System.out.println("\n==== TESTING NAGIOS METRIC COLLECTOR ====");
    
        // Print API configuration
        System.out.println("\nNagios API Configuration:");
        System.out.println("  API URL: " + apiConfig.getApiUrl());
        System.out.println("  API Key: " + (apiConfig.getApiKey() != null ? "[CONFIGURED]" : "[NOT CONFIGURED]"));
        System.out.println("  Hosts: " + apiConfig.getHostToAppMapping().keySet());

        //Test the Collector
        System.out.println("\n==== TESTING NAGIOS METRIC COLLECTOR ====");
        
        // Collect metrics from Nagios
        System.out.println("\nCollecting metrics from " + nagiosCollector.getCollectorName());
        List< MetricData > metrics = nagiosCollector.collectMetrics();
        
        // Print the number of metrics collected
        System.out.println("Collected " + metrics.size() + " metrics");
        
        // Print details of each metric
        System.out.println("\nMetric details:");
        for (MetricData metric : metrics) {
            System.out.println("\nHost: " + metric.getTags().get("host"));
            System.out.println("  Application: " + metric.getTags().get("application"));
            System.out.println("  Metric: " + metric.getMetricName());
            System.out.println("  Value: " + metric.getValue() + " " + metric.getTags().get("unit"));
            System.out.println("  Timestamp: " + metric.getTimestamp());
            System.out.println("  State: " + metric.getTags().get("state"));
            
            // Print thresholds if available
            if (metric.getTags().containsKey("warning_threshold")) {
                System.out.println("  Warning Threshold: " + metric.getTags().get("warning_threshold"));
            }
            if (metric.getTags().containsKey("critical_threshold")) {
                System.out.println("  Critical Threshold: " + metric.getTags().get("critical_threshold"));
            }
        }
        
        // Store the metrics in the repository
        repository.saveAll(metrics);
        System.out.println("\nStored " + metrics.size() + " metrics in the repository");
        
        // Test application filtering
        System.out.println("\n==== TESTING APPLICATION-SPECIFIC METHODS ====");
        
        // Test app1
        String appName = "app1";
        testApplicationMethods(appName);
        
        // Test app2
        appName = "app2";
        testApplicationMethods(appName);
        
        System.out.println("\n==== TEST COMPLETE ====");

         // Test application filtering
        System.out.println("\n==== TESTING APPLICATION-SPECIFIC METHODS ====\n");
        
        // Test app1
        appName = "app1";
        testApplicationMethods(appName);
        
        // Test app2
        appName = "app2";
        testApplicationMethods(appName);
        
        // Print metrics in JSON format
        printStoredMetricsAsJson();
        
        System.out.println("\n==== TEST COMPLETE ====\n");

        
    }
    
    private void printStoredMetricsAsJson() {
    System.out.println("\n==== STORED METRICS IN JSON FORMAT ====\n");
    
    // Get all metrics from repository
    List< MetricData > allMetrics = repository.findAll();
    
    // Filter to only include Nagios metrics
    List< MetricData > nagiosMetrics = allMetrics.stream()
        .filter(metric -> "Nagios".equals(metric.getTags().get("monitoringSystem")))
        .collect(Collectors.toList());
    
    System.out.println("Found " + nagiosMetrics.size() + " Nagios metrics");
    
    // Group by host and metric name
    Map<String, Map<String, List< MetricData >>> metricsByHostAndName = new HashMap<>();
    
    for (MetricData metric : nagiosMetrics) {
        String hostName = metric.getTags().get("host");
        String metricName = metric.getMetricName();
        
        // Create nested maps if they don't exist
        metricsByHostAndName.putIfAbsent(hostName, new HashMap<>());
        metricsByHostAndName.get(hostName).putIfAbsent(metricName, new ArrayList<>());
        
        // Add metric to the list
        metricsByHostAndName.get(hostName).get(metricName).add(metric);
    }
    
    // Print metrics in JSON format
    for (String hostName : metricsByHostAndName.keySet()) {
        System.out.println("Host: " + hostName);
        
        for (String metricName : metricsByHostAndName.get(hostName).keySet()) {
            System.out.println("  Metric: " + metricName);
            
            // Get the latest metric for this host and metric name
            MetricData latestMetric = metricsByHostAndName.get(hostName).get(metricName).stream()
                .max(Comparator.comparing(MetricData::getTimestamp))
                .orElse(null);
            
            if (latestMetric != null) {
                // Create JSON structure
                Map<String, Object> metricJson = new HashMap<>();
                metricJson.put("metric_name", latestMetric.getMetricName());
                metricJson.put("timestamp", latestMetric.getTimestamp());
                metricJson.put("value", latestMetric.getValue());
                
                Map<String, String> tags = new HashMap<>();
                tags.put("host", hostName);
                tags.put("application", latestMetric.getTags().get("application"));
                tags.put("state", latestMetric.getTags().get("state"));
                tags.put("warning_threshold", latestMetric.getTags().get("warning_threshold"));
                tags.put("critical_threshold", latestMetric.getTags().get("critical_threshold"));
                
                metricJson.put("tags", tags);
                
                // Convert to JSON string and print
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule()); // For proper timestamp serialization
                    String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(metricJson);
                    System.out.println(jsonString);
                } catch (Exception e) {
                    System.out.println("Error converting to JSON: " + e.getMessage());
                }
                
                System.out.println();
            }
        }
    }
    
    System.out.println("==== END OF STORED METRICS ====\n");
}

    private void testApplicationMethods(String appName) {
        System.out.println("\nTesting methods for application: " + appName);
        
        // Test getMetricsForApplication
        List< MetricData > appMetrics = metricService.getMetricsForApplication(appName);
        System.out.println("Found " + appMetrics.size() + " metrics for application " + appName);
        
        // Test getLatestMetricsForApplication
        Map<String, MetricData> latestMetrics = metricService.getLatestMetricsForApplication(appName);
        System.out.println("\nLatest metrics for application " + appName + ":");
        for (String key : latestMetrics.keySet()) {
            MetricData metric = latestMetrics.get(key);
            System.out.println("  " + metric.getTags().get("host") + " - " + 
                              metric.getMetricName() + ": " + 
                              metric.getValue() + " " + metric.getTags().get("unit"));
        }
        
        // Test getApplicationHealthStatus
        Map<String, Object> healthStatus = metricService.getApplicationHealthStatus(appName);
        System.out.println("\nHealth status for application " + appName + ":");
        System.out.println("  Overall status: " + healthStatus.get("overallStatus"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> metricStatuses = (Map<String, String>) healthStatus.get("metrics");
        System.out.println("  Metric statuses:");
        for (String metricName : metricStatuses.keySet()) {
            System.out.println("    " + metricName + ": " + metricStatuses.get(metricName));
        }
    }
    
}
