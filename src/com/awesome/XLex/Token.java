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
	// Assignment operator:
	//
	ASSIGN,
	
	//
	// Default Token type:
	//
	UNKNOWN,
	
}
