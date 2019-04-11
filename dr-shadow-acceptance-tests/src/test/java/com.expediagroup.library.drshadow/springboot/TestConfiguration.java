package com.expediagroup.library.drshadow.springboot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
public class TestConfiguration {

    @Bean
    public RestTemplate configClientRestTemplate(Environment environment) {
        return mock(RestTemplate.class);
    }

}