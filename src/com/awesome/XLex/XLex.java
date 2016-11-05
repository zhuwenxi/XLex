package com.awesome.XLex;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.awesome.regexp.DeterministicFiniteAutomata;
import com.awesome.regexp.FiniteAutomata;
import com.awesome.regexp.FiniteAutomataState;
import com.awesome.regexp.InputSymbol;
import com.awesome.regexp.Logger;
import com.awesome.regexp.ProductionToken;
import com.awesome.regexp.Regexp;
import com.awesome.regexp.util.TwoStageHashMap;

public class XLex {
	
	private final static TokenRule[] rules = {
		new TokenRule("[0-9]+", TokenType.NUM),
		new TokenRule("[_a-zA-Z][_a-zA-Z0-9]*", TokenType.VARIABLE),
		new TokenRule("#[a-zA-Z][_a-zA-Z0-9]*", TokenType.INCLUDE_OP),
		new TokenRule("<[_a-zA-Z][_a-zA-Z0-9]*.[a-zA-Z0-9]+>", TokenType.SYSTEM_PATH),
		new TokenRule("{", TokenType.LEFT_BRACE),
		new TokenRule("}", TokenType.RIGHT_BRACE),
		new TokenRule(";", TokenType.SEMICOLON),
		new TokenRule("-", TokenType.MINUS),
		new TokenRule("=", TokenType.ASSIGN),
		new TokenRule("(", TokenType.LEFT_PARENTHESIS),
//		new TokenRule(")", TokenType.RIGHT_PARENTHESIS),
		
		
		/*
		 * Test tokens here.
		 */
		
//		new TokenRule("<stdio>", TokenType.NUM),
//		new TokenRule("stdio", TokenType.VARIABLE),
	};
	
	private static int stateCount = 0;
	
	private static Map<FiniteAutomataState, TokenRule> stateDict = new HashMap<FiniteAutomataState, TokenRule>();
	
	static {
		setupAutomata();
	}
		
	private static char[] buffer = new char[1024 * 1024]; // Fix me. Currently I just read in the whole file, into this 1024 * 1024 buffer.
	private static int bufferSize = -1;
	private static int bufferCursor = 0;
	private static FileReader fileReader = null;
	
	private static FiniteAutomata dfa;
	
	public XLex() {
	}
	
	public XLex(char[] buffer) {
		this.buffer = buffer;
	}
	
	public XLex(String filename) {
		try {
			fileReader = new FileReader(filename);
		} catch(IOException ioe) {
			ioe.printStackTrace();
			return;
		}
		
		// Read first 1024 char into buffer
		readBuffer();
	}
	
	/*
	 * Get token stream, with the DFA we constructed.
	 */
	public List<Token> getTokens() {
		List<Token> tokens = null;
		
		Token next = getToken();
		while (next != null) {
			if (tokens == null) {
				tokens = new ArrayList<Token>();
			}
			
			tokens.add(next);
			
			next = getToken();
		}
		
		return tokens;
	}
	
	public Token getToken() {
		Token token = null;
		String tokenName = "";
		
		// This 'a' is a placeholder, no specific meaning.
		char next = 'a';
		InputSymbol ch = null;
		
		FiniteAutomataState currentState = null;
		FiniteAutomataState acceptState = null;
		
		boolean meetAccept = false;
		
		// Init a stack to hold states.
		Stack<FiniteAutomataState> stack = new Stack<FiniteAutomataState>();
		
		// Pop the top element of the stack, set it as current state.
		currentState = dfa.start;
		stack.push(currentState);
		
		while (currentState != null && next != 0) {
			if (currentState.isAcceptState) {
				meetAccept = true;
			}
			
			
			
			// Get next char from buffer.
			next = getChar();
			ch = new InputSymbol(next, ProductionToken.ch);
			
			// Transfor to next state.
			List<FiniteAutomataState> currentStates = dfa.transDiag.query(currentState, ch);
			currentState = currentStates != null ? currentStates.get(0) : null;
			
			// Push current state into stack, and grow token name.
			stack.push(currentState);
			tokenName += next;
			
			
			if (currentState == null && !meetAccept) {
				while (stack.size() != 1) {
					stack.pop();
					ungetChar();
				}
				stack.pop();
				
				// IMPORTANT! roll back, but not to the first char, instead, second char.
				getChar();
				
				tokenName = "";
				currentState = dfa.start;
				stack.push(currentState);
			}
		}
		
		// Error recovery, trace back to find the accept state.
		boolean foundAccept = false;
		while (!stack.isEmpty()) {
			FiniteAutomataState lastState = stack.pop();
			
			if (lastState != null && lastState.isAcceptState) {
				foundAccept = true;
				acceptState = lastState;
				break;
			} else {
				ungetChar();
				if (tokenName.length() > 0) {
					tokenName = tokenName.substring(0, tokenName.length() - 1);
				}
			}
		}
		
		// Decide if the any token found.
		if (foundAccept) {
			token = new Token(tokenName, getTokenType(acceptState.regexp), acceptState.regexp);
		} 
		
		return token;
	}
		
