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
        			
            // ���� 97 - 2003 ������ HSSF(xls),  ���� 2007 �̻��� XSSF(xlsx)
            if (file.getName().endsWith(".xls")) {
            	workbook = new HSSFWorkbook(in);
            } else {
                workbook = new XSSFWorkbook(in);
            }
        	
            // �������Ͽ��� ù��° ��Ʈ �ҷ�����
            Sheet worksheet = workbook.getSheet("���̺����Ǽ�");
            
            // getPhysicalNumberOfRow �� ���� ������ �ҷ����� �żҵ�
            for (int i = 6; i < worksheet.getPhysicalNumberOfRows(); i++) {

            	// DataMapDefinitionVo �ʱ�ȭ
            	temp = new DataMapDefinitionVo();

            	// i��° �� ���� ��������
                Row row = worksheet.getRow(i);

                if (row != null) {

                	String etc = getValue(row.getCell(16));	// ���
                	
                	if(null == etc || !"����".equals(etc)) continue;
                	
                	/**
                	 *  ���� ������ �Է�
                	 */
                	temp.setOldTableId(getValue(row.getCell(6)));		// (��)�������̺�ID-�빮��
                	temp.setOldTableName(getValue(row.getCell(7)));		// [1](��)�ѱ����̺��
                	temp.setOldColumnId(getValue(row.getCell(9)));		// [2](��)�����ʵ�ID-�빮��
                	temp.setOldColumnName(getValue(row.getCell(10)));	// [3](��)�ѱ��ʵ��
                	temp.setOldDataType(getValue(row.getCell(11)));		// [4](��)�����ͼӼ�
                	temp.setOldDataLength(getValue(row.getCell(12)));	// [5](��)�����ͱ���

                	temp.setNewTableId(getValue(row.getCell(18)));		// (��)�������̺�ID-�빮��
                	temp.setNewTableName(getValue(row.getCell(19)));		// [6](��)�ѱ����̺��
                	temp.setNewColumnId(getValue(row.getCell(21)));		// [7](��)�����ʵ�ID-�빮��
                	temp.setNewColumnName(getValue(row.getCell(22)));	// [8](��)�ѱ��ʵ��
                	temp.setNewDataType(getValue(row.getCell(23)));		// [9](��)�����ͼӼ�
                	temp.setNewDataLength(getValue(row.getCell(24)));	// [10](��)�����ͱ���
                	
                	String ������� = getValue(row.getCell(28));			// �������(�ؿ�, ��û��)

                	temp.setConvert(false);								// [11]��ȯ����
                	if(!"�ؿ�".equals(�������) && !"".equals(�������.trim())) temp.setConvert(true);
                	
                	temp.setConvertRule("");							// [12]��ȯ ��Ģ
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

        // ��¥����
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
        // return ����
        String value = "";
		
		if (cell == null) {
            return null;
        } else {
            // Ÿ�Ժ��� ���� �б�
            switch (cell.getCellType()) {
               case FORMULA:
                  value = cell.getCellFormula();
                  break;
               case NUMERIC:
                  if (HSSFDateUtil.isCellDateFormatted(cell)) { // ����- ��¥ Ÿ���̴�.
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
