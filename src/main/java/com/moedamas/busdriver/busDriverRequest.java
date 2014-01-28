/**
 * This class is a merge between the old busData datagram and a HttpServletRequestWrapper.
 * Values are stored in a Stack object, so one key can have many values.
 * 
 */
package com.moedamas.busdriver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Stack;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.moedamas.busdriver.modules.db.busDB;

//TODO Resolver problema no acesso aos dados. Ver os casos em que getAttribute n‹o devolve um Stack (quando n‹o h‡ tratamento do request do cliente)

/**
 * @author cemartins
 *
 */
public class busDriverRequest extends HttpServletRequestWrapper {
	private ServletContext context;
	private Locale locale;
	private String DateFmt = "yyyy-MM-dd";	// default date format
	private String NumFmt  = "#,###.##";	// default number format
	private Vector addedCookies;

	/**
	 * Reads all parameters sent in the request and stores their values in Key-Stack pairs (one key - many values).
	 * @param request an instance of HttpServletRequest sent by the client
	 */
	public busDriverRequest(ServletContext context, HttpServletRequest request) {

		super(request);

		this.context = context;
		this.addedCookies = new Vector();
		Enumeration attrset;
		String attribute;
		String[] values;

			attrset= request.getParameterNames();
			while (attrset.hasMoreElements()) {
				attribute = (String) attrset.nextElement();
				values = request.getParameterValues(attribute);
				if(values == null)
					addValue(attribute, "");		// Set the atribute value to an empty string - all attributes must have values
				else {
	 				int l = values.length;
					int i = 0;
					while(i < l) {
						addValue(attribute, values[i]);		// Add the values of the parameter to the request datagram
						i++;
					}
				}
			}

	}

	public void addValue(String key, Object value)	{
		Stack valueSet;

			if(value != null) {
				key = key.toUpperCase();
				valueSet = (Stack) getAttribute(key);
				if(valueSet == null)	{
					valueSet = new Stack();
					setAttribute(key, valueSet);
				}
				valueSet.add(value);
			}
	}
	/**
	* Sets the value of a key. If the key does not yet exist inside this jtpData object, a new key is
	* created with this value. If the key already exists, removes all existing values and stores this value as the first element.
	*
	* @param   key       the name of the key to be created or appended.
	* @param   value     the value to store in the key.
	*/
	public void setValue(String key, Object value)	{
		Stack valueSet;

			if(value != null) {
				key = key.toUpperCase();
				valueSet = new Stack();
				setAttribute(key, valueSet);
				valueSet.add(value);
			}
	}
	/**
	* Removes a key (attribute) and all associated values.
	*
	* @param   key       the name of the key to be deleted.
	*/
	public void removeValue(String key) {
		Object value;
		Stack node;

			key = key.toUpperCase();
			value = getAttribute(key);
			if (value != null)	{
				node = (Stack)value;
				node.removeAllElements();
			}
			removeAttribute(key);
	}

	/**
	* Removes a value from a key (attribute).
	*
	* @param   key       the name of the key.
	* @param   idx       the name of the key to be deleted.
	*/
	public void removeValue(String key, int idx)  throws busException	{
		Object value;
		Stack node;

			key = key.toUpperCase();
			value = getAttribute(key);
			if (value != null)	{
				node = (Stack)value;
				node.remove(idx);
				if(node.isEmpty())
					removeAttribute(key);
			}
	}


	/**
	* Returns the number of values associated with a key (attribute).
	*
	* @param   key       the name of the key.
	* @return  Number of values associated with the key.
	*/
	public int getNumValues(String key)	{
		Object value;
		Stack node;

			key = key.toUpperCase();
			value = getAttribute(key);
			if (value != null)	{
				node = (Stack) value;
				return node.size();
			}
			return 0;
	}

	/**
	 * Returns whether the key value has some text or is empty (the key value is of type String).
	 *
	 * @param   key       the name of the key.
	 * @return  1 if the key has something or 0 is the key is empty.
     * @throws  jtpException if the index is not valid for the key.
	 */
	public int HasValues(String key) throws busException	{
		Object value;
		String str;
		int hasContent=0;
		
		key = key.toUpperCase();
		value = getAttribute(key);
		if (value != null)	{
			Stack node = (Stack) value;
			try {
				str = (String) node.get(0);
				if(str != null)
					if(str.length() > 0)
						hasContent = 1;
			}
			catch(ArrayIndexOutOfBoundsException e) {
				throw new busException("busData.notEmpty(" + key + ") Exception.", e);
			}
		}
		return hasContent;
	}

