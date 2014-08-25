package com.daqi.tools.toolunit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.tmatesoft.svn.core.SVNErrorMessage;

public class Logger {
	
	public static boolean sIsDebug = true;
	
	public static void warn(String msg) {
		println(msg);
	}
	
	public static void error(SVNErrorMessage msg, Throwable e) {
		println(msg.toString(), e);
	}
	
	public static void println() {
		System.out.println();
	}

	public static void println(String msg) {
		if (sIsDebug) {
			System.out.println(msg);
		}
	}
	
	public static void println(String msg, Throwable e) {
		if (sIsDebug) {
			System.out.println(msg);
			e.printStackTrace(System.out);
		}
	}
	
	public static void debugPrint(String msg) {
		println(msg);
	}
	
	public static void setDebugMode(String configFile) {
		Properties property = new OrderProperties();
		try {
			property.load(new FileInputStream(configFile));
			String debugMode = property.getProperty("debug");
			if (debugMode != null && debugMode.equals("true")) {
				sIsDebug = true;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
