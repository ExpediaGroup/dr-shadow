package com.expediagroup.library.drshadow.springboot;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.expediagroup.library.drshadow.springboot.ShadowTrafficConfig.HeaderPattern;

/**
 * Generate shadow traffic by simply reproducing the original request and sending it to the configured destination.
 *
 * Created by ctse on 6/28/2017.
 */
public class ShadowTrafficFilter extends OncePerRequestFilter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ShadowTrafficFilter.class);
    private static final String ALL_METHODS = "*";
    
    private ShadowTrafficAdapter shadowTrafficAdapter;
    private ShadowTrafficConfigHelper shadowTrafficConfigHelper;
    private DrShadowHttpServletRequestFactory drShadowHttpServletRequestFactory;
    
    public ShadowTrafficFilter(ShadowTrafficConfigHelper shadowTrafficConfigHelper, ShadowTrafficAdapter shadowTrafficAdapter) {
        this.shadowTrafficAdapter = shadowTrafficAdapter;
        this.shadowTrafficConfigHelper = shadowTrafficConfigHelper;
    }
    
    /**
     * Does not filter if: 1. The servlet path does not match the inclusion configuration. 2. The configuration has shadow traffic disabled. 3. The request
     * itself already came from shadow traffic.
     * 
     * @param request
     * @return
     * @throws ServletException
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        Map<String, String> headerKeyValueMap = new HashMap<>();
        
        ShadowTrafficConfig shadowTrafficConfig = shadowTrafficConfigHelper.getConfig();
        
        // If shadow traffic is disabled then don't bother matching patterns nor invoke the filter
        if (shadowTrafficConfig == null || !shadowTrafficConfig.isEnabled()) {
            return true;
        }
        
        // If this was already shadow traffic, do not invoke the filter
        Enumeration<?> headerNames = request.getHeaderNames();
        
        // There is no way to optimize this as it's required to go through each header :(
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String key = (String) headerNames.nextElement();
                String value = request.getHeader(key);
                if (null != key) {
                    headerKeyValueMap.put(key, null != value ? value.toLowerCase() : null);

                    if (value != null && key.equalsIgnoreCase(ShadowTrafficAdapter.IS_SHADOW_TRAFFIC_KEY) &&
                            value.equalsIgnoreCase(ShadowTrafficAdapter.IS_SHADOW_TRAFFIC_VALUE)) {
                        return true;
                    }  // Only care about the shadow traffic key
                }
            }
        }
        
        if (CollectionUtils.isEmpty(shadowTrafficConfig.getInclusionPatterns())) {
            return true;
        }

        for (ShadowTrafficConfig.InclusionPattern inclusionPattern : shadowTrafficConfig.getInclusionPatterns()) {
            if (inclusionPattern != null) {
                try {
                    Pattern compiledExclusionPattern = Pattern.compile(inclusionPattern.getRequestURI());

                    // If the path and method match the configured inclusion pattern then allow the shadow traffic through
                    if (compiledExclusionPattern.matcher(path).matches() && (inclusionPattern.getMethod().equals(ALL_METHODS)
                            || request.getMethod().equalsIgnoreCase(inclusionPattern.getMethod()))) {

                        List<HeaderPattern> headerPatterns = inclusionPattern.getHeaderPattern();
                        if (CollectionUtils.isEmpty(headerPatterns)) {
                            return false;
                        } else {
                            for (HeaderPattern headerPattern : headerPatterns) {
                                if (null != headerPattern && null != headerPattern.getHeaderKey() && null != headerPattern.getHeaderValue() &&
                                        headerKeyValueMap.containsKey(headerPattern.getHeaderKey().toLowerCase())) {
                                    Pattern headerValuePattern = Pattern.compile(headerPattern.getHeaderValue().toLowerCase());
                                    String requestHeaderActualValue = headerKeyValueMap.get(headerPattern.getHeaderKey().toLowerCase());
                                    if (StringUtils.isNotBlank(requestHeaderActualValue) && headerValuePattern.matcher(requestHeaderActualValue).matches()) {
                                        return false;
                                    }
                                }

                            }
                        }

                    }
                } catch (PatternSyntaxException pse) { // give a better log warning for invalid syntax
                    LOGGER.warn("Invalid pattern syntax configured", pse);
                } catch (Exception ex) {
                    // if anything else unexpected happens then just ignore the inclusion pattern
                    LOGGER.warn("Invalid inclusion pattern", ex);
                }
            }
        }

        // default
        return true;
    }
    
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

    	if (drShadowHttpServletRequestFactory == null) {
    		this.drShadowHttpServletRequestFactory = new DrShadowHttpServletRequestFactory();
    	}

    	final DrShadowHttpServletRequest drShadowRequest = drShadowHttpServletRequestFactory.getHttpServletRequest(request);

    	try {            
    		shadowTrafficAdapter.invokeShadowTraffic(drShadowRequest, request);
    	} catch (Exception ex) { // Catch all to prevent any interruption to the original request
    		LOGGER.error("Shadow traffic was configured to be ON but invoking shadow traffic failed! Continuing w/ original request...", ex);
    	}

    	chain.doFilter(drShadowRequest, response);
    }
    
}