	/**
	 * Returns whether the key value is equal to the value passed (the key value is of type String).
	 * The string comparison is case insensitive.
	 *
	 * @param   key         the name of the key.
	 * @param   index       the index within the vector of values associated with the key.
	 * @param   strCompare  the string to compare to the value of the key
	 * @return  1 if the key value is equal to the strCompare string value or 0 otherwise.
	 * @throws  jtpException if the index is not valid for the key.
	 */
	public int isEqual(String key, int index, String strCompare) throws busException	{
		Object value;
		String str;
		Stack node;
		int sameContent=0;
		
		key = key.toUpperCase();
		value = getAttribute(key);
		if (value != null)	{
			node = (Stack) value;
			try {
				str = (String) node.get(index);
				if(str != null)
					if(str.equalsIgnoreCase(strCompare))
						sameContent = 1;
			}
			catch(ArrayIndexOutOfBoundsException e) {
				throw new busException("busData.notEmpty(" + key + ", " + index + ") Exception. There are only " + node.size() + " values.", e);
			}
		}
		return sameContent;
	}

	
	/**
	* Returns the first value (index = 0) associated with a key (attribute).
	* same as <code>value(key, 0)</code>.
	*
	* @param   key       the name of the key.
	* @return  First value associated with the key.
	*/
	public Object getValue(String key)	{
		Object value;
		Stack node;

			key = key.toUpperCase();
			value = getAttribute(key);
			if (value != null)	{
				node = (Stack) value;
				value = node.firstElement();
			}
			return value;
	}

	public Object getValue(String key, int index) throws busException	{
		Object value;
		Stack node;

			key = key.toUpperCase();
			value = getAttribute(key);
			if (value != null)	{
				node = (Stack) value;
				try {
					value = node.get(index);
				}
				catch(ArrayIndexOutOfBoundsException e) {
					throw new busException("busData.value(" + key + ", " + index + ") Exception. There are only " + node.size() + " values.", e);
				}
			}
			return value;
	}

	/**
	* Returns a vector with all the values associated with a key (attribute).
	*
	* @param   key       the name of the key.
	* @return  Vector of all the values associated with the key.
	*/
	public Vector getValues(String key)	{
		Object value;
		Stack node;

			key = key.toUpperCase();
			value = getAttribute(key);
			if (value != null)	{
				node = (Stack)value;
				return node;
			}
			return null;
	}

	/**
	* Returns the date representation of the value at index <code>index</code> associated with a key (attribute),
	* according to the default jtpData's date format or to the format set by <code>setDateFormat()</code>.<br>
	* The value adressed should be of type <code>Date</code>.<br>
	* Index = 0 returns the first value. Index = (length - 1) returns the last value of the key.
	*
	* @param   key       the name of the key
	* @param   index     the index within the vector of values associated with the key.
	* @return  Date value associated with the key at index.
	* @throws  jtpException if the index is not valid for the key.
	*/
	public Object getDateValue(String key, int index) throws busException	{
		Object value;

			value = getValue(key, index);
			if (value != null)	{
				try {
					value = new SimpleDateFormat(this.DateFmt, locale).format(value);
				}
				catch(Exception e) {
					throw new busException("dateValue: " + e.getMessage(), e);
				}
			}
			return value;
	}

