package com.awesome.XLex;

public class Config {
	
	public static boolean REGEXP_VERBOSE = false;
	
	public static boolean CONTEXT_FREE_GRAMMAR_VERBOSE = false;
	
	public static boolean LRAUTOMATA_VERBOSE = false;
	public static boolean LRAUTOMATA_ACTIONTABLE = false;
	public static boolean LRAUTOMATA_GOTOTABLE = false;
	
	public static boolean AST_VERBOSE = false;
	
	public static boolean NFA_VERBOSE = false;
	
	public static boolean DFA_VERBOSE = false;
	public static boolean DFA_BASIC = false;
	public static boolean DFA_RENAME_STATE = false;
	public static boolean DFA_RENAME_STATE_START_END = false;
	
	public static boolean STAT = false;
	public static boolean STAT_INTERVAL = false;
	
	public static void setAllOptons(boolean option) {
		Config.REGEXP_VERBOSE = option;
		
		Config.CONTEXT_FREE_GRAMMAR_VERBOSE = option;
		
		Config.LRAUTOMATA_VERBOSE = option;
		Config.LRAUTOMATA_ACTIONTABLE = option;
		Config.LRAUTOMATA_GOTOTABLE = option;
		
		Config.AST_VERBOSE = option;
		
		Config.NFA_VERBOSE = option;
		
		Config.DFA_VERBOSE = option;
		Config.DFA_BASIC = option;
		Config.DFA_RENAME_STATE = option;
		
		Config.STAT = option;
		Config.STAT_INTERVAL = option;
	}
}

