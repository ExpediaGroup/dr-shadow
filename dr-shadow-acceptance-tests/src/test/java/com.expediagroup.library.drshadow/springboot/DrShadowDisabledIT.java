package com.expediagroup.library.drshadow.springboot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.annotation.PostConstruct;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/*
    Goal of this test is to verify that the Spring Boot Auto Configuration is not wired when Dr Shadow is disabled.
 */
@RunWith(SpringRunner.class)
@DirtiesContext
@SpringBootTest(classes={TestApplication.class})
@ActiveProfiles("drshadow-disabled")
public class DrShadowDisabledIT {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    private MockHttpServletRequestBuilder requestBuilder;

    @MockBean
    private ShadowTrafficAdapter shadowTrafficAdapter;

    @PostConstruct
    public void setupMockMvc() {
        this.mockMvc = webAppContextSetup(wac).build();
    }

    @Test
    public void testNoShadowTraffic() throws Exception {

        //Arrange
        requestBuilder = get("/get");

        //Act
        mockMvc.perform(requestBuilder).andDo(print())
                .andExpect(status().is2xxSuccessful());

        //Assert
        verify(shadowTrafficAdapter, never()).invokeShadowTraffic(any(), any());

        //  Verify that the shadowTrafficAdapter never even got wired.
        assertFalse("shadowTrafficAdapter should not have been wired.", ((GenericWebApplicationContext) wac).isBeanNameInUse("shadowTrafficAdapter"));
    }

}
