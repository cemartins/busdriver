package com.moedamas.busdriver.modules.db;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import com.moedamas.busdriver.busException;


// TODO Construtor que recebe connection e c—digo de erro "duplicate row"

/*  busDB
 *  This class manages a database connection attached to a user session.
 * @author 	Carlos Martins
 * @version 	%I%, %G%
 * @since       BUS 1.0
*/
public class busDB {

	private java.sql.Connection DBconn = null;		// database connection for select queries.
	private String SQLDuplicate = "";				// SQL state corresponding to the SQLSTate code returned for duplicate row from the SQL driver.
	private boolean AutoCommit = true;


		// Contructor
		// Initializes the class with no database connection
		public busDB(java.sql.Connection dbconn) throws busException	{
			this.DBconn = dbconn;
		}

		
		/**
		 * @return Returns the autoCommit.
		 */
		public boolean isAutoCommit() {
			return AutoCommit;
		}

		/**
		 * @param autoCommit The autoCommit to set.
		 */
		public void setAutoCommit(boolean autoCommit) {
			AutoCommit = autoCommit;
			try {
				DBconn.setAutoCommit(true);
			}
			catch(SQLException e) {
				DBconn = null;
			}
		}
		/**
		 * @param autoCommit The autoCommit to set.
		 */
		public void setAutoCommit(String autoCommit) {

			if(autoCommit == null || autoCommit.equalsIgnoreCase("yes") || autoCommit.equalsIgnoreCase("true") || autoCommit.equals("1"))
				setAutoCommit(true);
			else
				setAutoCommit(false);
		}

		/**
		 * @return Returns the dBconn.
		 */
		public java.sql.Connection getConnection() {
			return DBconn;
		}


		/**
		 * @param bconn The dBconn to set.
		 */
		public void setConection(java.sql.Connection dbconn) {
			DBconn = dbconn;
		}

		/**
		 * @return Returns the sQLDuplicate.
		 */
		public String getSQLDuplicate() {
			return SQLDuplicate;
		}

		/**
		 * @param duplicate The sQLDuplicate to set.
		 */
		public void setSQLDuplicate(String duplicate) {
			SQLDuplicate = duplicate;
		}


		/**
		 * Prepares a string containing text to be inserted into a database register.<br>
		 * All apostrofes and commas that the text has will be preceded by the character '\' so as to be accepted by the database system.
		 *
		 * @param  txt A string containing text to prepare for insert or update in a database register.
		 * @return the received string good to be inserted in the database.
		 */
		public static String getDbString(String txt) {
			StringBuffer scan;
			int offset;

				if(txt == null || txt.length() == 0)
					return "";

				scan = new StringBuffer(txt);
				offset = scan.indexOf("'");
				while(offset != -1) {
					scan.insert(offset, '\\');
					offset = scan.indexOf("'", offset + 2);
				}
				return scan.toString();
		}

		// Executes a genï¿½ric SQL statement (SELECT) and returns the result set obtained.
		// The caller of this function must later close the result set through (Resultset.close().
		public ResultSet dbQuery(String query) throws busException	{
			Statement stmt = null;
			ResultSet rs = null;

				if (DBconn == null) {
					throw new busException("busDB.dbQuery(): Database connection not valid.");
				}

				try	{
					stmt = DBconn.createStatement();	// creates SQL statement
					rs = stmt.executeQuery(query);			// execute query
				}
				catch (SQLException E) {
					throw new busException("busDB.dbQuery(" + query + ")\nSQLException: " + E.getMessage() + "\nSQLState:     " + E.getSQLState() + "\nVendorError:  " + E.getErrorCode(), E);
				}
				return(rs);
		}



		public boolean dbQueryInsert(String query) throws busException	{
			Statement stmt = null;
			boolean SQLState;

				SQLState = true;
				if (DBconn == null) {
					throw new busException("busDB.dbQueryInsert(): Database connection not valid.");
				}

				try	{
					stmt = DBconn.createStatement();	// creates SQL statement
					stmt.executeUpdate(query);		// execute query
				}
				catch (SQLException E) {
					// Check wheather the exception is because of duplicate row.
					if (SQLDuplicate.equalsIgnoreCase(E.getSQLState()))
						SQLState = false;
					else
						throw new busException("busDB.dbQueryInsert(" + query + ")\nSQLException: " + E.getMessage() + "\nSQLState:     " + E.getSQLState() + "\nVendorError:  " + E.getErrorCode() + "\n(SQLDuplicate=" + SQLDuplicate + ")", E);
				}

				return SQLState;
		}

		public boolean dbQueryUpdate(String query) throws busException	{
			Statement stmt = null;
			boolean SQLState;

				SQLState = true;
				if (DBconn == null) {
					throw new busException("busDB.dbQueryUpdate(): Database connection not valid.");
				}

				try	{
					stmt = DBconn.createStatement();	// creates SQL statement
					stmt.executeUpdate(query);		// execute query
				}
				catch (SQLException E) {
					// Check wheather the exception is because of duplicate row.
					if (SQLDuplicate.equalsIgnoreCase(E.getSQLState()))
						SQLState = false;
					else
						throw new busException("busDB.dbQueryUpdate(" + query + ")\nSQLException: " + E.getMessage() + "\nSQLState:     " + E.getSQLState() + "\nVendorError:  " + E.getErrorCode() + "\n(SQLDuplicate=" + SQLDuplicate + ")", E);
				}
				return SQLState;
		}


		public boolean dbQueryDelete(String query) throws busException	{
			Statement stmt = null;
			boolean SQLState;

				SQLState = true;
				if (DBconn == null) {
					throw new busException("busDB.dbQueryDelete(): Database connection not valid.");
				}

				try	{
					stmt = DBconn.createStatement();	// creates SQL statement
					stmt.executeUpdate(query);		// execute query
				}
				catch (SQLException E) {
					// Check wheather the exception is because of duplicate row.
					if (SQLDuplicate.equalsIgnoreCase(E.getSQLState()))
						SQLState = false;
					else
						throw new busException("busDB.dbQueryDelete(" + query + ")\nSQLException: " + E.getMessage() + "\nSQLState:     " + E.getSQLState() + "\nVendorError:  " + E.getErrorCode() + "\n(SQLDuplicate=" + SQLDuplicate + ")", E);
				}
				return SQLState;
		}

		public void Commit() throws busException	{

				if (AutoCommit == false) {
					if (DBconn == null) {
						throw new busException("busDB.Commit(): Database connection not valid.");
					}

					try	{
						DBconn.commit();
					}
					catch (SQLException E) {
						throw new busException("busDB.Commit()\nSQLException: " + E.getMessage() + "\nSQLState:     " + E.getSQLState() + "\nVendorError:  " + E.getErrorCode() + "\n(SQLDuplicate=" + SQLDuplicate + ")", E);
					}
				}
		}

		public void Rollback() throws busException	{

				if (AutoCommit == false) {
					if (DBconn == null) {
						throw new busException("busDB.Rollback(): Database connection not valid.");
					}

					try	{
						DBconn.rollback();
					}
					catch (SQLException E) {
						throw new busException("busDB.Rollback()\nSQLException: " + E.getMessage() + "\nSQLState:     " + E.getSQLState() + "\nVendorError:  " + E.getErrorCode() + "\n(SQLDuplicate=" + SQLDuplicate + ")", E);
					}
				}
		}

}
