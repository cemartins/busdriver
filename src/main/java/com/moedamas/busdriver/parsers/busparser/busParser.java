package com.moedamas.busdriver.parsers.busparser;

// implements a StringBuffer with find, replace and substitute capabilities

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.*;

import com.moedamas.busdriver.busDriverRequest;
import com.moedamas.busdriver.busException;

/**
 * busParser works together with jtpData and it's purpose is to parse template files and merge them with the data
 * contained in the structure inside jtpData.
 * <p>
 * A template file can be any file, though this architecture is only stressly tested with HTML files. The template
 * file contains &lt;jtp&gt; and &lt;/jtp&gt; tags to define blocks that will be parsed.<br>
 * Attributes within these blocks defined by &lt;[ATTNAME]&gt; will be replaced by values in the jtpData object
 * associated with the ATTNAME attribute.
 * @author 	Carlos Martins
 * @version 	%I%, %G%
 * @since       BUS 1.0
 */
public class busParser	{
	private String buffer = "";
	private Pattern BLOCKBEGIN, BLOCKEND;
	private Matcher matchBegin, matchEnd;
	private Pattern JTP_PARENTROW = Pattern.compile("JTP_PARENTROW", Pattern.CASE_INSENSITIVE);
	private Pattern JTP_ROW = Pattern.compile("JTP_ROW", Pattern.CASE_INSENSITIVE);
	private String CharacterEncoding;
	private busTag roottag = null;

		/**
		* Creates a new, empty <code>busParser</code>.
		* @throws  jtpException if an error occurs.
		*/
		public busParser(String CharacterEncoding) throws busException	{

			this.CharacterEncoding = CharacterEncoding;
			/* This is a protyection for distributing tryeal versions */
			/*Calendar limit = Calendar.getInstance();
			Calendar now = Calendar.getInstance();

				limit.set(1999, 11, 31);
				if (now.after(limit))
					throw new jtpException("Java Template Pages is not licenced and the demo period is expired.");
					*/

		}


		/**
		* Creates a new, <code>busParser</code>, loads a file and builds its tag structure making it ready for parsing.
		* @throws  jtpException if an error occurs.
		*/
		public busParser(String CharacterEncoding, String FileName) throws busException	{

			this.CharacterEncoding = CharacterEncoding;
			readFile(FileName);
			setTags("jtp");
		}


	// extended interface

		/**
		* Reads the a template file into the object.
		*
		* @param   filename       full path of the template filename.
		* @throws  jtpException if an error occurs.
		*/
		public void readFile(String filename) throws busException {
			RandomAccessFile raf = null;
			byte[] buf;

				try	{
					// open the file for input
					raf = new RandomAccessFile(filename, "r");

					buf = new byte[(int)raf.length()];
					// read the file into the buffer
					raf.readFully(buf);
					buffer = new String(buf);
				}
				catch(FileNotFoundException e)	{
					throw new busException("busParser.readFile() File not found: " + e.getMessage(), e);
				}
				catch(IOException e)	{
					throw new busException("busParser.readFile() IO Exception: " + e.getMessage(), e);
				}
				finally	{
					try	{
						if(raf != null)
							raf.close();
					}
					catch (Exception e)	{
						throw new busException(e.getMessage(), e);
						}
				}
		}

		/**
		* Writes the a template stored in the object to an OutputStream.
		*
		* @param   out       OutputStream to write the contents of the template file to.
		* @throws  jtpException if an error occurs.
		*/
		public void writeStream(OutputStream out) throws busException	{
			byte[] buf;

				buf = buffer.getBytes();
				try	{
					out.write(buf, 0, buffer.length());
				}
				catch(Exception e)	{
					throw new busException("busParser.readFile() Cannot write to stream: " + e.getMessage(), e);
				}
		}


		/**
		* Builds a tree of blocks found in the template file.<br>
		* Blocks will be defined by <code>&lt;<i>blockid</i> repeat=value||expression&gt; ..... &lt;/<i>blockid</i>&gt;</code>.<br>
		* In the block text there may be other blocks which will be put in the blocks vector of their parent block.<br>
		* After executing this method the template file can be parsed.
		*
		* @param   blockid       Block indentifier.
		* @throws  jtpException if an error occurs (eg. more opening tags than closing tags).
		*/
		public void setTags(String blockid) throws busException	{
			String regexp;

				roottag = new busTag();

				regexp = "<" + blockid.toUpperCase();
				this.BLOCKBEGIN = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
				regexp = "</" + blockid.toUpperCase();
				this.BLOCKEND = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
				if(true) {
					this.matchBegin = this.BLOCKBEGIN.matcher(buffer);
					this.matchEnd = this.BLOCKEND.matcher(buffer);
				}

				scanTags(roottag, 0);
		}