	/**
	* Returns the date representation of the value at index <code>index</code> associated with a key (attribute),
	* according to the format specified by the parameter <code>DateFmt</code>.<br>
	* The value adressed should be of type <code>Date</code>.<br>
	* Index = 0 returns the first value. Index = (length - 1) returns the last value of the key.
	* <br>
	* Date format can be specified using the following codes:
	* <p><code><table>
	*	 <tr><td>Symbol</td><td>Meaning</td><td>Presentation</td><td>Example</td></tr>
	*	 <tr><td>------</td><td>-------</td><td>------------</td><td>-------</td></tr>
	*	 <tr><td>G</td><td>era designator</td><td>(Text)</td><td>AD</td></tr>
	*	 <tr><td>y</td><td>year</td><td>(Number)</td><td>1996</td></tr>
	*	 <tr><td>M</td><td>month in year</td><td>(Text & Number)</td><td>July & 07</td></tr>
	*	 <tr><td>d</td><td>day in month</td><td>(Number)</td><td>10</td></tr>
	*	 <tr><td>h</td><td>hour in am/pm (1~12)</td><td>(Number)</td><td>12</td></tr>
	*	 <tr><td>H</td><td>hour in day (0~23)</td><td>(Number)</td><td>0</td></tr>
	*	 <tr><td>m</td><td>minute in hour</td><td>(Number)</td><td>30</td></tr>
	*	 <tr><td>s</td><td>second in minute</td><td>(Number)</td><td>55</td></tr>
	*	 <tr><td>S</td><td>millisecond</td><td>(Number)</td><td>978</td></tr>
	*	 <tr><td>E</td><td>day in week</td><td>(Text)</td><td>Tuesday</td></tr>
	*	 <tr><td>D</td><td>day in year</td><td>(Number)</td><td>189</td></tr>
	*	 <tr><td>F</td><td>day of week in month</td><td>(Number)</td><td>2 (2nd Wed in July)</td></tr>
	*	 <tr><td>w</td><td>week in year</td><td>(Number)</td><td>27</td></tr>
	*	 <tr><td>W</td><td>week in month</td><td>(Number)</td><td>2</td></tr>
	*	 <tr><td>a</td><td>am/pm marker</td><td>(Text)</td><td>PM</td></tr>
	*	 <tr><td>k</td><td>hour in day (1~24)</td><td>(Number)</td><td>24</td></tr>
	*	 <tr><td>K</td><td>hour in am/pm (0~11)</td><td>(Number)</td><td>0</td></tr>
	*	 <tr><td>z</td><td>time zone</td><td>(Text)</td><td>Pacific Standard Time</td></tr>
	*	 <tr><td>'</td><td>escape for text</td><td>(Delimiter)</td></td><td></tr>
	*	 <tr><td>''</td><td>single quote</td><td>(Literal)</td><td>'</td></tr>
	* </table></code>
	* @param   key       the name of the key
	* @param   DateFmt   the date format to use
	* @param   index     the index within the vector of values associated with the key.
	* @return  Date value associated with the key at index.
	* @throws  jtpException if the index is not valid for the key.
	*/
	public Object getDateValue(String key, String DateFmt, int index) throws busException	{
		Object value;
		String fmtValue=null;

			value = getValue(key, index);
			if (value != null)	{
				try {
					fmtValue = new SimpleDateFormat(DateFmt, locale).format(value);
				}
				catch(Exception e) {
					throw new busException("dateValue: " + e.getMessage(), e);
				}

				// verifica se a formatacao esta englobada entre aspas (caso de ser despoletada por um template) e se sim, retira as aspas.
				if(fmtValue.startsWith("\"") && fmtValue.endsWith("\""))
					fmtValue = fmtValue.substring(1, fmtValue.length() - 1);

			}
			return fmtValue;
	}


	// get the number representation on a key's next value (the key's value should be a double or a long).
	/**
	* Returns the number representation of the value at index <code>index</code> associated with a key (attribute),
	* according to the default jtpData's number format or to the format set by <code>setNumberFormat()</code>.<br>
	* The value adressed should be of type <code>double</code>.<br>
	* Index = 0 returns the first value. Index = (length - 1) returns the last value of the key.
	*
	* @param   key       the name of the key
	* @param   index     the index within the vector of values associated with the key.
	* @return  Number value associated with the key at index.
	* @throws  jtpException if the index is not valid for the key.
	*/
	public Object getNumberValue(String key, int index) throws busException	{
		Object value;

			value = getValue(key, index);
			if (value != null)	{
				try {
					value = new DecimalFormat(this.NumFmt).format(value);
				}
				catch(Exception e) {
					throw new busException("numberValue: " + e.getMessage(), e);
				}
			}
			return value;
	}


	/**
	* Returns the number representation of the value at index <code>index</code> associated with a key (attribute),
	* according to the format specified by the parameter <code>NumFmt</code>.<br>
	* The value adressed should be of type <code>double</code>.<br>
	* Index = 0 returns the first value. Index = (length - 1) returns the last value of the key.
	*
	* @param   key       the name of the key
	* @param   NumFmt    the number format to use
	* @param   index     the index within the vector of values associated with the key.
	* @return  Number value associated with the key at index.
	* @throws  jtpException if the index is not valid for the key.
	*/
	public Object getNumberValue(String key, String NumFmt, int index) throws busException	{
		Object value;
		String fmtValue=null;

			value = getValue(key, index);
			if (value != null)	{
				try {
					fmtValue = new DecimalFormat(NumFmt).format(value);
				}
				catch(Exception e) {
					throw new busException("numberValue: " + e.getMessage(), e);
				}

				// verifica se a formatacao esta englobada entre aspas (caso de ser despoletada por um template) e se sim, retira as aspas.
				if(fmtValue.startsWith("\"") && fmtValue.endsWith("\""))
					fmtValue = fmtValue.substring(1, fmtValue.length() - 1);

			}
			return value;
	}
	
