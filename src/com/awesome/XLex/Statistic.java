package com.awesome.XLex;

import java.util.HashMap;
import java.util.Map;

import com.awesome.regexp.Config;
import com.awesome.regexp.DebugCode;
import com.awesome.regexp.Logger;

public class Statistic {
	
	public static enum Tag  {
		AllSpecs,
		Automata, 
		FormatFixup,
		AST,
		NFA,
		DFA,
		FuncMatch,
	}
	
	private static Map<Tag, TimeStamp> timeCounter;
	
	static {
		//
		// Initialize the time counter hash-table, set keys to the tags in enum "Tag", and values to -1. 
		//
		timeCounter = new HashMap<>();
		
		for (Tag t : Tag.values()) {
			timeCounter.put(t, null);
		}
	}
	
	public static void start(Tag tag) {
		TimeStamp ts = timeCounter.get(tag);
		
		if (ts == null) {
			ts = new TimeStamp();
			timeCounter.put(tag, ts);
		}
		
		ts.lastUpdateTime = System.nanoTime();
	}
	
	public static void pause(Tag tag) {
		TimeStamp ts = timeCounter.get(tag);
		ts.update();
		Logger.println(Config.STAT_INTERVAL && tag == Tag.DFA, String.format("Interval increase on %1$-15s to %2$fs", tag.name(), ts.getMillis()));
	} 
	
	public static void print(Tag tag) {
		TimeStamp ts = timeCounter.get(tag);
		
		if (ts == null) return;
		
		Logger.print(String.format("Cost on %1$-15s: ", tag.name()));
		Logger.println(ts.getMillis());
	}
	
	public static void print() {
		Logger.tprint(true, new DebugCode() {

			@Override
			public void code() {
				for (Tag t : Tag.values()) {
					print(t);
				}
			}
			
		}, "Stat for all specs");
		
	}
	
	public static void printPercent(float total) {
		Logger.tprint(true, new DebugCode() {

			@Override
			public void code() {
				for (Tag t : Tag.values()) {
					TimeStamp ts = timeCounter.get(t);
					
					if (ts == null) return;
					
					Logger.print(String.format("Cost on %1$-15s: ", t.name()));
					Logger.print(String.format("%1$10.2f ms", ts.getMillis()));
					Logger.println(" [" + ((float)ts.getMillis() / total) * 100 + "%]");
					
				}
			}
			
		}, "Stat for all specs");
	}
	
	public static void printPercent() {
		float total = timeCounter.get(Tag.AllSpecs).getMillis();
		printPercent(total);
	}
}

class TimeStamp {
	long timeInterval;
	long lastUpdateTime;
	
	public TimeStamp() {
		this(0, 0);
	}
	
	public TimeStamp(long timeInterval, long lastUpdateTime) {
		this.timeInterval = timeInterval;
		this.lastUpdateTime = lastUpdateTime;
	}
	
	public void update() {
		long currentTime = System.nanoTime();
		long newInterval = currentTime - lastUpdateTime;
		this.timeInterval =  newInterval + this.timeInterval;
		this.lastUpdateTime = currentTime;
	}
	
	public float getMillis() {
		return (float)this.timeInterval / (1000 * 1000);
//		return this.timeInterval;
	}
}