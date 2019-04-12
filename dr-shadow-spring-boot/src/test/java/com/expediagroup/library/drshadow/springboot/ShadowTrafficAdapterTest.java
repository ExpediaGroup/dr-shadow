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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import static com.expediagroup.library.drshadow.springboot.ShadowTrafficAdapter.IS_SHADOW_TRAFFIC_KEY;
import static com.expediagroup.library.drshadow.springboot.ShadowTrafficAdapter.IS_SHADOW_TRAFFIC_VALUE;
import static com.expediagroup.library.drshadow.springboot.ShadowTrafficAdapter.SHADOW_TRAFFIC_FROM_KEY;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by ctse on 6/11/18.
 */

@RunWith(MockitoJUnitRunner.class)
public class ShadowTrafficAdapterTest {

    private ShadowTrafficAdapter shadowTrafficAdapter;

    @Mock
    private DrShadowHttpServletRequest shadowServletRequest;

    @Mock
    private HttpServletRequest originalServletRequest;

    @Mock
    private ShadowTrafficConfig shadowTrafficConfig;

    @Mock
    private ShadowTrafficConfigHelper shadowTrafficConfigHelper;

    @Mock
    private AsyncRestTemplate restTemplate;

    @Mock
    private Random random;

    private String machineName = "testMachineName";

    @Before
    public void setUp() {
        shadowTrafficAdapter = new ShadowTrafficAdapter(shadowTrafficConfigHelper, restTemplate, machineName);
        shadowTrafficAdapter.setRandom(random);
    }

