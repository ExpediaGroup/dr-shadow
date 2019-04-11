package hello;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.web.filter.OncePerRequestFilter;


public class LogFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogFilter.class);
    private static final String ALL_METHODS = "*";

    /**
     * Invoke shadow traffic asynchronously and continue the chain
     *
     * @param request
     * @param response
     * @param chain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        MDC.put("test", "carlson");

        chain.doFilter(request, response);
    }

}
