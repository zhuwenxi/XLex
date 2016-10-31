package com.awesome.XLex;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.awesome.regexp.DeterministicFiniteAutomata;
import com.awesome.regexp.FiniteAutomata;
import com.awesome.regexp.FiniteAutomataState;
import com.awesome.regexp.InputSymbol;
import com.awesome.regexp.Logger;
import com.awesome.regexp.Regexp;
import com.awesome.regexp.util.TwoStageHashMap;

public class XLex {
	
	private final static TokenRule[] rules = {
		new TokenRule("[0-9]+", TokenType.NUM),
		new TokenRule("[_a-zA-Z][_a-zA-Z0-9]*", TokenType.VARIABLE),
	};
	
	private static int stateCount = 0;
	
	private static Map<FiniteAutomataState, TokenRule> stateDict = new HashMap<FiniteAutomataState, TokenRule>();
	
	static {
		setupAutomata();
	}
		
	private static char[] buffer = new char[1024];
	
	public XLex() {
	}
	
	public XLex(char[] buffer) {
		this.buffer = buffer;
	}
	
	public XLex(String filename) {
		
	}
	
	public Token[] getTokenStream(String filename) {
		try (FileInputStream fis = new FileInputStream(filename)) {
			
		} catch (IOException ioe) {
			
		}
		return null;
	}
	
	public Token getToken() {
		return null;
	}
	
	public char nextChar() {
		return 0;
	}
	
	/*
	 * Set up a automata for retrieving token from input stream.
	 */
	private static FiniteAutomata setupAutomata() {
		FiniteAutomata nfa = buildDfa();
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
		Logger.tprint(true, nfa.transDiag, "NFA transDiag");
		// Convert NFA to DFA.
		dfa = new DeterministicFiniteAutomata(nfa, false);
		Logger.tprint(true, dfa.transDiag, "DFA transDiag");
		Logger.tprint(true, dfa.states, "DFA states");
		return nfa;
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
			
			Logger.tprint(true, nfa.transDiag, "nfa.transDiag");
			Logger.tprint(true, nfa.states, "states");
			Logger.tprint(true, nfa.transDiag.getL1KeySet(), "L1 Keyset");
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
					Logger.tprint(true, state, "nfa.start");
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
