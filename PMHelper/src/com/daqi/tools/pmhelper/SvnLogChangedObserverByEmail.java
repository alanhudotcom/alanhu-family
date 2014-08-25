package com.daqi.tools.pmhelper;

import org.tmatesoft.svn.core.SVNLogEntry;

import com.daqi.tools.mailsender.SimpleMailSender;
import com.daqi.tools.svnmonitor.SvnLogHandler.ISvnLogChangedObserver;

public class SvnLogChangedObserverByEmail implements ISvnLogChangedObserver{

	private SvnLogMailOperator mMailOperator;
	private SvnLogFindBugsOperator mFindBugsOperator;
	private SimpleMailSender mSimplemailSender;
	
	public SvnLogChangedObserverByEmail(String configFile) {
		// TODO Auto-generated constructor stub
		mSimplemailSender = new SimpleMailSender(configFile);
		mMailOperator = new SvnLogMailOperator(mSimplemailSender, configFile);
		mFindBugsOperator = new SvnLogFindBugsOperator(mSimplemailSender, configFile);
	}
	
	@Override
	public void notifyPrepare() {
		mSimplemailSender.doPrepareWork();
		mFindBugsOperator.doPrepare();
	}
	
	@Override
	public void notifyLogChanged(SVNLogEntry logEntry) {
		// TODO Auto-generated method stub
		
		//有数据信息更新
		mMailOperator.logChanged(logEntry);
		
		mFindBugsOperator.logChanged(logEntry);
		
	}
	
	@Override
	public void notifyFinish() {
		// TODO Auto-generated method stub
		
		mMailOperator.sendMailLogInfo();

		// 3.开始进行FindBugs处理
		// TODO
//		runFindBugs();

	}
	
	private void runFindBugs() {
		// TODO：执行findbugs
		
		mFindBugsOperator.runFindbugs();
	}
	
}
