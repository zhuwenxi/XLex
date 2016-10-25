package com.awesome.XLex;

public class TokenRule {
	
	public String pattern;
	public TokenType tokenType;
	
	public TokenRule(String pattern, TokenType tokenType) {
		this.pattern = pattern;
		this.tokenType = tokenType;
	}
	
	public TokenRule() {
		this.pattern = null;
		this.tokenType = null;
	}
	
}
