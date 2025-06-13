package com.veolia.dbt.observabilityplatform.metrics.collection;

import com.veolia.dbt.observabilityplatform.metrics.model.MetricData;
import com.veolia.dbt.observabilityplatform.metrics.model.NagiosResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NagiosMetricCollector implements MetricCollector {
    
    private static final Logger logger = LoggerFactory.getLogger(NagiosMetricCollector.class);
    
    // Regular expression pattern to extract values from perfdata strings
    private static final Pattern PERFDATA_PATTERN = Pattern.compile("'([^']+)'=([0-9.]+)([^;]*);([0-9.]*);([0-9.]*);([0-9.]*);([0-9.]*);?");
    
    private final RestTemplate restTemplate;
    private final NagiosApiConfig apiConfig;
    
    // List of service descriptions we're interested in monitoring
    private final List< String > serviceDescriptions = List.of(
        "CPU Usage",
        "Memory Usage",
        "Disk Usage on /"
    );
    
    public NagiosMetricCollector(RestTemplate restTemplate, NagiosApiConfig apiConfig) {
        this.restTemplate = restTemplate;
        this.apiConfig = apiConfig;
    }
    
    /**
     * Collects metrics from Nagios for all configured hosts and services.
     * 
     * @return List of MetricData objects containing the collected metrics
     */
    @Override
    public List< MetricData > collectMetrics() {
        logger.info("Collecting Nagios metrics");
        List< MetricData > metrics = new ArrayList<>();
        
        // For each host in our mapping
        apiConfig.getHostToAppMapping().forEach((hostName, appName) -> {
            // For each service we want to monitor
            serviceDescriptions.forEach(serviceDescription -> {
                try {
                    // Get data from Nagios API
                    Map<String, Object> nagiosData = fetchNagiosData(hostName, serviceDescription);
                    
                    // Convert Nagios data to our MetricData format and add to results
                    metrics.add(createMetricFromNagiosData(hostName, appName, serviceDescription, nagiosData));
                } catch (Exception e) {
                    // Log error but continue with other metrics to prevent one failure from stopping all collection
                    logger.error("Error collecting {} metric for host {}: {}", 
                                serviceDescription, hostName, e.getMessage(), e);
                }
            });
        });
        
        logger.info("Collected {} Nagios metrics", metrics.size());
        return metrics;
    }
    
    /**
     * Fetches data from the Nagios API for a specific host and service.
     * 
     * @param hostName The host to get metrics for
     * @param serviceDescription The service/metric to retrieve
     * @return A map representing the Nagios API response
     */
  private Map<String, Object> fetchNagiosData(String hostName, String serviceDescription) {
    try {
        // Build the URL with API key as query parameter, but without the service_description
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiConfig.getApiUrl())
            .queryParam("apikey", apiConfig.getApiKey())
            .queryParam("host_name", hostName);
        
        // Get all services for this host
        String url = builder.toUriString();
        logger.debug("Calling Nagios API for all services: {}", url);
        
        // Make the API call
        HttpEntity< String > entity = new HttpEntity<>(new HttpHeaders());
        ResponseEntity< Map > response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            Map.class
        );
        
        Map<String, Object> body = response.getBody();
        if (body == null) {
            logger.warn("Received null response body");
            throw new IllegalArgumentException("Null response from Nagios API");
        }
        
        // Check if we have servicestatus in the response
        if (!body.containsKey("servicestatus")) {
            logger.warn("No 'servicestatus' key found in response");
            throw new IllegalArgumentException("No service status found in Nagios data");
        }
        
        // Get the list of services
        List<Map<String, Object>> serviceStatusList = (List<Map<String, Object>>) body.get("servicestatus");
        if (serviceStatusList.isEmpty()) {
            logger.warn("No services found for host {}", hostName);
            throw new IllegalArgumentException("No services found for host");
        }
        
        // Find the specific service we're looking for
        Map<String, Object> matchingService = null;
        for (Map<String, Object> service : serviceStatusList) {
            String desc = (String) service.get("service_description");
            if (serviceDescription.equals(desc)) {
                matchingService = service;
                break;
            }
        }
        
        if (matchingService == null) {
            logger.warn("Service '{}' not found for host {}", serviceDescription, hostName);
            throw new IllegalArgumentException("Service not found");
        }
        
        // Create a new response with just the matching service
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> matchingServiceList = new ArrayList<>();
        matchingServiceList.add(matchingService);
        result.put("servicestatus", matchingServiceList);
        result.put("recordcount", 1);
        
        return result;
    } catch (Exception e) {
        logger.error("Error fetching data from Nagios API for {} - {}: {}", 
                   hostName, serviceDescription, e.getMessage(), e);
        throw e;
    }
}



    
    /**
     * Converts Nagios API data into our standardized MetricData format.
     * 
     * @param hostName The host name
     * @param appName The application name this host belongs to
     * @param serviceDescription The service/metric name
     * @param nagiosData The Nagios API response data
     * @return A MetricData object representing the metric
     */
    private MetricData createMetricFromNagiosData(String hostName, String appName, 
                                            String serviceDescription, 
                                            Map<String, Object> nagiosData) {
    // Extract the service status record
    List<Map<String, Object>> serviceStatusList = (List<Map<String, Object>>) nagiosData.get("servicestatus");
    Map<String, Object> serviceStatus = serviceStatusList.get(0);
    
    // Log key fields in a clean format
    String perfdata = (String) serviceStatus.get("perfdata");
    String output = (String) serviceStatus.get("output");
    String currentState = (String) serviceStatus.get("current_state");
    
    logger.info("-----------------------------------------------------");
    logger.info("Processing metric: {} for host: {}", serviceDescription, hostName);
    logger.info("Output: {}", output);
    logger.info("Perfdata: {}", perfdata);
    logger.info("Current state: {}", currentState);
    
    // Create Nagios resource
    NagiosResource resource = NagiosResource.builder()
            .id(hostName)
            .name(hostName)
            .resourceType("Server")
            .hostGroup(appName)
            .serviceGroup("Monitoring")
            .hostAddress((String) serviceStatus.get("host_address"))
            .build();
    
    // Parse the timestamp
    String statusUpdateTime = (String) serviceStatus.get("status_update_time");
    Instant timestamp = LocalDateTime.parse(statusUpdateTime, 
                                          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant();
    
    // Extract value and thresholds based on service type
    double value = 0.0;
    String unit = "%";
    Double warningThreshold = null;
    Double criticalThreshold = null;
    
    // CPU Usage
    if (serviceDescription.equals("CPU Usage")) {
        // Extract from perfdata: 'percent'=9.40%;80;95;
        Matcher percentMatcher = Pattern.compile("'percent'=([0-9.]+)%;([0-9.]*);([0-9.]*)").matcher(perfdata);
        if (percentMatcher.find()) {
            value = Double.parseDouble(percentMatcher.group(1));
            
            if (!percentMatcher.group(2).isEmpty()) {
                warningThreshold = Double.parseDouble(percentMatcher.group(2));
            }
            
            if (!percentMatcher.group(3).isEmpty()) {
                criticalThreshold = Double.parseDouble(percentMatcher.group(3));
            }
            
            logger.info("Extracted CPU value: {}%, warning: {}%, critical: {}%", 
                      value, warningThreshold, criticalThreshold);
        } else {
            // Try to extract from output
            Matcher outputMatcher = Pattern.compile("was\\s+([0-9.]+)\\s+%").matcher(output);
            if (outputMatcher.find()) {
                value = Double.parseDouble(outputMatcher.group(1));
                logger.info("Extracted CPU value from output: {}%", value);
            }
        }
    }
    // Memory Usage
    else if (serviceDescription.equals("Memory Usage")) {
        // Extract from perfdata: 'percent'=30.80%;95;98;
        Matcher percentMatcher = Pattern.compile("'percent'=([0-9.]+)%;([0-9.]*);([0-9.]*)").matcher(perfdata);
        if (percentMatcher.find()) {
            value = Double.parseDouble(percentMatcher.group(1));
            
            if (!percentMatcher.group(2).isEmpty()) {
                warningThreshold = Double.parseDouble(percentMatcher.group(2));
            }
            
            if (!percentMatcher.group(3).isEmpty()) {
                criticalThreshold = Double.parseDouble(percentMatcher.group(3));
            }
            
            logger.info("Extracted Memory value: {}%, warning: {}%, critical: {}%", 
                      value, warningThreshold, criticalThreshold);
        } else {
            // Try to extract from output
            Matcher outputMatcher = Pattern.compile("was\\s+([0-9.]+)\\s+%").matcher(output);
            if (outputMatcher.find()) {
                value = Double.parseDouble(outputMatcher.group(1));
                logger.info("Extracted Memory value from output: {}%", value);
            }
        }
    }
    // Disk Usage
    else if (serviceDescription.equals("Disk Usage on /")) {
        // Try to extract from output first
        Matcher outputMatcher = Pattern.compile("was\\s+([0-9.]+)\\s+%").matcher(output);
        if (outputMatcher.find()) {
            value = Double.parseDouble(outputMatcher.group(1));
            logger.info("Extracted Disk value from output: {}%", value);
        } else {
            // Calculate from used and total in perfdata
            Matcher usedMatcher = Pattern.compile("'used'=([0-9.]+)([^;]*)").matcher(perfdata);
            Matcher totalMatcher = Pattern.compile("'total'=([0-9.]+)").matcher(perfdata);
            
            if (usedMatcher.find() && totalMatcher.find()) {
                double used = Double.parseDouble(usedMatcher.group(1));
                double total = Double.parseDouble(totalMatcher.group(1));
                
                if (total > 0) {
                    value = (used / total) * 100;
                    logger.info("Calculated Disk value: {}% (used: {}, total: {})", 
                              value, used, total);
                }
            }
        }
        
        // Extract thresholds from check_command
        String checkCommand = (String) serviceStatus.get("check_command");
        if (checkCommand != null) {
            Matcher warnMatcher = Pattern.compile("-w\\s+'?([0-9.]+)'?").matcher(checkCommand);
            if (warnMatcher.find()) {
                warningThreshold = Double.parseDouble(warnMatcher.group(1));
            }
            
            Matcher critMatcher = Pattern.compile("-c\\s+'?([0-9.]+)'?").matcher(checkCommand);
            if (critMatcher.find()) {
                criticalThreshold = Double.parseDouble(critMatcher.group(1));
            }
            
            logger.info("Extracted Disk thresholds from command: warning: {}%, critical: {}%", 
                      warningThreshold, criticalThreshold);
        }
    }
    
    // Set default thresholds if not found
    if (warningThreshold == null) {
        warningThreshold = serviceDescription.equals("Disk Usage on /") ? 70.0 : 80.0;
        logger.info("Using default warning threshold: {}%", warningThreshold);
    }
    
    if (criticalThreshold == null) {
        criticalThreshold = serviceDescription.equals("Disk Usage on /") ? 90.0 : 95.0;
        logger.info("Using default critical threshold: {}%", criticalThreshold);
    }
    
    // Create tags
    Map<String, String> tags = new HashMap<>();
    tags.put("host", hostName);
    tags.put("resourceId", hostName);
    tags.put("resourceName", hostName);
    tags.put("resourceType", "Server");
    tags.put("monitoringSystem", "Nagios");
    tags.put("application", appName);
    tags.put("unit", unit);
    tags.put("state", currentState);
    tags.put("warning_threshold", warningThreshold.toString());
    tags.put("critical_threshold", criticalThreshold.toString());

    // Create and return the MetricData object
    MetricData metricData = MetricData.builder()
            .metricName(serviceDescription)
            .value(value)
            .timestamp(timestamp)
            .tags(tags)
            .build();

    logger.info("Created metric: {} = {} {} (state: {})", 
            serviceDescription, value, unit, currentState);
    logger.info("-----------------------------------------------------");

    return metricData;

}
    /**
     * Returns the name of this collector.
     * 
     * @return The collector name
     */
    @Override
    public String getCollectorName() {
        return "NagiosCollector";
    }
}
