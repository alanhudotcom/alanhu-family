package com.daqi.tools.pmhelper;

import java.io.File;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Set;

import org.tmatesoft.svn.core.SVNLogEntry;

import com.daqi.tools.mailsender.SimpleMailSender;
import com.daqi.tools.svnmonitor.SVNUtil;
import com.daqi.tools.toolunit.Converter;
import com.daqi.tools.toolunit.FileUtils;
import com.daqi.tools.toolunit.Logger;


public class SvnLogFindBugsOperator {
	
	SimpleMailSender mMailSender;
	
	private String mRootWorkspace = System.getProperty("user.dir");//".";
	private String mProjectWorkspace = mRootWorkspace;
	private String mSrc;
	private String mBin;
	private String mFindbugs;
	private String mFindbugsReports;
	private boolean mNotRunFindbugs = true;
	
	private String mAntFindbugs;
	
	private String mMailReceivers;
	private String mMailCcReceivers;
	private String mMailSubject;
	private String mMailComAddress;
	
	public SvnLogFindBugsOperator(SimpleMailSender mailSender, String configFile) {
		// TODO Auto-generated constructor stub
		mMailSender = mailSender;
		loadConfig(configFile);
	}
	
	private void initWorkspace() {
		String rootPath = mRootWorkspace + "/changedfiles/";
		mSrc = rootPath + "src/";
		mBin = rootPath + "bin/";
		mFindbugs = rootPath + "findbugs/";
		mFindbugsReports = rootPath + "report.html";
		FileUtils.resetDir(mSrc);
		FileUtils.resetDir(mBin);
		FileUtils.resetDir(mFindbugs);
		Logger.println("initWorkspace for " + mSrc + ", " + mBin);
	}
	
