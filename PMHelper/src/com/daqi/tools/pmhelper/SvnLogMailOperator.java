package com.daqi.tools.pmhelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import com.daqi.tools.mailsender.SimpleMailSender;
import com.daqi.tools.svnmonitor.SvnConfigLoader;
import com.daqi.tools.svnmonitor.SvnLogHandler.ISvnLogChangedObserver;
import com.daqi.tools.toolunit.Converter;
import com.daqi.tools.toolunit.Logger;

public class SvnLogMailOperator {

	private SimpleMailSender mMailSender;
	
	private ArrayList<SvnChangedInfoBean> mChangedInfoList;
	
	private boolean mMonitorNotRun = false;
	private ArrayList<String> mMonitorAuthors;
	private ArrayList<String> mMonitorFiles;
	private ArrayList<String> mMonitorPaths;
	
	private String mMailReceivers;
	private String mMailCcReceivers;
	private String mMailSubject;
	
	public SvnLogMailOperator(SimpleMailSender mailSender, String configFile) {
		// TODO Auto-generated constructor stub
		mMailSender = mailSender;
		mChangedInfoList = new ArrayList<SvnChangedInfoBean>();
		
		loadConfig(configFile);
	}
	
	public void notifyLogChanged(SvnChangedInfoBean bean) {
		// TODO Auto-generated method stub
		mChangedInfoList.add(bean);
	}
	
	public void logChanged(SVNLogEntry logEntry) {
		if (mMonitorNotRun) {
			return;
		}
		Logger.println("==SvnLog begin to logchanged");
		
		//TODO：文件匹配
		filterAuthor(logEntry);
		
		//TODO：路径匹配
		filterFile(logEntry);
		
		//TODO: 是否需要更新本次的版本
		filterPath(logEntry);
		
	}

	private void loadConfig(String configFile) {
		SvnConfigLoader configLoader = new SvnConfigLoader(configFile);
		
		String monitor = configLoader.getProperty(SvnConfigLoader.SVN_MONITOR_RUN);
		if (monitor != null && monitor.equals("false")) {
			mMonitorNotRun = true;
		}
		
		String authors = configLoader.getProperty(SvnConfigLoader.SVN_MONITOR_AUTHOR);
		if (authors != null && !(authors.equals(""))) {
			if (authors.equals("*")) {
				mMonitorAuthors = new ArrayList<String>(1);
				mMonitorAuthors.add(authors);
			} else {
				//当没有填写字段
				mMonitorAuthors = new ArrayList<String>(Arrays.asList(authors.split(",")));
			}
		}
		
		String files = configLoader.getProperty(SvnConfigLoader.SVN_MONITOR_FILE);
		if (files != null && !files.equals("")) {
			mMonitorFiles = new ArrayList<String>(Arrays.asList(files.split(",")));
		}
		
		String paths = configLoader.getProperty(SvnConfigLoader.SVN_MONITOR_PATH);
		if (paths != null && !paths.equals("")) {
			paths.replaceAll(".", "/");
			mMonitorPaths = new ArrayList<String>(Arrays.asList(paths.split(",")));
		}
		
		mMailReceivers = configLoader.getProperty(SvnConfigLoader.SVN_KEY_MAIL_RECEIVERS);
		mMailCcReceivers = configLoader.getProperty(SvnConfigLoader.SVN_MAIL_CC_RECEIVERS);
		String subject = configLoader.getProperty(SvnConfigLoader.SVN_MAIL_SUBJECT);
		if (subject != null && !subject.equals("")) {
			subject = Converter.suppoetTozh(subject);
		}
		mMailSubject = subject;
	}

	private void filterAuthor(SVNLogEntry logEntry) {
		String author = logEntry.getAuthor();
		if (mMonitorAuthors != null 
				&& (mMonitorAuthors.contains(author) || mMonitorAuthors.contains("*"))) {
			//TODO: 作者匹配，通知外界
			SvnChangedInfoBean bean = new SvnChangedInfoBean(
					ISvnLogChangedObserver.EVENT_AUTHOR_COMMITED, 
					logEntry.getAuthor(), 
					null, 
					null, 
					String.valueOf(logEntry.getRevision()),
					logEntry.getMessage());
			mChangedInfoList.add(bean);
			Logger.println("==SvnLog filter author for " + author);
		}
	}
	
