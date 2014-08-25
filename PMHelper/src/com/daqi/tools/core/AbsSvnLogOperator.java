package com.daqi.tools.core;

import org.tmatesoft.svn.core.SVNLogEntry;

public abstract class AbsSvnLogOperator {

	public abstract void svnLogPrepare();
	
	public abstract void svnLogChanged(SVNLogEntry logEntry);
	
	public abstract void svnLogFinish();
}