    @Test
    public void testInvokeShadowTrafficWithNullShadowTrafficConfig_expectNoShadowTrafficPerformed() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(null);

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, originalServletRequest);

        verify(restTemplate, times(0)).exchange(any(), any(), any(), eq(String.class));
    }

    @Test
    public void testInvokeShadowTrafficWithConfigAsFalse_expectNoShadowTrafficPerformed() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.isEnabled()).thenReturn(false);

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, originalServletRequest);

        verify(restTemplate, times(0)).exchange(any(), any(), any(), eq(String.class));
    }

    @Test
    public void testInvokeShadowTrafficWithRandomValueGreaterThanPercentageConfigured_expectNoShadowTrafficPerformed() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.isEnabled()).thenReturn(true);
        when(shadowTrafficConfig.getPercentage()).thenReturn(50);
        when(random.nextInt(eq(100))).thenReturn(51);

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, originalServletRequest);

        verify(restTemplate, times(0)).exchange(any(), any(), any(), eq(String.class));
    }

    @Test
    public void testInvokeShadowTrafficWithNullDrShadowServletRequest_expectNoShadowTrafficPerformed() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.isEnabled()).thenReturn(true);
        when(shadowTrafficConfig.getPercentage()).thenReturn(50);
        when(random.nextInt(eq(100))).thenReturn(30);

        shadowTrafficAdapter.invokeShadowTraffic(null, originalServletRequest);

        verify(restTemplate, times(0)).exchange(any(), any(), any(), eq(String.class));
    }

    @Test
    public void testInvokeShadowTrafficWithNullOriginalRequest_expectNoShadowTrafficPerformed() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.isEnabled()).thenReturn(true);
        when(shadowTrafficConfig.getPercentage()).thenReturn(50);
        when(random.nextInt(eq(100))).thenReturn(30);

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, null);

        verify(restTemplate, times(0)).exchange(any(), any(), any(), eq(String.class));
    }

    @Test
    public void testInvokeShadowTrafficWithNullMethod_expectNoShadowTrafficPerformed() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.isEnabled()).thenReturn(true);
        when(shadowTrafficConfig.getPercentage()).thenReturn(50);
        when(random.nextInt(eq(100))).thenReturn(30);
        when(shadowServletRequest.getMethod()).thenReturn(null);

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, originalServletRequest);

        verify(restTemplate, times(0)).exchange(any(), any(), any(), eq(String.class));
        verify(shadowServletRequest, times(1)).getMethod();
    }

    @Test
    public void testInvokeShadowTraffic_withHttpsHost() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.isEnabled()).thenReturn(true);
        when(shadowTrafficConfig.getPercentage()).thenReturn(50);
        when(random.nextInt(eq(100))).thenReturn(30);
        when(shadowServletRequest.getMethod()).thenReturn("GET");
        when(shadowTrafficConfig.getCustomHeaders()).thenReturn(new HashMap<>());
        when(shadowTrafficConfig.getForwardHeaders()).thenReturn(new ArrayList<>());

        // Test with 1 http and 1 https
        when(shadowTrafficConfig.getHosts()).thenReturn(Arrays.asList("int-maui.karmalab.net"));
        when(shadowServletRequest.getRequestURI()).thenReturn("/hotels?param=abcd");

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, originalServletRequest);

        UriComponentsBuilder uriCompBuilder = UriComponentsBuilder.fromHttpUrl("https://int-maui.karmalab.net/hotels?param=abcd");
        URI expectedShadowUrl = uriCompBuilder.build().toUri();

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.add(IS_SHADOW_TRAFFIC_KEY, IS_SHADOW_TRAFFIC_VALUE);
        expectedHeaders.add(SHADOW_TRAFFIC_FROM_KEY, machineName);

        HttpEntity<String> httpEntity = new HttpEntity<>(null,
                expectedHeaders);

        verify(restTemplate, times(1)).exchange(eq(expectedShadowUrl), eq(HttpMethod.GET), eq(httpEntity), eq(String.class));
    }

    @Test
    public void testInvokeShadowTraffic_withHttpHost() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.isEnabled()).thenReturn(true);
        when(shadowTrafficConfig.getPercentage()).thenReturn(50);
        when(random.nextInt(eq(100))).thenReturn(30);
        when(shadowServletRequest.getMethod()).thenReturn("GET");
        when(shadowTrafficConfig.getCustomHeaders()).thenReturn(new HashMap<>());
        when(shadowTrafficConfig.getForwardHeaders()).thenReturn(new ArrayList<>());

        // Test with 1 http and 1 https
        when(shadowTrafficConfig.getHosts()).thenReturn(Arrays.asList("http://int-maui.karmalab.net"));
        when(shadowServletRequest.getRequestURI()).thenReturn("/hotels?param=abcd");

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, originalServletRequest);

        UriComponentsBuilder uriCompBuilder = UriComponentsBuilder.fromHttpUrl("http://int-maui.karmalab.net/hotels?param=abcd");
        URI expectedShadowUrl = uriCompBuilder.build().toUri();

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.add(IS_SHADOW_TRAFFIC_KEY, IS_SHADOW_TRAFFIC_VALUE);
        expectedHeaders.add(SHADOW_TRAFFIC_FROM_KEY, machineName);

        HttpEntity<String> httpEntity = new HttpEntity<>(null,
                expectedHeaders);

        verify(restTemplate, times(1)).exchange(eq(expectedShadowUrl), eq(HttpMethod.GET), eq(httpEntity), eq(String.class));
    }

    @Test
    public void testInvokeShadowTraffic_verifyOriginalUrlEncodedParamsDoesNotDoubleEncode() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.isEnabled()).thenReturn(true);
        when(shadowTrafficConfig.getPercentage()).thenReturn(50);
        when(random.nextInt(eq(100))).thenReturn(30);
        when(shadowServletRequest.getMethod()).thenReturn("GET");
        when(shadowTrafficConfig.getCustomHeaders()).thenReturn(new HashMap<>());
        when(shadowTrafficConfig.getForwardHeaders()).thenReturn(new ArrayList<>());

        // Test with 1 http and 1 https
        when(shadowTrafficConfig.getHosts()).thenReturn(Arrays.asList("http://int-maui.karmalab.net"));
        // make sure that the param here is url encoded
        when(shadowServletRequest.getRequestURI()).thenReturn("/hotels?param=ab%20cd");

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, originalServletRequest);

        UriComponentsBuilder uriCompBuilder = UriComponentsBuilder.fromHttpUrl("http://int-maui.karmalab.net/hotels?param=ab cd");
        URI expectedShadowUrl = uriCompBuilder.build().toUri();

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.add(IS_SHADOW_TRAFFIC_KEY, IS_SHADOW_TRAFFIC_VALUE);
        expectedHeaders.add(SHADOW_TRAFFIC_FROM_KEY, machineName);

        HttpEntity<String> httpEntity = new HttpEntity<>(null,
                expectedHeaders);

        verify(restTemplate, times(1)).exchange(eq(expectedShadowUrl), eq(HttpMethod.GET), eq(httpEntity), eq(String.class));
    }

    @Test
    public void testInvokeShadowTraffic_verifyWithPostBody() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.isEnabled()).thenReturn(true);
        when(shadowTrafficConfig.getPercentage()).thenReturn(50);
        when(random.nextInt(eq(100))).thenReturn(30);
        when(shadowServletRequest.getMethod()).thenReturn("GET");
        when(shadowTrafficConfig.getCustomHeaders()).thenReturn(new HashMap<>());
        when(shadowTrafficConfig.getForwardHeaders()).thenReturn(new ArrayList<>());
        when(shadowServletRequest.getBody()).thenReturn("testBody");

        // Test with 1 http and 1 https
        when(shadowTrafficConfig.getHosts()).thenReturn(Arrays.asList("http://int-maui.karmalab.net"));
        // make sure that the param here is url encoded
        when(shadowServletRequest.getRequestURI()).thenReturn("/hotels?param=ab%20cd");

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, originalServletRequest);

        UriComponentsBuilder uriCompBuilder = UriComponentsBuilder.fromHttpUrl("http://int-maui.karmalab.net/hotels?param=ab cd");
        URI expectedShadowUrl = uriCompBuilder.build().toUri();

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.add(IS_SHADOW_TRAFFIC_KEY, IS_SHADOW_TRAFFIC_VALUE);
        expectedHeaders.add(SHADOW_TRAFFIC_FROM_KEY, machineName);

        HttpEntity<String> httpEntity = new HttpEntity<>("testBody",
                expectedHeaders);

        verify(restTemplate, times(1)).exchange(eq(expectedShadowUrl), eq(HttpMethod.GET), eq(httpEntity), eq(String.class));
    }

    @Test
    public void testInvokeShadowTraffic_verifyWithCustomHeaders() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.isEnabled()).thenReturn(true);
        when(shadowTrafficConfig.getPercentage()).thenReturn(50);
        when(random.nextInt(eq(100))).thenReturn(30);
        when(shadowServletRequest.getMethod()).thenReturn("GET");

        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("carlson", "tse");

        when(shadowTrafficConfig.getCustomHeaders()).thenReturn(customHeaders);
        when(shadowTrafficConfig.getForwardHeaders()).thenReturn(new ArrayList<>());

        // Test with 1 http and 1 https
        when(shadowTrafficConfig.getHosts()).thenReturn(Arrays.asList("http://int-maui.karmalab.net"));
        // make sure that the param here is url encoded
        when(shadowServletRequest.getRequestURI()).thenReturn("/hotels?param=ab%20cd");

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, originalServletRequest);

        UriComponentsBuilder uriCompBuilder = UriComponentsBuilder.fromHttpUrl("http://int-maui.karmalab.net/hotels?param=ab cd");
        URI expectedShadowUrl = uriCompBuilder.build().toUri();

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.add(IS_SHADOW_TRAFFIC_KEY, IS_SHADOW_TRAFFIC_VALUE);
        expectedHeaders.add(SHADOW_TRAFFIC_FROM_KEY, machineName);
        expectedHeaders.add("shadow-traffic-carlson", "tse");

        HttpEntity<String> httpEntity = new HttpEntity<>(null,
                expectedHeaders);

        verify(restTemplate, times(1)).exchange(eq(expectedShadowUrl), eq(HttpMethod.GET), eq(httpEntity), eq(String.class));
    }

    @Test
    public void testInvokeShadowTraffic_verifyApplicationJsonUTF8Set() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.isEnabled()).thenReturn(true);
        when(shadowTrafficConfig.getPercentage()).thenReturn(50);
        when(random.nextInt(eq(100))).thenReturn(30);
        when(shadowServletRequest.getMethod()).thenReturn("GET");

        Vector headerNames = new Vector();
        headerNames.add("Content-Type");

        Enumeration<String> headers =  headerNames.elements();

        when(shadowServletRequest.getHeaderNames()).thenReturn(headers);
        when(shadowServletRequest.getHeader(eq("Content-Type"))).thenReturn(MediaType.APPLICATION_JSON_VALUE);

        when(shadowTrafficConfig.getCustomHeaders()).thenReturn(new HashMap<>());
        when(shadowTrafficConfig.getForwardHeaders()).thenReturn(Arrays.asList("Content-Type"));

        // Test with 1 http and 1 https
        when(shadowTrafficConfig.getHosts()).thenReturn(Arrays.asList("http://int-maui.karmalab.net"));
        // make sure that the param here is url encoded
        when(shadowServletRequest.getRequestURI()).thenReturn("/hotels?param=ab%20cd");

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, originalServletRequest);

        UriComponentsBuilder uriCompBuilder = UriComponentsBuilder.fromHttpUrl("http://int-maui.karmalab.net/hotels?param=ab cd");
        URI expectedShadowUrl = uriCompBuilder.build().toUri();

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.add(IS_SHADOW_TRAFFIC_KEY, IS_SHADOW_TRAFFIC_VALUE);
        expectedHeaders.add(SHADOW_TRAFFIC_FROM_KEY, machineName);
        expectedHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);

        HttpEntity<String> httpEntity = new HttpEntity<>(null,
                expectedHeaders);

        verify(restTemplate, times(1)).exchange(eq(expectedShadowUrl), eq(HttpMethod.GET), eq(httpEntity), eq(String.class));
    }

}