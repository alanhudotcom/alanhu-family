package com.daqi.tools.svnmonitor;

import com.daqi.tools.core.ConfigLoader;

public class SvnConfigLoader extends ConfigLoader {

	public static final String SVN_MONITOR_RUN = "svnmonitor_run";
	public static final String SVN_KEY_ROOT = "svn_key_pathroot";
	public static final String SVN_KEY_USERNAME = "svn_key_username";
	public static final String SVN_KEY_PASSWD = "svn_key_passwd";
	public static final String SVN_KEY_MAIL_RECEIVERS = "svn_key_mail_receivers";

	public static final String SVN_STARTVERISON = "svn_startverison";
	public static final String SVN_PERIOD_MIN = "svn_period_min";
	public static final String SVN_MONITOR_AUTHOR = "svn_monitor_author";
	public static final String SVN_MONITOR_FILE = "svn_monitor_file";
	public static final String SVN_MONITOR_PATH = "svn_monitor_pkg";
	
	public static final String SVN_MAIL_CC_RECEIVERS = "svn_mail_cc_receivers";
	public static final String SVN_MAIL_COM_ADDRESS = "svn_mail_com_address";
	public static final String SVN_MAIL_SUBJECT = "svn_mail_subject";
	
	public SvnConfigLoader(String configFile) {
		super(configFile);
	}

	/**
	 * 基本配置信息检查
	 */
	public void checkBaseConfig() {
		checkProperty(SVN_KEY_ROOT);
		checkProperty(SVN_KEY_USERNAME);
		checkProperty(SVN_KEY_PASSWD);
		checkProperty(SVN_KEY_MAIL_RECEIVERS);
	}
	
}