	public ServletContext getContext() {
		return this.context;
	}

	public busDB getDefaultDb() {

		String defaultdbmodule = this.context.getInitParameter("defaultdbmodule");
		busDB busdb = (busDB) getValue(defaultdbmodule);
		
		return busdb;
	}
	
	/**
	 * @return Returns the locale.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale The locale to set.
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	/**
	* Performs a select query with the database connection and creates attributes in the structure to
	* store the results. An attribute will be created with the same name as the columns selected in the
	* query and values for these attributes will be added for each row returned by the query.
	* <p>
	* Example:<br>
	* <code>dbQuery("Select NTA_TITLE, NTA_TEXT FROM NEWS")</code>  will create 2 keys (NTA_TITLE and NTA_TEXT) and
	* each key will have as many values as the rows retuned by the query.<br>
	* If the keys already exist in the datagram they will be deleted prior.
	*
	* @param   query 	the SQL query to perform on the database connection.
	* @return  the number of rows returned by the query.
	*/
	public int dbExecuteQuery(String dbmodule, String query, boolean addRows) throws busException	{
		Statement stmt = null;
		ResultSet rs = null;
		ResultSetMetaData rsMeta = null;
		int columnIdx, rowCount;


			// verifica se a query esta englobada entre aspas (caso de ser despoletada por um template) e se sim, retira as aspas.
			if(query.startsWith("\"") && query.endsWith("\""))
				query = query.substring(1, query.length() - 1);

			// if the class was not initialized with a database connection then cannot proceed.
			busDB busdb = (busDB) getValue(dbmodule);
			if(busdb == null)
				throw new busException("busDriverRequest.dbQuery(): dbconnection not found under the attribute " + dbmodule);

			try	{
				Connection DBconn = busdb.getConnection();
				stmt = DBconn.createStatement();	// creates SQL statement
				rs = stmt.executeQuery(query);		// execute query
				rsMeta = rs.getMetaData();			// get result meta data
				rowCount = 0;
				if(addRows == false) {
					// clear all the keys if they exist
					columnIdx = 1;
					while(columnIdx <= rsMeta.getColumnCount()) {
						removeValue(rsMeta.getColumnName(columnIdx));
						columnIdx++;
					}
				}
				while(rs.next())	{				// sets the keys (column names) and values for all
					columnIdx = 1;					// columns and all rows returned.
					while(columnIdx <= rsMeta.getColumnCount()) {
						addValue(rsMeta.getColumnName(columnIdx), rs.getObject(columnIdx));
						columnIdx++;
					}
					rowCount++;
				}
				rs.close();
			}
			catch (SQLException E) {
				throw new busException("jtpData.dbQuery(" + query + ")\nSQLException: " + E.getMessage() + "\nSQLState:     " + E.getSQLState() + "\nVendorError:  " + E.getErrorCode(), E);
			}
			//add("LOG", "dbQuery: Ok - rowCount = " + rowCount);
			return(rowCount);
	}


	public int dbQuery(String dbmodule, String query) throws busException	{
		
		return dbExecuteQuery(dbmodule, query, false);
	}
	public int dbQueryAdd(String dbmodule, String query)  throws busException 	{
		
		return dbExecuteQuery(dbmodule, query, true);
	}
	
	public Cookie getCookie(String cookieName) {
		
		for (Cookie cookie : this.getCookies())
			if(cookieName.equals(cookie.getName()))
				return cookie;
		return null;
	}
	
	public String getCookieValue(String cookieName) {
		
		Cookie cookie = this.getCookie(cookieName);
		if(cookie == null)
			return "";
		else
			return cookie.getValue();
	}
	
	public void addCookie(Cookie cookie) {
		this.addedCookies.add(cookie);
	}
	
	public void addCookie(String cookieName, String cookieValue) {
		
		Cookie cookie = new Cookie(cookieName, cookieValue);
		addCookie(cookie);
	}
	
	public Vector getAddedCookies() {
		return this.addedCookies;
	}
	
