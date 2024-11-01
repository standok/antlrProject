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

		Log.debug("============================================================");
		Log.debug("[ExcelDataLoad] 엑셀파일에서 테이블정의 데이터 추출 시작");
		
		double beforeTime = System.currentTimeMillis();
		
		String excelFilePath = System.getProperty("user.dir") + "/data/data.xlsx";
		File excelFile = new File(excelFilePath);

		IPoiSvr poiSvr = new PoiSvr();

		try {
			dataMapDefinitionVoMap = poiSvr.readExcelToMap(excelFile);
			
			// 테이블수
			tableNum = dataMapDefinitionVoMap.size();
			
//        	Log.debug(" < Excel Data Check > ");
//        	Log.debug("=======================================================================================");        	
	        for(Object key : dataMapDefinitionVoMap.keySet()) {
	        	String tableName = (String) key;
	        	HashMap<String, DataMapDefinitionVo> tmpMap = (HashMap<String, DataMapDefinitionVo>) dataMapDefinitionVoMap.get(tableName);
	        	
	        	// 컬럼수
	        	totNum = totNum + tmpMap.size();
	        	
	        	// 로그확인
//	        	Log.debug("=======================================================================================");
//	        	Log.debug(" 테이블명 ["+tableName+"]["+tmpMap.size()+"]");
//	        	Log.debug("=======================================================================================");	        	
//	        	Log.debug("테이블ID | 테이블명 | 컬럼ID | 컬럼명 | 타입 | 길이 ===> 테이블명 | 테이블ID | 컬럼ID | 컬럼명 | 타입 | 길이");
//	        	for(Object key2 : tmpMap.keySet()) {
//					DataMapDefinitionVo vo = tmpMap.get((String)key2);
//					Log.debug(ConverterUtil.fillPadValue(vo.getOldTableId(), ' ', false, 30)
//							 +" | "+vo.getOldTableName()
//							 +" | "+ConverterUtil.fillPadValue(vo.getOldColumnId(), ' ', false, 30)
//							 +" | "+vo.getOldColumnName()
//							 +" | "+ConverterUtil.fillPadValue(vo.getOldDataType(), ' ', false, 10)
//							 +" | "+ConverterUtil.fillPadValue(vo.getOldDataLength(), ' ', false, 6)
//							 +" | "+ConverterUtil.fillPadValue(vo.getNewTableId(), ' ', false, 30)
//							 +" | "+vo.getNewTableName()
//							 +" | "+ConverterUtil.fillPadValue(vo.getNewColumnId(), ' ', false, 30)
//							 +" | "+vo.getNewColumnName()
//							 +" | "+ConverterUtil.fillPadValue(vo.getNewDataType(), ' ', false, 10)
//							 +" | "+ConverterUtil.fillPadValue(vo.getNewDataLength(), ' ', false, 6)
//					         );
//				}
//	        	Log.debug("=======================================================================================");
	        }
		} catch (Exception e) {
			Log.error(e);
		}
		double afterTime = System.currentTimeMillis();
		
		double diffTime = afterTime - beforeTime;

		Log.debug("[ExcelDataLoad] finished time ["+diffTime/1000+"/Sec]");
		Log.debug("[ExcelDataLoad] 테이블건수 tableNum : ["+tableNum+"]");
		Log.debug("[ExcelDataLoad] 전체건수 totNum : ["+totNum+"]");
	}
	
	public static void refresh() {
//		setDataMapDefinitionVoList();
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
