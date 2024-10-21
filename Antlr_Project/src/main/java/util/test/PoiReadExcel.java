package util.test;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class PoiReadExcel {

    public static String filePath = "C:\\poi_temp";
    public static String fileNm = "poi_reading_test.xlsx";

    public static void main(String[] args) {

        try (FileInputStream file = new FileInputStream(new File(filePath, fileNm))){

            // ���� ���Ϸ� Workbook instance�� �����Ѵ�.
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            // workbook�� ù��° sheet�� �����´�.
            XSSFSheet sheet = workbook.getSheetAt(0);

            // ���� Ư�� �̸��� ��Ʈ�� ã�´ٸ� workbook.getSheet("ã�� ��Ʈ�� �̸�");
            // ���� ��� ��Ʈ�� ��ȸ�ϰ� ������
            // for(Integer sheetNum : workbook.getNumberOfSheets()) {
            //      XSSFSheet sheet = workbook.getSheetAt(i);
            // }
            // �ƴϸ� Iterator<Sheet> s = workbook.iterator() �� ����ؼ� ��ȸ�ص� ����.

            // ��� ��(row)���� ��ȸ�Ѵ�.
            for (Row row : sheet) {
                // ������ �࿡ �����ϴ� ��� ��(cell)�� ��ȸ�Ѵ�.
                Iterator<Cell> cellIterator = row.cellIterator();

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();

                    // cell�� Ÿ���� �ϰ�, ���� �����´�.
                    switch (cell.getCellType()) {

                        case NUMERIC:
                            //getNumericCellValue �޼���� �⺻���� double�� ��ȯ
                            System.out.print((int) cell.getNumericCellValue() + "\t");
                            break;

                        case STRING:
                            System.out.print(cell.getStringCellValue() + "\t");
                            break;
                    }
                }
                System.out.println();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}