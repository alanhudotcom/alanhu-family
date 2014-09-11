package com.daqi.tools.pmhelper;

import java.util.ArrayList;

import org.tmatesoft.svn.core.SVNLogEntry;

import com.daqi.tools.core.AbsSvnLogOperator;
import com.daqi.tools.mailsender.SimpleMailSender;
import com.daqi.tools.svnmonitor.SvnLogHandler.ISvnLogChangedObserver;

public class SvnLogChangedObserverByEmail implements ISvnLogChangedObserver{
	private SimpleMailSender mSimplemailSender;
	
	private ArrayList<AbsSvnLogOperator> mSvnLogOperators;
	
	public SvnLogChangedObserverByEmail(String configFile) {
		// TODO Auto-generated constructor stub
		mSimplemailSender = new SimpleMailSender(configFile);
		initOperators(mSimplemailSender, configFile);
	}
	
	private void initOperators(SimpleMailSender sender, String configFile) {
		
		mSvnLogOperators = new ArrayList<AbsSvnLogOperator>(12);

		//增加邮件处理
		AbsSvnLogOperator operator = new SvnLogMailOperator(mSimplemailSender, configFile);
		mSvnLogOperators.add(operator);

		//增加Findbugs处理
		operator = SvnLogFindBugsOperator.getFindBugsOperator(mSimplemailSender, configFile);
		mSvnLogOperators.add(operator);

	}
	
	
	
	@Override
	public void notifyPrepare() {
		mSimplemailSender.doPrepareWork();
		
		for (AbsSvnLogOperator operator : mSvnLogOperators) {
			operator.svnLogPrepare();
		}
	}
	
	@Override
	public void notifyLogChanged(SVNLogEntry logEntry) {
		// TODO Auto-generated method stub

		//有数据信息更新
		for (AbsSvnLogOperator operator : mSvnLogOperators) {
			operator.svnLogChanged(logEntry);
		}
	}
	
	@Override
	public void notifyFinish() {
		// TODO Auto-generated method stub
		
		for (AbsSvnLogOperator operator : mSvnLogOperators) {
			operator.svnLogFinish();
		}
	}
	
}
