package com.dkaedv.glghproxy;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingFilter implements Filter {
	private final static Log LOG = LogFactory.getLog(LoggingFilter.class);

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;

		String query = (request.getQueryString() == null) ? "" : "?" + request.getQueryString();
		LOG.info("Request to " + request.getRequestURI() + query);
		
		try {
			chain.doFilter(req, res);
			request.getPathInfo();
		} catch (Throwable ex) {
			LOG.error("Error handling request " + request.getRequestURI(), ex);
			throw ex;
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}
}
