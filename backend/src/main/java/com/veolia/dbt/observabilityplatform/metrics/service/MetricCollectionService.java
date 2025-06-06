package com.veolia.dbt.observabilityplatform.metrics.service;

import com.veolia.dbt.observabilityplatform.metrics.collection.MetricCollector;
import com.veolia.dbt.observabilityplatform.metrics.model.MetricData;
import com.veolia.dbt.observabilityplatform.metrics.repository.MetricRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@EnableScheduling
public class MetricCollectionService {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollectionService.class);
    
    private final List< MetricCollector > collectors;
    private final MetricRepository metricRepository;
    
    @Autowired
    public MetricCollectionService(List< MetricCollector > collectors, MetricRepository metricRepository) {
        this.collectors = collectors;
        this.metricRepository = metricRepository;
    }
    
    /**
     * Scheduled method that collects metrics from all registered collectors
     * Runs every minute by default
     */
    @Scheduled(fixedRateString = "${metrics.collection.interval:5000}")
    public void collectMetrics() {
        logger.info("Starting metrics collection at {}", Instant.now());
        
        for (MetricCollector collector : collectors) {
            try {
                logger.debug("Collecting metrics from {}", collector.getCollectorName());
                List< MetricData > metrics = collector.collectMetrics();
                metricRepository.saveAll(metrics);
                logger.debug("Collected {} metrics from {}", metrics.size(), collector.getCollectorName());
            } catch (Exception e) {
                logger.error("Error collecting metrics from {}: {}", 
                        collector.getCollectorName(), e.getMessage(), e);
            }
        }
        
        logger.info("Completed metrics collection at {}", Instant.now());
    }
    
    /**
     * Manually trigger metrics collection
     * @return number of metrics collected
     */
    public int triggerCollection() {
        int totalCollected = 0;
        
        for (MetricCollector collector : collectors) {
            List< MetricData > metrics = collector.collectMetrics();
            metricRepository.saveAll(metrics);
            totalCollected += metrics.size();
        }
        
        return totalCollected;
    }
}
