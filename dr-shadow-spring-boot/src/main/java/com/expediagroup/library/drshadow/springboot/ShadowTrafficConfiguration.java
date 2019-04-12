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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.AsyncRestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by ctse on 6/28/2017.
 */
@Configuration
@Conditional(DrShadowEnable.class)
@ComponentScan("com.expediagroup.library.drshadow")
@EnableAsync
public class ShadowTrafficConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShadowTrafficConfiguration.class);

    public static final Integer SHADOW_TRAFFIC_HTTP_DEFAULT_CORE_POOL_SIZE = 5;
    public static final Integer SHADOW_TRAFFIC_INVOKER_DEFAULT_CORE_POOL_SIZE = 5;
    public static final Integer SHADOW_TRAFFIC_HTTP_CONNECTION_TIMEOUT = 1000;
    public static final Integer SHADOW_TRAFFIC_HTTP_READ_TIMEOUT = 300;
    public static final Integer DEFAULT_FILTER_ORDER = 3;

    @Autowired
    private ShadowTrafficConfig shadowTrafficConfig;

    private String getMachineName() {
        String machineName = "localhost";
        try {
            machineName = InetAddress.getLocalHost().getHostName();
            LOGGER.info("Detected application machine name {} for shadow traffic reporting.", machineName);
        } catch (UnknownHostException e) {
            LOGGER.warn("Unable to get machine name, \"localhost\" will be used instead.", e);
        }
        return machineName;
    }

    @Bean
    public ShadowTrafficAdapter shadowTrafficAdapter(@Qualifier("shadowRestTemplate") AsyncRestTemplate shadowRestTemplate,
            ShadowTrafficConfigHelper shadowTrafficConfigHelper) {
        return new ShadowTrafficAdapter(shadowTrafficConfigHelper, shadowRestTemplate, getMachineName());
    }
    
    @Bean
    public ShadowTrafficConfigHelper shadowTrafficConfigHelper() {
        return new ShadowTrafficConfigHelper(shadowTrafficConfig);
    }
    
    @Bean
    public FilterRegistrationBean shadowTrafficFilterBean(ShadowTrafficFilter shadowTrafficFilter) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(shadowTrafficFilter);
        registrationBean.setMatchAfter(true);
        // Order is set to three so the LoggingMetadataFilter can populate the metadata (e.g. MessageId, SessionId, etc.)
        registrationBean.setOrder(shadowTrafficConfig.getFilterOrder());
        return registrationBean;
    }
    
    @Bean
    public ThreadPoolTaskExecutor shadowRestTemplateTaskExecutor(ShadowTrafficConfigHelper shadowTrafficConfigHelper) {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setTaskDecorator(new ShadowTrafficLoggingTaskDecorator());
        
        ShadowTrafficConfig shadowTrafficConfig = shadowTrafficConfigHelper.getConfig();
        
        int corePoolSize = SHADOW_TRAFFIC_HTTP_DEFAULT_CORE_POOL_SIZE;
        
        if (shadowTrafficConfig != null) {
            corePoolSize = shadowTrafficConfig.getHttpCorePoolSize();
        }
        
        // Default value is already set inside the config
        threadPool.setCorePoolSize(corePoolSize);
        threadPool.initialize();
        return threadPool;
    }
    
    @Bean
    public ThreadPoolTaskExecutor shadowTrafficTaskExecutor(ShadowTrafficConfigHelper shadowTrafficConfigHelper) {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setTaskDecorator(new ShadowTrafficLoggingTaskDecorator());

        ShadowTrafficConfig shadowTrafficConfig = shadowTrafficConfigHelper.getConfig();
        
        int corePoolSize = SHADOW_TRAFFIC_INVOKER_DEFAULT_CORE_POOL_SIZE;
        
        if (shadowTrafficConfig != null) {
            corePoolSize = shadowTrafficConfig.getInvokerCorePoolSize();
        }
        
        threadPool.setCorePoolSize(corePoolSize);
        threadPool.initialize();
        return threadPool;
    }
    
    @Bean
    public AsyncRestTemplate shadowRestTemplate(ThreadPoolTaskExecutor shadowRestTemplateTaskExecutor, ShadowTrafficConfigHelper shadowTrafficConfigHelper) {
        
        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setTaskExecutor(shadowRestTemplateTaskExecutor);
        // No need for this to be configurable because we ignore the response anyways
        
        ShadowTrafficConfig shadowTrafficConfig = shadowTrafficConfigHelper.getConfig();
        
        int httpConnectionTimeoutMs = SHADOW_TRAFFIC_HTTP_CONNECTION_TIMEOUT;
        int httpReadTimeoutMs = SHADOW_TRAFFIC_HTTP_READ_TIMEOUT;
        
        if (shadowTrafficConfig != null) {
            httpConnectionTimeoutMs = shadowTrafficConfig.getHttpConnectionTimeoutMs();
            httpReadTimeoutMs = shadowTrafficConfig.getHttpReadTimeoutMs();
        }
        
        clientHttpRequestFactory.setConnectTimeout(httpConnectionTimeoutMs);
        clientHttpRequestFactory.setReadTimeout(httpReadTimeoutMs);
        
        return new AsyncRestTemplate(clientHttpRequestFactory);
    }
    
    @Bean
    public ShadowTrafficFilter shadowTrafficFilter(ShadowTrafficAdapter shadowTrafficAdapter, ShadowTrafficConfigHelper shadowTrafficConfigHelper) {
        return new ShadowTrafficFilter(shadowTrafficConfigHelper, shadowTrafficAdapter);
    }
    
}

class DrShadowEnable implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        return Boolean.valueOf(env.getProperty("drshadow.enabled", "false"));
        
    }
}
