package com.daqi.tools.mailsender;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.daqi.tools.toolunit.OrderProperties;

public class MailConfigLoader {

	public static final String MAIL_KEY_HOST = "mail_key_host";
	public static final String MAIL_KEY_USER = "mail_key_user";
	public static final String MAIL_KEY_PASSWD = "mail_key_passwd";
	
	public static final String MAIL_PORT = "mail_port";
	public static final String MAIL_VALIDATE = "mail_validate";
	public static final String MAIL_FROM_NAME = "mail_show_from_name";
	
	private Properties mProperties;
	private String mConfigFile;

	public MailConfigLoader(String configFile) {
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

	/**
	 * 基本配置信息检查
	 */
	public void checkBaseConfig() {
		checkProperty(MAIL_KEY_HOST);
		checkProperty(MAIL_KEY_USER);
		checkProperty(MAIL_KEY_PASSWD);
	}
	
	private void checkProperty(String key) {
		if (mProperties.getProperty(key) == null) {
			throw new RuntimeException("not set value for '" + key + "' in file " + mConfigFile);
		};
		return;
	}
	
}
