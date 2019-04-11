
package com.expediagroup.library.drshadow.springboot;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

public class BasicHttpServletRequest extends DrShadowHttpServletRequest {
    
    protected byte[] body;
    private ByteArrayInputStream copy;
    private ServletInputStream sis;
    private BufferedReader reader;
    
    public BasicHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        // can't access the input stream and access the request parameters
        // at the same time
        final InputStream input = request.getInputStream();
        if (input != null) {
            final InputStream bis = IOUtils.toBufferedInputStream(input);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(bis, baos);
            body = baos.toByteArray();
            copy = new ByteArrayInputStream(body);
        }
    }
    
    String getBody() {
        String sBody = null;
        sBody = (body != null) ? new String(body) : null;
        return sBody;
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (copy != null) {
            copy.reset();
            sis = new DrShadowTrafficServletInputStream(copy);
        }
        return sis;
    }
    
    @Override
    public BufferedReader getReader() throws IOException {
        if (copy != null && reader == null) {
            reader = IOUtils.toBufferedReader(new InputStreamReader(copy));
        }
        return reader;
    }
    
    protected static class DrShadowTrafficServletInputStream extends ServletInputStream {
        
        private final InputStream in;
        
        public DrShadowTrafficServletInputStream(final InputStream in) {
            this.in = in;
        }
        
        @Override
        public int read() throws IOException {
            return in.read();
        }
        
        @Override
        public boolean isFinished() {
            try {
                return in.available() == 0;
            } catch (IOException e) {
                return true;
            }
        }
        
        @Override
        public boolean isReady() {
            return true;
        }
        
        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }
    }
    
}