		private int scanTags(busTag parenttag, int start) throws busException	{
			int startBlockBegin;
			int startBlockEnd;
			int len, maxlen, startTag;
			boolean scanning = true;
			String tagtext;
			char c;
			busTag tag;

				maxlen = buffer.length();
				//System.out.println("scanTags entered - start=" + start + ", maxlen=" + maxlen);

				// main loop
				while(start < maxlen) {

					// look for the opening tag symbol ("<JTP")
					if(this.matchBegin.find(start)) {
						// found an opening tag
						startBlockBegin = this.matchBegin.start();
						//System.out.println("Found opening tag at " + startBlockBegin);
					}
					else
						startBlockBegin = -1;

					// look for the end tag symbol ("</JTP")
					if(this.matchEnd.find(start)) {
						// found an end tag
						startBlockEnd = this.matchEnd.start();
						//System.out.println("Found closing tag at " + startBlockEnd);
					}
					else
						startBlockEnd = -1;

					// 1st option - did not find any tags
					if(startBlockBegin == -1 && startBlockEnd == -1) {
						//System.out.println("No JTP tags found - returning maxlen");
						getLastTag(parenttag).setBodyOrTrailerText(buffer.substring(start));
						return maxlen;
					}
					else {
						// get the text within the TAG marks
						if(startBlockBegin != -1 && startBlockBegin < startBlockEnd)
							startTag = startBlockBegin;
						else
							startTag = startBlockEnd;

						len = startTag + 1;
						while(len < maxlen)	{
							c=buffer.charAt(len);
							if (c == '>' && scanning)
								break;
							if (c == '"')
								scanning = !scanning;
							len++;
						}
						if (len == maxlen)
							throw new busException("busParser.scanTags(): Tag at offset " + startTag + " not closed.");

						getLastTag(parenttag).setBodyOrTrailerText(buffer.substring(start, startTag));
						start = len + 1;

						// 2nd option - found an opening tag
						if(startBlockBegin != -1 && startBlockBegin < startBlockEnd) {
							tagtext = buffer.substring(startTag + 1, len);
							//System.out.println("OPEN tag - len=" + len + ", tagtext=" + tagtext);
							try {
								tag = new busTag(tagtext);
							}
							catch(busException e) {
								throw new busException("busParser.scanTags() threw TagException: " + e.getMessage(), e);
							}
							parenttag.addElement(tag);
							// scan its child tags
							start = scanTags(tag, start);
						}
						// 3rd option - found an ending tag
						else {
							//System.out.println("CLOSE tag - returning start=" + start);
							return (start);
						}
					}

				} // main loop

				//getLastTag(parenttag).setBodyOrTrailerText(buffer.substring(start));
				return(maxlen);
		}


		private busTag getLastTag(busTag parenttag)	{
			Vector tagsvector;

				tagsvector = parenttag.getTagsVector();
				if (tagsvector.size() == 0)
					return(parenttag);
				else
					return((busTag)tagsvector.lastElement());
		}


		/**
		* Parses the template file, merges it with the data contained in the jtpData object and returns the result in a string.
		*
		* @param   data       jtpData object containing the attributes and values to use.
		* @return  string containing the result of merging the the template file with the data structure.
		* @throws  jtpException if an error occurs.
		*/
		public String parseString(busDriverRequest data) throws busException	{
			ByteArrayOutputStream out;
			String outString;

				out = new ByteArrayOutputStream();
				parseStream(data, out);
				try {
					outString = out.toString(CharacterEncoding);
					out.close();
				}
				catch(java.io.UnsupportedEncodingException e) {
					throw new busException("busParser.parseString() threw UnsupportedEncodingException: " + e.getMessage(), e);
				}
				catch(java.io.IOException e) {
					throw new busException("busParser.parseString() threw IOException: " + e.getMessage(), e);
				}
				return outString;
		}

		/**
		* Parses the template file, merges it with the data contained in the jtpData object and writes the result to an OutputStream.
		*
		* @param   data       jtpData object containing the attributes and values to use.
		* @param   out        OutputStream to write the result to.
		* @throws  jtpException if an error occurs.
		*/
		public void parseStream(busDriverRequest data, OutputStream out) throws busException	{

				// substitute all elements
//				tag = setTags("jtp");s
				if(roottag == null)
					throw new busException("bus.Parser.parseStream(): roottag is null. use setTags() first.");
				try {
					out.write(roottag.getBlockText().getBytes());
					displaytags(data, 0, roottag.elements(), out);
					out.write(roottag.getTrailerText().getBytes());
				}
				catch(java.io.IOException e) {
					throw new busException("busParser.parseStream() threw IOException: " + e.getMessage(), e);
				}
		}

		private void displaytags(busDriverRequest data, int initRow, Enumeration tags, OutputStream outstrm) throws busException	{
			busTag tag;
			int repeat;						// Number of times to repeat a block (Number of rows)
			int row;						// Row number being processed ( 0 through repeat - 1 ).
			String outtext;

				while(tags.hasMoreElements())	{

					tag = (busTag) tags.nextElement();
					
					repeat = tag.getRepeatCount(data, initRow);
					row = 0;
					while(row < repeat)	{
						outtext = tag.resolveBlockVars(data, row);
						try {
							outstrm.write(outtext.getBytes(CharacterEncoding));
						}
						catch(java.io.IOException e) {
							throw new busException("busParser.displaytags() threw IOException: " + e.getMessage(), e);
						}
						displaytags(data, row, tag.elements(), outstrm);
						row++;
					}
					outtext = tag.resolveTrailerVars(data, initRow);
					try {
						outstrm.write(outtext.getBytes(CharacterEncoding));
					}
					catch(java.io.IOException e) {
						throw new busException("busParser.displaytags() threw IOException: " + e.getMessage(), e);
					}
				}
		}

		private String strReplace(String scanstr, Pattern search, int replace) {
			String repl;

				repl = String.valueOf(replace);
				return strReplace(scanstr, search, repl);

		}

		private String strReplace(String scan, Pattern search, String replace) {
			Matcher mat;

				mat = search.matcher(scan);
				return mat.replaceAll(replace);
		}

}
