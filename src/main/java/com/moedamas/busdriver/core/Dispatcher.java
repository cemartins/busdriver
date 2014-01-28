package com.moedamas.busdriver.core;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;

import com.moedamas.busdriver.busDriverRequest;
import com.moedamas.busdriver.busException;
import com.moedamas.busdriver.busService;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/*       SERVLET TEST
    This class demonstrates the use of the Java Template Pages classes in building dynamic web pages through Servlets.
    The example uses fixed data to produce a fixed html file, but you're wellcome to try and change it as you will.
    Remeber that, for this example to work, you must place the template file (test.html) in the documents root of your http server.
    Also, you must have all JTP Classes and the busDriver.class in the classpath or in a location where your http server looks for servlets
*/

public class Dispatcher extends HttpServlet	{

static final long serialVersionUID=1;

//private Connection DBconn =null;		// database connection for select queries.
private ServletContext context;			// context in witch this servlet is running.


	/* (non-Javadoc)
	 * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
	 * Init Method -> inicializtion of the servlet just after it's load by the container.
	 * See also SessionListener.contextInitialized()
	 */
	public void init(ServletConfig config) throws ServletException {
		this.context = config.getServletContext();
	}


	// destructor - frees things just before being unloaded by the servlet container
	public void destroy()	{

	}


	// Get method, where the jtp deal goes...
	public void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException	{

		if (!(req instanceof busDriverRequest))
			throw new busException("Request is not a busDriverRequest, so cannot process dispatcher.");
		
		busDriverRequest request = (busDriverRequest) req;

		// Dispatch the request to the apropriate service
		// busLog("busDriver.doGet(): dispatching request...", requestData);
		callService(request);	// Dispatch the request to the application servlet and get the template file to return to the user

	}


	// answer post requests as well
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException	{

			doGet(req, res);
	}



	// Dispatches the request to the class especified in the attribute SERVICENAME
	// returns the name of the template file to be parsed.
	private void callService(busDriverRequest request) throws busException {
		String ErrMsg;

//		TODO Just like busParserFacotry, should develop some Service Factory, to pool services
		
			String ServiceName = (String)request.getValue("SERVICENAME"); 		// Get the name of the class to call

			if (ServiceName == null || ServiceName.equals("")) {			// If there is no class to call then
				context.log("busDriver.callService(): No service to call - returning template only.");
				return;
			}

			Class[] ArgsClass = new Class[] {ServletContext.class};
			Object[] ArgsObject = new Object[] {context};

			context.log("busDriver.callService(): calling service (" + ServiceName + ").");

			try {
	        		Class classDefinition = Class.forName(ServiceName);
	        		Constructor ArgsConstructor = classDefinition.getConstructor(ArgsClass);
	        		busService srv = (busService)ArgsConstructor.newInstance(ArgsObject);
				srv.goService(request);
      		}
			catch (InstantiationException e) {
				ErrMsg = "busDriver.callService(): InstantiationException " + e.getMessage();
				throw new busException(ErrMsg, e);
      		}
			catch (IllegalAccessException e) {
				ErrMsg = "busDriver.callService(): IllegalAccessException " + e.getMessage();
				throw new busException(ErrMsg, e);
      		}
			catch (ClassNotFoundException e) {
				ErrMsg = "busDriver.callService(): ClassNotFoundException " + e.getMessage();
				throw new busException(ErrMsg, e);
			}
			catch (NoSuchMethodException e) {
				ErrMsg = "busDriver.callService(): NoSuchMethodException " + e.getMessage();
				throw new busException(ErrMsg, e);
			}
			catch (IllegalArgumentException e) {
				ErrMsg = "busDriver.callService(): IllegalArgumentException " + e.getMessage();
				throw new busException(ErrMsg, e);
			}
			catch (InvocationTargetException e) {
				ErrMsg = "busDriver.callService(): InvocationTargetException " + e.getMessage();
				throw new busException(ErrMsg, e);
			}

	}


}
