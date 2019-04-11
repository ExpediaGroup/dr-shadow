package com.expediagroup.library.drshadow.springboot;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author surawat
 */
public class MultiPartHttpServletRequest extends DrShadowHttpServletRequest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiPartHttpServletRequest.class);
    private final Pattern pattern = Pattern.compile("boundary=(.*)", Pattern.CASE_INSENSITIVE);

    /**
     *
     * @param request
     * @throws IOException
     */
    public MultiPartHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
    }
    
    @Override
    String getBody() {
        String sBody = null;
        Collection<Part> parts = null;
        try {
            parts = super.getParts();
        } catch (Exception e) {
            LOGGER.warn("Unable to get parts of multipart request for logging", e);
        }
        if (CollectionUtils.isNotEmpty(parts)) {
            StringBuilder sb = new StringBuilder();
            String boundary = extractBoundary(this);
            for (Part part : parts) {
                sb.append("--").append(boundary).append("\n");
                for (String headerName : part.getHeaderNames()) {
                    for (String headerValue : part.getHeaders(headerName)) {
                        sb.append(headerName).append(": ").append(headerValue).append("\n");
                    }
                }
                try {
                    sb.append(IOUtils.toString(part.getInputStream())).append("\n");
                } catch (IOException e) {
                	LOGGER.warn("Exception while getting input stream data", e);
                }
            }
            sb.append("--").append(boundary).append("--");
            sBody = sb.toString();
        }
        return sBody;
    }
    
    private String extractBoundary(DrShadowHttpServletRequest request) {
        String header = request.getHeader("content-type");
        if (header != null) {
            Matcher matcher = pattern.matcher(header);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }
}
