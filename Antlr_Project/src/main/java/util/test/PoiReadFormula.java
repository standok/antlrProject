package util.test;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

public class PoiReadFormula {


    public static String filePath = "C:\\poi_temp";
    public static String fileNm = "poi_formula_test.xlsx";

    public static void main(String[] args) {

        try (FileInputStream file = new FileInputStream(new File(filePath, fileNm))) {

            XSSFWorkbook workbook = new XSSFWorkbook(file);

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            XSSFSheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if(row.getRowNum() == 0 ) { continue; }

                Iterator<Cell> cellIterator = row.cellIterator();

                while (cellIterator.hasNext()) {

                    Cell cell = cellIterator.next();
                    System.out.print("변환전: " + cell);
                    System.out.print(" / ");
                    System.out.println("변환후: " + evaluator.evaluateInCell(cell));

                    // 굳이 타입을 확인하고 싶다면 아래처럼 할 수도 있다.
                    /*
                    switch (evaluator.evaluateInCell(cell).getCellType()) {

                        case NUMERIC:
                            System.out.println(cell.getNumericCellValue() + "\t");
                            break;
                        case STRING:
                            System.out.println(cell.getStringCellValue() + "\t");
                            break;
                        case FORMULA:
                            //
                            break;
                    }
                    */
                }
                System.out.println("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}