	/**
	* Receives a text string and returns the HTML equivalent of the String <code>src</code> passed.<br>
	*
	* @param   src       a Java UTF-8 String to be converted.
	* @return  HTML equivalent of the string.
	* @throws  jtpException if the index is not valid for the key.
	*/
	public static String getHTML(String src) {
		src = src.replaceAll("&", "&amp;");
		src = src.replaceAll("»", "&ordf;");
		src = src.replaceAll("¼", "&ordm;");	
		src = src.replaceAll("¡", "&deg;");
		src = src.replaceAll(">", "&gt;");
		src = src.replaceAll("<", "&lt;");
		src = src.replaceAll("Ö", "&divide;");
		src = src.replaceAll("-", "&shy;");
		src = src.replaceAll("¨", "&reg;");
		src = src.replaceAll("ª", "&trade;");
		src = src.replaceAll("©", "&copy;");
		src = src.replaceAll("Û", "&euro;");
		src = src.replaceAll("£", "&pound;");
		src = src.replaceAll("´", "&yen;");
		src = src.replaceAll("¢", "&cent;");
		src = src.replaceAll("¦", "&para;");
		src = src.replaceAll("¤", "&sect;");
		src = src.replaceAll("Õ", "&rsquo;");
		src = src.replaceAll("'", "&rsquo;");
		src = src.replaceAll("Ô", "&lsquo;");
		src = src.replaceAll("Ç", "&laquo;");
		src = src.replaceAll("È", "&raquo;");
		src = src.replaceAll("Ò", "&ldquo;");
		src = src.replaceAll("Ó", "&rdquo;");
		src = src.replaceAll("\"", "&quot;");
		src = src.replaceAll("‚", "&Ccedil;");
		src = src.replaceAll("", "&ccedil;");
		src = src.replaceAll("ç", "&Aacute;");
		src = src.replaceAll("‡", "&aacute;");
		src = src.replaceAll("Ì", "&Atilde;");
		src = src.replaceAll("‹", "&atilde;");
		src = src.replaceAll("å", "&Acirc;");
		src = src.replaceAll("‰", "&acirc;");
		src = src.replaceAll("Ë", "&Agrave;");
		src = src.replaceAll("ˆ", "&agrave;");
		src = src.replaceAll("î", "&Oacute;");
		src = src.replaceAll("—", "&oacute;");
		src = src.replaceAll("Í", "&Otilde;");
		src = src.replaceAll("›", "&otilde;");
		src = src.replaceAll("ï", "&Ocirc;");
		src = src.replaceAll("™", "&ocirc;");
		src = src.replaceAll("ƒ", "&Eacute;");
		src = src.replaceAll("Ž", "&eacute;");
		src = src.replaceAll("æ", "&Ecirc;");
		src = src.replaceAll("", "&ecirc;");
		src = src.replaceAll("ê", "&Iacute;");
		src = src.replaceAll("’", "&iacute;");	
		src = src.replaceAll("ò", "&Uacute;");
		src = src.replaceAll("œ", "&uacute;");	
		return src;
	}
	
	/**
	* Returns the first value (index = 0) associated with a key (attribute).
	* same as <code>value(key, 0)</code>.
	*
	* @param   key       the name of the key.
	* @return  First value associated with the key.
	*/
	public Object getHtmlValue(String key)	{
		Object value;
		Stack node;

			key = key.toUpperCase();
			value = getAttribute(key);
			if (value != null)	{
				node = (Stack) value;
				value = getHTML((String) node.firstElement());
			}
			return value;
	}
	
	/**
	* Returns the value at index <code>index</code> associated with a key (attribute).<br>
	* Index = 0 returns the first value. Index = (length - 1) returns the last value of the key.
	*
	* @param   key       the name of the key
	* @param   index     the index within the vector of values associated with the key.
	* @return  Value associated with the key at index.
	* @throws  jtpException if the index is not valid for the key.
	*/
	public Object getHtmlValue(String key, int index) throws busException	{
		Object value;
		Stack node;

			key = key.toUpperCase();
			value = getAttribute(key);
			if (value != null)	{
				node = (Stack) value;
				try {
					value = getHTML((String) node.elementAt(index));
				}
				catch(ArrayIndexOutOfBoundsException e) {
					throw new busException("jtpData.value(" + key + ", " + index + ") Exception. There are only " + node.size() + " values.", e);
				}
			}
			return value;
	}

}
