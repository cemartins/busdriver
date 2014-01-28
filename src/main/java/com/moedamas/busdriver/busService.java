package com.moedamas.busdriver;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.disk.DiskFileItem;


/**
 * busService is the service base class that all buDriver application services extend.<br>
 * busDriver calls an application service by instantiating this class and calling it's constructor.<br>
 * busDriver informs this object of what servlet context and servlet session to use and also passes
 * the information submited by the client request (browser) in a jtpData object.<br>
 * After initializing this class, buDriver then calls the method goService, which is overriden by the
 * application service class, and in this method, the application should decide what to do.
 * @author 	Carlos Martins
 * @version 	%I%, %G%
 * @since       BUS 1.0
 */
public abstract class busService {
	protected ServletContext context;

	public long ONE_SECOND = 1000;
	public long ONE_MINUTE = 60000;

		/**
		 * Initializes the busService object setting the context attribute
		 * This constructor is called by the Dispatcher servlet
		 * @param context The Servlet Context of the web application
		 */
		public busService(ServletContext context)	{
			this.context = context;
		}


		/**
		 * Method invoked by the Dispatcher servlet each time a SERVICENAME attribute is specified in the request.
		 * The invoked class name is the value of SERVICENAME attribute and it extends the busService class.
		 * @param request busDriverRequest originated at the client
		 * @throws busException
		 */
		public void goService(busDriverRequest request) throws busException	{

		}

		/**
		 * Returns filename of the template that should be parsed with the request
		 * @param request busDriverRequest originated at the client
		 * @return the String corresponding to the TEMPLATE attribute stored in the request
		 */
		public String getTemplateName(busDriverRequest request) {
			
			String TemplateFileName = (String)request.getValue("TEMPLATE");	// Get the filename of the template to parse with the output and return.
			return TemplateFileName;
		}
		
		public void setTemplateName(busDriverRequest request, String templateName) {
			
			request.setValue("TEMPLATE", templateName);
		}
		
		/**
		 * Returns the step number corresponding to the task that the goService method should perform
		 * @param request
		 * @return
		 */
		public int getStep(busDriverRequest request) {

			int step = Integer.parseInt((String)request.getValue("STEP"));
			return step;
		}
		
		public String getSessionTmpDir(busDriverRequest request) throws busException {
			
			HttpSession session = request.getSession();
			if (session == null)
				throw new busException("Trying to get session tmpdir but there is no session.");
			
			String tmpDir = (String) session.getAttribute("tempdir");
			if (tmpDir == null)
				throw new busException("tempdir attribute not set in active session.");
			return tmpDir;
		}
		
		public String getSessionTmpUrl(busDriverRequest request) throws busException {
			
			HttpSession session = request.getSession();
			if (session == null)
				throw new busException("Trying to get session tmpdir but there is no session.");
			
			String tmpUrl = (String) session.getAttribute("tempurl");
			if (tmpUrl == null)
				throw new busException("tempurl attribute not set in active session.");
			return tmpUrl;
		}
		
		public BufferedImage getBufferedImage(busDriverRequest request, String attribute) throws busException {
			
			DiskFileItem item = (DiskFileItem) request.getValue(attribute);
			if(item == null)
				throw new busException("Error trying to obtain a BufferedImage from the value attached to the attribute " + attribute + ". The value is not a DiskFileItem");
			
			try {
				InputStream inputStream = item.getInputStream();
				BufferedImage bufferedImage = ImageIO.read(inputStream);
				return bufferedImage;
			}
			catch (IOException e) {
				throw new busException("Error trying to obtain a BufferedImage from the value attached to the attribute " + attribute, e);
			}
		}
		

		// Writes a message to the log file of the context and registers the message in the log attribute in the datagram.
		protected void ServiceLog(String LogMessage) {

				context.log(LogMessage);
		}

}
