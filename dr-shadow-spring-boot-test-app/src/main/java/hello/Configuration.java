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
package hello;

import com.expediagroup.library.drshadow.springboot.ShadowTrafficFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public FilterRegistrationBean logFilterBean(LogFilter logFilter) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(logFilter);
        registrationBean.setMatchAfter(true);
        // Order is set to three so the LoggingMetadataFilter can populate the metadata (e.g. MessageId, SessionId, etc.)
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    public LogFilter logFilter() {
        return new LogFilter();
    }
}