	private void loadConfig(String configFile) {
		//初始化获取邮件标题；
		//初始化ant配置信息等；
		FindbugsConfigLoader configLoader = new FindbugsConfigLoader(configFile);
		mAntFindbugs = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_KEY_ANT);
		String runFindBugs = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_RUN);
		if (runFindBugs.equals("true")) {
			mNotRunFindbugs = false;
		}
		 
		mProjectWorkspace = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_PROJECT_WORKSPACE);
		mRootWorkspace = mProjectWorkspace;
		mMailReceivers = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_MAIL_RECEIVERS);
		mMailCcReceivers = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_MAIL_CCREIVERS);
		mMailComAddress = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_MAIL_COM_ADDRESS);
		String subject  = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_MAIL_SUBJECT);
		if (subject != null && !subject.equals("")) {
			subject = Converter.suppoetTozh(subject);
		}
		mMailSubject = subject;
	}
	
	public void doPrepare() {
		//初始化工作目录；
		initWorkspace();
	}
	

	private void copyChangedFile(String path) {
		Logger.println("FindBugs changedpath = " + path);
		int index = path.indexOf("src");
		if (index > 0) {
			String changedFile = path.substring(index);
			String fileSrc = mProjectWorkspace + "/" + changedFile;
			
			int fileIndex = changedFile.lastIndexOf("/");
			String changedFileName = changedFile.substring(fileIndex + 1);
			String fileDest = mSrc + changedFileName;
			Logger.println("FindBugs copy changedfile = from " + fileSrc + ", to " + fileDest);
			FileUtils.copyFile(fileSrc, fileDest);
		}
	}
	
	private void copyChangedFile2(String changedPath, long revision) {
		if (changedPath.endsWith(".java")) {
			// 仅仅导出修改的java文件即可
			SVNUtil.exportSingleFile(changedPath, mSrc, revision);
		}
	}
	
	public void logChanged(SVNLogEntry logEntry) {
		if (mNotRunFindbugs) {
			return;
		}
		
		Logger.println("==FindBugs begin to logchanged");
		
		//1.拷贝修改的文件
		Set<String> changedPath = logEntry.getChangedPaths().keySet();
		for (String path : changedPath) {
//			copyChangedFile(path);
			copyChangedFile2(path, logEntry.getRevision());
		}
		//2.执行脚本文件
		
		if (new File(mSrc).list().length == 0) {
			//no file in srcret
			Logger.println("NO File in src to findbugs");
			return;
		}
		
		if (exeFindBugsShell() == 0) {
			//3.发送邮件
			sendReport(logEntry);
			//更新本地bin目录下的class文件
			String tmpChangedBin = mBin.substring(0, mBin.length() - 1);
			FileUtils.copyDir(tmpChangedBin, mProjectWorkspace + "/bin/classes");
			FileUtils.resetDir(mSrc);
			FileUtils.resetDir(mBin);
			FileUtils.resetDir(mFindbugs);
			FileUtils.delFile(mFindbugsReports);
		};
		
	}
	
	public void runFindbugs() {
		exeFindBugsShell();
	}
	
	private int exeFindBugsShell() {
		int result = -1;
		try {
			Process process = Runtime.getRuntime().exec(mAntFindbugs);
			InputStreamReader ir = new InputStreamReader(process.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			String line;
			while((line = input.readLine()) != null) {
				System.out.println(line);
			}
			int exitVaule = process.waitFor();
			Logger.println("=======FindBugs Process exitvalue = " + exitVaule);
			input.close();
			ir.close();
			result = exitVaule;//process.exitValue();
		} catch (Exception e) {
			Logger.println("exeFindBugsShell", e);
		}
		return result;
	}
	
	private void sendReport(SVNLogEntry logEntry) {
		String content = getFindbugsReport();
		if (content == null) {
			return;
		}
		String to = logEntry.getAuthor() + mMailComAddress;
		System.out.println("send findbugs to " + logEntry.getAuthor());
		if (mMailReceivers != null && !mMailReceivers.equals("")) {
			to += ",";
			to += mMailReceivers;
		}
		mMailSender.updateMailToAndCc(to, mMailCcReceivers);

		StringBuilder tmpBuilder = new StringBuilder(500);
		tmpBuilder.append("<p style=\"font-family:Helvetica, 'Microsoft Yahei', verdana;font-size:13.63636302947998px;\">")
		.append("<span style=\"color:#E53333;font-size:24px;\"><strong>")
		.append(logEntry.getAuthor())
		.append("</strong></span><span style=\"line-height:1.5;\">&nbsp;进行了修改提交。</span> </p>")
		.append("<p>svn版本号：")
		.append("<span style=\"font-size:18px;color:#003399;\"><strong>")
		.append(logEntry.getRevision())
		.append("</strong></span></p>")
		.append("<p><span style=\"line-height:1.5;\">FindBugs 结果为：</span> </p>");
		
		final String tips = "<hr /><p style=\"font-family:Helvetica, 'Microsoft Yahei', verdana;font-size:13.63636302947998px;\">"
				+ "若您不想收到此邮件，请回复邮件给我，取消通知。"
				+ "</p>";
		StringBuilder sBuilder = new StringBuilder(100*1024);
		sBuilder.append(tmpBuilder);
		sBuilder.append(content);
		sBuilder.append(tips);
		mMailSender.sendHtmlMail(mMailSubject, sBuilder.toString());
	}
	
	private String getFindbugsReport() {
		return FileUtils.readFileToString(mFindbugsReports);
	}
	
	
	private void testLogChanged(String path) {
		
		copyChangedFile(path);
		//2.执行脚本文件
		exeFindBugsShell();
		
		//3.发送邮件
//		sendReport();
	}
	
	public static void main(String[] args) {
		String path2 = System.getProperty("user.dir") + "/build-findbugs.sh";
		System.out.println("===path=" + path2);
//		new SvnLogFindBugsOperator().exeFindBugsShell(path);
		SimpleMailSender sender = new SimpleMailSender("monitor_golauncher.property");
		sender.doPrepareWork();
		SvnLogFindBugsOperator findBugs = new SvnLogFindBugsOperator(sender, "monitor_golauncher.property");
		findBugs.doPrepare();
		
		String path = "/home/huyong/workspace/work5/SourceCode/Project/OPENSOURCE/SVNMonitor/src/com/daqi/tools/pmhelper/SvnLogChangedObserverByEmail.java";
		System.out.println("===========path====" + path);
		
//		findBugs.testLogChanged(path);
//		findBugs.sendReport();
    }
	
}

