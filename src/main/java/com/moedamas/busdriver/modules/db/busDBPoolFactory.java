/**
 * 
 */
package com.moedamas.busdriver.modules.db;

import javax.servlet.ServletContext;

import com.moedamas.busdriver.busDriverRequest;
import com.moedamas.busdriver.busException;
import com.moedamas.busdriver.core.ConfigMgr;
import com.moedamas.busdriver.modules.busAbstractModule;
import javax.servlet.http.HttpServletRequest;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author cemartins
 *
 */
public class busDBPoolFactory extends busAbstractModule {

	private DataSource datasource;
	private String datasourceparam;
	private String username;
	private String password;
	private String SQLDuplicate = "";				// SQL state corresponding to the SQLSTate code returned for duplicate row from the SQL driver.
	private String AutoCommit = "";
	private javax.servlet.ServletContext context;

	/**
	 * 
	 */
	public busDBPoolFactory(String moduleRef, ConfigMgr config) {
		super(moduleRef, config);

		datasourceparam = config.getStringValue("datasource");
		username = config.getStringValue("username");
		password = config.getStringValue("password");
		SQLDuplicate = config.getStringValue("SQLDuplicate");
		AutoCommit = config.getStringValue("AutoCommit");
		
	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.modules.busModule#prepareContext(javax.servlet.ServletContext)
	 */
	@Override
	public void prepareContext(ServletContext context) throws busException {

		this.context = context;

		try {
			context.log("busDBPoolFactory init()... ");

			javax.naming.Context initCtx = new InitialContext();
			javax.naming.Context envCtx = (Context) initCtx.lookup("java:comp/env");

//		 	Look up our data source
			datasource = (DataSource) envCtx.lookup(datasourceparam);

			context.log("busDBPoolFactory Datasource read from " + datasourceparam);
			context.log("Datasource = " + datasource.toString());
			context.log("busDBPoolFactory Done! ");
		}
		catch (NamingException e) {
			throw new busException(e.getMessage());
		}

	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.modules.busModule#finishContext(javax.servlet.ServletContext)
	 */
	@Override
	public void finishContext() throws busException {
		// TODO clean up after busPool context

	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.modules.busModule#prepareService(com.moedamas.busdriver.core.busDriverRequest)
	 */
	@Override
	public void prepareService(busDriverRequest request) throws busException {
		try {
			
			context.log("busDBPoolFactory getting connection for username=" + username);
			Connection conn = datasource.getConnection();
			context.log("busDBPoolFactory got connection OK");
			
			
//			... use this connection to access the database ...
			busDB dbconn = new busDB(conn);
			dbconn.setAutoCommit(this.AutoCommit);
			dbconn.setSQLDuplicate(this.SQLDuplicate);
			request.addValue(moduleRef, dbconn);
		}
		catch (SQLException e) {
			throw new busException(e.getMessage());
		}

	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.modules.busModule#finishService(com.moedamas.busdriver.core.busDriverRequest)
	 */
	@Override
	public void finishService(busDriverRequest request, boolean success) throws busException {

		busDB dbconn = (busDB) request.getValue(moduleRef);
		request.removeValue(moduleRef);
		try {
			Connection conn = dbconn.getConnection();
			dbconn = null;
			conn.close();
		} catch (SQLException e) {
			throw new busException(e.getMessage());
		}
	}

}
