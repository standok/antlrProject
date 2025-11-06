package com.svc.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.svc.ICreateFileSvr;
import com.util.Log;
import com.util.LogManager;
import com.util.PropertyManager;

public class CreateFileSvr implements ICreateFileSvr {

	// 파일생성 여부
	static boolean createFileYn = PropertyManager.getProperty("CREATE_FILE_YN").equals("Y")?true:false;

	@Override
	public void createJavaFile(String filePath, StringBuilder str) throws IOException {
		Log.printMethod("[START]");

		String oldPath = PropertyManager.getProperty("OLD_FILE_PATH").replace("/", "\\");
		String newPath = PropertyManager.getProperty("NEW_FILE_PATH").replace("/", "\\");
//		Log.debug("[oldPath]=>["+oldPath+"]");
//		Log.debug("[newPath]=>["+newPath+"]");

		String resultFilePath = filePath.replace(oldPath, newPath)
		                        .replace(".java", "")
		                        .replace(".JAVA", "")+".java";

//		Log.debug("[createFileYn]=>["+createFileYn+"]");
//		Log.debug("[path]=>["+path+"]");
//		Log.debug("[fileName]=>["+fileName+"]");
//		Log.debug("[resultFilePath]=>["+resultFilePath+"]");

		if(createFileYn) createFile(resultFilePath, str);

		Log.printMethod("[END]");
	}

	@Override
	public void createSqlFile(String filePath, StringBuilder str) throws IOException {
		Log.printMethod("[START]");

		String resultFilePath = filePath.replace(".sql", "").replace(".SQL", "")+"_NEW.sql";
		LogManager.getLogger("debug").debug("[resultFilePath]=>["+resultFilePath+"]");

		if(createFileYn) createFile(resultFilePath, str);

		Log.printMethod("[END]");
	}

	private void createFile(String filePath, StringBuilder str) throws IOException {

		// 파일 생성
		File file = new File(filePath);	// File(디렉터리 객체, 파일명)

		if (!file.exists()) {	// 파일이 존재하지 않으면 생성
			try {
				if (file.createNewFile()) {
					System.out.println("파일 생성 성공");
				} else {
					System.out.println("파일 생성 실패");
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {	// 파일이 존재한다면
			System.out.println("파일이 이미 존재합니다.");
//			return;
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			if(str != null) {
				fos.write(str.toString().getBytes());
			}
		} catch(Exception e) {
			Log.error(e);
		} finally {
			try { if(fos != null) fos.close(); } catch(IOException ioe) { Log.error(ioe); }
		}
	}

}
