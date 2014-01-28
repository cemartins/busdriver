package com.moedamas.busdriver.parsers.busparser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.moedamas.busdriver.busDriverRequest;
import com.moedamas.busdriver.busException;
import com.moedamas.busdriver.modules.busAbstractModule;



/**
 * @author cemartins
 * This class represents the method to call in busDriverRequest to resolve a variable found in the template
 * It holds the method and the parameters defined in the template
 *
 */
public class VarResolver {

	Method invoquemethod;
	Object[] arguments;
	int indexArgumentPos;
	Pattern varPattern;

	public VarResolver() {
		super();
		this.indexArgumentPos = 0;
//		this.varPattern = Pattern.compile("<?\\S+?>",Pattern.CASE_INSENSITIVE);
		this.varPattern = Pattern.compile("\\x3C\\x3F\\S+\\x3F\\x3E",Pattern.CASE_INSENSITIVE);

	}
	
	public VarResolver(String descmethod) throws busException {
		this();
		setVarMethod(descmethod);
	}
	
	public String getVarValue(busDriverRequest request, int index) throws busException {
		String result = "";		
		
			try {
				arguments[indexArgumentPos] = index;
				Object object = invoquemethod.invoke(request, arguments);
				if(object != null)
					result = (String) object; 
			} catch (IllegalArgumentException e) {
				throw new busException("VarResolver.getValue(): Illegal argument: " + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new busException("VarResolver.getValue(): IllegalAccessException: " + e.getMessage());
			} catch (InvocationTargetException e) {
				throw new busException("VarResolver.getValue(): InvocationTargetException: " + e.getMessage());
			}
		return result;
	}
	
	public int getRepeatValue(busDriverRequest request, int index) throws busException {
		int result = 0;
		
		try {
			Matcher argMatcher;
			String arg, var, value;
			int i = 0;
			while (i < this.arguments.length) {
				arg = (String)this.arguments[i];
				argMatcher = varPattern.matcher(arg);
				while(argMatcher.find()) {
					var = arg.substring(argMatcher.start() + 2, argMatcher.end() - 2);
					value = (String)request.getValue(var, index);
					if(value == null)
						value = "";
					this.arguments[i] = argMatcher.replaceFirst(value);
				}
				i++;
			}

			result = (Integer) invoquemethod.invoke(request, arguments);
		} catch (IllegalArgumentException e) {
			throw new busException("VarResolver.getValue(): Illegal argument: " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new busException("VarResolver.getValue(): IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new busException("VarResolver.getValue(): InvocationTargetException: " + e.getMessage());
		}
		return result;
	}
	
	public void setVarMethod(String descmethod) throws busException {
		Class[] parameterTypes;
		String param;
		TextBlocks parameters;
		int i, j;

		i = descmethod.indexOf("(");
		if(i == -1)	{
			parameterTypes = new Class[] {String.class, int.class};
			indexArgumentPos = 1;
			try {
				invoquemethod = busDriverRequest.class.getMethod("getValue", parameterTypes);
				arguments = new Object[] {descmethod, null};
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		else	{
			// yes, there is a function to invoke - value the funtion name
			String methodname = descmethod.substring(0, i);

			// value the funtion parameters
			TextBlocks expression = new TextBlocks(descmethod, "(", ")", '"', '"');
			// check if there are nested functions
			if (expression.size() > 1)	{
				// yes, there is another funtion as the parameter for this funtion - call recursive.

/*				param = (String)invoke(expression.textAt(0), index);
				if (param == null)
					throw new busException("busData: expression \"" + descmethod + "\" did not produce any value");
*/
				throw new busException("VarResolver.setVarMethod(): nested functions not supported: " + descmethod);
			}
			else {
				// there's no other function as the parameter - check how many parameters there are
				// Tokenize all the parameters passed
				param = expression.textAt(0);
			}

			param = "," + param + ",";
			parameters = new TextBlocks(param, ",", ",", '"', '"');

			parameterTypes = new Class[parameters.size() + 1];
			arguments = new Object[parameters.size() + 1];
			i = 0;
			j = 0;
			while(i < parameters.size())	{

				param = parameters.textAt(i).trim();
				if(param.length() > 0)	{
					
					try	{	// check wheather the value is an iteger.
						arguments[j] = Integer.valueOf(param);
						parameterTypes[j] = Integer.class;
					}
					catch (NumberFormatException e)	{	// the value is not an Integer - it's a string.
						parameterTypes[j] = String.class;
						arguments[j] = param;
					}

					j++;
				}
				i++;
			}
			parameterTypes[j] = int.class;  // add the last parameter refering to the index of the attribute to retrieve
			indexArgumentPos = j;

			try {
				invoquemethod = busDriverRequest.class.getMethod(methodname, parameterTypes);
			}
			catch (NoSuchMethodException e) {
				param = "VarResolver.setVarMethod(): has no such method: " + descmethod + " - Exception " + e.getMessage();
				throw new busException(param, e);
			}
		}

	}

	public void setRepeatMethod(String descmethod) throws busException {
		Class[] parameterTypes;
		String param;
		TextBlocks parameters;
		int i, j;

		i = descmethod.indexOf("(");
		if(i == -1)	{
			parameterTypes = new Class[] {String.class};
			try {
				invoquemethod = busDriverRequest.class.getMethod("getNumValues", parameterTypes);
				arguments = new Object[] {descmethod};
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		else	{
			// yes, there is a function to invoke - value the funtion name
			String methodname = descmethod.substring(0, i);

			// value the funtion parameters
			TextBlocks expression = new TextBlocks(descmethod, "(", ")", '"', '"');
			// check if there are nested functions
			if (expression.size() > 1)	{
				// yes, there is another funtion as the parameter for this funtion - call recursive.

/*				param = (String)invoke(expression.textAt(0), index);
				if (param == null)
					throw new busException("busData: expression \"" + descmethod + "\" did not produce any value");
*/
				throw new busException("VarResolver.setRepeatMethod(): nested functions not supported: " + descmethod);
			}
			else {
				// there's no other function as the parameter - check how many parameters there are
				// Tokenize all the parameters passed
				param = expression.textAt(0);
			}

			param = "," + param + ",";
			parameters = new TextBlocks(param, ",", ",", '"', '"');

			parameterTypes = new Class[parameters.size()];
			arguments = new Object[parameters.size()];
			i = 0;
			j = 0;
			while(i < parameters.size())	{
				param = parameters.textAt(i).trim();
				if(param.length() > 0)	{
					parameterTypes[j] = String.class;
					arguments[j] = param;
					j++;
				}
				i++;
			}

			try {
				invoquemethod = busDriverRequest.class.getMethod(methodname, parameterTypes);
			}
			catch (NoSuchMethodException e) {
				param = "VarResolver.setRepeatMethod(): has no such method: " + descmethod + " - Exception " + e.getMessage();
				throw new busException(param, e);
			}
		}

	}
}
