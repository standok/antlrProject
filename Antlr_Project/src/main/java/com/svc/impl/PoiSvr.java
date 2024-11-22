package com.svc.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.svc.IPoiSvr;
import com.util.Log;
import com.util.PropertyManager;
import com.vo.DataMapDefinitionVo;

public class PoiSvr implements IPoiSvr {

	/**
	 * 설명 : Excel 파일내용을 읽고 Table정보를 Map으로 변경하여 리턴
	 *
	 * @param File file
	 * @return HashMap<String, HashMap>
	 * @throws IOException
	 */
	@Override
	public HashMap readExcelToMap(File file) throws IOException {

		HashMap rtnMap = new HashMap<String, HashMap>();
		HashMap dataMap = new HashMap<String, DataMapDefinitionVo>();
		DataMapDefinitionVo dataVo = new DataMapDefinitionVo();

        Workbook workbook = null;

        try {
        	InputStream in = new FileInputStream(file);

            // 엑셀 97 - 2003 까지는 HSSF(xls),  엑셀 2007 이상은 XSSF(xlsx)
            if (file.getName().endsWith(".xls")) {
            	workbook = new HSSFWorkbook(in);
            } else {
                workbook = new XSSFWorkbook(in);
            }

			/*********************
			 * 엑셀변수 초기값 세팅하기
			 *********************/
            // 엑셀파일에서 첫번째 시트 불러오기
            Sheet worksheet = workbook.getSheet("테이블정의서");
            int startLine = Integer.parseInt(PropertyManager.getProperty("EXCEL_START_LINE").trim());	// 엑셀시작라인
            int endLine = Integer.parseInt(PropertyManager.getProperty("EXCEL_END_LINE").trim());		// 엑셀마지막라인
            /*********************/

            String currentTableId = "";
            String currentTableName = "";

            // getPhysicalNumberOfRow 는 행의 갯수를 불러오는 매소드
            for (int i = startLine; i < endLine; i++) {

            	// i번째 행 정보 가져오기
                Row row = worksheet.getRow(i);

                if (row != null) {

                	// TODO: 비고가 '유지'가 아닌경우 skip
//                	String etc = getValue(row.getCell(15));	// 비고
//                	if(null == etc || !"유지".equals(etc)) continue;

                	String oldTableId = getValue(row.getCell(5));
                	String oldTableName = getValue(row.getCell(6));
                	String oldColumnId = getValue(row.getCell(8));

                	// currentTableId 초기화
                	if("".equals(currentTableId)) {
                		currentTableId = oldTableId;
                		currentTableName = oldTableName;
                	}

                	// 같은 테이블끼리 rtnMap에 저장
                	// 새로운 테이블이 들어왔을경우 dataMap 초기화
                	if(!"".equals(oldTableId) && !currentTableId.equals(oldTableId)) {
//                		Log.debug("테이블삽입 new["+currentTableId+"],old["+oldTableId+"]");
                		if(rtnMap.containsKey(currentTableId)) {
                			// 중복테이블 임시 Skip
                			if(!(currentTableId.contains("TB_REINV_ERR_BK"))) {
                				throw new Exception("같은테이블존재 확인필 new["+currentTableId+"],old["+oldTableId+"]");
                			}
//                			continue;
                		}
                		rtnMap.put(currentTableId, dataMap);

                		currentTableId = oldTableId;
                		currentTableName = oldTableName;

                		dataMap = new HashMap<String, DataMapDefinitionVo>();
                	}

                	// 엑셀 데이터 입력
                	dataVo = new DataMapDefinitionVo();
                	dataVo.setOldTableId(currentTableId);					// (구)영문테이블ID-대문자
                	dataVo.setOldTableName(currentTableName);				// [1](구)한글테이블명
                	dataVo.setOldColumnId(oldColumnId);						// [2](구)영문필드ID-대문자
                	dataVo.setOldColumnName(getValue(row.getCell(9)));		// [3](구)한글필드명
                	dataVo.setOldDataType(getValue(row.getCell(10)));		// [4](구)데이터속성
                	dataVo.setOldDataLength(getValue(row.getCell(11)));		// [5](구)데이터길이

                	// TODO: 테스트용
//                	dataVo.setNewTableId(getValue(row.getCell(17)));		// (신)영문테이블ID-대문자
//                	dataVo.setNewTableName(getValue(row.getCell(18)));		// [6](신)한글테이블명
//                	dataVo.setNewColumnId(getValue(row.getCell(20)));		// [7](신)영문필드ID-대문자
//                	dataVo.setNewColumnName(getValue(row.getCell(21)));		// [8](신)한글필드명
//                	dataVo.setNewDataType(getValue(row.getCell(22)));		// [9](신)데이터속성
//                	dataVo.setNewDataLength(getValue(row.getCell(23)));		// [10](신)데이터길이
                	dataVo.setNewTableId(dataVo.getOldTableId()+"(NEW)");	// (신)영문테이블ID-대문자
                	dataVo.setNewTableName(dataVo.getOldTableName());		// [6](신)한글테이블명
                	dataVo.setNewColumnId(dataVo.getOldColumnId()+"(NEW)");	// [7](신)영문필드ID-대문자
                	dataVo.setNewColumnName(dataVo.getOldColumnName());		// [8](신)한글필드명
                	dataVo.setNewDataType(dataVo.getOldDataType());			// [9](신)데이터속성
                	dataVo.setNewDataLength(dataVo.getOldDataLength());		// [10](신)데이터길이

                	String 적용상태 = getValue(row.getCell(27));			// 적용상태(준용, 신청중)

                	dataVo.setConvert(false);								// [11]전환여부
                	if(!"준용".equals(적용상태) && !"".equals(적용상태.trim())) dataVo.setConvert(true);

                	dataVo.setConvertRule("");							// [12]변환 규칙

                	dataMap.put(oldColumnId, dataVo);
                }
            }

            // 마지막 테이블 저장
            rtnMap.put(currentTableId, dataMap);

            workbook.close();

        } catch (Exception e) {
        	Log.error(e);
        }

        return rtnMap;
	}

