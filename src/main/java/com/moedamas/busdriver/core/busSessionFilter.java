package com.moedamas.busdriver.core;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.moedamas.busdriver.busException;


/**
 * busSessionFilter is the first filter in the chain of filters
 * this filter is optional.
 * makes sure 
 * @author cemartins
 *
 */

public class busSessionFilter implements Filter {

	public busSessionFilter() {
		super();
	}

	public void init(FilterConfig arg0) throws ServletException {

	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		if (!(request instanceof HttpServletRequest))
			throw new busException("Request is not an HttpServletRequest, so cannot process busDriver.");
		
		HttpServletRequest httprequest = (HttpServletRequest) request;
		
		HttpSession session = httprequest.getSession(true);
		
		String userid = httprequest.getRemoteUser();
		if(userid != null)
			session.setAttribute("USERID", userid);

		chain.doFilter(request, response);
	}

	public void destroy() {

	}

}
