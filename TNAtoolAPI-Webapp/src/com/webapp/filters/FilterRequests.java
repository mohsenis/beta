package com.webapp.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class FilterRequests implements Filter {
    private int limit = 20;
    private int count;
    private Object lock = new Object();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
    	HttpServletRequest myHttpRequest = (HttpServletRequest) request;
    	HttpSession myHttpSession = myHttpRequest.getSession(false);
    	if (null == myHttpSession) {
	        try {
	            boolean ok;
	            synchronized (lock) {
	                ok = count++ < limit;
	            }
	            if (ok) {
	                // let the request through and process as usual
	                chain.doFilter(request, response);
	            } else {
	                // handle limit case, e.g. return status code 429 (Too Many Requests)
	                // see http://tools.ietf.org/html/rfc6585#page-3
	            }
	        } finally {
	            synchronized (lock) {
	                count--;
	            }           
	        }
    	}
    }

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}
}