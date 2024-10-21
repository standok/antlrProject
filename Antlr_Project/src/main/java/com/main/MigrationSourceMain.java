package com.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.svc.ITokenInfoSvc;
import com.svc.impl.TokenInfoSvc;
import com.util.Log;

public class MigrationSourceMain {
	
	static ITokenInfoSvc tokenInfoSvc = new TokenInfoSvc();
	
	public static void main(String[] args) {
		
		String DATA_DIRECTORY = System.getProperty("user.dir") + "/query/";
		File dir = new File(DATA_DIRECTORY);
		
		List<File> sqlFileList = new ArrayList<File>();
		
		String[] fileNames = dir.list();
		for(String fileName : fileNames) {
			if(fileName.toUpperCase().endsWith(".SQL")) {
				sqlFileList.add(new File(DATA_DIRECTORY + fileName));
			}
		}
		
		for(File file : sqlFileList) {
			Log.debug("startMigrationSource ÆÄÀÏ : "+file.getName());
			startMigrationSource(file);
		}
	}
	
	public static void startMigrationSource(File file) {
		
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
		
		Log.debug("================================");
		Log.debug("Finished ["+diffTime/1000+"/Sec]");
		Log.debug("================================");
	}
}
