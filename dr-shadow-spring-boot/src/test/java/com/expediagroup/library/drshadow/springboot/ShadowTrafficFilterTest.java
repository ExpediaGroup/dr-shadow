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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by ctse on 7/5/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShadowTrafficFilterTest {

    @Mock
    private ShadowTrafficAdapter adapter;

    @Mock
    private DrShadowHttpServletRequest request;

    @Mock
    private ShadowTrafficConfig config;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private ShadowTrafficConfigHelper shadowTrafficConfigHelper;

    private ShadowTrafficFilter filter;

    @Before
    public void setUp() {
        filter = new ShadowTrafficFilter(shadowTrafficConfigHelper, adapter);
    }

    @Test
    public void shouldNotFilter_isAlreadyShadowTraffic_expectNoFilter() throws Exception {

        boolean result = filter.shouldNotFilter(request);

        assertTrue(result);
    }

    @Test
    public void shouldNotFilter_hasShadowTrafficConfigDisabled_expectNoFilter() throws Exception {

        when(shadowTrafficConfigHelper.getConfig()).thenReturn(config);
        when(config.isEnabled()).thenReturn(false);

        boolean result = filter.shouldNotFilter(request);

        assertTrue(result);
    }

    @Test
    public void shouldNotFilter_inclusionPatternListIsEmpty_expectNoFilter() throws Exception {
        when(shadowTrafficConfigHelper.getConfig()).thenReturn(config);
        when(config.isEnabled()).thenReturn(true);

        when(request.getRequestURI()).thenReturn("/hotels");
        List<ShadowTrafficConfig.InclusionPattern> inclusionPatterns = new ArrayList<>();

        when(config.getInclusionPatterns()).thenReturn(inclusionPatterns);

        boolean result = filter.shouldNotFilter(request);

        assertTrue(result);
    }

    @Test
    public void shouldNotFilter_oneInclusionPatternWithnNeitherPathNorMethodMatching_expectNoFilter() throws Exception {
        when(shadowTrafficConfigHelper.getConfig()).thenReturn(config);
        when(config.isEnabled()).thenReturn(true);

        when(request.getRequestURI()).thenReturn("/hotels");
        List<ShadowTrafficConfig.InclusionPattern> inclusionPatterns = new ArrayList<>();
        ShadowTrafficConfig.InclusionPattern pattern1 = new ShadowTrafficConfig.InclusionPattern();
        pattern1.setRequestURI("^/asdfasdfs$");
        pattern1.setMethod("GET");
        inclusionPatterns.add(pattern1);

        when(config.getInclusionPatterns()).thenReturn(inclusionPatterns);

        boolean result = filter.shouldNotFilter(request);

        assertTrue(result);
    }

    @Test
    public void shouldNotFilter_oneInclusionPatternMatchesPathButNotMethod_expectNoFilter() throws Exception {
        when(shadowTrafficConfigHelper.getConfig()).thenReturn(config);
        when(config.isEnabled()).thenReturn(true);

        when(request.getRequestURI()).thenReturn("/hotels");
        when(request.getMethod()).thenReturn("POST");
        List<ShadowTrafficConfig.InclusionPattern> inclusionPatterns = new ArrayList<>();
        ShadowTrafficConfig.InclusionPattern pattern1 = new ShadowTrafficConfig.InclusionPattern();
        pattern1.setRequestURI("^/hotels$");
        pattern1.setMethod("GET");
        inclusionPatterns.add(pattern1);

        when(config.getInclusionPatterns()).thenReturn(inclusionPatterns);

        boolean result = filter.shouldNotFilter(request);

        assertTrue(result);
    }

    @Test
    public void shouldNotFilter_oneInclusionPatternMatchesPathAndMethod_expectFilter() throws Exception {
        when(shadowTrafficConfigHelper.getConfig()).thenReturn(config);
        when(config.isEnabled()).thenReturn(true);

        when(request.getRequestURI()).thenReturn("/hotels");
        when(request.getMethod()).thenReturn("GET");
        List<ShadowTrafficConfig.InclusionPattern> inclusionPatterns = new ArrayList<>();
        ShadowTrafficConfig.InclusionPattern pattern1 = new ShadowTrafficConfig.InclusionPattern();
        pattern1.setRequestURI("^/hotels$");
        pattern1.setMethod("GET");
        inclusionPatterns.add(pattern1);

        when(config.getInclusionPatterns()).thenReturn(inclusionPatterns);

        boolean result = filter.shouldNotFilter(request);

        assertFalse(result);
    }

    @Test
    public void shouldNotFilter_oneInclusionPatternMatchesPathAndMethodWithLowerCase_expectFilter() throws Exception {
        when(shadowTrafficConfigHelper.getConfig()).thenReturn(config);
        when(config.isEnabled()).thenReturn(true);

        when(request.getRequestURI()).thenReturn("/hotels");
        when(request.getMethod()).thenReturn("GET");
        List<ShadowTrafficConfig.InclusionPattern> inclusionPatterns = new ArrayList<>();
        ShadowTrafficConfig.InclusionPattern pattern1 = new ShadowTrafficConfig.InclusionPattern();
        pattern1.setRequestURI("^/hotels$");
        pattern1.setMethod("get");
        inclusionPatterns.add(pattern1);

        when(config.getInclusionPatterns()).thenReturn(inclusionPatterns);

        boolean result = filter.shouldNotFilter(request);

        assertFalse(result);
    }

    @Test
    public void shouldNotFilter_oneInclusionPatternMatchesPathAndAllMethods_expectFilter() throws Exception {
        when(shadowTrafficConfigHelper.getConfig()).thenReturn(config);
        when(config.isEnabled()).thenReturn(true);

        when(request.getRequestURI()).thenReturn("/hotels");
        List<ShadowTrafficConfig.InclusionPattern> inclusionPatterns = new ArrayList<>();
        ShadowTrafficConfig.InclusionPattern pattern1 = new ShadowTrafficConfig.InclusionPattern();
        pattern1.setRequestURI("^/hotels$");
        pattern1.setMethod("*");
        inclusionPatterns.add(pattern1);

        when(config.getInclusionPatterns()).thenReturn(inclusionPatterns);

        boolean result = filter.shouldNotFilter(request);

        assertFalse(result);
    }

    @Test
    public void shouldNotFilter_oneInvalidInclusionPatternMatch_expectNoFilter() throws Exception {
        when(shadowTrafficConfigHelper.getConfig()).thenReturn(config);
        when(config.isEnabled()).thenReturn(true);

        when(request.getRequestURI()).thenReturn("/hotels");
        List<ShadowTrafficConfig.InclusionPattern> inclusionPatterns = new ArrayList<>();
        ShadowTrafficConfig.InclusionPattern pattern1 = new ShadowTrafficConfig.InclusionPattern();
        pattern1.setRequestURI("[");
        pattern1.setMethod("*");
        inclusionPatterns.add(pattern1);

        when(config.getInclusionPatterns()).thenReturn(inclusionPatterns);

        boolean result = filter.shouldNotFilter(request);

        assertTrue(result);
    }

    @Test
    public void shouldNotFilter_oneNullInclusionPatternMatch_expectNoFilter() throws Exception {
        when(shadowTrafficConfigHelper.getConfig()).thenReturn(config);
        when(config.isEnabled()).thenReturn(true);

        when(request.getRequestURI()).thenReturn("/hotels");
        List<ShadowTrafficConfig.InclusionPattern> inclusionPatterns = new ArrayList<>();
        inclusionPatterns.add(null);

        when(config.getInclusionPatterns()).thenReturn(inclusionPatterns);

        boolean result = filter.shouldNotFilter(request);

        assertTrue(result);
    }

    @Test
    public void shouldNotFilter_multipleInclusionPatternMatches_withLastOneMatching_expectFilter() throws Exception {
        when(shadowTrafficConfigHelper.getConfig()).thenReturn(config);
        when(config.isEnabled()).thenReturn(true);

        when(request.getRequestURI()).thenReturn("/hotel-details");
        when(request.getMethod()).thenReturn("GET");
        List<ShadowTrafficConfig.InclusionPattern> inclusionPatterns = new ArrayList<>();
        ShadowTrafficConfig.InclusionPattern pattern1 = new ShadowTrafficConfig.InclusionPattern();
        pattern1.setRequestURI("^/hotels$");
        pattern1.setMethod("get");
        inclusionPatterns.add(pattern1);

        ShadowTrafficConfig.InclusionPattern pattern2 = new ShadowTrafficConfig.InclusionPattern();
        pattern2.setRequestURI("^/hotel-details$");
        pattern2.setMethod("get");
        inclusionPatterns.add(pattern2);

        when(config.getInclusionPatterns()).thenReturn(inclusionPatterns);

        boolean result = filter.shouldNotFilter(request);

        assertFalse(result);
    }

    @Test
    public void shouldNotFilter_multipleInclusionPatternMatches_withFirstOneMatching_expectFilter() throws Exception {
        when(shadowTrafficConfigHelper.getConfig()).thenReturn(config);
        when(config.isEnabled()).thenReturn(true);

        when(request.getRequestURI()).thenReturn("/hotels");
        when(request.getMethod()).thenReturn("GET");
        List<ShadowTrafficConfig.InclusionPattern> inclusionPatterns = new ArrayList<>();
        ShadowTrafficConfig.InclusionPattern pattern1 = new ShadowTrafficConfig.InclusionPattern();
        pattern1.setRequestURI("^/hotels$");
        pattern1.setMethod("get");
        inclusionPatterns.add(pattern1);

        ShadowTrafficConfig.InclusionPattern pattern2 = new ShadowTrafficConfig.InclusionPattern();
        pattern2.setRequestURI("^/hotel-details$");
        pattern2.setMethod("get");
        inclusionPatterns.add(pattern2);

        when(config.getInclusionPatterns()).thenReturn(inclusionPatterns);

        boolean result = filter.shouldNotFilter(request);

        assertFalse(result);
    }

    @Test
    public void shouldNotFilter_multipleInclusionPatternMatches_withNoneMatching_expectNoFilter() throws Exception {
        when(shadowTrafficConfigHelper.getConfig()).thenReturn(config);
        when(config.isEnabled()).thenReturn(true);

        when(request.getRequestURI()).thenReturn("/hotels");
        when(request.getMethod()).thenReturn("POST");
        List<ShadowTrafficConfig.InclusionPattern> inclusionPatterns = new ArrayList<>();
        ShadowTrafficConfig.InclusionPattern pattern1 = new ShadowTrafficConfig.InclusionPattern();
        pattern1.setRequestURI("^/hotels$");
        pattern1.setMethod("get");
        inclusionPatterns.add(pattern1);

        ShadowTrafficConfig.InclusionPattern pattern2 = new ShadowTrafficConfig.InclusionPattern();
        pattern2.setRequestURI("^/hotel-details$");
        pattern2.setMethod("get");
        inclusionPatterns.add(pattern2);

        when(config.getInclusionPatterns()).thenReturn(inclusionPatterns);

        boolean result = filter.shouldNotFilter(request);

        assertTrue(result);
    }
}
