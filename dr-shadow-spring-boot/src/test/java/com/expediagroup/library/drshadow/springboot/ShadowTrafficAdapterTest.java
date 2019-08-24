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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
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
import java.util.function.Consumer;

import static com.expediagroup.library.drshadow.springboot.ShadowTrafficAdapter.IS_SHADOW_TRAFFIC_KEY;
import static com.expediagroup.library.drshadow.springboot.ShadowTrafficAdapter.IS_SHADOW_TRAFFIC_VALUE;
import static com.expediagroup.library.drshadow.springboot.ShadowTrafficAdapter.SHADOW_TRAFFIC_FROM_KEY;
import static org.mockito.ArgumentMatchers.isNull;
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
    private WebClient webClient;

    @Mock
    private Random random;

    private String machineName = "testMachineName";

    private WebClient.RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
    private WebClient.RequestBodySpec requestBodySpec = Mockito.mock(WebClient.RequestBodySpec.class);
    private WebClient.RequestHeadersSpec requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
    private WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

    @Before
    public void setUp() {
        shadowTrafficAdapter = new ShadowTrafficAdapter(shadowTrafficConfigHelper, machineName);
        shadowTrafficAdapter.setRandom(random);
        shadowTrafficAdapter.setWebClient(webClient);

        when(webClient.method(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.syncBody(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    public void testInvokeShadowTrafficWithNullShadowTrafficConfig_expectNoShadowTrafficPerformed() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(null);

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, originalServletRequest);

        verify(webClient, times(0)).method(any());
    }

    @Test
    public void testInvokeShadowTrafficWithRandomValueGreaterThanPercentageConfigured_expectNoShadowTrafficPerformed() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.getPercentage()).thenReturn(50);
        when(random.nextInt(eq(100))).thenReturn(51);

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, originalServletRequest);

        verify(webClient, times(0)).method(any());
    }

    @Test
    public void testInvokeShadowTrafficWithNullDrShadowServletRequest_expectNoShadowTrafficPerformed() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.getPercentage()).thenReturn(50);
        when(random.nextInt(eq(100))).thenReturn(30);

        shadowTrafficAdapter.invokeShadowTraffic(null, originalServletRequest);

        verify(webClient, times(0)).method(any());
    }

    @Test
    public void testInvokeShadowTrafficWithNullOriginalRequest_expectNoShadowTrafficPerformed() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.getPercentage()).thenReturn(50);
        when(random.nextInt(eq(100))).thenReturn(30);

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, null);

        verify(webClient, times(0)).method(any());
    }

    @Test
    public void testInvokeShadowTrafficWithNullMethod_expectNoShadowTrafficPerformed() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
        when(shadowTrafficConfig.getPercentage()).thenReturn(50);
        when(random.nextInt(eq(100))).thenReturn(30);
        when(shadowServletRequest.getMethod()).thenReturn(null);

        shadowTrafficAdapter.invokeShadowTraffic(shadowServletRequest, originalServletRequest);

        verify(webClient, times(0)).method(any());
        verify(shadowServletRequest, times(1)).getMethod();
    }

    @Test
    public void testInvokeShadowTraffic_withHttpsHost() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
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

        verify(webClient, times(1)).method(eq(HttpMethod.GET));
        verify(requestBodyUriSpec, times(1)).uri(eq(expectedShadowUrl));
        verify(requestBodySpec, times(1)).headers(any(Consumer.class));
        verify(requestBodySpec, times(1)).syncBody(isNull());
    }

    @Test
    public void testInvokeShadowTraffic_withHttpHost() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
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

        verify(webClient, times(1)).method(eq(HttpMethod.GET));
        verify(requestBodyUriSpec, times(1)).uri(eq(expectedShadowUrl));
        verify(requestBodySpec, times(1)).headers(any(Consumer.class));
        verify(requestBodySpec, times(1)).syncBody(isNull());
    }

    @Test
    public void testInvokeShadowTraffic_verifyOriginalUrlEncodedParamsDoesNotDoubleEncode() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
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

        verify(webClient, times(1)).method(eq(HttpMethod.GET));
        verify(requestBodyUriSpec, times(1)).uri(eq(expectedShadowUrl));
        verify(requestBodySpec, times(1)).headers(any(Consumer.class));
        verify(requestBodySpec, times(1)).syncBody(isNull());
    }

    @Test
    public void testInvokeShadowTraffic_verifyWithPostBody() {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(shadowTrafficConfig);
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

        verify(webClient, times(1)).method(eq(HttpMethod.GET));
        verify(requestBodyUriSpec, times(1)).uri(eq(expectedShadowUrl));
        verify(requestBodySpec, times(1)).headers(any(Consumer.class));
        verify(requestBodySpec, times(1)).syncBody(eq("testBody"));
    }

    @Test
    public void testHeaderCreationWithCustomHeaders() {
        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("carlson", "tse");

        HttpHeaders httpHeaders = shadowTrafficAdapter.createHeaders(shadowServletRequest, customHeaders, new ArrayList<>());

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.add(IS_SHADOW_TRAFFIC_KEY, IS_SHADOW_TRAFFIC_VALUE);
        expectedHeaders.add(SHADOW_TRAFFIC_FROM_KEY, machineName);
        expectedHeaders.add("shadow-traffic-carlson", "tse");

        Assert.assertEquals(expectedHeaders, httpHeaders);
    }

    @Test
    public void testHeaderCreationWithApplicationJsonUTF8Set() {
        Vector headerNames = new Vector();
        headerNames.add("Content-Type");

        Enumeration<String> headers =  headerNames.elements();

        when(shadowServletRequest.getHeaderNames()).thenReturn(headers);
        when(shadowServletRequest.getHeader(eq("Content-Type"))).thenReturn(MediaType.APPLICATION_JSON_VALUE);

        HttpHeaders httpHeaders = shadowTrafficAdapter.createHeaders(shadowServletRequest, new HashMap<>(), Arrays.asList("Content-Type"));

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.add(IS_SHADOW_TRAFFIC_KEY, IS_SHADOW_TRAFFIC_VALUE);
        expectedHeaders.add(SHADOW_TRAFFIC_FROM_KEY, machineName);
        expectedHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);

        Assert.assertEquals(expectedHeaders, httpHeaders);
    }
}