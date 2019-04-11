package com.expediagroup.library.drshadow.springboot;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * Creates the servlet request based on what type of HttpServletRequest is incoming
 * @author surawat
 */
public class DrShadowHttpServletRequestFactory {
	
	/**
	 * Returns the DrShadowHttpServletRequest constructed from HttpServletRequest
	 * @param httpServletRequest
	 * @return
	 * @throws IOException
	 */
    public DrShadowHttpServletRequest getHttpServletRequest(HttpServletRequest httpServletRequest) throws IOException {
        if (isFormUrlEncoded(httpServletRequest)) {
            return new FormUrlEncodedHttpServletRequest(httpServletRequest);
        } else if (isMultipart(httpServletRequest)) {
            return new MultiPartHttpServletRequest(httpServletRequest);
        } else {
            return new BasicHttpServletRequest(httpServletRequest);
        }
    }
    
    private boolean isFormUrlEncoded(HttpServletRequest request) {
        String contentType = request.getContentType();
        return "POST".equalsIgnoreCase(request.getMethod()) && contentType != null && contentType.startsWith(APPLICATION_FORM_URLENCODED_VALUE);
    }
    
    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.startsWith(MULTIPART_FORM_DATA_VALUE);
    }
}
