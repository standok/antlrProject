package com.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.svc.ITokenInfoSvc;
import com.svc.impl.TokenInfoSvc;
import com.util.DataManager;
import com.util.Log;

public class ConvertSourceMain {
	
	static ITokenInfoSvc tokenInfoSvc = new TokenInfoSvc();
	
	public static void main(String[] args) {
		
		// DataManager 초기화(임시)
		String tmp = DataManager.getNewColumnId("", "");
		
		// 소스 폴더 setting
		String DATA_DIRECTORY = System.getProperty("user.dir") + "/files/";
		File dir = new File(DATA_DIRECTORY);
		
		List<File> javaFileList = new ArrayList<File>();
		List<File> sqlFileList = new ArrayList<File>();
		
		String[] fileNames = dir.list();
		for(String fileName : fileNames) {
			if(fileName.toUpperCase().endsWith(".JAVA")) {
				javaFileList.add(new File(DATA_DIRECTORY + fileName));
			}
			if(fileName.toUpperCase().endsWith(".SQL")) {
				sqlFileList.add(new File(DATA_DIRECTORY + fileName));
			}
		}
		
		Log.debug("=============================================");
		Log.debug("convert Java File 전체건수 = ["+javaFileList.size()+"]");
		Log.debug("convert Sql File 전체건수 = ["+sqlFileList.size()+"]");
		Log.debug("=============================================");
		
		// Java 파일 Parsing
		for(int i=0; i<javaFileList.size(); i++) {
			File file = javaFileList.get(i);
			Log.debug("["+i+"]=============================================");
			Log.debug("["+i+"] Java File = ["+file.getName()+"]");
			Log.debug("["+i+"]=============================================");			
			convertJavaSource(file);
		}
		
		// SQL 파일 Parsing
//		for(File file : sqlFileList) {
//			convertSQLSource(file);
//		}
	}
	
	public static void convertJavaSource(File file) {
		
		double beforeTime = System.currentTimeMillis();
		
		try {
			tokenInfoSvc.parsing(file);
		} catch (IOException e) {
			Log.error(e);
		} catch (Exception e) {
			Log.error(e);
		}
		
		double afterTime = System.currentTimeMillis();
		
		double diffTime = afterTime - beforeTime;
		
		Log.debug("=============================================");
		Log.debug("convertJavaSource finished time ["+diffTime/1000+"/Sec]");
		Log.debug("=============================================");
	}
	
	public static void convertSQLSource(File file) {
		
		double beforeTime = System.currentTimeMillis();
		
		try {
			tokenInfoSvc.parsing(file);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double afterTime = System.currentTimeMillis();
		
		double diffTime = afterTime - beforeTime;
		
		Log.debug("=============================================");
		Log.debug("convertSQLSource finished time ["+diffTime/1000+"/Sec]");
		Log.debug("=============================================");
	}
}
