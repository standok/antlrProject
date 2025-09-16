package com.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.svc.IConvertSvc;
import com.svc.impl.ConvertSvc;
import com.util.DataManager;
import com.util.Log;
import com.util.PropertyManager;

public class ConvertSourceMain {

	static IConvertSvc convertSvc = new ConvertSvc();

	public static void main(String[] args) {

		// DataManager 초기화(임시)
		String tmp = DataManager.getNewColumnId("TB_IIS_ACIF_I_A", "ACNM");
		//Log.debug("===[ACNM]=====>["+tmp+"]");

		// 소스 폴더 setting
		String filePath = System.getProperty("user.dir") + PropertyManager.getProperty("FILE_PATH");
		File dir = new File(filePath);

		List<File> javaFileList = new ArrayList<File>();
		List<File> sqlFileList = new ArrayList<File>();

		String[] fileNames = dir.list();
		for(String fileName : fileNames) {
//			if(fileName.toUpperCase().endsWith(".JAVA")) {
			if(fileName.toUpperCase().endsWith("DAO.JAVA")) {	// DAO만 조회
				javaFileList.add(new File(filePath + fileName));
			}
			if(fileName.toUpperCase().endsWith(".SQL")
				&& !fileName.toUpperCase().endsWith("_NEW.SQL")) {
				sqlFileList.add(new File(filePath + fileName));
			}
		}

		Log.debug("============================================================");
		Log.debug("==                   ConvertSourceMain                    ==");
		Log.debug("============================================================");
		Log.debug("convert Java File 전체건수 = ["+javaFileList.size()+"]");
		Log.debug("convert Sql File 전체건수 = ["+sqlFileList.size()+"]");
		Log.debug("============================================================");

		// Java 파일 Parsing
		for(File file : javaFileList) {
			convertJavaSource(file);
		}

		// SQL 파일 Parsing
		for(File file : sqlFileList) {
//			convertSQLSource(file);
		}
	}

	/**
	 * 설명 : Java File Convert
	 *
	 * @param File
	 * @return
	 * @throws
	 */
	public static void convertJavaSource(File file) {

		Log.debug("ANTLR4 JavaParser: Release 0.1 - Production on Nov 20 2024 ");
		Log.debug("Copyright (c) 2024, IBK System co., Ltd. All Rights Reserved.");
		Log.debug("[Antlr] Java File Name : ["+file.getPath()+"]\n");

		double startTime = System.currentTimeMillis();

		try {
			convertSvc.convertFileToString(file);
		} catch (IOException e) {
			Log.error(e);
		} catch (Exception e) {
			Log.error(e);
		}

		double endTime = System.currentTimeMillis();
		double diff = (endTime - startTime) / 1000;

		Log.debug("=============================================");
		Log.debug("convertJavaSource finished time ["+diff+"/Sec]");
		Log.debug("=============================================");
	}

	/**
	 * 설명 : SQL File Convert
	 *
	 * @param File
	 * @return
	 * @throws
	 */
	public static void convertSQLSource(File file) {

		Log.debug("ANTLR4 SQLParser: Release 0.1 - Production on Nov 20 2024");
		Log.debug("Copyright (c) 2024, IBK System co., Ltd. All Rights Reserved.");
		Log.debug("[Antlr] SQL File Name : ["+file.getPath()+"]");

		double startTime = System.currentTimeMillis();

		try {
			convertSvc.convertFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		double endTime = System.currentTimeMillis();
		double diff = (endTime - startTime) / 1000;

		Log.debug("=============================================");
		Log.debug("convertSQLSource finished time ["+diff+"/Sec]");
		Log.debug("=============================================");
	}
}
