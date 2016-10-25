package com.awesome.XLex;

public class XLex {
	
	private static TokenRule[] rule = {
		new TokenRule("[0-9]", TokenType.NUM),
		new TokenRule("[_a-zA-Z][_a-zA-Z0-9]*", TokenType.VARIABLE),
	};
		
	private char[] buffer;
	
	public XLex() {
		
	}
	
	public XLex(char[] buffer) {
		this.buffer = buffer;
	}
	
	public XLex(String filename) {
		
	}
	
	public Token getToken() {
		return null;
	}
	
	public char nextChar() {
		return 0;
	}
	
	/*
	 * Set up a automata for retrieving token from input strem.
	 */
	private static void setupAutomata() {
		
	}
	
}
