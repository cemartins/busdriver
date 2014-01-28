package com.moedamas.busdriver.modules;

import javax.servlet.ServletContext;

import com.moedamas.busdriver.busDriverRequest;
import com.moedamas.busdriver.busException;
import com.moedamas.busdriver.core.ConfigMgr;

abstract public class busAbstractModule {
	
	protected String moduleRef;
	
	public busAbstractModule(String moduleRef, ConfigMgr config) {
		this.moduleRef = moduleRef;
	}

	public void setModuleRef(String moduleRef) {
		this.moduleRef = moduleRef;
	}
	public String getModuleRef() {
		return this.moduleRef;
	}
	
	/**
	 * Method called when the web application has just been started
	 * @param context Web application contest
	 */
	abstract public void prepareContext(ServletContext context) throws busException;
	
	/**
	 * Method called when the web application is going to end
	 * @param context Web application contest
	 */
	abstract public void finishContext() throws busException;
	
	/**
	 * Method called by the modules filter before the destination servlet is called
	 * @param request Web application request
	 */
	abstract public void prepareService(busDriverRequest request) throws busException;
	
	/**
	 * Method called by the modules filter before the destination servlet is called
	 * @param request Web application request
	 */
	abstract public void finishService(busDriverRequest request, boolean success) throws busException;
}
