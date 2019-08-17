/**
 * Copyright (C) 2019 Expedia, Inc.
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

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * ShadowTrafficAdapter invokes the shadow traffic to the configured destination host(s) w/ the exact same incoming request.
 * A header is explicitly added to shadow traffic so we don't end up in infinite loops and all custom headaeres have a prefix to prevent
 * collision with potentially existing headers.
 * Created by ctse on 6/28/2017.
 */
public class ShadowTrafficAdapter {
    
    protected static final String IS_SHADOW_TRAFFIC_KEY = "is-shadow-traffic";
    protected static final String IS_SHADOW_TRAFFIC_VALUE = "true";
    protected static final String SHADOW_TRAFFIC_FROM_KEY = "shadow-traffic-from";
    protected static final String SHADOW_TRAFFIC_PREFIX_CUSTOM_KEY = "shadow-traffic-";
    protected static final String HTTP_PREFIX = "http://";
    protected static final String HTTPS_PREFIX = "https://";
    private static Logger LOGGER = LoggerFactory.getLogger(ShadowTrafficAdapter.class);

    //TODO: AsyncRestTemplate is deprecated for WebFlux in Spring 5
    private final AsyncRestTemplate restTemplate;
    private final String machineName;
    private final ShadowTrafficConfigHelper shadowTrafficConfigHelper;
    private Random random;

    /**
     *
     * @param shadowTrafficConfigHelper Configuration helper
     * @param restTemplate Rest Template to make the shadow requests
     * @param machineName Machine name used for adding a header on where the shadow traffic came from
     */
    public ShadowTrafficAdapter(ShadowTrafficConfigHelper shadowTrafficConfigHelper, AsyncRestTemplate restTemplate, String machineName) {
        this.shadowTrafficConfigHelper = shadowTrafficConfigHelper;
        this.restTemplate = restTemplate;
        this.machineName = machineName;
        this.random = new Random();
    }

    /**
     * Only used for unit testing to manipulate the randomization
     * @param random Random instance for generating random number for tests
     */
    protected void setRandom(Random random) {
        this.random = random;
    }
    
    /**
     * Add custom headers to the shadow traffic
     * 
     * @param request - original request
     * @param customHeaders - configured custom headers to send off to shadow traffic
     * @param forwardOnlyHeaders - headers that need to be forwarded in shadow request
     * @return
     */
    private HttpHeaders createHeaders(DrShadowHttpServletRequest request, Map<String, String> customHeaders, List<String> forwardOnlyHeaders) {
        HttpHeaders headers = getHeaders(request, forwardOnlyHeaders);
        headers.add(IS_SHADOW_TRAFFIC_KEY, IS_SHADOW_TRAFFIC_VALUE);
        headers.add(SHADOW_TRAFFIC_FROM_KEY, machineName);

        addContentTypeUtf8CharsetIfNotSet(headers);
        
        if (MapUtils.isNotEmpty(customHeaders)) {
            for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
                headers.add(SHADOW_TRAFFIC_PREFIX_CUSTOM_KEY + entry.getKey(), entry.getValue());
            }
        }
        
