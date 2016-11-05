package com.awesome.XLex;

public class Token {
	private TokenType type;
	private String name;
	private String pattern;
	
	public Token(String name, TokenType type, String pattern) {
		this.name = name;
		this.type = type;
		this.pattern = pattern;
	}
	
	public Token() {
		this(null, TokenType.UNKNOWN, null);
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getPattern() {
		return this.pattern;
	}
	
	public TokenType getType() {
		return this.type;
	}
	
	@Override
	public String toString() {
		return name + "       (" + type.name() + ")               " + pattern;
	}
	
}

enum TokenType {
	
	//
	// Basic token type:
	//
	NUM,
	VARIABLE,
	RESERVED_WORD,  // keyword or type.
	
	
	//
	// Arithmetic operator:
	//
	PLUS,
	MINUS,
	MUL,
	DIV,
	
	//
	// Logistic operator:
	//
	EQUAL,
	NOT_EQUAL,
	GREAT_THAN,
	LESS_THAN,
	GREAT_EQUAL_THAN,
	LESS_EQUAL_THAN,
	
	//
	// Special symbol:
	//
	LEFT_BRACE,
	RIGHT_BRACE,
	LEFT_PARENTHESIS,
	RIGHT_PARENTHESIS,
	SEMICOLON,
	
	//
	// Assignment operator:
	//
	ASSIGN,
	
	// 
	// Pre-process operator:
	//
	INCLUDE_OP,
	SYSTEM_PATH,
	
	//
	// Default Token type:
	//
	UNKNOWN,
	
}
