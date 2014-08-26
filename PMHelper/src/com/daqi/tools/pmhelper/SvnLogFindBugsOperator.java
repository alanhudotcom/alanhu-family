package com.daqi.tools.pmhelper;

import java.util.Set;

import org.tmatesoft.svn.core.SVNLogEntry;

import com.daqi.tools.core.AbsSvnLogOperator;
import com.daqi.tools.findbugs.FindBugsInstance;
import com.daqi.tools.findbugs.FindbugsConfigLoader;
import com.daqi.tools.mailsender.SimpleMailSender;
import com.daqi.tools.toolunit.Converter;


public class SvnLogFindBugsOperator extends AbsSvnLogOperator {
	
	SimpleMailSender mMailSender;
	
	private FindBugsInstance mFindbugsInstance;
	
	private boolean mNotRunFindbugs = true;
	
	private String mMailReceivers;
	private String mMailCcReceivers;
	private String mMailSubject;
	private String mMailComAddress;
	
	public SvnLogFindBugsOperator(SimpleMailSender mailSender, String configFile) {
		// TODO Auto-generated constructor stub
		mMailSender = mailSender;
		
		mFindbugsInstance = new FindBugsInstance(configFile);
		loadConfig(configFile);
	}
	
	private void loadConfig(String configFile) {
		//初始化获取邮件标题；
		//初始化ant配置信息等；
		FindbugsConfigLoader configLoader = new FindbugsConfigLoader(configFile);
		String runFindBugs = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_RUN);
		if (runFindBugs.equals("true")) {
			mNotRunFindbugs = false;
		}
		 
		mMailReceivers = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_MAIL_RECEIVERS);
		mMailCcReceivers = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_MAIL_CCREIVERS);
		mMailComAddress = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_MAIL_COM_ADDRESS);
		String subject  = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_MAIL_SUBJECT);
		if (subject != null && !subject.equals("")) {
			subject = Converter.suppoetTozh(subject);
		}
		mMailSubject = subject;
	}
	
	@Override
	public void svnLogPrepare() {
		// TODO Auto-generated method stub
	}

	@Override
	public void svnLogChanged(SVNLogEntry logEntry) {
		// TODO Auto-generated method stub
		logChanged(logEntry);
	}

	@Override
	public void svnLogFinish() {
		// TODO Auto-generated method stub
		// nothing to do
	}

	private void logChanged(SVNLogEntry logEntry) {
		if (mNotRunFindbugs) {
			return;
		}
		
		//1.导出当前版本修改的文件
		Set<String> changedPath = logEntry.getChangedPaths().keySet();
		long revision = logEntry.getRevision();
		boolean handleFileResult = mFindbugsInstance.handleChangedFile(changedPath, revision);
		if (handleFileResult) {
			if (mFindbugsInstance.exeFindBugsShell()) {
				//执行Findbugs脚本成功，则发送邮件并清理现有
				sendFindBugsSuccess(logEntry);
			} else {
				sendFindBugsFailed(logEntry);
			}
		};
		
		mFindbugsInstance.clearFindBugsFile();
	}
	
	private void sendFindBugsSuccess(SVNLogEntry logEntry) {
		String content = mFindbugsInstance.getFindbugsReport();
		if (content == null) {
			return;
		}
		String to = /*logEntry.getAuthor()*/"huyong" + mMailComAddress;
		System.out.println("send findbugs to " + to);
		if (mMailReceivers != null && !mMailReceivers.equals("")) {
			to += ",";
			to += mMailReceivers;
		}
		mMailSender.updateMailToAndCc(to, mMailCcReceivers);
		
		sendReport(logEntry, content);
	}
	
	private void sendFindBugsFailed(SVNLogEntry logEntry) {
		StringBuilder tmpContent = new StringBuilder(500);
		tmpContent.append("<p><span style=\"color:#E53333;font-size:24px;\"><strong>FindBugs编译未通过，本次检查无效！！！</strong></span></p>")
					.append("<p>可能是由于本次修改涉及资源文件或Manifest文件或AIDL或其他依赖关系，")
					.append( "需要您手动编译一次您的工程项目，才能继续进行FindBugs检查。请查看编译结果后，进行手动更新并编译。</p>");

		mMailSender.updateMailToAndCc(null, mMailCcReceivers);
		
		sendReport(logEntry, tmpContent.toString());
	}
	
	private void sendReport(SVNLogEntry logEntry, String content) {
		
		// 修改的文件路径
		Set<String> changedFileSet = logEntry.getChangedPaths().keySet();
		StringBuilder tmpFileBuilder = new StringBuilder(500);
		for (String filePath : changedFileSet) {
			tmpFileBuilder.append("<p><span style=\"font-size:18px;color:#003399;\">")
							.append(filePath)
							.append("</span></p>");
		}
		
		StringBuilder tmpBuilder = new StringBuilder(1000);
		tmpBuilder.append("<p style=\"font-family:Helvetica, 'Microsoft Yahei', verdana;font-size:13.63636302947998px;\">")
		.append("<span style=\"color:#E53333;font-size:24px;\"><strong>")
		.append(logEntry.getAuthor())
		.append("</strong></span><span style=\"line-height:1.5;\">&nbsp;进行了修改提交。</span> </p>")
		.append("<p>svn版本号：")
		.append("<span style=\"font-size:18px;color:#003399;\"><strong>")
		.append(logEntry.getRevision())
		.append("</strong></span></p>")
		.append("<p>修改文件：</p>")
		.append(tmpFileBuilder)
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
	
	/*public static void main(String[] args) {
		String path2 = System.getProperty("user.dir") + "/build-findbugs.sh";
		System.out.println("===path=" + path2);
		SimpleMailSender sender = new SimpleMailSender("monitor_golauncher.property");
		sender.doPrepareWork();
		SvnLogFindBugsOperator findBugs = new SvnLogFindBugsOperator(sender, "monitor_golauncher.property");
		findBugs.svnLogPrepare();
		
		String path = "/home/huyong/workspace/work5/SourceCode/Project/OPENSOURCE/SVNMonitor/src/com/daqi/tools/pmhelper/SvnLogChangedObserverByEmail.java";
		System.out.println("===========path====" + path);
    }*/

}

