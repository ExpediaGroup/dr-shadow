package com.expediagroup.library.drshadow.springboot;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public abstract class DrShadowHttpServletRequest extends HttpServletRequestWrapper {
    public DrShadowHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
    }
    
    abstract String getBody();
}
