/**
 * 
 */
package com.moedamas.busdriver.core;

import javax.servlet.ServletContext;

import com.moedamas.busdriver.busException;

/**
 * @author cemartins
 *
 */
public class ConfigContext extends ConfigMgr {
	
	private ServletContext context;

	/**
	 * 
	 */
	public ConfigContext() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.ConfigMgr#load(javax.servlet.ServletContext, java.lang.String)
	 */
	@Override
	public void load(ServletContext context, String source) throws busException {
		this.context = context;

	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.ConfigMgr#getStringValue(java.lang.String)
	 */
	@Override
	public String getStringValue(String key) {
		return (String)context.getInitParameter(key);
	}

}
