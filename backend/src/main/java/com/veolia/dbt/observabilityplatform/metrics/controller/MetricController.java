package com.veolia.dbt.observabilityplatform.metrics.controller;

import com.veolia.dbt.observabilityplatform.metrics.model.MetricData;
import com.veolia.dbt.observabilityplatform.metrics.service.MetricCollectionService;
import com.veolia.dbt.observabilityplatform.metrics.service.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "*") // For development - restrict in production
public class MetricController {

    private final MetricService metricService;
    private final MetricCollectionService collectionService;

    public MetricController(MetricService metricService, MetricCollectionService collectionService) {
        this.metricService = metricService;
        this.collectionService = collectionService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<MetricData>> getAllMetrics() {
        List<MetricData> metrics = metricService.getAllMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get metrics for a specific resource and metric name within a time range
     */
    @GetMapping("/resource/{resourceId}/metric/{metricName}")
    public ResponseEntity<List< MetricData >> getMetricsForResource(
            @PathVariable String resourceId,
            @PathVariable String metricName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        
        // Default to last hour if not specified
        if (startTime == null) {
            startTime = Instant.now().minus(1, ChronoUnit.HOURS);
        }
        if (endTime == null) {
            endTime = Instant.now();
        }
        
        List< MetricData > metrics = metricService.getMetricsForResource(resourceId, metricName, startTime, endTime);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get metrics for a resource type and metric name within a time range
     */
    @GetMapping("/type/{resourceType}/metric/{metricName}")
    public ResponseEntity<List< MetricData >> getMetricsForResourceType(
            @PathVariable String resourceType,
            @PathVariable String metricName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        
        // Default to last hour if not specified
        if (startTime == null) {
            startTime = Instant.now().minus(1, ChronoUnit.HOURS);
        }
        if (endTime == null) {
            endTime = Instant.now();
        }
        
        List< MetricData > metrics = metricService.getMetricsForResourceType(resourceType, metricName, startTime, endTime);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get the latest metrics for all resources
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, MetricData>> getLatestMetrics() {
        Map<String, MetricData> latestMetrics = metricService.getLatestMetricsForAllResources();
        return ResponseEntity.ok(latestMetrics);
    }

    //Triger further for a dynamic url
    /**
     * Get all metrics for a specific application 
     */
    @GetMapping("/application/{appName}/all")
    public ResponseEntity<List< MetricData >> getApplicationMetrics(@PathVariable String appName) {
        List< MetricData > metrics = metricService.getMetricsForApplication(appName);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get latest metrics for a specific application
     */
    @GetMapping("/application/{appName}/latest")
    public ResponseEntity<Map<String, MetricData>> getLatestApplicationMetrics(@PathVariable String appName) {
        Map<String, MetricData> latestMetrics = metricService.getLatestMetricsForApplication(appName);
        return ResponseEntity.ok(latestMetrics);
    }

    /**
     * Get health status for a specific application
     */
    @GetMapping("/application/{appName}/health")
    public ResponseEntity<Map<String, Object>> getApplicationHealth(@PathVariable String appName) {
        Map<String, Object> healthStatus = metricService.getApplicationHealthStatus(appName);
        return ResponseEntity.ok(healthStatus);
    }


    /**
     * Manually trigger metrics collection
     */
    @PostMapping("/collect")
    public ResponseEntity<Map<String, Object>> triggerCollection() {
        int count = collectionService.triggerCollection();
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "metricsCollected", count,
            "timestamp", Instant.now()
        ));
    }
}
