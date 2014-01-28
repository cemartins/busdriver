/**
 * 
 */
package com.moedamas.busdriver.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

import com.moedamas.busdriver.busDriverRequest;
import com.moedamas.busdriver.busException;
import com.moedamas.busdriver.busServiceException;
import com.moedamas.busdriver.modules.busAbstractModule;
import com.moedamas.busdriver.parsers.ParserFactoryInterface;
import com.moedamas.busdriver.parsers.busparser.busParser;



/**
 * @author cemartins
 * This is the first incoming filter in the filter chain of busDriver.
 */
public class busRequestFilter implements Filter {

	private ServletContext context;			// context in witch this servlet is running.
	private String CharacterEncoding;
	private Locale ctxLocale;					// locale for the active context
	private Vector moduleslist;				// list of busModule objects representing the modules to manage.
	private String parserModule;				// the module that will do the parsing of the templates.
	private String defaultErrorTemplate;				// The template file to return is case of error

	/**
	 * 
	 */
	public busRequestFilter() {
		super();
		moduleslist = new Vector();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {

		String param1, param2;
		Integer sizeThreshold;

			this.context = config.getServletContext();

			context.log("busRequestFilter init()... ");

			param1 = (String)context.getInitParameter("sizeThreshold");
			if (param1 == null)
				throw new ServletException("Parameter sizeThreshold not found in deployment descriptor file");
//			context.log("sizeThreshold = " + param1);
			sizeThreshold = new Integer(param1);
			context.log("sizeThreshold = " + sizeThreshold);
			
			parserModule = (String)context.getInitParameter("parsermodule");
			if (parserModule == null)
				throw new ServletException("Parameter parsermodule not found in properties file");
			
			param1 = (String)context.getInitParameter("languagecode");
			if (param1 == null)
				throw new ServletException("Parameter languagecode not found in properties file");
			param2 = (String)context.getInitParameter("countrycode");
			if (param2 == null)
				throw new ServletException("Parameter countrycode not found in properties file");
			ctxLocale = new Locale(param1, param2);
			context.log("ctxLocale: language=" + param1 + ", country=" + param2);
			
			CharacterEncoding = (String)context.getInitParameter("CharacterEncoding");
			context.log("Character encoding = " + CharacterEncoding);
			
			defaultErrorTemplate = (String) context.getInitParameter("defaultErrorTemplate");

//			Initialize extra bus modules
			param1 = (String)context.getInitParameter("usemodules");
			if (param1 != null && param1.length() > 0) {
				java.util.StringTokenizer st = new StringTokenizer(param1,";");
				String moduleName;
				while (st.hasMoreTokens()) {
					moduleName = st.nextToken();
					initModule(moduleName);
				}
				
			}
			
			context.log("busRequestFilter init() - Done!");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response,	FilterChain chain) throws IOException, ServletException {
		
		boolean success = true;

//		context.log("busRequestFilter doFilter() - Enter...");
		if (!(request instanceof HttpServletRequest))
			throw new busException("Request is not an HttpServletRequest, so cannot process busDriver.");

		// prepare the request
		busDriverRequest busRequest = new busDriverRequest(context, (HttpServletRequest) request);

		busRequest.setCharacterEncoding(CharacterEncoding);
		busRequest.setLocale(ctxLocale);
		
		// prepare the modules
		for(Object obj : this.moduleslist) {
			((busAbstractModule) (obj)).prepareService(busRequest);
		}
//		context.log("busRequestFilter doFilter() - modules have been notified of service start... ");
		
		// Call the service or remaining filters down the chain.
		try {

			success = dispatchRequest(busRequest, response, chain);
			
			// add the added cookies to the response
			for(Object object : busRequest.getAddedCookies())
				((HttpServletResponse)response).addCookie((Cookie)object);
			
			String template = (String) busRequest.getValue("template");
			if(template != null) {
				parseTemplate(busRequest, (HttpServletResponse)response, template);
			}
			else {
				context.log("busDriver.doGet(): Service completed. No file to parse.");
				((HttpServletResponse)response).setStatus(HttpServletResponse.SC_NO_CONTENT);
			}
			
		}
		catch (busException e) {
//			Just mark the service as unsuccessful
			success = false;
			throw (busException) e;
		}
		finally {
			for(Object obj : this.moduleslist) {
				((busAbstractModule) (obj)).finishService(busRequest, success);
			}
//			context.log("busRequestFilter doFilter() - modules have been notified of service return... ");
		}
		
//		context.log("busRequestFilter doFilter() - done!");

	}
	
	/**
	 * Forwards the request and manages service errors
	 * @param request
	 * @param response
	 * @param chain
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean dispatchRequest(busDriverRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		boolean success = true;

		try {
			chain.doFilter(request, response);
		}
		catch (busServiceException e) {
//			Just mark the service as unsuccessful
			success = false;
			request.addValue("ERROR", e.getMessage());
			String template = (String)request.getValue("errortemplate");
			if(template == null)
				template = this.defaultErrorTemplate;
			if(template == null)
				request.removeValue("template");
			else
				request.setValue("template", template);
		}

		return success;
	}
	
	public void parseTemplate(busDriverRequest request, HttpServletResponse response, String template) throws busException {
		busParser parser;
				
		String fileParse = ".." + File.separator;
		if(template.indexOf(fileParse) >= 0) {
			throw new busException("template file name not valid - " + template);
		}

		ParserFactoryInterface parserFactory = (ParserFactoryInterface) request.getValue(this.parserModule);
		if(parserFactory == null)
			throw new busException("busRequestFilter.parseTemplate(): the busRequestFilter class was not stored in the datagram under the attribute " + this.parserModule);
		parser = parserFactory.getParser(template);
		ServletOutputStream out;
		try {
			out = response.getOutputStream();
		} catch (IOException e) {
			throw new busException("Error trying to obtain output stream from response: " + e.getMessage());
		}			
		// Get the output stream from the Servlet
		response.setContentType("text/html; charset=" + CharacterEncoding);		// set the mime-type for the returning page

		try {
//			merge the tmplate and the data collected and place the output in the Servlet's output stream
			parser.parseStream(request, out);
		} catch (busException e) {
			parserFactory.removeParser(template);
			throw new busException("busRequestFilter.parseTemplate(): Error in template (" + template + ")", e);
		}
	}


	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {

		try {
			for(Object obj : this.moduleslist) {
				((busAbstractModule) (obj)).finishContext();
			}
			this.moduleslist.clear();
		}
		catch (busException e) {}
	}
	
	private void initModule(String moduleRef) throws busException {
		
		String param1 = (String) context.getInitParameter(moduleRef);
		java.util.StringTokenizer st = new StringTokenizer(param1,";");
		String strImplementingClass = st.nextToken();
		String strImplementingConfigClass = st.nextToken();
		String strConfigResource = st.nextToken();
		ConfigMgr configMgr;
		
		context.log("busRequestFilter: initializing module " + moduleRef);
		
//		Loads the class that implements the ConfigMgr
		try {
        		configMgr = (ConfigMgr) Class.forName(strImplementingConfigClass).newInstance();
        		configMgr.load(context, strConfigResource);
  		}
		catch (InstantiationException e) {
			throw new busException("busModulesFilter.initModule(" + moduleRef + "): InstantiationException " + e.getMessage());
  		}
		catch (IllegalAccessException e) {
			throw new busException("busModulesFilter.initModule(" + moduleRef + "): IllegalAccessException " + e.getMessage());
  		}
		catch (ClassNotFoundException e) {
			throw new busException("busModulesFilter.initModule(" + moduleRef + "): ClassNotFoundException " + e.getMessage());
		}
		catch (IllegalArgumentException e) {
			throw new busException("busModulesFilter.initModule(" + moduleRef + "): IllegalArgumentException " + e.getMessage());
		}
		catch (busException e) {
			throw new busException("busModulesFilter.initModule(" + moduleRef + "): busException " + e.getMessage());
		}
		
		
//		Loads the class that implements busModule and stores it in the bag
		try {
			Class[] moduleArgsClass = {String.class, ConfigMgr.class};
			Object[] moduleArgsObject = new Object[] {moduleRef, configMgr};
        		Class classDefinition = Class.forName(strImplementingClass);
			Constructor moduleConstructor = classDefinition.getConstructor(moduleArgsClass);
			busAbstractModule module = (busAbstractModule) moduleConstructor.newInstance(moduleArgsObject);
			module.prepareContext(this.context);
			this.moduleslist.add(module);
  		}
		catch (InstantiationException e) {
			throw new busException("busModulesFilter.initModule(" + moduleRef + "): InstantiationException ", e);
  		}
		catch (IllegalAccessException e) {
			throw new busException("busModulesFilter.initModule(" + moduleRef + "): IllegalAccessException ", e);
  		}
		catch (ClassNotFoundException e) {
			throw new busException("busModulesFilter.initModule(" + moduleRef + "): ClassNotFoundException ", e);
		}
		catch (NoSuchMethodException e) {
			throw new busException("busModulesFilter.initModule(" + moduleRef + "): NoSuchMethodException ", e);
		}
		catch (IllegalArgumentException e) {
			throw new busException("busModulesFilter.initModule(" + moduleRef + "): IllegalArgumentException ", e);
		}
		catch (InvocationTargetException e) {
			throw new busException("busModulesFilter.initModule(" + moduleRef + "): InvocationTargetException ", e);
		}
		catch (busException e) {
			throw new busException("busModulesFilter.initModule(" + moduleRef + "): busException ", e);
		}


		context.log("busRequestFilter: module " + moduleRef + " initialized sucessfully");
	}


}
