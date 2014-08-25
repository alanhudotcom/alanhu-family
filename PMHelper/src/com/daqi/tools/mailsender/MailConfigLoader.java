package com.daqi.tools.mailsender;

import com.daqi.tools.core.ConfigLoader;

public class MailConfigLoader extends ConfigLoader {

	public static final String MAIL_KEY_HOST = "mail_key_host";
	public static final String MAIL_KEY_USER = "mail_key_user";
	public static final String MAIL_KEY_PASSWD = "mail_key_passwd";
	
	public static final String MAIL_PORT = "mail_port";
	public static final String MAIL_VALIDATE = "mail_validate";
	public static final String MAIL_FROM_NAME = "mail_show_from_name";
	
	public MailConfigLoader(String configFile) {
		super(configFile);
	}
	
	/**
	 * 基本配置信息检查
	 */
	public void checkBaseConfig() {
		checkProperty(MAIL_KEY_HOST);
		checkProperty(MAIL_KEY_USER);
		checkProperty(MAIL_KEY_PASSWD);
	}
	
}
