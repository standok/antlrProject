package com.svc.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.domain.DataMapDefinitionVo;
import com.svc.IPoiSvr;
import com.util.Log;

public class PoiSvr implements IPoiSvr {

	@Override
	public List<DataMapDefinitionVo> readExcel(File file) throws IOException {
		
		List<DataMapDefinitionVo> rtnList = new ArrayList<DataMapDefinitionVo>();
		DataMapDefinitionVo temp = new DataMapDefinitionVo();
		
		String filePath = "";

		InputStream in = new FileInputStream(file);
		
        Workbook workbook = null;

        try {
        			
            // 엑셀 97 - 2003 까지는 HSSF(xls),  엑셀 2007 이상은 XSSF(xlsx)
            if (file.getName().endsWith(".xls")) {
            	workbook = new HSSFWorkbook(in);
            } else {
                workbook = new XSSFWorkbook(in);
            }
        	
            // 엑셀파일에서 첫번째 시트 불러오기
            Sheet worksheet = workbook.getSheet("테이블정의서");
            
            // getPhysicalNumberOfRow 는 행의 갯수를 불러오는 매소드
            for (int i = 6; i < worksheet.getPhysicalNumberOfRows(); i++) {

            	// DataMapDefinitionVo 초기화
            	temp = new DataMapDefinitionVo();

            	// i번째 행 정보 가져오기
                Row row = worksheet.getRow(i);

                if (row != null) {

                	String etc = getValue(row.getCell(16));	// 비고
                	
                	if(null == etc || !"유지".equals(etc)) continue;
                	
                	/**
                	 *  엑셀 데이터 입력
                	 */
                	temp.setOldTableId(getValue(row.getCell(6)));		// (구)영문테이블ID-대문자
                	temp.setOldTableName(getValue(row.getCell(7)));		// [1](구)한글테이블명
                	temp.setOldColumnId(getValue(row.getCell(9)));		// [2](구)영문필드ID-대문자
                	temp.setOldColumnName(getValue(row.getCell(10)));	// [3](구)한글필드명
                	temp.setOldDataType(getValue(row.getCell(11)));		// [4](구)데이터속성
                	temp.setOldDataLength(getValue(row.getCell(12)));	// [5](구)데이터길이

                	temp.setNewTableId(getValue(row.getCell(18)));		// (신)영문테이블ID-대문자
                	temp.setNewTableName(getValue(row.getCell(19)));		// [6](신)한글테이블명
                	temp.setNewColumnId(getValue(row.getCell(21)));		// [7](신)영문필드ID-대문자
                	temp.setNewColumnName(getValue(row.getCell(22)));	// [8](신)한글필드명
                	temp.setNewDataType(getValue(row.getCell(23)));		// [9](신)데이터속성
                	temp.setNewDataLength(getValue(row.getCell(24)));	// [10](신)데이터길이
                	
                	String 적용상태 = getValue(row.getCell(28));			// 적용상태(준용, 신청중)

                	temp.setConvert(false);								// [11]전환여부
                	if(!"준용".equals(적용상태) && !"".equals(적용상태.trim())) temp.setConvert(true);
                	
                	temp.setConvertRule("");							// [12]변환 규칙
                }
                rtnList.add(temp);
            }
            
            workbook.close();
        	
        } catch (Exception e) {
//        	throw new BizException(e);
        	Log.error(e.getStackTrace().toString());
        }
        
        return rtnList;
	}
	
	@Override
	public void writeExcel(Map<String, String> columnMap) {
		
		
	}
	
	public String getValue(Cell cell) {

        // 날짜포맷
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
        // return 변수
        String value = "";
		
		if (cell == null) {
            return null;
        } else {
            // 타입별로 내용 읽기
            switch (cell.getCellType()) {
               case FORMULA:
                  value = cell.getCellFormula();
                  break;
               case NUMERIC:
                  if (HSSFDateUtil.isCellDateFormatted(cell)) { // 숫자- 날짜 타입이다.
                       value = formatter.format(cell.getDateCellValue());
                  } else {
                       double numericCellValue = cell.getNumericCellValue();
                       value = String.valueOf(numericCellValue);
                       if (numericCellValue == Math.rint(numericCellValue)) {
                           value = String.valueOf((int) numericCellValue);
                       } else {
                           value = String.valueOf(numericCellValue);
                       }
                  }
                  break;
               case STRING:
                  value = cell.getStringCellValue() + "";
                  break;
               case BLANK:
                  value = cell.getBooleanCellValue() + "";
                  break;
               case ERROR:
                  value = cell.getErrorCellValue() + "";
                  break;
               default:
                  value = cell.getStringCellValue();
                  break;
            }                                  
        }
		return value;
	}

}
