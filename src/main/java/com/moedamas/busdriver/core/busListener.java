package com.moedamas.busdriver.core;

/*
 * $Header: /cvsroot/busdriver/busdriver/com/moedamas/busdriver/core/busListener.java,v 1.1 2005/09/17 00:30:10 cemartins Exp $
 * $Revision: 1.1 $
 * $Date: 2005/09/17 00:30:10 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */


//package listeners;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.moedamas.busdriver.busException;


import java.io.File;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Vector;



/**
 * Example listener for context-related application events, which were
 * introduced in the 2.3 version of the Servlet API.  This listener
 * merely documents the occurrence of such events in the application log
 * associated with our servlet context.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2005/09/17 00:30:10 $
 */

public final class busListener implements ServletContextListener,  HttpSessionListener {


    // ----------------------------------------------------- Instance Variables

    private ServletContext context = null;					// The servlet context with which we are associated.
	private Properties busProperties = new Properties();	// Properties containing inicialization parameters read from busDriver.properties file



    // --------------------------------------------------------- Public Methods

    /**
     * Record the fact that this web application has been destroyed.
     *
     * @param event The servlet context event
     */
    public void contextDestroyed(ServletContextEvent event) {
		Enumeration SessionIdList;		// The list of the active session IDs in this context
		Vector SessionList;				// The session list of the context
		String SessionId;				// This session ID
		String nsessions;

			SessionList = (Vector)this.context.getAttribute("sessionlist");
			SessionIdList = SessionList.elements();

			while(SessionIdList.hasMoreElements()) {
				SessionId = (String)SessionIdList.nextElement();
				try {
					destroySession(SessionId);		// Detroy the sessions in the session list
				}
				catch(busException e) {
					e.printStackTrace();
				}
			}

			// Prepare the context and save it's status back to the properties file
			//log("contextDestroyed() - nsession...");
			nsessions = (String)context.getAttribute("nsessions");
			if(nsessions == null || nsessions.length() == 0)
				nsessions = "0";
			log("contextDestroyed() - nsessions = " + nsessions);
			busProperties.setProperty("nsessions", nsessions);
    }


    /**
     * Record the fact that this web application has been initialized.
     *
     * @param event The servlet context event
     */
    public void contextInitialized(ServletContextEvent event) {
		String ApplicationName;			// Name of this application
		Vector SessionList;				// List of active sessions in the context.

			this.context = event.getServletContext();

			ApplicationName = this.context.getServletContextName();

			SessionList = new Vector();										// Initialize the session list
			this.context.setAttribute("sessionlist", SessionList);


			log("contextInitialized(" + ApplicationName + "): Initialization completed successfuly.");
    }


    /**
     * Record the fact that a session has been created.
     *
     * @param event The session event
     */
    public void sessionCreated(HttpSessionEvent event) {
		HttpSession session;			// This session (the session being created)
		String SessionId= null;			// This session ID
		Vector SessionList;				// The session list of the context
		File tempContextDirectory;		// The directory to which save temporary files for this context
		File tempSessionDirectory;		// The directory to which save temporary files for this session being created.
		String strTempSessionDir;			// Same as tempSessionDirectory but in string format.
		String tempContextURLDirectory;	// The URL path for the temporary direcory of the context.
		String tempSessionURLDirectory;	// The URL path for the temporary directory of the session being created.
		Integer nsessions;				// the number of session created in this application.

			// log("SessionListener.sessionCreated(" + event.getSession().getId() + "): creating new session...");

			try {
				session = event.getSession();
				SessionId = session.getId();

				// session.setMaxInactiveInterval(-1);				// indicate that the session should never expire

				// Create the temporary directory for the session and save it's path on the session's attributes.
				tempContextDirectory = (File)context.getAttribute("javax.servlet.context.tempdir");
				tempSessionDirectory = new File(tempContextDirectory.toString() + File.separator + SessionId);
				if(tempSessionDirectory.mkdir()) {
					// log("SessionListener.sessionCreated(): temporary directory created: " + tempSessionDirectory.toString());
					strTempSessionDir = tempSessionDirectory.toString().replace("\\", "\\\\");
;					
					session.setAttribute("tempdir", strTempSessionDir);
				}

				// Get the temporary directory URL of the context and set the temporary directory URL for the session being created.
				tempContextURLDirectory = (String)context.getAttribute("javax.servlet.context.tempurl");
				tempSessionURLDirectory = tempContextURLDirectory + "/" + SessionId;
				session.setAttribute("tempurl", tempSessionURLDirectory);	// Register the temporary directory URL for the session being created.

				SessionList = (Vector)this.context.getAttribute("sessionlist");		// Add this session ID to the list of session of the context
				SessionList.addElement(SessionId);
				this.context.setAttribute(SessionId, session);						// Register this session in the context.

				try {
					nsessions = new Integer((String)(context.getAttribute("nsessions")));
					nsessions = new Integer(nsessions.intValue() + 1);
				}
				catch(NumberFormatException e) {
					nsessions = new Integer(1);
				}
				context.setAttribute("nsessions", nsessions.toString());

				// log("sessionCreated('" + SessionId + "') created successfully. Will stand idle for at most " + session.getMaxInactiveInterval() + " seconds.");
			}
			catch(Exception e) {
				log("SessionListener.sessionCreated(" + SessionId + "): Exception: " + e.getMessage(), e);
				e.printStackTrace();
			}
    }




