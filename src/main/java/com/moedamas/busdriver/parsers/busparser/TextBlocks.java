package com.moedamas.busdriver.parsers.busparser;

import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

import com.moedamas.busdriver.busException;

// TODO Don't know how, but blockvars should also resolve a keep the method in busDriverRequest to call for the attribute's value.

/**
 * TextBlocks is a utility class to aid analyse a String containing text
 * that is separated by blocks.
 * <p>
 * Example<br>
 * a string conatining the text "hello <?block 1?> some more text <?block 2?> some more text '<?block 3?>'"<br>
 * can be manupulated using this class to quickly retrieve the blocks of text enclosed within the markers <? and ?>
 * @author 	Carlos Martins
 * @version 	%I%, %G%
 * @since       BUS 1.0
 */
public class TextBlocks	{
	private Vector blocks;
	private class block	{
		public int beginpos;
		public int endpos;
		public String innertext;
		public VarResolver resolver = null;
	}
	private class blockelements implements Enumeration	{
		private int currentelement;
		private int maxelements;

			public blockelements()	{
					currentelement = 0;
					maxelements = blocks.size();
			}
			public boolean hasMoreElements()	{
					if (currentelement < maxelements)
						return true;
					else
						return false;
			}
			public Object nextElement()	{
				Object obj;
					obj = blocks.elementAt(currentelement);
					currentelement++;
					return ((block)obj).innertext;
			}
	}

		/**
		 * Creates a new <code>TextBlocks</code> with a
		 * vector of blocks found in the text parameter.
		 * <p>
		 * Example<br>
		 * <code>new TextBlocks("hello <[block 1]> some more text <[block 2]> some more text '<[block 3]>'", "<[", "]>");</code><br>
		 * can be used to quickly retieve "block 1", "block 2" and "block 3".
		 *
		 * @param   text       text string with one or more blocks
		 * @param   BB	       substring within the text that defines the begining of a block
		 * @param   BE	       substring within the text that defines the end of a block
		 */
		public TextBlocks(String text, String BB, String BE)	{

				blocks = new Vector();
				if (BB == BE)
					scanBlocks(text, text, BB);
				else
					scanBlocksNested(text, text, BB, BE);
		}

		/**
		 * Creates a new <code>TextBlocks</code> with a
		 * vector of blocks found in the text parameter, ignoring text within blocks comprised
		 * between the characters IGNB and IGNE.
		 * <p>
		 * Example<br>
		 * <code>new TextBlocks("hello <[block 1]> some more text <[block 2]> some more text '<[block 3]>'", "<[", "]>", '\'', '\'');</code><br>
		 * can be used to quickly retieve "block 1", "block 2" ignoring "block 3" because this is enclosed between the ignoring marks.
		 *
		 * @param   text       text string with one or more blocks
		 * @param   BB	       substring within the text that defines the begining of a block
		 * @param   BE	       substring within the text that defines the end of a block
		 * @param   IGNB       character within the text that defines the begining of a block to skip
		 * @param   IGNE       character within the text that defines the end of a skipping block
		 */
		public TextBlocks(String copyText, String BB, String BE, char IGNB, char IGNE)	{
			StringBuffer text;
			int i, l, skipping;

				blocks = new Vector();
				l = copyText.length();
				i = 0;
				skipping = 0;
				text = new StringBuffer(copyText);

				if (IGNB == IGNE) {
					while(i < l) {
						if(copyText.charAt(i) == IGNB) {
							skipping = 1 - skipping;
							text.setCharAt(i, ' ');
						}
						else
							if(skipping > 0)
								text.setCharAt(i, ' ');
						i++;
					}
				}
				else {
					while(i < l) {
						if(copyText.charAt(i) == IGNB)
							skipping++;
						if(skipping > 0)
							text.setCharAt(i, ' ');
						if(copyText.charAt(i) == IGNE)
							skipping--;
						i++;
					}
				}
				if (BB == BE)
					scanBlocks(text.toString(), copyText, BB);
				else
					scanBlocksNested(text.toString(), copyText, BB, BE);
		}