	private void filterFile(SVNLogEntry logEntry) {
		if (mMonitorFiles == null) {
			return;
		}

		Map<String, SVNLogEntryPath> changedPaths = logEntry.getChangedPaths();
		Set<String> paths = changedPaths.keySet();
		
		for (String keyFile : mMonitorFiles) {
			for (String changedPath : paths) {
				if (changedPath.contains(keyFile)) {
					SvnChangedInfoBean bean = new SvnChangedInfoBean(
							ISvnLogChangedObserver.EVENT_FILE_CHANGED, 
							logEntry.getAuthor(), 
							keyFile, 
							changedPath, 
							String.valueOf(logEntry.getRevision()),
							logEntry.getMessage());
					
					mChangedInfoList.add(bean);
					Logger.println("==SvnLog filter file for " + keyFile);
				}
			}
		}
	}
	
	private void filterPath(SVNLogEntry logEntry) {
		if (mMonitorPaths == null) {
			return;
		}
		
		Map<String, SVNLogEntryPath> changedPaths = logEntry.getChangedPaths();
		Set<String> paths = changedPaths.keySet();
		for (String keyPath : mMonitorPaths) {
			for (String changedPath : paths) {
				if (changedPath.contains(keyPath)) {
					SvnChangedInfoBean bean = new SvnChangedInfoBean(
							ISvnLogChangedObserver.EVENT_PATH_CHANGED, 
							logEntry.getAuthor(), 
							null, 
							changedPath, 
							String.valueOf(logEntry.getRevision()),
							logEntry.getMessage());
					mChangedInfoList.add(bean);
					Logger.println("==SvnLog filter package for " + changedPath);
				}
			}
		}
	}
	
	
	public void sendMailLogInfo() {
		// TODO Auto-generated method stub
		if (mChangedInfoList.size() == 0) {
			return;
		}
		// 1.导出信息
		String content = dumpLogInfoWithHtml();
		
		// 2. 更新收件人与标题
		mMailSender.updateMailToAndCc(mMailReceivers, mMailCcReceivers);
		
		Logger.println("==SvnLog send emal with subject " + mMailSubject);
		
		// 3.发送信息send
		mMailSender.sendHtmlMail(mMailSubject, content);
		
		// 4.清理此轮信息
		clearInfo();
	}
	
	private void clearInfo() {
		mChangedInfoList.clear();
	}
	
	private String dumpLogInfoWithHtml() {
		StringBuilder changedInfo = new StringBuilder(100*1024);
		String tips = "<p style=\"font-family:Helvetica, 'Microsoft Yahei', verdana;font-size:13.63636302947998px;\">"
				+ "若您不想收到此邮件，请回复邮件给我，取消通知。"
				+ "</p>";
		changedInfo.append(createAuthorInfo())
					.append(createFileInfo())
					.append(createPathInfo())
					.append(tips);
		
		return changedInfo.toString();
	}
	
	private String createAuthorInfo() {
		StringBuilder authorBuilder = new StringBuilder(500);
		final String endLine = "<p>	<span style=\"line-height:1.5;\"><br /></span> </p>"; 
		final String sperateLine = "<hr />";
		
		String authorTitle = "<p style=\"font-family:Helvetica, 'Microsoft Yahei', verdana;font-size:13.63636302947998px;\">"
				+ "<strong><span style=\"font-size:16px;\">关键作者提交：</span></strong></p>";
		
		StringBuilder tmpAuthorBuilder = new StringBuilder(10*1024);
		for (SvnChangedInfoBean changedBean : mChangedInfoList) {
			if (changedBean.type == ISvnLogChangedObserver.EVENT_AUTHOR_COMMITED) {
				tmpAuthorBuilder.append("<p style=\"font-family:Helvetica, 'Microsoft Yahei', verdana;font-size:13.63636302947998px;\">")
				.append("<span style=\"color:#E53333;font-size:24px;\"><strong>")
				.append(changedBean.author)
				.append("</strong></span><span style=\"line-height:1.5;\">&nbsp;进行了修改提交。</span> </p>")
				.append("<p>svn版本号：")
				.append("<span style=\"font-size:18px;color:#003399;\"><strong>")
				.append(changedBean.revision)
				.append("</strong></span></p>")
				.append("<p style=\"font-family:Helvetica, 'Microsoft Yahei', verdana;font-size:13.63636302947998px;\">	提交细节：</p>")
				.append("<p style=\"font-family:Helvetica, 'Microsoft Yahei', verdana;font-size:13.63636302947998px;\">")
				.append(changedBean.detail)
				.append("</p>");
				
				tmpAuthorBuilder.append(endLine);
			}
		}
		
		if (tmpAuthorBuilder.length() > 0) {
			authorBuilder.append(authorTitle);
			authorBuilder.append(tmpAuthorBuilder);
			authorBuilder.append(sperateLine);
		}
		return authorBuilder.toString();
	}

