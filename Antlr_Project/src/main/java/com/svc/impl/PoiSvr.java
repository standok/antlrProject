package com.svc.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.domain.DataMapDefinitionVo;
import com.svc.IPoiSvr;
import com.util.Log;

public class PoiSvr implements IPoiSvr {

	@Override
	public HashMap readExcelToMap(File file) throws IOException {

		HashMap rtnMap = new HashMap<String, HashMap>();
		HashMap dataMap = new HashMap<String, DataMapDefinitionVo>();
		DataMapDefinitionVo dataVo = new DataMapDefinitionVo();
		
        Workbook workbook = null;

        try {
        	InputStream in = new FileInputStream(file);
        	
        			
            // ���� 97 - 2003 ������ HSSF(xls),  ���� 2007 �̻��� XSSF(xlsx)
            if (file.getName().endsWith(".xls")) {
            	workbook = new HSSFWorkbook(in);
            } else {
                workbook = new XSSFWorkbook(in);
            }
        	
            // �������Ͽ��� ù��° ��Ʈ �ҷ�����
            Sheet worksheet = workbook.getSheet("���̺����Ǽ�");
            
            String currentTableId = "";
            
            // getPhysicalNumberOfRow �� ���� ������ �ҷ����� �żҵ�
            for (int i = 6; i < worksheet.getPhysicalNumberOfRows(); i++) {

            	// i��° �� ���� ��������
                Row row = worksheet.getRow(i);

                if (row != null) {
                	
                	// ��� '����'�� �ƴѰ�� skip
//                	String etc = getValue(row.getCell(16));	// ���                	
//                	if(null == etc || !"����".equals(etc)) continue;

                	String oldTableId = getValue(row.getCell(6));
                	String oldColumnId = getValue(row.getCell(9));
                	
                	// currentTableId �ʱ�ȭ
                	if("".equals(currentTableId)) currentTableId = oldTableId;
                	
                	// ���� ���̺��� rtnMap�� ����
                	// ���ο� ���̺��� ��������� dataMap �ʱ�ȭ
                	if(!currentTableId.equals(oldTableId)) {
                		if(rtnMap.containsKey(currentTableId)) throw new Exception("�������̺����� Ȯ����");
                		rtnMap.put(currentTableId, dataMap);
                		dataMap = new HashMap<String, DataMapDefinitionVo>();
                	}
                	
                	// ���� ������ �Է�
                	dataVo = new DataMapDefinitionVo();
                	dataVo.setOldTableId(oldTableId);						// (��)�������̺�ID-�빮��
                	dataVo.setOldTableName(getValue(row.getCell(7)));		// [1](��)�ѱ����̺��
                	dataVo.setOldColumnId(oldColumnId);						// [2](��)�����ʵ�ID-�빮��
                	dataVo.setOldColumnName(getValue(row.getCell(10)));		// [3](��)�ѱ��ʵ��
                	dataVo.setOldDataType(getValue(row.getCell(11)));		// [4](��)�����ͼӼ�
                	dataVo.setOldDataLength(getValue(row.getCell(12)));		// [5](��)�����ͱ���

                	dataVo.setNewTableId(getValue(row.getCell(18)));		// (��)�������̺�ID-�빮��
                	dataVo.setNewTableName(getValue(row.getCell(19)));		// [6](��)�ѱ����̺��
                	dataVo.setNewColumnId(getValue(row.getCell(21)));		// [7](��)�����ʵ�ID-�빮��
                	dataVo.setNewColumnName(getValue(row.getCell(22)));		// [8](��)�ѱ��ʵ��
                	dataVo.setNewDataType(getValue(row.getCell(23)));		// [9](��)�����ͼӼ�
                	dataVo.setNewDataLength(getValue(row.getCell(24)));		// [10](��)�����ͱ���
                	
                	String ������� = getValue(row.getCell(28));			// �������(�ؿ�, ��û��)

                	dataVo.setConvert(false);								// [11]��ȯ����
                	if(!"�ؿ�".equals(�������) && !"".equals(�������.trim())) dataVo.setConvert(true);
                	
                	dataVo.setConvertRule("");							// [12]��ȯ ��Ģ
                	
                	dataMap.put(oldColumnId, dataVo);
                }                
            }
            
            // ������ ���̺� ����
            rtnMap.put(currentTableId, dataMap);

            workbook.close();
        	
        } catch (Exception e) {
        	Log.error(e);
        }
        
        return rtnMap;	
	}
	
	@Override
	public List<DataMapDefinitionVo> readExcelToList(File file) throws IOException {
		
		List<DataMapDefinitionVo> rtnList = new ArrayList<DataMapDefinitionVo>();
		DataMapDefinitionVo temp = new DataMapDefinitionVo();

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

            	// i��° �� ���� ��������
                Row row = worksheet.getRow(i);

                if (row != null) {
                	
                	String etc = getValue(row.getCell(16));	// ���
                	
                	if(null == etc || !"����".equals(etc)) continue;
                	
                	/**
                	 *  ���� ������ �Է�
                	 */
                	temp = new DataMapDefinitionVo();
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
                	
                	rtnList.add(temp);
                }
                
            }
            
            workbook.close();
        	
        } catch (Exception e) {
        	Log.error(e);
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
//                  if (HSSFDateUtil.isCellDateFormatted(cell)) { // ����- ��¥ Ÿ���̴�.
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
		
		// ��������
		value = value.replaceAll(" ", "");
		
		return value;
	}

}