    /**
     * Record the fact that a session has been destroyed (the contents of the session are no longer available).
     *
     * @param event The session event
     */
    public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session;			// This session (the session being destroyed)
		String SessionId= "";			// This session ID
		String userId= "";				// the user for this session
		Vector SessionList;				// The session list of the context

			// log("SessionListener.sessionDestroyed('" + event.getSession().getId() + "'): destroying...");

			try {
				session = event.getSession();
				SessionId = session.getId();
				//userId = (String)session.getAttribute("USERID");

				log("SessionListener.sessionDestroyed(" + SessionId + ", " + userId + "): destroying...");

				destroySession(SessionId);

				SessionList = (Vector)this.context.getAttribute("sessionlist");		// Remove this session ID from the list of session of the context
				SessionList.removeElement(SessionId);
				this.context.removeAttribute(SessionId);							// Remove this session from the context.

				System.gc();		// runs the garbage collector freeing up memory.

			}
			catch(busException e) {
				log("SessionListener.sessionDestroyed(" + SessionId + "): Exception: " + e.getMessage(), e);
				e.printStackTrace();
			}
			catch(Exception e) {
				log("SessionListener.sessionDestroyed(" + SessionId + "): Exception: " + e.getMessage(), e);
				e.printStackTrace();
			}

    }


    // -------------------------------------------------------- Private Methods

	// Destroy a session
	private void destroySession(String SessionId) throws busException {
		File tempContextDirectory;		// The directory to which save temporary files for this context
		File tempSessionDirectory;		// The directory to which save temporary files for this session
		String[] filesInDirectory;		// Array of filenames of the files in the temporary directory
		File fileIn;					// A file in the temporary directory
		int i = 0;

			if(SessionId == null || SessionId.equals("")) {
				log("SessionListener.destroySession(): cannot destroy Session null");
				return;
			}

			log("SessionListener.destroySession('" + SessionId + "') cleaning up session data...");


			try {
				// Get the name of the temporary directory for this session
				tempContextDirectory = (File)this.context.getAttribute("javax.servlet.context.tempdir");
				tempSessionDirectory = new File(tempContextDirectory.toString() + File.separator + SessionId);

				if(tempSessionDirectory != null) {
					filesInDirectory = tempSessionDirectory.list();
					// Remove all the files in the temporary directory
					if(filesInDirectory != null) {
						while(i < filesInDirectory.length) {
							// log("SessionListener.destroySession(): removing file: " + filesInDirectory[i]);
							fileIn = new File(tempSessionDirectory.toString() + File.separator + filesInDirectory[i]);
							fileIn.delete();
							i++;
						}
					}
					// log("SessionListener.destroySession(): removing temporary directory: " + tempSessionDirectory.toString());
					tempSessionDirectory.delete();		// Remove the temporary directory for this session
				}
			}
			catch(SecurityException e) {
				log("SessionListener.destroySession SecurityException: " + e.getMessage());
				throw new busException("SessionListener.destroySession SecurityException: " + e.getMessage(), e);
			}

			log("SessionListener.destroySession('" + SessionId + "') destroyed successfully.");
	}


    /**
     * Log a message to the servlet context application log.
     *
     * @param message Message to be logged
     */
    private void log(String message) {

	if (context != null)
	    context.log("SessionListener: " + message);
	else
	    System.out.println("SessionListener: " + message);

    System.err.println("SessionListener: " + message);
    }


    /**
     * Log a message and associated exception to the servlet context
     * application log.
     *
     * @param message Message to be logged
     * @param throwable Exception to be logged
     */
    private void log(String message, Throwable throwable) {

	if (context != null)
	    context.log("SessionListener: " + message, throwable);
	else {
	    System.out.println("SessionListener: " + message);
	    throwable.printStackTrace(System.out);
	}

    }


}
