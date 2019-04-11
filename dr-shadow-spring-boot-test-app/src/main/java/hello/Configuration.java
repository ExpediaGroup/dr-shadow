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
