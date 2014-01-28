package com.moedamas.busdriver;

import javax.servlet.ServletException;

/**
 * jtpException is the exception thrown by bus classes and services to signal an architectural error, i.e. an error that can not be corrected by the user.
 * @author 	Carlos Martins
 * @version 	%I%, %G%
 * @since       BUS 1.0
 */
public class busException extends ServletException	{
	static final long serialVersionUID=1;
		/**
		* Creates a new, empty <code>jtpException</code>.
		*/
		public busException()	{
			super();
		}
		/**
		* Creates a new <code>jtpException</code> with an explanation message.
		*
		* @param message error explanation message
		*/
		public busException(String message)	{
			super(message);
		}
		/**
		* Creates a new <code>jtpException</code> with an explanation message and cause.
		*
		* @param message error explanation message.
		* @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
		*/
		public busException(String message, Throwable cause)	{
			super(message, cause);
		}
}

