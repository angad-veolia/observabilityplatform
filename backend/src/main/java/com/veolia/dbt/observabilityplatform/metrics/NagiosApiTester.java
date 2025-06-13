package com.veolia.dbt.observabilityplatform.metrics;

import com.veolia.dbt.observabilityplatform.metrics.collection.NagiosApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Profile("apitest")
public class NagiosApiTester implements CommandLineRunner {

    private final RestTemplate restTemplate;
    private final NagiosApiConfig apiConfig;
    private static final Logger logger = LoggerFactory.getLogger(NagiosApiTester.class);
    
    public NagiosApiTester(RestTemplate restTemplate, NagiosApiConfig apiConfig) {
        this.restTemplate = restTemplate;
        this.apiConfig = apiConfig;
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Nagios API test");
        
        // Print configuration
        logger.info("API URL: {}", apiConfig.getApiUrl());
        logger.info("API Key configured: {}", (apiConfig.getApiKey() != null && !apiConfig.getApiKey().isEmpty()));
        
        // First, list all hosts
        listAllHosts();
        
        // Then list all services for a specific host
        String hostName = "ae1lrmulep1";
        listAllServicesForHost(hostName);
    }
    
    private void listAllHosts() {
        try {
            logger.info("\nListing all hosts in Nagios");
            
            // Build URL to get all hosts
            String baseUrl = apiConfig.getApiUrl().replace("/servicestatus", "/hoststatus");
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("apikey", apiConfig.getApiKey());
            
            String url = builder.toUriString();
            logger.info("API URL for hosts: {}", url);
            
            // Make request
            HttpEntity< String > entity = new HttpEntity<>(new HttpHeaders());
            ResponseEntity< String > response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            logger.info("Response status: {}", response.getStatusCode());
            logger.info("Response body: {}", response.getBody());
        } catch (Exception e) {
            logger.error("Error listing hosts: {}", e.getMessage(), e);
        }
    }
    
    private void listAllServicesForHost(String hostName) {
        try {
            logger.info("\nListing all services for host: {}", hostName);
            
            // Build URL to get all services for a host
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiConfig.getApiUrl())
                .queryParam("apikey", apiConfig.getApiKey())
                .queryParam("host_name", hostName);
            
            String url = builder.toUriString();
            logger.info("API URL for services: {}", url);
            
            // Make request
            HttpEntity< String > entity = new HttpEntity<>(new HttpHeaders());
            ResponseEntity< String > response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            logger.info("Response status: {}", response.getStatusCode());
            logger.info("Response body: {}", response.getBody());
        } catch (Exception e) {
            logger.error("Error listing services: {}", e.getMessage(), e);
        }
    }
    
    private void testDirectApiCall(String hostName, String serviceDescription) {
        try {
            logger.info("\nTesting direct API call for host: {} and service: {}", hostName, serviceDescription);
            
            // Manually encode the service description to avoid double encoding
            String encodedServiceDescription = serviceDescription.replace(" ", "%20");
            if (serviceDescription.contains("/")) {
                encodedServiceDescription = encodedServiceDescription.replace("/", "%2F");
            }
            
            // Build URL
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiConfig.getApiUrl())
                .queryParam("apikey", apiConfig.getApiKey())
                .queryParam("host_name", hostName);
            
            // Add service description without automatic encoding
            String url = builder.build().toUriString() + "&service_description=" + encodedServiceDescription;
            
            logger.info("Full API URL: {}", url);
            
            // Make request
            HttpEntity< String > entity = new HttpEntity<>(new HttpHeaders());
            ResponseEntity< String > response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            logger.info("Response status: {}", response.getStatusCode());
            logger.info("Response body: {}", response.getBody());
        } catch (Exception e) {
            logger.error("Error testing API: {}", e.getMessage(), e);
        }
    }
}
