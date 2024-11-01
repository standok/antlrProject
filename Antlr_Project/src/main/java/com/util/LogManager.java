package com.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LogManager {

	static {
		// log4j 폴더 setting
		String configFile = System.getProperty("user.dir") + "/config/log4j.properties";
		PropertyConfigurator.configure(configFile);
		
		FileInputStream fi = null;
		
		try {
			fi = new FileInputStream(new File(configFile));
			
			Properties prop = new Properties();
			prop.load(fi);
		} catch(IOException ioe) {
			Logger.getLogger(LogManager.class).error("Log4j configuration failed : "+ioe.getMessage(), ioe);
		} finally {
			try {if(fi != null) {fi.close();}} catch(IOException e) {}
		}
	}
	
	public static Logger getLogger(String name) {
		return Logger.getLogger(name);
	}
	
	public static Logger getLogger(Class clazz) {
		return Logger.getLogger(clazz);
	}
	
	public static Logger getRootLogger() {
		return Logger.getRootLogger();
	}
	
}
