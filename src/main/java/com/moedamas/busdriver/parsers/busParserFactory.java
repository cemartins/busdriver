/**
 * 
 */
package com.moedamas.busdriver.parsers;

import java.io.File;
import java.util.Hashtable;

import javax.servlet.ServletContext;

import com.moedamas.busdriver.busDriverRequest;
import com.moedamas.busdriver.busException;
import com.moedamas.busdriver.core.ConfigMgr;
import com.moedamas.busdriver.modules.busAbstractModule;
import com.moedamas.busdriver.parsers.busparser.busParser;

/**
 * @author cemartins
 * Maintains a pool of a specific parser
 * getParser receives a simple filename representing a file inside the templates directory and returns the busParser that has it.
 * Is the respective busParser does not exist yet, create it first and then keep it in the pool.
 * This class is also a module, so any service can obtain its instance through the busDriverRequest as follos:
 * ParserFactory parserFactory = (ParserFactory) request.getValue("parsermodule");
 *
 */
public class busParserFactory extends busAbstractModule implements ParserFactoryInterface {

	private  Hashtable hash;
	private  String CharacterEncoding;
	private  String templatesDir;
	private  String basedir;

	/**
	 * @param moduleRef
	 * @param config
	 */
	public busParserFactory(String moduleRef, ConfigMgr config) {
		super(moduleRef, config);

		this.hash = new Hashtable();

		this.CharacterEncoding = config.getStringValue("CharacterEncoding");
		this.templatesDir = config.getStringValue("templatesdirectory");
	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.modules.busModule#prepareContext(javax.servlet.ServletContext)
	 */
	@Override
	public void prepareContext(ServletContext context) throws busException {

		this.basedir = context.getRealPath("/") + File.separator + this.templatesDir + File.separator;
	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.modules.busModule#finishContext()
	 */
	@Override
	public void finishContext() throws busException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.modules.busModule#prepareService(com.moedamas.busdriver.core.busDriverRequest)
	 */
	@Override
	public void prepareService(busDriverRequest request) throws busException {

		request.addValue(moduleRef, this);
	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.modules.busModule#finishService(com.moedamas.busdriver.core.busDriverRequest)
	 */
	@Override
	public void finishService(busDriverRequest request, boolean success) throws busException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.parsers.ParserFactory#getParser(java.lang.String)
	 */
	public busParser getParser(String filename) throws busException {

		String key = filename.toLowerCase();
		busParser parser = (busParser) hash.get(key);
		if(parser == null) {
			parser = new busParser(CharacterEncoding, basedir + filename);
			hash.put(key, parser);
		}
		return parser;
	}
	
	public void removeParser(String filename) throws busException {
		String key = filename.toLowerCase();
		hash.remove(key);
	}

}
