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

import com.expediagroup.library.drshadow.springboot.DrShadowHttpServletRequest;
import org.apache.commons.collections4.MapUtils;

import static org.springframework.util.StringUtils.hasText;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

/**
 * @author surawat
 */
public class FormUrlEncodedHttpServletRequest extends DrShadowHttpServletRequest {

    /**
     *
     * @param request
     * @throws IOException
     */
    public FormUrlEncodedHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
    }
    
    @Override
    String getBody() {
        String sBody = null;
        final Map<String, String[]> parameters = super.getParameterMap();
        if (MapUtils.isNotEmpty(parameters)) {
            final StringBuilder buffer = new StringBuilder();
            boolean first = true;
            for (Entry<String, String[]> param : parameters.entrySet()) {
                if (!first) {
                    buffer.append('&');
                }
                buffer.append(param.getKey()).append('=');
                if (hasText(param.getValue()[0])) {
                    buffer.append(param.getValue()[0]);
                }
                first = false;
            }
            sBody = buffer.toString();
        }
        return sBody;
    }

}