	/**
	 * 설명 : Excel 파일내용을 읽고 Table정보를 List으로 변경하여 리턴
	 *
	 * @param File file
	 * @return List<DataMapDefinitionVo>
	 * @throws IOException
	 */
	@Override
	public List<DataMapDefinitionVo> readExcelToList(File file) throws IOException {

		List<DataMapDefinitionVo> rtnList = new ArrayList<DataMapDefinitionVo>();
		DataMapDefinitionVo temp = new DataMapDefinitionVo();

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

            	// i번째 행 정보 가져오기
                Row row = worksheet.getRow(i);

                if (row != null) {

                	String etc = getValue(row.getCell(15));	// 비고

                	if(null == etc || !"유지".equals(etc)) continue;

                	/**
                	 *  엑셀 데이터 입력
                	 */
                	temp = new DataMapDefinitionVo();
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

                	rtnList.add(temp);
                }

            }

            workbook.close();

        } catch (Exception e) {
        	Log.error(e);
        }

        return rtnList;
	}

	/**
	 * 설명 : Excel Cell 내용을 String으로 리턴
	 *
	 * @param Cell cell
	 * @return String
	 * @throws IOException
	 */
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
//                  if (HSSFDateUtil.isCellDateFormatted(cell)) { // 숫자- 날짜 타입이다.
//                       value = formatter.format(cell.getDateCellValue());
//                  } else {
                       double numericCellValue = cell.getNumericCellValue();
                       value = String.valueOf(numericCellValue);
                       if (numericCellValue == Math.rint(numericCellValue)) {
                           value = String.valueOf((int) numericCellValue);
                       } else {
                           value = String.valueOf(numericCellValue);
                       }
//                  }
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

		// 공백제거
		value = value.replaceAll(" ", "");

		return value;
	}

}
