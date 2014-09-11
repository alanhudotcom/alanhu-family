package com.daqi.tools.pmhelper;

import java.util.ArrayList;

import com.daqi.tools.svnmonitor.SvnMonitorInstanse;
import com.daqi.tools.toolunit.Logger;

public class PMHelperMain {
	
	private ArrayList<SvnMonitorInstanse> mSvnMontiorList;
	
	public PMHelperMain() {
		// TODO Auto-generated constructor stub
		String[] propertiesLists = new ConfigScanner().scanProperties();
		if (propertiesLists == null || propertiesLists.length == 0) {
			throw new RuntimeException("There is no worked properties");
		}
		mSvnMontiorList =  new ArrayList<SvnMonitorInstanse>(propertiesLists.length);
		for (String configFile : propertiesLists) {
			SvnMonitorInstanse instanse = new SvnMonitorInstanse(configFile);
			instanse.setSvnChangedObserver(new SvnLogChangedObserverByEmail(configFile));
			mSvnMontiorList.add(instanse);
			Logger.setDebugMode(configFile);
		}
	}
	
	private void doCheckConfig() {
		
		Logger.println("2.Check properties for projects");
		
		ArrayList<SvnMonitorInstanse> tmpNotworkMonitorList = 
				new ArrayList<SvnMonitorInstanse>(mSvnMontiorList.size());
		for (SvnMonitorInstanse svnMonitorInstanse : mSvnMontiorList) {
			try {
				svnMonitorInstanse.checkConfig();
			} catch (Exception e) {
				String msg = "Project [" + svnMonitorInstanse.getProjectName() + "] can't work, please check your properties";
				Logger.println(msg, e);
				tmpNotworkMonitorList.add(svnMonitorInstanse);
			}
		}
		// 更新不能工作的monitor实例
		mSvnMontiorList.removeAll(tmpNotworkMonitorList);
		
		if (mSvnMontiorList.size() == 0) {
			throw new RuntimeException("NO One can work, please check your properties !");
		}
	}
	
	private void doWork(long revision, String changedUrl) {
		for (SvnMonitorInstanse svnMonitorInstanse : mSvnMontiorList) {
			Logger.println("3.Project [ " + svnMonitorInstanse.getProjectName() + " ] begin to work");
			svnMonitorInstanse.doMonitorWork(revision, changedUrl);
		}
	}
	
	public static void main(String[] args) {
		
		PMHelperMain pmHelper = new PMHelperMain();
		
		pmHelper.doCheckConfig();
		
		String svnRevision = args[0];
		long revision = Long.parseLong(svnRevision);
		String svnUrl = args[1]; 
		
		pmHelper.doWork(revision, svnUrl);
	}
	
}
