package com.daqi.tools.svnmonitor;

import java.util.Collection;
import java.util.Set;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import com.daqi.tools.toolunit.Logger;

public class SvnLogHandler implements ISVNLogEntryHandler {

	private ISvnLogChangedObserver mObserver;
	
	private long mLatestRevision = 0l;
	
	private long mLastLogRevision = 0l;		// 上次log的版本
	
	private int mDoLogCount = 0;
	
	private SvnConfigLoader mConfigLoader;
	
	public void setObserver(ISvnLogChangedObserver observer) {
		mObserver = observer;
	}
	
	public void setLatestVerision(long verision) {
		mLatestRevision = verision;
	}
	
	public void doPrepareWork(SvnConfigLoader configLoader) {
		mConfigLoader = configLoader;
		mObserver.notifyPrepare();
	}
	
	@Override
	public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
		
		if (mLastLogRevision >= logEntry.getRevision() && mDoLogCount == 0) {
			//若当前获取到的log版本小于等于最近一次的log版本，且没有以往log信息存在，则不用处理，直接返回。
			Logger.println("Last log info is new revision, not need to dump log info");
			return;
		}
		
		++mDoLogCount;

		if (mLastLogRevision != logEntry.getRevision()) {
			//上次已经检查过的revision若与当次版本相同，则说明是相同的log，不用再处理。
			mObserver.notifyLogChanged(logEntry);
			mLastLogRevision = logEntry.getRevision();
			//更新当前内存中版本值
			if (mConfigLoader != null) {
				mConfigLoader.setProperty(SvnConfigLoader.SVN_STARTVERISON, String.valueOf(mLastLogRevision));
			}
		}
		
		//TODO: 判断当前通知是否已经结束
		if (logEntry.getRevision() >= mLatestRevision || mDoLogCount > 4) {
			Logger.println("do log version " + logEntry.getRevision() + " for log count = " + mDoLogCount);
			mObserver.notifyFinish();
			mDoLogCount = 0;
			if (mConfigLoader != null) {
				mConfigLoader.updateProperty(SvnConfigLoader.SVN_STARTVERISON, String.valueOf(mLastLogRevision));
			}
			
		}
		
	}

	public interface ISvnLogChangedObserver {
		public static final int EVENT_AUTHOR_COMMITED = 0;
		public static final int EVENT_FILE_CHANGED = 1;
		public static final int EVENT_PATH_CHANGED = 2;
		
		//准备通知
		public void notifyPrepare();
		
		public void notifyLogChanged(SVNLogEntry logEntry);

		// 通知结束
		public void notifyFinish();
		
	}
	
	
}
