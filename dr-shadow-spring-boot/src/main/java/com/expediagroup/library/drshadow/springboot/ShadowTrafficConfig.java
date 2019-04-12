/**
 * Copyright (C) ${license.git.copyrightYears} Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.library.drshadow.springboot;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Created by ctse on 6/28/2017.
 *
 * Shaddow traffic json configuration
 */
@Configuration
@EnableConfigurationProperties(ShadowTrafficConfig.class)
@ConfigurationProperties(prefix="drshadow")
public class ShadowTrafficConfig {
    
    /**
     * Represents the endpoint configuration for including shadow traffic
     */
    static class InclusionPattern {

        @JsonProperty("requestURI") private String requestURI;
        
        @JsonProperty("method") private String method;
        
        @JsonProperty("headerPatterns") private List<HeaderPattern> headerPattern;

        public String getRequestURI() { return requestURI; }

        public void setRequestURI(String requestURI) { this.requestURI = requestURI; }

        public String getMethod() {
            return method;
        }
        
        public void setMethod(String method) {
            this.method = method;
        }
        
        public List<HeaderPattern> getHeaderPattern() {
            return headerPattern;
        }
        
        public void setHeaderPattern(List<HeaderPattern> headerPattern) {
            this.headerPattern = headerPattern;
        }
        
    }
    
    static class HeaderPattern {
        
        @JsonProperty("headerKey") private String headerKey;
        @JsonProperty("headerValue") private String headerValue;
        
        public String getHeaderKey() {
            return headerKey;
        }
        
        public void setHeaderKey(String headerKey) {
            this.headerKey = headerKey;
        }
        
        public String getHeaderValue() {
            return headerValue;
        }
        
        public void setHeaderValue(String headerValue) {
            this.headerValue = headerValue;
        }
        
    }
    
    @JsonProperty("hosts") private List<String> hosts;
    
    @JsonProperty("inclusionPatterns") private List<InclusionPattern> inclusionPatterns;
    
    @JsonProperty("enabled") private boolean enabled = false;
    
    @JsonProperty("percentage") private int percentage;
    
    @JsonProperty("ssl") private boolean ssl = true;
    
    @JsonProperty("customHeaders") private Map<String, String> customHeaders;
    
    @JsonProperty("forwardHeaders") private List<String> forwardHeaders;
    
    @JsonProperty("invoker.corePoolSize") private int invokerCorePoolSize = ShadowTrafficConfiguration.SHADOW_TRAFFIC_INVOKER_DEFAULT_CORE_POOL_SIZE;
    
    @JsonProperty("http.corePoolSize") private int httpCorePoolSize = ShadowTrafficConfiguration.SHADOW_TRAFFIC_HTTP_DEFAULT_CORE_POOL_SIZE;
    
    @JsonProperty("http.connectionTimeoutMs") private int httpConnectionTimeoutMs = ShadowTrafficConfiguration.SHADOW_TRAFFIC_HTTP_CONNECTION_TIMEOUT;
    
    @JsonProperty("http.readTimeoutMs") private int httpReadTimeoutMs = ShadowTrafficConfiguration.SHADOW_TRAFFIC_HTTP_READ_TIMEOUT;

    @JsonProperty("filterOrder") private int filterOrder = ShadowTrafficConfiguration.DEFAULT_FILTER_ORDER;
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public List<String> getHosts() {
        return hosts;
    }
    
    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }
    
    public List<InclusionPattern> getInclusionPatterns() {
        return inclusionPatterns;
    }
    
    public void setInclusionPatterns(List<InclusionPattern> inclusionPatterns) {
        this.inclusionPatterns = inclusionPatterns;
    }
    
    public int getPercentage() {
        return percentage;
    }
    
    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
    
    public int getInvokerCorePoolSize() {
        return invokerCorePoolSize;
    }
    
    public void setInvokerCorePoolSize(int invokerCorePoolSize) {
        this.invokerCorePoolSize = invokerCorePoolSize;
    }
    
    public int getHttpCorePoolSize() {
        return httpCorePoolSize;
    }
    
    public void setHttpCorePoolSize(int httpCorePoolSize) {
        this.httpCorePoolSize = httpCorePoolSize;
    }
    
    public Map<String, String> getCustomHeaders() {
        return customHeaders;
    }
    
    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }
    
    public int getHttpConnectionTimeoutMs() {
        return httpConnectionTimeoutMs;
    }
    
    public void setHttpConnectionTimeoutMs(int httpConnectionTimeoutMs) {
        this.httpConnectionTimeoutMs = httpConnectionTimeoutMs;
    }
    
    public int getHttpReadTimeoutMs() {
        return httpReadTimeoutMs;
    }
    
    public void setHttpReadTimeoutMs(int httpReadTimeoutMs) {
        this.httpReadTimeoutMs = httpReadTimeoutMs;
    }
    
    public boolean isSsl() {
        return ssl;
    }
    
    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }
    
    public List<String> getForwardHeaders() {
        return forwardHeaders;
    }
    
    public void setForwardHeaders(List<String> forwardHeaders) {
        this.forwardHeaders = forwardHeaders;
    }

    public int getFilterOrder() {
        return filterOrder;
    }

    public void setFilterOrder(int filterOrder) {
        this.filterOrder = filterOrder;
    }
}
