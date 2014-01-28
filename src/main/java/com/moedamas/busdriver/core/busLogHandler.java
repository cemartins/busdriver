/**
 * 
 */
package com.moedamas.busdriver.core;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import javax.servlet.ServletContext;


/**
 * @author cemartins
 *
 */
public class busLogHandler extends Handler {
	
	ServletContext context;

	/**
	 * 
	 */
	public busLogHandler(ServletContext context) {
		super();
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
//	@Override
	public void publish(LogRecord record) {
		context.log(record.getMessage());
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Handler#flush()
	 */
//	@Override
	public void flush() {

	}

	/* (non-Javadoc)
	 * @see java.util.logging.Handler#close()
	 */
//	@Override
	public void close() throws SecurityException {

	}

}