		/**
		 * dbQuery("SELECT nta_titulo, nta_texton FROM T_1010Noticia")
		 * dbQuery(                                                  )
		 * If the mark that defines the beggining of the block is different from the
		 * mark that defines de end of the block then there is the possibility that there
		 * are nested blocks. In this case the blocks are stored from inside out.
		 * example
		 * text = "text1(text2(text3)text4)text5"   BB="("   BE=")" will produce 2 blocks:
		 * block 1 = text3
		 * block 2 = text2(text3)text4
		 */
		private void scanBlocksNested(String text, String copyText, String BB, String BE)	{
			block b;
			Stack openmark = new Stack();
			Stack nestedblocks = new Stack();
			int posb;
			int lbb = BB.length();
			int pose;
			int lbe = BE.length();
			int i = 0;
			int l = text.length();

				while(i < l)	{
					posb = text.indexOf(BB, i);
					pose = text.indexOf(BE, i);
					// check if no one was found
					if(pose == -1)
						break;
					if(posb == -1)	{
						if(openmark.empty())
							break;
						else
							posb = l;
					}
					// check wich was found first
					if (posb < pose)	{
						// found a begin mark before an end mark
						openmark.push(new Integer(posb));
						i = posb + lbb;
					}
					else	{
						// found an end mark before a begin mark
						posb = ((Integer)openmark.pop()).intValue();
						b = new block();
						b.beginpos = posb;
						b.endpos = pose;
						b.innertext = copyText.substring(posb + lbb, pose);
						nestedblocks.push(b);
						if(openmark.empty())	{
							while(!nestedblocks.empty())	{
								b = (block)nestedblocks.pop();
								blocks.addElement(b);
							}
						}
						i = pose + lbe;
					}
				}
		}

		// If the mark that defines the beggining of the block is the same as the
		// mark that defines de end of the block then the blocks are stored sequencially.
		// example
		// text = "text1(text2(text3(text4)text5"   BB="(" will produce 2 blocks:
		// block 1 = text2
		// block 2 = text3
		private void scanBlocks(String text, String copyText, String BB)	{
			block b;
			int posb;
			int lbb = BB.length();
			int pose;

				posb = text.indexOf(BB, 0);
				pose = text.indexOf(BB, posb + lbb);
				while(pose != -1)	{

					b = new block();
					b.beginpos = posb;
					b.endpos = pose;
					b.innertext = copyText.substring(posb + lbb, pose);
					blocks.addElement(b);
					posb = pose;
					pose = text.indexOf(BB, posb + lbb);

				}
		}

		/**
		 * For each block, lookup the function in busDriverRequest (resolver) that will be used to obtain the
		 * the value corresponding to blocks method description. 
		 *
		 */
		public void lookupResolvers() throws busException {
			int n= blocks.size();
			int i = 0;
			block blk;
			while(i < n) {
				blk = (block)blocks.get(i);
				blk.resolver = new VarResolver(blk.innertext);
				
				i++;
			}
		}

		/**
		 * Returns the number of blocks detected in this <code>TextBlocks</code> object
		 *
		 * @return    <code>int</code> the number of blocks within this <code>TextBlocks</code> object
		 */
		public int size()	{
				return blocks.size();
		}

		/**
		 * Returns the text inside block number <code>i</code>.
		 *
		 * @return    the text inside block <code>i</code>
		 */
		public String textAt(int i)	{
				return ((block)blocks.elementAt(i)).innertext;
		}

		/**
		 * Returns position (offset) within the text where block number <code>i</code> begins.
		 *
		 * @return    position (offset) within the text where block number <code>i</code> begins (0 to text.length() - 1).
		 */
		public int beginOffsetAt(int i)	{
				return ((block)blocks.elementAt(i)).beginpos;
		}

		/**
		 * Returns position (offset) within the text where block number <code>i</code> ends.
		 *
		 * @return    position (offset) within the text where block number <code>i</code> ends (0 to text.length() - 1).
		 */
		public int endOffsetAt(int i)	{
				return ((block)blocks.elementAt(i)).endpos;
		}
		
		/**
		 * Gets the resolver of a block
		 * @param i index of the block within the TextBlocks list (0..size-1)
		 * @return the VarResolver corresponding to the text within the specific block (null is there is no resolver)
		 */
		public VarResolver resolverAt(int i) {
			return ((block)blocks.elementAt(i)).resolver;
		}

		/**
		 * Returns an <code>Enumeration</code> of all the text blocks found within the text.
		 *
		 * @return    <code>Enumeration</code> of all the text blocks found within the text.
		 */
		public Enumeration elements()	{
				return new blockelements();
		}
}