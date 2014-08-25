package com.daqi.tools.svnmonitor;
import java.util.Map;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepository;
import org.tmatesoft.svn.core.io.SVNRepository;

import com.daqi.tools.svnmonitor.SvnLogHandler.ISvnLogChangedObserver;
import com.daqi.tools.toolunit.Converter;
import com.daqi.tools.toolunit.Logger;


public class SvnMonitorInstanse {
	
	private SvnConfigLoader mConfigLoader;
	private SvnLogHandler mSVNLogEntryHandler;
	
	private DAVRepository mSvnRepository;
	
	private int mSvnPeriodMin = 5;		//监控间隔时长
	
	private long mSvnStartRevision = 0L;	// 监控svn的起始版本号
	private String[] mSvnTargetPaths;
	private String mProjectName;

	public SvnMonitorInstanse(String configFile) {
		// TODO Auto-generated constructor stub
		
		mConfigLoader = new SvnConfigLoader(configFile);
		mSVNLogEntryHandler = new SvnLogHandler();
	}

	public void checkConfig() {
		mConfigLoader.checkBaseConfig();
	}
	
	public String getProjectName() {
		if (mProjectName == null) {
			String config = mConfigLoader.getConfigFile();
			int startIndex = 8;
			int endIndex = config.indexOf(".property");
			mProjectName = config.substring(startIndex, endIndex);
		}
		return mProjectName;
	}
	
	public void setSvnChangedObserver(ISvnLogChangedObserver observer) {
		mSVNLogEntryHandler.setObserver(observer);
	}
	
	private void doMonitorPrepareWork() {
		String svnroot = mConfigLoader.getProperty(SvnConfigLoader.SVN_KEY_ROOT);
		String username = mConfigLoader.getProperty(SvnConfigLoader.SVN_KEY_USERNAME);
		String tmpPD = mConfigLoader.getProperty(SvnConfigLoader.SVN_KEY_PASSWD);
		String password = Converter.decryptBASE64(tmpPD);
		mConfigLoader.updateProperty(SvnConfigLoader.SVN_KEY_PASSWD, Converter.encryptBASE64(password));
		
		mSvnRepository = (DAVRepository)SVNUtil.createRepository(svnroot, username, password);

		mSvnTargetPaths = new String[] {""};
		
		String period =	mConfigLoader.getProperty(SvnConfigLoader.SVN_PERIOD_MIN);
		try {
			mSvnPeriodMin = Integer.valueOf(period);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mSVNLogEntryHandler.doPrepareWork(mConfigLoader);
	}
	
	public void doMonitorWork() {
		//开始前的准备工作
		doMonitorPrepareWork();
		
		//进入监控循环状态
		enterMonitorLoop();
	}
	
	/**
	 * 重新加载监控svn-log所需的信息
	 */
	private void reloadDoLogInfo() {
		String startRevisionStr = mConfigLoader.getProperty(SvnConfigLoader.SVN_STARTVERISON);
		try {
			mSvnStartRevision = Long.parseLong(startRevisionStr);
		} catch (Exception e) {
			mSvnStartRevision = 0L;
		}
	}
	
	private void doSvnLog() {

		try {
			long latestRevision = mSvnRepository.getLatestRevision();
			if (mSvnStartRevision >= latestRevision) {
				//没有数据更新，不用导出log处理
				Logger.println("NO new revision, not need to dump log info");
				return;
			}
			
			mSVNLogEntryHandler.setLatestVerision(latestRevision);
			if (mSvnStartRevision == 0) {
				mSvnStartRevision = latestRevision - 1;
			}
			Logger.println("start revision is " + mSvnStartRevision + ", and latestRevision is " + latestRevision);
			mSvnRepository.log(mSvnTargetPaths, mSvnStartRevision, latestRevision, true, true, mSVNLogEntryHandler);
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void enterMonitorLoop() {
		
		Logger.println("Project [" + getProjectName() + "] enter monitor loop for loop " + mSvnPeriodMin + " min");
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					try {
						Thread.sleep(mSvnPeriodMin /** 60*/ * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Logger.println("== NEW Loop time is out, begin to check log info");
					reloadDoLogInfo();
					doSvnLog();
					
				}
				
			}
		}, "SVN-monitor-" + getProjectName()).start();
	}
	
	
	
	private void test() {
		
		String svnroot = "https://svn.3g.net.cn/svn/GTP-GOLauncherEX/GOLauncherEX_Branches/GOLauncherEX_v5.03/GOLauncherEX_Main";
		String username = "huyong";
		String password = "dqHG710";
		
		ISVNLogEntryHandler logHandler = new ISVNLogEntryHandler() {
			
			@Override
			public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
				// TODO Auto-generated method stub
				//版本信息即在logEntry中
				Logger.println("===============================================");
				Logger.println("=====" + logEntry.getAuthor() + " = " 
				+ logEntry.getRevision() + " = " + logEntry.getDate().toLocaleString()
				+ logEntry.getMessage());
				
				Logger.println("====map string");
				Map<String, SVNLogEntryPath> map = logEntry.getChangedPaths();
				for (String string : map.keySet()) {
					System.out.print(string + " , ");
				}
				Logger.println();
				Logger.println("====map entry path");
				for (SVNLogEntryPath path : map.values()) {
					System.out.print(path.getPath() + " , ");
				}
				Logger.println();
			}
		};
		
		String[] paths = new String[2];
		paths[0] = "src";
		paths[1] = "res";
		SVNURL svnurls;
		try {
			svnurls = SVNURL.create("http", null, "svn.3g.net.cn", -1, "svn/GTP-GOLauncherEX/GOLauncherEX_Branches/GOLauncherEX_v5.03/GOLauncherEX_Main", false);
//			svnCM.getLogClient().doLog(svnurls, paths, SVNRevision.create(124283), SVNRevision.create(124283), 
//					SVNRevision.create(124363), false, false, 10, logHandler);
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		SVNRepository repository = SVNUtil.createRepository(svnroot, username, password);
		try {
			repository.log(new String[] {""}, 124283, 124363, true, true, logHandler);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}


	private void testVersion() {
		try {
			
			System.out.println("location = " + mSvnRepository.getLocation());
			System.out.println("root = " + mSvnRepository.getRepositoryRoot(true).getPath());
			System.out.println("root = " + mSvnRepository.getRepositoryRoot(true).toString());
			long last = mSvnRepository.getLatestRevision();
			System.out.println("last == " + last);
			
			SVNURL svnurls = SVNURL.create("http", null, "svn.3g.net.cn", -1, "svn/GTP-GOLauncherEX/GOLauncherEX_Branches/GOLauncherEX_v5.03/GOLauncherEX_Main", false);
			mSvnRepository.setRepositoryRoot(svnurls);
			System.out.println("root2 = " + mSvnRepository.getRepositoryRoot(true).getPath());
			System.out.println("root2 = " + mSvnRepository.getRepositoryRoot(true).toString());
			
			last = mSvnRepository.getLatestRevision();
			System.out.println("last2 == " + last);
			
//			Collection entriesList = mSvnRepository.getDir("", last, null, (Collection) null); 
//			   for (Iterator entries = entriesList.iterator(); entries.hasNext();) { 
//			        SVNDirEntry entry = (SVNDirEntry) entries.next(); 
//			        System.out.println("entry: " + entry.getName()); 
//			        System.out.println("last modified at revision: " + entry.getDate() + 
//			                                        " by " + entry.getAuthor()); 
//			   } 
			
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
//		new SvnMonitorInstanse("monitor_golauncher.property").doMonitorWork();
	}
		
	
}
