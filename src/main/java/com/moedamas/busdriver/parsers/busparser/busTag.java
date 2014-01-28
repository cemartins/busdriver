package com.moedamas.busdriver.parsers.busparser;

// Implements a text block (similar to HTML blocks) within a TextBuffer
// The block has a begin tag and an end tag. The begin tag may include a set of modifiers

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.moedamas.busdriver.busDriverRequest;
import com.moedamas.busdriver.busException;

// TODO The Repeat modifier of the tag should be given special treatment. It can ve a resolver or a constant and no generic "modifiers"

public class busTag	{
	private String VARBEGIN = "<?";					// the left delimiter of a variable
	private String VAREND = "?>";					// the left delimiter of a variable
	private String tagtext;							// the text inside the block begin tag
	private Pattern repeatPattern;
	private VarResolver repeatResolver = null;
	private int repeatConstant = 0;
	private String blocktext="";					// the text between the block begin and block end tags
	private String trailertext="";					// the text after the block end tag and untill the next tag or end of text
	private Vector childtags;						// other tags inside of this tag
	private TextBlocks blockvars;					// the variables found in the blocktext
	private TextBlocks trailervars;					// the variables found in the trailertext
	private class tagelements implements Enumeration	{
		int currentelement = 0;
		int maxelements;

			public tagelements(int currelements)	{
					maxelements = currelements;
			}
			public boolean hasMoreElements()	{
					if (currentelement < maxelements)
						return true;
					else
						return false;
			}
			public Object nextElement()	{
					return childtags.elementAt(currentelement++);
			}
	}

		// constructor
		public busTag()	{
			childtags = new Vector();
			this.repeatConstant = 1;
			this.repeatPattern = Pattern.compile("repeat\\s*=\\s*",Pattern.CASE_INSENSITIVE);
		}
		// constructor
		public busTag(String tagtext) throws busException	{
				this();
				this.tagtext = tagtext;
				if(tagtext.indexOf("=") == -1)
					this.repeatConstant = 1;
				else
					parseModifiers();
		}

		// PUBLIC MEMBERS

		// sets the Block text (the text next to the opening tag and untill the next tag).
		public void setBlockText(String blocktext)	throws busException {

			this.blocktext = blocktext;
			blockvars = new TextBlocks(blocktext, VARBEGIN, VAREND);
			blockvars.lookupResolvers();
		}

		/**
		 * returns the Block text (the text next to the opening tag and untill the next tag).
		 */
		public String getBlockText()	{
				return blocktext;
		}

		/**
		 * returns the enumeration of the variables in the block text
		 */
		public Enumeration blockVars()	{
				return blockvars.elements();
		}
		/**
		 * returns the block text after substituting the variables by the values in the received vector
		 */
		public String substBlockVars(Vector values)	{
				return substVars(blocktext, blockvars, values);
		}
		/**
		 * Returns the block text after obtaining the values that correspond to their respective methods 
		 * @param request
		 * @param index
		 * @return
		 * @throws busException
		 */
		public String resolveBlockVars(busDriverRequest request, int index) throws busException {
			return resolveVars(blocktext, blockvars, request, index);
		}
		/**
		 * sets the Trailer text (the text next to the closing tag and untill the next tag).
		 */
		public void setTrailerText(String trailertext)	throws busException {
			this.trailertext = trailertext;
			trailervars = new TextBlocks(trailertext, VARBEGIN, VAREND);
			trailervars.lookupResolvers();
		}
		// returns the Trailer text (the text next to the closing tag and untill the next tag).
		public String getTrailerText()	{
				return trailertext;
		}
		// returns the enumeration of the variables in the trailer text
		public Enumeration TrailerVars()	{
				return trailervars.elements();
		}
		// returns the trailer text after substituting the variables by the values in the received vector
		public String substTrailerVars(Vector values)	{
				return substVars(trailertext, trailervars, values);
		}
		/**
		 * Returns the block text after obtaining the values that correspond to their respective methods 
		 * @param request
		 * @param index
		 * @return
		 * @throws busException
		 */
		public String resolveTrailerVars(busDriverRequest request, int index) throws busException {
			return resolveVars(trailertext, trailervars, request, index);
		}
		// sets the Block text if it is empty. Otherwise sets the trailing text
		public void setBodyOrTrailerText(String text) throws busException	{
				if(blocktext == "")
					setBlockText(text);
				else
					setTrailerText(text);
		}
		//Return this tags child vector
		public Vector getTagsVector()	{
				return childtags;
		}
		// Return an enumeration of this tag child tags
		public Enumeration elements()	{
				return new tagelements(childtags.size());
		}

		// Add a tag to the tags vector
		public void addElement(busTag tag)	{
				childtags.addElement(tag);
		}

		// Return the text in the opening tag
		public String getText()	{
				return tagtext;
		}
		
		public int getRepeatCount(busDriverRequest request, int index) throws busException {
			if(this.repeatResolver == null)
				return this.repeatConstant;
			else {
				int ret = this.repeatResolver.getRepeatValue(request, index);
				return ret;
			}
		}

		// PRIVATE MEMBERS
		
		private void parseModifiers() throws busException {
			Matcher repeatMatcher = repeatPattern.matcher(tagtext);
			if(repeatMatcher.find()) {
				String expression = tagtext.substring(repeatMatcher.end()).trim();
//				expression = expression.replace('"', ' ').trim();
				
				try {
					this.repeatConstant = Integer.parseInt(expression);
				}
				catch (NumberFormatException e)	{
					this.repeatResolver = new VarResolver();
					this.repeatResolver.setRepeatMethod(expression);
				}
			}
			else
				this.repeatConstant = 1;
		}


		/**
		 * used by substBlockVars and substTrailerVars.
		 * returns the block text or the trailer text after substitution of the respective variables.
		 * 
		 */
		private String substVars(String text, TextBlocks vars, Vector values)	{
			StringBuffer retblock;
			String variable;
			int nvars;
			int i = 0, beginpos = 0, endpos = 0;

				if(vars == null)
					return "";
				nvars= vars.size();
				if(nvars == 0)
					retblock = new StringBuffer(text);
				else	{
					retblock = new StringBuffer();
					while (i < nvars)	{
						beginpos = vars.beginOffsetAt(i);
						retblock.append(text.substring(endpos, beginpos));
						variable = (String)values.elementAt(i);
						if(variable != null)
							retblock.append(variable);
						endpos = vars.endOffsetAt(i) + VAREND.length();
						i++;
					}
					retblock.append(text.substring(endpos));
				}
				return retblock.toString();
		}
		
		private String resolveVars(String text, TextBlocks vars, busDriverRequest request, int index) throws busException {

			VarResolver resolver;
			int i = 0, beginpos = 0, endpos = 0;
			int nvars = vars.size();
			StringBuffer retblock = new StringBuffer();
			String value;
			
			while(i < nvars) {
				beginpos = vars.beginOffsetAt(i);
				retblock.append(text.substring(endpos, beginpos));
				resolver = vars.resolverAt(i);
				value = resolver.getVarValue(request, index);
				retblock.append(value);
				endpos = vars.endOffsetAt(i) + VAREND.length();
				
				i++;
			}
			retblock.append(text.substring(endpos));
			return retblock.toString();
		}

}