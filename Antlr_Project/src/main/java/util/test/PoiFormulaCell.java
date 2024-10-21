package util.test;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PoiFormulaCell {

    public static String filePath = "C:\\poi_temp";
    public static String fileNm = "poi_formula_test.xlsx";

    public static void main(String[] args) {
        
		XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("example");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("width");
        header.createCell(1).setCellValue("height");
        header.createCell(2).setCellValue("area");


        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(100);
        dataRow.createCell(1).setCellValue(100);
        dataRow.createCell(2).setCellFormula("A2*B2");

        try (FileOutputStream out = new FileOutputStream(new File(filePath, fileNm))) {
            workbook.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}