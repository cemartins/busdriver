/**
 * 
 */
package com.moedamas.busdriver.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

import com.moedamas.busdriver.busException;


/**
 * @author cemartins
 *
 */
public class ConfigFile extends ConfigMgr {

	private Properties busProperties;	// Properties containing inicialization parameters read from busDriver.properties file

	/**
	 * 
	 */
	public ConfigFile() {
		super();
		busProperties = new Properties();
	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.ConfigMgr#load(javax.servlet.ServletContext, java.lang.String)
	 */
	public void load(ServletContext context, String source) throws busException {
		String propertiesFile;			// File containing inicialization parameters
		FileInputStream instrm=null;	// Inputstream to read the properties file

			propertiesFile = context.getRealPath(BASEDIR + source);
			context.log("ConfigFile.load(): Reading properties from " + propertiesFile);
			try	{
				instrm = new FileInputStream(propertiesFile);
				busProperties.load(instrm);
				instrm.close();

			}
			catch (SecurityException e) {
//				log("SessionListener.contextInitialized(" + ApplicationName + "): Unable to load busDriver.properties file: " + propertiesFile + " - " + e.getMessage(), e);
				throw new busException (e.getMessage());
			}
			catch (IOException e) {
//				log("SessionListener.contextInitialized(" + ApplicationName + "): Unable to load busDriver.properties file: " + propertiesFile + " - " + e.getMessage(), e);
				throw new busException (e.getMessage());
			}


//			log("contextInitialized(" + ApplicationName + "): Initialization completed successfuly.");

	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.ConfigMgr#getStringValue(java.lang.String)
	 */
	public String getStringValue(String key) {
		
		return busProperties.getProperty(key);
	}

}
