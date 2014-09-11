package com.daqi.tools.toolunit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.daqi.tools.pmhelper.PMHelperMain0;

public class FileUtils {

	public static void copyDir(String dirFrom, String dirTo) {

		Logger.println("copyDir from " + dirFrom + ", to " + dirTo);
		//新建目标目录
        (new File(dirTo)).mkdirs();
        //获取源文件夹当下的文件或目录
        File[] file=(new File(dirFrom)).listFiles();
        for (int i = 0; i < file.length; i++) {
            if(file[i].isFile()){
                //源文件
                String sourceFile = file[i].getAbsolutePath();
                //目标文件
                String targetFile = new File(dirTo).getAbsolutePath() + File.separator + file[i].getName();
                copyFile(sourceFile, targetFile);
            }
            
            if(file[i].isDirectory()){
                //准备复制的源文件夹
                String dir1 = dirFrom + File.separator + file[i].getName();
                //准备复制的目标文件夹
                String dir2 = dirTo + File.separator  + file[i].getName();
                copyDir(dir1, dir2);
            }
        }
	}
	
	
	public static void copyFile(String fileFrom, String fileTo) {
		File srcFile = new File(fileFrom);
		if (!srcFile.exists()) {
			return;
		}
		File destFile = new File(fileTo);
		if (destFile.exists()) {
			destFile.delete();
		}
		try {
			destFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FileInputStream in = null;  
		FileOutputStream out = null;  
		try {  
			in = new FileInputStream(fileFrom);  
			out = new FileOutputStream(fileTo);
            byte[] bt = new byte[5 * 1024];  
            int count;  
            while ((count = in.read(bt)) > 0) {  
                out.write(bt, 0, count);  
            }  
        } catch (IOException ex) {
        	ex.printStackTrace();
        }  finally {
        	try {
        		if (in != null) {
        			in.close();  
        		}
        		if (out != null) {
        			out.close();  
        		}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
        }
	}
	
	public static void resetDir(String path) {
		delDir(path);
		
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}
	
	public static void delDir(String path) {
		File dir = new File(path);
		if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i < children.length; i++) {
            	delDir(path + "/" + children[i]);
            }
		}
		dir.delete();
	}
	
	public static void delFile(String filePath) {
		File file = new File(filePath);
		file.delete();
	}
	
	public static String readFileToString(String fileName) {
		
		if ( fileName == null || !new File(fileName).exists() ) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder(80*1024);
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line = null;
			String hideInfo = "style=\"display: none";   		//隐藏了bug详情信息
			String showInfo = "style=\"display: table";			//直接展示bug详情信息
			while((line = br.readLine()) != null){
				if (line.contains(hideInfo)) {
					line = line.replace(hideInfo, showInfo);
				}
				sb.append(line);
			}
			br.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		
		String str = FileUtils.readFileToString(args[0]);
		
	}
	
}
