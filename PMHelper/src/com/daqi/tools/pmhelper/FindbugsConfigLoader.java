package com.daqi.tools.pmhelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.daqi.tools.toolunit.OrderProperties;

public class FindbugsConfigLoader {

	public static final String FINDBUG_RUN = "findbugs_run";
	public static final String FINDBUG_KEY_ANT = "findbugs_key_ant_findbugs";
	public static final String FINDBUG_PROJECT_WORKSPACE = "findbug_project_workspace";
//	public static final String FINDBUG_KEY_SHELL = "findbug_key_shell";
	public static final String FINDBUG_MAIL_SUBJECT = "findbug_mail_subject";
	public static final String FINDBUG_MAIL_RECEIVERS = "findbug_mail_receivers";
	public static final String FINDBUG_MAIL_CCREIVERS = "findbug_mail_ccreivers";
	public static final String FINDBUG_MAIL_COM_ADDRESS = "findbug_mail_com_address";
	
	private Properties mProperties;
	private String mConfigFile;

	public FindbugsConfigLoader(String configFile) {
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

}
