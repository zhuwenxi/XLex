package com.awesome.XLex;

import com.awesome.regexp.DebugCode;

public class Logger {
	public static void print(boolean config, Object msg) {
		if (config) {
			print(msg);
		}
	}
	
	public static void print(Object msg) {
		System.out.print(msg);
	}
	
	public static void printSpaces(int num) {
		for (int i = 0; i < num; i ++) {
			print(" ");
		}
	}
	
	public static void println(Object msg) {
		System.out.println(msg);
	}
	
	public static void println(boolean config, Object msg) {
		print(config, msg);
		print(config, "\n");
	}
	
	/*
	 * Print with tag.
	 */
	public static void tprint(boolean config, Object msg, String tag) {
		if (config) {
			println(config, "{ =========================  " + tag + "\n");
			println(config, msg);
			println(config, "\n========================= } End\n");
		}
	}
	
	public static void tprint(boolean config, DebugCode dbcode, String tag) {
		if (config) {
			println(config, "{ =========================  " + tag + "\n");
			dbcode.code();
			println(config,  "\n========================= } End\n");
		}
	}
}

