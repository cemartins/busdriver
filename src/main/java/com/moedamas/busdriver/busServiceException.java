package com.moedamas.busdriver;

import javax.servlet.ServletException;


/**
 * busServiceException is the exception thrown by bus classes and services to signal a user error, i.e. an error that can be corrected by the user.
 * @author 	Carlos Martins
 * @version 	%I%, %G%
 * @since       BUS 1.0
 */
public class busServiceException extends busException	{
	static final long serialVersionUID=1;
	int errtemplateindex = 0;	// Index of the ERRORTEMPLATE attribute to obtain the value of the template to return

		/**
		* Creates a new, empty <code>busServiceException</code>.
		*/
		public busServiceException()	{
			super();
		}

		/**
		* Creates a new <code>busServiceException</code> with an explanation message.
		*
		* @param message error explanation message
		*/
		public busServiceException(String message)	{
			super(message);
		}

		/**
		* Creates a new <code>busServiceException</code> with an explanation message and an indication
		* as to whether the service that threw the exception should be called back after the user corrects the problem.
		*
		* @param message error explanation message
		* @param errtplidx index of the value in ERRORTEMPLATE attribute that corresponds to the template file to return to the user.
		*/
		public busServiceException(String message, int errtplidx)	{
			super(message);
			errtemplateindex = errtplidx;
		}

		public int getErrorTemplateIndex() {

				return errtemplateindex;
		}

}
