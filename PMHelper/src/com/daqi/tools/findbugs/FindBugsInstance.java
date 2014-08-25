package com.daqi.tools.findbugs;

import java.io.File;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Set;

import com.daqi.tools.svnmonitor.SVNUtil;
import com.daqi.tools.toolunit.FileUtils;
import com.daqi.tools.toolunit.Logger;


public class FindBugsInstance {
	
	private String mRootWorkspace;
	private String mProjectWorkspace;
	private String mSrc;
	private String mBin;
	private String mFindbugs;
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
		 
		mProjectWorkspace = configLoader.getProperty(FindbugsConfigLoader.FINDBUG_PROJECT_WORKSPACE);
		mRootWorkspace = mProjectWorkspace;
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

	private void copyChangedFileFromLocal(String path) {
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
	
	private void copyChangedFilesFromSvn(String changedPath, long revision) {
		if (changedPath.endsWith(".java")) {
			// 仅仅导出修改的java文件即可
			SVNUtil.exportSingleFile(changedPath, mSrc, revision);
		}
	}
	
	public void clearFindBugsFile() {
		//更新本地bin目录下的class文件
		String tmpChangedBin = mBin.substring(0, mBin.length() - 1);
		FileUtils.copyDir(tmpChangedBin, mProjectWorkspace + "/bin/classes");
		FileUtils.resetDir(mSrc);
		FileUtils.resetDir(mBin);
		FileUtils.resetDir(mFindbugs);
//		FileUtils.delFile(mFindbugsReports);
	}
	
	public void handleChangedFile(Set<String> filePath, long revision) {
		Logger.println("==FindBugs begin to logchanged");
		
		//1.从svn中拷贝修改的文件
		for (String path : filePath) {
//			copyChangedFilesFromLocal(path);
			copyChangedFilesFromSvn(path, revision);
		}
		if (new File(mSrc).list().length == 0) {
			//no file in srcret
			Logger.println("NO File in src to findbugs");
			return;
		}
		
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
		return FileUtils.readFileToString(mFindbugsReports);
	}

}