	public char getChar() {
		char next;
		if (bufferCursor < bufferSize) {
			next = buffer[bufferCursor];
			bufferCursor ++;
			return next;
		} else {
			boolean hasContent = readBuffer();
			if (!hasContent) {
				return 0;
			} else {
				return getChar();
			}
		}
	}
	
	public void ungetChar() {
		bufferCursor --;
	}
	
	private boolean readBuffer() {
		try {
			bufferSize = fileReader.read(buffer);
			
			if (bufferSize > 0) {
				return true;
			} else {
				return false;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
		}
	}
	
	/*
	 * Get token type from it's regexp.
	 */
	private TokenType getTokenType(String regexp) {
		for (TokenRule rule : rules) {
			if (regexp.equals(rule.pattern)) {
				return rule.tokenType;
			}
		}
		
		return TokenType.UNKNOWN;
	}
	
	/*
	 * Set up a automata for retrieving token from input stream.
	 */
	private static FiniteAutomata setupAutomata() {
		dfa = buildDfa();
		return null;
	}
	
	/*
	 * Build DFA ( deterministic automata ) according to token-rules their pattern.
	 */
	private static FiniteAutomata buildDfa() {
		FiniteAutomata nfa = null;
		FiniteAutomata dfa = null;
		
		// Create a NFA array for holding NFA for each of the rule.
		TokenRule[] rules = XLex.rules;
		int ruleNumber = rules.length;
		FiniteAutomata[] nfaArray = new FiniteAutomata[ruleNumber];
		
		// Initialize each of NFA array element.
		for (int i = 0; i < ruleNumber; i ++) {
			String pattern = rules[i].pattern;
			nfaArray[i] = new Regexp(pattern).getNfa();
		}
		
		TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState> newTransDiag = renameNfaStates(nfaArray);
		markAcceptState(nfaArray);
		
		// Convert all NFAs to one big NFA.
//		com.awesome.regexp.Config.DFA_VERBOSE = true;
//		com.awesome.regexp.Config.DFA_BASIC = true;
//		com.awesome.regexp.Config.DFA_RENAME_STATE = true;
		
		nfa = allToOne(nfaArray, newTransDiag);
		nfa.transDiag = newTransDiag;
		Logger.tprint(true, nfa.start, "Big NFA start");
		Logger.tprint(true, nfa.end, "Big NFA end");
		// Convert NFA to DFA.
		dfa = new DeterministicFiniteAutomata(nfa, false);
		Logger.tprint(true, dfa.transDiag, "DFA transDiag");
		Logger.tprint(true, dfa.states, "DFA states");
		return dfa;
	}
	
	/*
	 * Rename all states in nfaArray, to ensure they could have unique names in the coming big NFA. 
	 */
	private static TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState> renameNfaStates(FiniteAutomata[] nfaArray) {
		TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState> newTransDiag = new TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState>(); 
		for (int i = 0; i < nfaArray.length; i ++) {
			FiniteAutomata nfa = nfaArray[i];
			Logger.tprint(true, nfa.transDiag, "origin nfa.transDiag");
			Logger.tprint(true, nfa.states, "origin nfa.states");
			
			for (FiniteAutomataState state : nfa.states) {
				updateTransDiag(state,stateCount, nfa, newTransDiag);
				stateCount ++;
			}
			
			Logger.tprint(true, nfa.states, "states");
		}
		
		return newTransDiag;
		
	}
	
	private static void markAcceptState(FiniteAutomata[] nfaArray) {
		for (int i = 0; i < nfaArray.length; i ++) {
			FiniteAutomata nfa = nfaArray[i];
			
			// Mark accept states' corresponding regexp.
			for (FiniteAutomataState state : nfa.states) {
				if (state.isAcceptState) {
					state.regexp = rules[i].pattern;
					Logger.println(true, state + ": " + state.regexp);
					
					stateDict.put(state, rules[i]);
				}
			}
		}
	}
	
	/*
	 * Convert all NFAs to one single NFA, with epsilon closures.
	 */
	private static FiniteAutomata allToOne(FiniteAutomata[] nfaArray, TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState> transDiag) {
		FiniteAutomata bigNfa = new FiniteAutomata();
		FiniteAutomataState startState = new FiniteAutomataState(stateCount);
		List<FiniteAutomataState> nfaStartStates = new ArrayList<FiniteAutomataState>();
		bigNfa.transDiag = transDiag;
		
		//
		// Merge all transform diagrams into one.
		// 
		for (FiniteAutomata nfa : nfaArray) {
			// Merge transDiag
			Set<FiniteAutomataState> keySets = nfa.transDiag.getL1KeySet();
			for (FiniteAutomataState key : keySets) {
				Map<InputSymbol, List<FiniteAutomataState>> value = nfa.transDiag.impl.get(key);
				bigNfa.transDiag.impl.put(key, value);
			}
			
			// Merge states
			for (FiniteAutomataState state : nfa.states) {
				bigNfa.states.add(state);
				
				// Add end state.
				if (state.isAcceptState) {
					bigNfa.end.add(state);
				}
				
				// Collect start state.
				if (state == nfa.start) {
					nfaStartStates.add(state);
				}
			}
			
			// Merge symbolSet
			for (InputSymbol symbol : nfa.symbolSet) {
				if (!bigNfa.symbolSet.contains(symbol)) {
					bigNfa.symbolSet.add(symbol);
				}
			}
		}
		
		//
		// Add new startState. It connects to existing NFAs start state with a epsilon closure.
		//
		bigNfa.start = startState;
		for (FiniteAutomataState currentStartState : nfaStartStates) {
			bigNfa.transDiag.update(startState, InputSymbol.epsilon, currentStartState);
		}
		
		Logger.tprint(true, bigNfa.transDiag, "big NFA");
		Logger.tprint(true, bigNfa.states, "big NFA states");
		
		return bigNfa;
	}
	
	/*
	 * Workaround approach to rename NFA states number.
	 * Due to fact when change the property of a HashMap's key, it's hash value also changes,
	 * so we aren't able to search the HashMap with the key again.
	 * 
	 * To reproduce the behavior:
	 * 
	 * Map<TestKey, TestValue> tMap = new HashMap<TestKey, TestValue>();
	 * TestKey k = new TestKey(125);
	 * TestValue v = new TestValue("0x7d");
	 * tMap.put(k, v);
	 * 
	 * k.key = 111;
	 * searchV = tMap.get(new TestKey(111));
	 * Logger.println(searchV);  // print "null"
	 * 
	 */
	private static void updateTransDiag(FiniteAutomataState originState, int newStateNumber, FiniteAutomata nfa, TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState> newTransDiag) {
		TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState> transDiag = nfa.transDiag;
		Map<InputSymbol, List<FiniteAutomataState>> secondStageHashMap = transDiag.impl.get(originState);
		
		//
		// Change originState -> newState, in "L1Key" set.
		//
		if (secondStageHashMap != null) {
				
			// Remove (originState, symbol, targetStates)
			transDiag.impl.remove(originState);
				
			// Put (newState, symbol, targetStates)
			originState.stateNumber = newStateNumber;
//			Logger.println(true, secondStageHashMap);
//			transDiag.impl.put(originState, secondStageHashMap);
			newTransDiag.impl.put(originState, secondStageHashMap);
		} else if (originState.isAcceptState) {
			//
			// Need special treatment to the accept state, since it could has no edge connect to other states.
			//
			originState.stateNumber = newStateNumber;
		}
		
		
		
	}
	
}
