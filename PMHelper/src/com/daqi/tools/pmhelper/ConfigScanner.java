package com.daqi.tools.pmhelper;

import java.io.File;
import java.io.FilenameFilter;

import com.daqi.tools.toolunit.Logger;


/**
 * 配置信息扫描器
 * @author huyong
 *
 */
public class ConfigScanner {
	
	private final String CONFIG_PREFIX = "monitor_";
	private final String CONFIG_SUFFIX = ".property";
	
	public String[] scanProperties() {
		
		String workspace = System.getProperty("user.dir");
		
		String[] list = new File(workspace).list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				
				if (name.startsWith(CONFIG_PREFIX) && name.endsWith(CONFIG_SUFFIX)) {
					return true;
				}
				return false;
			}
		});
		
		Logger.println("1.Current workspace = " + workspace + " scaned " + list.length + " project to monitor");
		for (String string : list) {
			Logger.println(string);
		}
		
		return list;
		
	}
	
	public static void main(String[] args) {
		new ConfigScanner().scanProperties();
	}
}
