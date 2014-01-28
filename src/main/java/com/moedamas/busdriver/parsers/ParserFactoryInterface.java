package com.moedamas.busdriver.parsers;

import com.moedamas.busdriver.busException;
import com.moedamas.busdriver.parsers.busparser.busParser;

// TODO istead of busParser, use an interface so you can use different parser implementations and not just busParser

public interface ParserFactoryInterface {

	
	public busParser getParser(String filename) throws busException;
	
	public void removeParser(String filename) throws busException;
}
