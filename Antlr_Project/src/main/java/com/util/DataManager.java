package com.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.domain.DataMapDefinitionVo;
import com.svc.IPoiSvr;
import com.svc.impl.PoiSvr;

public class DataManager {
	
	private static List<DataMapDefinitionVo> dataMapDefinitionVoList = null;
//	private static Map<String, DataMapDefinitionVo> dataMapDefinitionVoMap = null;
	private static HashMap dataMapDefinitionVoMap = null;
	
	private static int tableNum = 0;
	private static int totNum = 0;
	
	static {
		//setDataMapDefinitionVoList();
		setDataMapDefinitionVoMap();
	}
	
	private static void setDataMapDefinitionVoList() {

		Log.debug("setDataMapDefinitionVoList start");
		
		String excelFilePath = System.getProperty("user.dir") + "/data/data.xlsx";
		File excelFile = new File(excelFilePath);

		IPoiSvr poiSvr = new PoiSvr();

		try {
			dataMapDefinitionVoList = poiSvr.readExcelToList(excelFile);
		} catch (Exception e) {
			Log.error(e);
		}		
	}
	
	private static void setDataMapDefinitionVoMap() {

		Log.debug("setDataMapDefinitionVoMap start");
		
		double beforeTime = System.currentTimeMillis();
		
		String excelFilePath = System.getProperty("user.dir") + "/data/data.xlsx";
		File excelFile = new File(excelFilePath);

		IPoiSvr poiSvr = new PoiSvr();

		try {
			dataMapDefinitionVoMap = poiSvr.readExcelToMap(excelFile);
			tableNum = dataMapDefinitionVoMap.size();
			for(int i=0; i<dataMapDefinitionVoMap.size(); i++) {
				HashMap tmpMap = (HashMap) dataMapDefinitionVoMap.get(i);
				totNum = totNum + tmpMap.size();
			}
		} catch (Exception e) {
			Log.error(e);
		}
		double afterTime = System.currentTimeMillis();
		
		double diffTime = afterTime - beforeTime;

		Log.debug("=======================");
		Log.debug("== 엑셀 파일읽기 finished time ["+diffTime/1000+"/Sec]");
		Log.debug("== 테이블건수 tableNum : ["+tableNum+"]");
		Log.debug("== 전체건수 totNum : ["+totNum+"]");
		Log.debug("=======================");
	}
	
	public static void refresh() {
		setDataMapDefinitionVoMap();
	}
	
	public static String getnewTableId(String oldTableId) {
		String newTableId = "";
//		try {
//			if(oldTableId==null || oldTableId.trim().equals("")) {
//				return "";
//			}
//			return (String) ((HashMap) dataMapDefinitionVoMap.get(newTableId)).get(0).get;
//		} catch (Exception e) {
//			Log.error(e);
//		}
		return newTableId;				
	}
	
	public static String getNewColumnId(String oldTableId, String oldColumnId) {
		String newColumnId = "";
		try {
			
			if(oldTableId==null || oldTableId.trim().equals("")) {
				return "";
			}
			
			if(oldColumnId==null || oldColumnId.trim().equals("")) {
				return "";
			}
			
			DataMapDefinitionVo rtnVo = (DataMapDefinitionVo) ((HashMap) dataMapDefinitionVoMap.get(oldTableId)).get(oldColumnId);
			if( rtnVo != null ) newColumnId = rtnVo.getNewColumnId().trim();
			
		} catch (Exception e) {
			Log.error(e);
		}		
		return newColumnId;				
	}
}
