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
