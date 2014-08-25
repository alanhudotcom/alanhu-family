package com.daqi.tools.findbugs;

import com.daqi.tools.core.ConfigLoader;

public class FindbugsConfigLoader extends ConfigLoader {

	public static final String FINDBUG_RUN = "findbugs_run";
	public static final String FINDBUG_KEY_ANT = "findbugs_key_ant_findbugs";
	public static final String FINDBUG_PROJECT_WORKSPACE = "findbug_project_workspace";
//	public static final String FINDBUG_KEY_SHELL = "findbug_key_shell";
	public static final String FINDBUG_MAIL_SUBJECT = "findbug_mail_subject";
	public static final String FINDBUG_MAIL_RECEIVERS = "findbug_mail_receivers";
	public static final String FINDBUG_MAIL_CCREIVERS = "findbug_mail_ccreivers";
	public static final String FINDBUG_MAIL_COM_ADDRESS = "findbug_mail_com_address";
	
	public FindbugsConfigLoader(String configFile) {
		// TODO Auto-generated constructor stub
		super(configFile);
	}
	
}
