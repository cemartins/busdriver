package com.moedamas.busdriver.core;

import javax.servlet.ServletContext;

import com.moedamas.busdriver.busException;



public abstract class ConfigMgr {
	
	final String BASEDIR = "/WEB-INF/etc/";
	
	/**
	 * Initialize the configuration manager
	 * @param context Servlet context
	 * @param source Source of the configuration data to be handled. This the parameter comes from the web.xml webapp configuration file (plugin section).
	 * @throws busException
	 */
	public abstract void load(ServletContext context, String source) throws busException;

	/**
	 * Get a string value from the configuration reader
	 * @param key attribute name from witch to return a value
	 * @return the string value associated with the attribute, or null if the key is not found
	 */
	public abstract String getStringValue(String key);

}