	private String createFileInfo() {
		StringBuilder fileBuilder = new StringBuilder(20*1024);
		final String endLine = "<p>	<span style=\"line-height:1.5;\"><br /></span> </p>"; 
		final String sperateLine = "<hr />";
		
		String fileTitle = "<p style=\"font-family:Helvetica, 'Microsoft Yahei', verdana;font-size:13.63636302947998px;\">"
				+ "<strong><span style=\"font-size:16px;\">关键文件修改：</span></strong></p>";
		
		StringBuilder tmpBuilder = new StringBuilder(10*1024);
		for (SvnChangedInfoBean changedBean : mChangedInfoList) {
			if (changedBean.type == ISvnLogChangedObserver.EVENT_FILE_CHANGED) {
				tmpBuilder.append("<p style=\"font-family:Helvetica, 'Microsoft Yahei', verdana;font-size:13.63636302947998px;\">")
				.append("<span style=\"color:#E53333;font-size:24px;\"><strong>")
				.append(changedBean.file)
				.append("</strong></span>")
				.append("被<span style=\"font-size:18px;color:#003399;\"><strong>")
				.append(changedBean.author)
				.append("</strong></span>修改。</p>")
				.append("<p>svn版本号：")
				.append("<span style=\"font-size:18px;color:#003399;\"><strong>")
				.append(changedBean.revision)
				.append("</strong></span></p>");
				
				tmpBuilder.append(endLine);
			}
		}
		
		if (tmpBuilder.length() > 0) {
			fileBuilder.append(fileTitle);
			fileBuilder.append(tmpBuilder);
			fileBuilder.append(sperateLine);
		}
		return fileBuilder.toString();
	}
	
	private String createPathInfo() {
		StringBuilder fileBuilder = new StringBuilder(20*1024);
		final String endLine = "<p>	<span style=\"line-height:1.5;\"><br /></span> </p>"; 
		final String sperateLine = "<hr />";
		
		String fileTitle = "<p style=\"font-family:Helvetica, 'Microsoft Yahei', verdana;font-size:13.63636302947998px;\">"
				+ "<strong><span style=\"font-size:16px;\">关键路径修改：</span></strong></p>";
		
		StringBuilder tmpBuilder = new StringBuilder(10*1024);
		for (SvnChangedInfoBean changedBean : mChangedInfoList) {
			if (changedBean.type == ISvnLogChangedObserver.EVENT_PATH_CHANGED) {
				tmpBuilder.append("<p style=\"font-family:Helvetica, 'Microsoft Yahei', verdana;font-size:13.63636302947998px;\">")
				.append("<span style=\"color:#E53333;font-size:24px;\"><strong>")
				.append(changedBean.path)
				.append("</strong></span>")
				.append("被<span style=\"font-size:18px;color:#003399;\"><strong>")
				.append(changedBean.author)
				.append("</strong></span>修改。</p>")
				.append("<p>svn版本号：")
				.append("<span style=\"font-size:18px;color:#003399;\"><strong>")
				.append(changedBean.revision)
				.append("</strong></span></p>");
				
				tmpBuilder.append(endLine);
			}
		}
		
		if (tmpBuilder.length() > 0) {
			fileBuilder.append(fileTitle);
			fileBuilder.append(tmpBuilder);
			fileBuilder.append(sperateLine);
		}
		return fileBuilder.toString();
		
	}
	

	public class SvnChangedInfoBean {
		public int type;
		public String author;
		public String file;
		public String path;
		public String revision;
		public String detail;
		
		public SvnChangedInfoBean(int eventType, String auth, String changedFile, 
				String changedPath, String svnCode, String commitDetail) {
			type = eventType;
			author = auth;
			file = changedFile;
			path = changedPath;
			revision = svnCode;
			detail = commitDetail;
		}
	}
	
}
