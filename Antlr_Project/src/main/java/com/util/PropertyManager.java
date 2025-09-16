package com.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

public class PropertyManager {
	private static Properties prop;

	static {
		reload();
	}

	private PropertyManager() {

	}

	public static String getProperty(String propertyName) {
		return prop.getProperty(propertyName);
	}

	public static synchronized void reload() {
		FileInputStream fi = null;

		try {
			fi = new FileInputStream(new File(System.getProperty("user.dir")+"/config/main.properties"));

			prop = new Properties();
			prop.load(fi);


		} catch(IOException ioe) {
			Logger.getLogger(PropertyManager.class).error("main.properties load failed : "+ioe.getMessage(), ioe);
		} finally {
			try {if(fi != null) {fi.close();}} catch(IOException e) {}
		}

		// Log
		if(prop != null) {
			Log.debug("=========================================");
			Log.debug("==          Properties Check           ==");
			Log.debug("=========================================");
			Set<String> propertyNames = prop.stringPropertyNames();
            for (String name : propertyNames) {
                // 로거를 사용하여 속성 출력
                Log.debug(name + "=" + prop.getProperty(name));
            }
            Log.debug("==========================================\n");
		}
	}
}
