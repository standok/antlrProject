package com.util;

import java.io.File;
import java.util.HashMap;

import com.svc.IPoiSvr;
import com.svc.impl.PoiSvr;
import com.vo.DataMapDefinitionVo;

public class DataManager {

	//private static List<DataMapDefinitionVo> dataMapDefinitionVoList = null;
	private static HashMap dataMapDefinitionVoMap = null;
	private static HashMap dataTableMap = null;

	private static int tableNum = 0;
	private static int totNum = 0;

	static {
//		setDataMapDefinitionVoList();
		setDataMapDefinitionVoMap();
	}

//	private static void setDataMapDefinitionVoList() {
//
//		Log.debug("setDataMapDefinitionVoList start");
//
//		String excelFilePath = System.getProperty("user.dir") + "/data/data.xlsx";
//		File excelFile = new File(excelFilePath);
//
//		IPoiSvr poiSvr = new PoiSvr();
//
//		try {
//			dataMapDefinitionVoList = poiSvr.readExcelToList(excelFile);
//		} catch (Exception e) {
//			Log.error(e);
//		}
//	}

	private static void setDataMapDefinitionVoMap() {

		Log.debug("============================================================");
		Log.debug("[ExcelDataLoad] 엑셀파일에서 테이블정의 데이터 추출 시작");

		double beforeTime = System.currentTimeMillis();

		String excelFilePath = System.getProperty("user.dir") + "/data/data.xlsx";
		File excelFile = new File(excelFilePath);

		IPoiSvr poiSvr = new PoiSvr();

		try {
			// 테이블정보
			dataTableMap = new HashMap<String, String>();

			dataMapDefinitionVoMap = poiSvr.readExcelToMap(excelFile);

			// 테이블수
			tableNum = dataMapDefinitionVoMap.size();

//        	Log.debug(" < Excel Data Check > ");
//        	Log.debug("=======================================================================================");
	        for(Object key : dataMapDefinitionVoMap.keySet()) {
	        	String oldTableId = (String) key;
	        	HashMap<String, DataMapDefinitionVo> columnMap = (HashMap<String, DataMapDefinitionVo>) dataMapDefinitionVoMap.get(oldTableId);

	        	// 컬럼수 저장
	        	totNum = totNum + columnMap.size();

	        	// 테이블정보 저장
	        	for(Object key2 : columnMap.keySet()) {
	        		DataMapDefinitionVo vo = columnMap.get(key2);
	        		dataTableMap.put(oldTableId, vo.getNewTableId());
	        		break;
	        	}

	        	// 로그확인
//	        	Log.debug("=======================================================================================");
//	        	Log.debug(" 테이블명 ["+tableId+"]["+tmpMap.size()+"]");
//	        	Log.debug("=======================================================================================");
//	        	Log.debug("테이블ID | 테이블명 | 컬럼ID | 컬럼명 | 타입 | 길이 ===> 테이블명 | 테이블ID | 컬럼ID | 컬럼명 | 타입 | 길이");
//	        	for(Object key2 : tmpMap.keySet()) {
//					DataMapDefinitionVo vo = tmpMap.get((String)key2);
//					Log.debug(ConverterUtil.fillPadValue(vo.getOldTableId(), ' ', false, 30)
//							 +" | "+vo.getOldTableId()
//							 +" | "+ConverterUtil.fillPadValue(vo.getOldColumnId(), ' ', false, 30)
//							 +" | "+vo.getOldColumnName()
//							 +" | "+ConverterUtil.fillPadValue(vo.getOldDataType(), ' ', false, 10)
//							 +" | "+ConverterUtil.fillPadValue(vo.getOldDataLength(), ' ', false, 6)
//							 +" | "+ConverterUtil.fillPadValue(vo.getNewTableId(), ' ', false, 30)
//							 +" | "+vo.getNewTableId()
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

	public static String getNewTableId(String oldTableId) {
		String newTableId = oldTableId+"(없음)";
		try {
			if(oldTableId==null || oldTableId.trim().equals("")) {
				return newTableId;
			}

			if(dataTableMap.size() == 0) {
				return newTableId;
			}

			newTableId = (String) dataTableMap.get(oldTableId);
		} catch (Exception e) {
			Log.error(e);
		}
//		Log.debug("[getNewTableId] : ["+oldTableId+"] => ["+newTableId+"]");
		return newTableId;
	}

	public static String getNewColumnId(String oldTableId, String oldColumnId) {
		String newColumnId = oldColumnId+"(없음)";
		try {

			if(oldTableId==null || oldTableId.trim().equals("")) {
				return newColumnId;
			}

			if(oldColumnId==null || oldColumnId.trim().equals("")) {
				return newColumnId;
			}

			if(dataMapDefinitionVoMap.size() == 0) {
				return newColumnId;
			}

			if(dataMapDefinitionVoMap.get(oldTableId) != null) {
				DataMapDefinitionVo rtnVo = (DataMapDefinitionVo) ((HashMap) dataMapDefinitionVoMap.get(oldTableId)).get(oldColumnId);
				if( rtnVo != null ) newColumnId = rtnVo.getNewColumnId().trim();
			}
		} catch (Exception e) {
			Log.error(e);
		}
		return newColumnId;
	}

	public static DataMapDefinitionVo getDataMapDefinitionVo(String oldTableId, String oldColumnId) {

		DataMapDefinitionVo rtnVo = null;
		try {

			if(oldTableId==null || oldTableId.trim().equals("")) {
				return null;
			}

			if(oldColumnId==null || oldColumnId.trim().equals("")) {
				return null;
			}

	        HashMap<String, DataMapDefinitionVo> tmpMap = (HashMap<String, DataMapDefinitionVo>) dataMapDefinitionVoMap.get(oldTableId);

	        if(tmpMap == null) {
	        	return null;
	        }

	        rtnVo = tmpMap.get(oldColumnId);

		} catch (Exception e) {
			Log.error(e);
		}
		return rtnVo;
	}
}
