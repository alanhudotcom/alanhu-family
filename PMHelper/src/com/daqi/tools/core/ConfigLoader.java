package com.daqi.tools.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
	
	private Properties mProperties;
	private String mConfigFile;
	
	public ConfigLoader(String configFile) {
		// TODO Auto-generated constructor stub
		mConfigFile = configFile;

		mProperties = new OrderProperties();
		try {
			mProperties.load(new FileInputStream(mConfigFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getConfigFile() {
		return mConfigFile;
	}
	
	public void setProperty(String key, String value) {
		mProperties.setProperty(key, value);
	}
	
	public String getProperty(String key) {
		return mProperties.getProperty(key);
	}
	
	public String getProperty(String key, String defaultValue) {
		String value = mProperties.getProperty(key);
		if (value == null || value.equals("")) {
			value = defaultValue;
			mProperties.put(key, defaultValue);
		}
		return value;
	}
	
	public void updateProperty(String key, String value) {
		mProperties.setProperty(key, value);
		try {
			mProperties.store(new FileOutputStream(mConfigFile), "");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void checkProperty(String key) {
		String value = mProperties.getProperty(key); 
		if (value == null || value.equals("")) {
			throw new RuntimeException("not set value for '" + key + "' in file " + mConfigFile);
		};
		return;
	}
}
