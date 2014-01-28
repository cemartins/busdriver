/**
 * 
 */
package com.moedamas.busdriver.core;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.disk.*;

import com.moedamas.busdriver.busDriverRequest;
import com.moedamas.busdriver.busException;

/**
 * @author cemartins
 * if the request sent by the browser is a multipart request, read it into a servlet request wrapper
 */
public class MultipartReqFilter implements Filter {

	private ServletContext context;			// context in witch this servlet is running.
	private DiskFileItemFactory fuFactory;

	/**
	 * 
	 */
	public MultipartReqFilter() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		String param1;
		File tmpdir;
		Integer sizeThreshold;

			this.context = config.getServletContext();

			context.log("MultipartReqFilter init()... ");
			
//			Get the initialization parameters from web.xml
			param1 = (String)context.getInitParameter("systmpdir");
			if (param1 == null)
				throw new ServletException("Parameter systmpdir not found in properties file");
			context.log("systmpdir = " + param1);
			tmpdir = new File(param1);
			param1 = (String)context.getInitParameter("sizeThreshold");
			if (param1 == null)
				throw new ServletException("Parameter sizeThreshold not found in properties file");
			sizeThreshold = new Integer(param1);
			fuFactory = new DiskFileItemFactory(sizeThreshold.intValue(), tmpdir);
			context.log("sizeThreshold = " + sizeThreshold);

			context.log("MultipartReqFilter init() - Done! ");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse response,	FilterChain chain) throws IOException, ServletException {

		context.log("MultipartReqFilter doFilter() - Enter... ");

		if (!(req instanceof busDriverRequest))
			throw new busException("Request is not a busDriverRequest, so cannot process multipart.");
		
		busDriverRequest request = (busDriverRequest) req;
		ServletRequestContext reqCtx = new ServletRequestContext(request);

		if(ServletFileUpload.isMultipartContent(reqCtx)) {

				HttpSession session;
				String saveDirectoryName;
				String fileName = "";
				ServletFileUpload fu;
				Integer maxUploadSize;
				File upload;


				// Get input from the web client.
				// Use the MultipartRequest class for all requests.
				session = request.getSession();
				if(session == null)
					throw new busException("Non existent user session.");
				saveDirectoryName = (String)session.getAttribute("tempdir");
				maxUploadSize = new Integer((String)context.getInitParameter("requestinputlimit"));
				if(saveDirectoryName == null || saveDirectoryName.length() == 0)
					throw new busException("tempdir session attribute not defined in context parameters.");

				fu = new ServletFileUpload(fuFactory);
				// maximum size before a FileUploadException will be thrown
				fu.setSizeMax(maxUploadSize.longValue());

				try {
					List items = fu.parseRequest((HttpServletRequest)request);
				    Iterator i = items.iterator();
				    while(i.hasNext()) {
				   		DiskFileItem item = (DiskFileItem) i.next();
				   		if(item.isFormField()) {
				   			request.addValue(item.getFieldName(), item.getString());
				   		}
			    		else {
							fileName = item.getName();
							if (fileName.length() > 0) {
								int n = fileName.lastIndexOf('\\');
								if(n != -1) {
									fileName = fileName.substring(++n);
								}
					    		upload = new File(saveDirectoryName + File.separator + fileName);
				    			item.write(upload);
						    	request.addValue(item.getFieldName(), item);
							}
			    		}
				    }
				}
				catch (FileUploadException e) {
					throw new busException(e.getMessage());
				}
				catch (Exception e) {
					throw new busException("Error writing filename " + fileName + ": " + e.getMessage()); 
				}
		}

		context.log("MultipartReqFilter doFilter() - call chain... ");
		chain.doFilter(request, response);
		context.log("MultipartReqFilter doFilter() - return from chain... ");
		context.log("MultipartReqFilter doFilter() - Done! ");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {

	}

}
