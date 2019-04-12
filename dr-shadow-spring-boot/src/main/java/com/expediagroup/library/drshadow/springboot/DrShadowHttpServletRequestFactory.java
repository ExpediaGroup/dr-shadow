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
	 * @param httpServletRequest Original servlet request
	 * @return Newly wrapped request shadow request
	 * @throws IOException IOException
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
