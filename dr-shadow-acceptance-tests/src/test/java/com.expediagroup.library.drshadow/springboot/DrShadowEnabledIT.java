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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.annotation.PostConstruct;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/*
    Goal of this test is to verify the Dr Shadow Spring Boot Library works with a sample Spring Boot application.
    It can only do so much testing - we are mocking the actual shadow call and have to specify the filter explicitly to use in this
    test (ie. not actually testing the FilterRegistrationBean).
 */
@RunWith(SpringRunner.class)
@DirtiesContext
@SpringBootTest(classes={TestApplication.class})
@ActiveProfiles("drshadow-enabled")
public class DrShadowEnabledIT {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    private MockHttpServletRequestBuilder requestBuilder;

    @Autowired
    private ShadowTrafficFilter shadowTrafficFilter;

    @MockBean
    private ShadowTrafficAdapter shadowTrafficAdapter;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void setupMockMvc() {
        this.mockMvc = webAppContextSetup(wac).addFilter(shadowTrafficFilter).build();
    }

    @Test
    public void testSuccessfulShadowTraffic() throws Exception {

        //Arrange
        requestBuilder = get("/get");

        //Act
        mockMvc.perform(requestBuilder).andDo(print())
            .andExpect(status().is2xxSuccessful());

        //Assert
        verify(shadowTrafficAdapter, times(1)).invokeShadowTraffic(any(), any());

        //  Verify that the shadowTrafficAdapter never even got wired.
        assertTrue("shadowTrafficAdapter should have been wired.", ((GenericWebApplicationContext) wac).isBeanNameInUse("shadowTrafficAdapter"));
    }

    @Test
    public void testShadowTrafficFailureDoesNotImpactOriginalRequest() throws Exception {

        //Arrange
        requestBuilder = get("/get");
        doThrow(new RuntimeException("mock failure")).when(shadowTrafficAdapter).invokeShadowTraffic(any(), any());

        //Act
        mockMvc.perform(requestBuilder).andDo(print())
                .andExpect(status().is2xxSuccessful());

        //Assert
        //  Verify that the shadowTrafficAdapter never even got wired.
        assertTrue("shadowTrafficAdapter should have been wired.", ((GenericWebApplicationContext) wac).isBeanNameInUse("shadowTrafficAdapter"));
    }

}