        return headers;
    }
    
    private HttpHeaders getHeaders(DrShadowHttpServletRequest request, List<String> forwardOnlyHeaders) {
        HttpHeaders headers = new HttpHeaders();

        if (CollectionUtils.isEmpty(forwardOnlyHeaders)) {
            // if no specific headers to be forwarded, we can skip processing further
            return headers;
        }

        // match the request headers with configured headers that need to be forwarded
        Enumeration<?> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            if (CollectionUtils.isNotEmpty(forwardOnlyHeaders) &&
                    forwardOnlyHeaders.stream().anyMatch(key::equalsIgnoreCase)) {
                headers.add(key, value);
            }
        }

        return headers;
    }

    private void addContentTypeUtf8CharsetIfNotSet(HttpHeaders headers) {
        if (MediaType.APPLICATION_JSON.equals(headers.getContentType())) {
            headers.remove(HttpHeaders.CONTENT_TYPE);
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        }
    }

    private void processFuturesByIgnoringThem(List<ListenableFuture<ResponseEntity<String>>> futures) {
        if (CollectionUtils.isEmpty(futures)) {
            // if no futures were generated, return
            return;
        }

        for (ListenableFuture<ResponseEntity<String>> future : futures) {
            // don't really care what we got back from the shadow traffic
            try {
                future.get();
            } catch (ExecutionException executionException) {
                LOGGER.debug("ExecutionException() ", executionException);
            } catch (Exception ex) {
                // This is to prevent flooding of the shadow traffic logs in the service
                LOGGER.debug("Shadow traffic call failed.", ex);
            }
        }
    }
    
    /**
     * Asynchronously execute the shadow traffic of the exact same incoming HTTP request. For now, we don't know how to deal w/ body POSTs. This should only be
     * used for GETs w/ query parameters.
     *
     * Note that this library will not throw ANY exceptions. So it is safe to assume it is resilient.
     *
     * Note that every call to ShadowTrafficRequestWrapper must be a copy of the original request or there is a race condition that causes the values to
     * disappear when the original request is completed.
     *
     * @param drShadowHttpServletRequest - Initial incoming request configured to invoke shadow traffic
     * @param originalHttpServletRequest  - Original http servlet request
     */
    @Async("shadowTrafficTaskExecutor")
    public void invokeShadowTraffic(DrShadowHttpServletRequest drShadowHttpServletRequest, HttpServletRequest originalHttpServletRequest) {
        
    	boolean isFormEncodedRequest = false;
        
        try {

            ShadowTrafficConfig shadowTrafficConfig = shadowTrafficConfigHelper.getConfig();
            List<ListenableFuture<ResponseEntity<String>>> futures = new ArrayList<>();

            // Check whether shadow traffic is enabled and also generate a random number to see if it falls within percentage
            if (shadowTrafficConfig != null && (random.nextInt(100) + 1) <= shadowTrafficConfig.getPercentage()) {

                if (drShadowHttpServletRequest == null) {
                    LOGGER.error("DrShadowHttpServletRequest is null. Shadow traffic will not be invoked.");
                    return;
                }
                if (originalHttpServletRequest == null) {
                    LOGGER.error("Original HttpServletRequest is null. Shadow traffic will not be invoked.");
                    return;
                }

                HttpMethod drShadowRequestHttpMethod = HttpMethod.resolve(drShadowHttpServletRequest.getMethod());
                if (drShadowRequestHttpMethod == null) {
                    LOGGER.error("Shadow traffic was configured to be ON skipping shadow since the httpMethod for drShadowRequest is invalid. The original request HttpMethod is: {} and URI is: {}",
                            originalHttpServletRequest.getMethod(),
                            originalHttpServletRequest.getRequestURI());
                    return;
                }

                List<String> hosts = shadowTrafficConfig.getHosts();

                if (CollectionUtils.isEmpty(hosts)) {
                    LOGGER.error("Shadow traffic was configured to be ON but no hosts specified, so no shadow requests are sent.");
                    return;
                }

                for (String host : hosts) {

                    if (StringUtils.isBlank(host)) {
                        LOGGER.warn("Shadow traffic is enabled but no host specified!!!");
                        continue;
                    }

                    StringBuilder sb = new StringBuilder();

                    if (host.contains(HTTP_PREFIX)) {
                        sb.append(host).append(drShadowHttpServletRequest.getRequestURI());
                    } else {
                        sb.append(HTTPS_PREFIX).append(host).append(drShadowHttpServletRequest.getRequestURI());
                    }

                    if (drShadowHttpServletRequest instanceof FormUrlEncodedHttpServletRequest) {
                        isFormEncodedRequest = true;
                    }

                    if (StringUtils.isNotBlank(drShadowHttpServletRequest.getQueryString()) && !isFormEncodedRequest) { // we don't need to send the query param in URL for form
                        // encodes as it is posted as body
                        sb.append("?").append(drShadowHttpServletRequest.getQueryString());
                    }

                    // Decode the URL since we encode it again when constructing the URI again
                    String urlDecodedStr = URLDecoder.decode(sb.toString(), CharEncoding.UTF_8);

                    // Building the URL here will URL encode the query parameters
                    UriComponentsBuilder uriCompBuilder = UriComponentsBuilder.fromHttpUrl(urlDecodedStr);
                    URI shadowUrl = uriCompBuilder.build().toUri();

                    String postBody = drShadowHttpServletRequest.getBody();

                    HttpEntity<String> httpEntity = new HttpEntity<>(postBody,
                            createHeaders(drShadowHttpServletRequest, shadowTrafficConfig.getCustomHeaders(), shadowTrafficConfig.getForwardHeaders()));

                    LOGGER.info("Forwarding shadow traffic url: {} to host: {}", shadowUrl, host);
                    // we never care about the response of shadow traffic
                    ListenableFuture<ResponseEntity<String>> future = restTemplate.exchange(shadowUrl, drShadowRequestHttpMethod, httpEntity, String.class);
                    futures.add(future);
                }

                // clean up by ignoring the futures
                processFuturesByIgnoringThem(futures);
            }
        } catch (Exception ex) {
            LOGGER.warn("Invoking shadow traffic failed", ex);
        }

    }
}
