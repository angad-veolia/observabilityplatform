package com.veolia.dbt.observabilityplatform.metrics.collection;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "nagios")
public class NagiosApiConfig {
    private String apiUrl;
    private String apiKey;
    private Map<String, String> hostToAppMapping = new HashMap<>();

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Map<String, String> getHostToAppMapping() {
        return hostToAppMapping;
    }

    public void setHostToAppMapping(Map<String, String> hostToAppMapping) {
        this.hostToAppMapping = hostToAppMapping;
    }
}
