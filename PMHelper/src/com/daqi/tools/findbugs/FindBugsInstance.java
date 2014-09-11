package com.daqi.tools.findbugs;

import java.io.File;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Set;

import com.daqi.tools.svnmonitor.SVNUtil;
import com.daqi.tools.toolunit.FileUtils;
import com.daqi.tools.toolunit.Logger;


public class FindBugsInstance {
	
	private String mRootWorkspace;		//根工程目录，也即主工程目录
	private String mProjectWorkspace;	//当前监控工程目录，仅在更新各工程的资源/lib/manifest等文件时才需要，更新对应文件到对应目录中去即可。
	private String mFindbugsSrc;
	private String mFindbugsBin;
	private String mFindbugsTmpFolder;
	private String mFindbugsReports;
	
	private String mAntFindbugs;
	
	public FindBugsInstance(String configFile) {
		// TODO Auto-generated constructor stub
		loadConfig(configFile);
		
		//初始化工作目录；
		initWorkspace();
	}
	
	private void loadConfig(String configFile) {
		//初始化ant配置信息等；
		FindbugsConfigLoader configLoader = new FindbugsConfigLoader(configFile);
		mAntFindbugs = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_KEY_ANT);
		mRootWorkspace = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_ROOT_WORKSPACE);
		mProjectWorkspace = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_PROJECT_WORKSPACE);
	}

	private void initWorkspace() {
		String rootPath = mRootWorkspace + "/changedfiles/";
		mFindbugsSrc = rootPath + "src/";
		mFindbugsBin = rootPath + "bin/";
		mFindbugsTmpFolder = rootPath + "findbugs/";
		mFindbugsReports = rootPath + "report.html";
		FileUtils.resetDir(mFindbugsSrc);
		FileUtils.resetDir(mFindbugsBin);
		FileUtils.resetDir(mFindbugsTmpFolder);
		Logger.println("initWorkspace for " + mFindbugsSrc + ", " + mFindbugsBin);
	}

	private void copyChangedFileFromLocal(String path) {
		Logger.println("FindBugs changedpath = " + path);
		int index = path.indexOf("src");
		if (index > 0) {
			String changedFile = path.substring(index);
			String fileSrc = mProjectWorkspace + "/" + changedFile;
			
			int fileIndex = changedFile.lastIndexOf("/");
			String changedFileName = changedFile.substring(fileIndex + 1);
			String fileDest = mFindbugsSrc + changedFileName;
			Logger.println("FindBugs copy changedfile = from " + fileSrc + ", to " + fileDest);
			FileUtils.copyFile(fileSrc, fileDest);
		}
	}
	
	private void copyChangedFilesFromSvn(String changedPath, long revision) {
		
		// 仅仅导出修改的java文件即可
		
	}
	
	public void clearFindBugsFile() {
		FileUtils.resetDir(mFindbugsBin);
		FileUtils.resetDir(mFindbugsTmpFolder);
		FileUtils.delFile(mFindbugsReports);
	}
	
	public void backupFindBugsFile() {
		//更新本地bin目录下的class文件
		String tmpChangedBin = mFindbugsBin.substring(0, mFindbugsBin.length() - 1);
		FileUtils.copyDir(tmpChangedBin, mRootWorkspace + "/bin/classes");
		FileUtils.resetDir(mFindbugsSrc);
	}
	
	public boolean handleChangedFile(Set<String> filePath, long revision) {
		Logger.println("==FindBugs begin to logchanged");
		boolean result = true;
		//1.从svn中拷贝修改的文件
		for (String path : filePath) {
			if (path.endsWith(".java")) {
//				copyChangedFilesFromLocal(path);
				SVNUtil.exportSingleFile(path, mFindbugsSrc, revision);
			} else if (path.endsWith(".jar")) {
				//jar包有更新，需要更新整个lib包
				int endIndex = path.lastIndexOf("/");
				int fromIndex = path.lastIndexOf("/", endIndex - 1);
				String libPath = path.substring(fromIndex, endIndex);
				String libsDir = mProjectWorkspace + libPath;
				if (!new File(libsDir).exists()) {
					libsDir = mProjectWorkspace + "/lib";
				}
				SVNUtil.updateDirTree(libsDir, revision);
			} else if (path.contains("/res/")) {
				//需要更新res文件并重新appt
				String resDir = mProjectWorkspace + "/res";
				SVNUtil.updateDirTree(resDir, revision);
			} else if (path.endsWith("AndroidManifest.xml")) {
				String xmlDir = mProjectWorkspace + "/AndroidManifest.xml";
				SVNUtil.updateDirTree(xmlDir, revision);
			}
				
		}
		
		if (new File(mFindbugsSrc).list().length == 0) {
			//no file in srcret
			Logger.println("NO File in src to findbugs");
			result = false;
		}
		return result;
	}
	
	/**
	 * 执行指定的Findbugs脚本文件
	 * @return
	 */
	public boolean exeFindBugsShell() {
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
		return result == 0;
	}
	
	public String getFindbugsReport() {
		if (isFindBugsNoBugInstance()) {
			return null;
		} else {
			return FileUtils.readFileToString(mFindbugsReports);
		}
	}
	
	private boolean isFindBugsNoBugInstance() {
		//检查是否不存在bug，避免多次发送。
		boolean result = false;
		String findbugsOut = mRootWorkspace + "/changedfiles/findbugs/out.xml";
		String findbugsResult = FileUtils.readFileToString(findbugsOut);
		if (findbugsResult != null) {
			if ( !findbugsResult.contains("BugInstance") ) {
				//不包含任何BugInstance，则说明没有任何bug，不需要发送报告邮件。
				result = true;
				Logger.println("========================Findbugs 检查结果为空，不需要发送报告邮件。");
			}
		}
		return result;
	}

}

