package com.veolia.dbt.observabilityplatform.metrics.collection;

import com.veolia.dbt.observabilityplatform.metrics.model.AWSResource;
import com.veolia.dbt.observabilityplatform.metrics.model.MetricData;
import com.veolia.dbt.observabilityplatform.metrics.model.NagiosResource;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DummyMetricCollector implements MetricCollector {

    private final Random random = new Random();
    
    @Override
    public List< MetricData > collectMetrics() {
        List< MetricData > metrics = new ArrayList<>();
        
        // Add EC2 metrics
        metrics.addAll(generateEC2Metrics());
        
        // Add RDS metrics
        metrics.addAll(generateRDSMetrics());
        
        // Add S3 metrics
        metrics.addAll(generateS3Metrics());
        
        // Add Nagios metrics
        metrics.addAll(generateNagiosMetrics());
        
        return metrics;
    }
    
    @Override
    public String getCollectorName() {
        return "DummyCollector";
    }
    
    private List< MetricData > generateEC2Metrics() {
        List< MetricData > metrics = new ArrayList<>();
        
        // Create EC2 resource
        AWSResource ec2Resource = AWSResource.builder()
                .id("i-1234567890abcdef0")
                .name("Production Web Server")
                .resourceType("EC2")
                .awsRegion("us-east-1")
                .accountId("123456789012")
                .build();
        
        // CPU Utilization
        metrics.add(MetricData.builder()
                .resource(ec2Resource)
                .metricName("CPUUtilization")
                .value(10.0 + random.nextDouble() * 80.0) // Random between 10-90%
                .timestamp(Instant.now())
                .unit("Percent")
                .build());
        
        // Status Check Failed
        metrics.add(MetricData.builder()
                .resource(ec2Resource)
                .metricName("StatusCheckFailed")
                .value(random.nextDouble() < 0.05 ? 1.0 : 0.0) // 5% chance of failure
                .timestamp(Instant.now())
                .unit("Count")
                .build());
        
        return metrics;
    }
    
    private List< MetricData > generateRDSMetrics() {
        List< MetricData > metrics = new ArrayList<>();
        
        // Create RDS resource
        AWSResource rdsResource = AWSResource.builder()
                .id("db-1234567890abcdef0")
                .name("Production Database")
                .resourceType("RDS")
                .awsRegion("us-east-1")
                .accountId("123456789012")
                .build();
        
        // CPU Utilization
        metrics.add(MetricData.builder()
                .resource(rdsResource)
                .metricName("CPUUtilization")
                .value(5.0 + random.nextDouble() * 75.0) // Random between 5-80%
                .timestamp(Instant.now())
                .unit("Percent")
                .build());
        
        // Read Latency
        metrics.add(MetricData.builder()
                .resource(rdsResource)
                .metricName("ReadLatency")
                .value(1.0 + random.nextDouble() * 99.0) // Random between 1-100ms
                .timestamp(Instant.now())
                .unit("Milliseconds")
                .build());
        
        // Read Throughput
        metrics.add(MetricData.builder()
                .resource(rdsResource)
                .metricName("ReadThroughput")
                .value(100.0 + random.nextDouble() * 9900.0) // Random between 100-10000 ops/sec
                .timestamp(Instant.now())
                .unit("Count/Second")
                .build());
        
        return metrics;
    }
    
    private List< MetricData > generateS3Metrics() {
        List< MetricData > metrics = new ArrayList<>();
        
        // Create S3 resource
        AWSResource s3Resource = AWSResource.builder()
                .id("my-production-bucket")
                .name("Production Data Bucket")
                .resourceType("S3")
                .awsRegion("us-east-1")
                .accountId("123456789012")
                .build();
        
        // Bucket Size
        metrics.add(MetricData.builder()
                .resource(s3Resource)
                .metricName("BucketSizeBytes")
                .value(1.0 + random.nextDouble() * 999.0) // Random between 1-1000 GB
                .timestamp(Instant.now())
                .unit("Gigabytes")
                .build());
        
        // Number of Objects
        metrics.add(MetricData.builder()
                .resource(s3Resource)
                .metricName("NumberOfObjects")
                .value(100.0 + random.nextDouble() * 99900.0) // Random between 100-100000
                .timestamp(Instant.now())
                .unit("Count")
                .build());
        
        return metrics;
    }
    
    private List< MetricData > generateNagiosMetrics() {
        List< MetricData > metrics = new ArrayList<>();
        
        // Create Nagios resource
        NagiosResource nagiosResource = NagiosResource.builder()
                .id("app-server-01")
                .name("Application Server")
                .resourceType("Server")
                .hostGroup("AppServers")
                .serviceGroup("WebServices")
                .hostAddress("10.0.0.100")
                .build();
        
        // CPU Load
        metrics.add(MetricData.builder()
                .resource(nagiosResource)
                .metricName("CPULoad")
                .value(0.1 + random.nextDouble() * 3.9) // Random between 0.1-4.0
                .timestamp(Instant.now())
                .unit("Load")
                .build());
        
        // Memory Usage
        metrics.add(MetricData.builder()
                .resource(nagiosResource)
                .metricName("MemoryUsage")
                .value(20.0 + random.nextDouble() * 60.0) // Random between 20-80%
                .timestamp(Instant.now())
                .unit("Percent")
                .build());
        
        // Disk Usage
        metrics.add(MetricData.builder()
                .resource(nagiosResource)
                .metricName("DiskUsage")
                .value(30.0 + random.nextDouble() * 50.0) // Random between 30-80%
                .timestamp(Instant.now())
                .unit("Percent")
                .build());
        
        return metrics;
    }
}
