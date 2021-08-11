package fuzzing4j.core.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-08-10 17:03
 * @description
 */
public class ExcelExporter {
    Workbook workbook;
    String[] overviewTitle;
    String[] detailTitle;

    public ExcelExporter() {
        workbook = new HSSFWorkbook();
        overviewTitle = new String[]{"耗时(秒)", "测试类", "测试方法", "失败的类", "失败的方法", "测试次数", "失败次数"};
        detailTitle = new String[]{"类", "方法", "耗时(秒)", "总次数", "失败次数", "失败入参", "异常栈"};
    }
    public void writeOverview(Object[] overview)throws Exception{
        HSSFSheet sheet=(HSSFSheet)workbook.createSheet("Overview");
        HSSFRow row=sheet.createRow(0);
        HSSFCell cell;
        for(int i=0;i<overview.length;i++){
            cell=row.createCell(i, CellType.STRING);
            cell.setCellValue(overviewTitle[i]);
        }
        row=sheet.createRow(1);
        for(int i=0;i<overview.length;i++){
            cell=row.createCell(i,CellType.STRING);
            cell.setCellValue(overview[i].toString());
        }
    }
    public void writeData(Map<String,List<DetailBean>> data)throws Exception{
        HSSFSheet sheet=(HSSFSheet)workbook.createSheet("Detail");
        HSSFRow row=sheet.createRow(0);
        HSSFCell cell;
        for(int i=0;i<detailTitle.length;i++){
            cell=row.createCell(i,CellType.STRING);
            cell.setCellValue(detailTitle[i]);
        }
        int rownum=1;
        for(String classFullName:data.keySet()){
            int rowspan=0;
            for(DetailBean detail:data.get(classFullName)){
                rowspan+=(detail.failures==null?1:detail.failures.size());
            }
            for(DetailBean detail:data.get(classFullName)) {
                row = sheet.createRow(rownum++);
                cell = row.createCell(0);
                cell.setCellValue(classFullName);
                cell = row.createCell(1);
                cell.setCellValue(detail.method);
                cell = row.createCell(2);
                cell.setCellValue(detail.ranSeconds);
                cell = row.createCell(3);
                cell.setCellValue(detail.ranTimes);
                cell = row.createCell(4);
                cell.setCellValue(detail.failedTimes);
                if (detail.failures != null) {
                    for (String[] failure : detail.failures) {
                        row = row == null ? sheet.createRow(rownum++) : row;
                        cell = row.createCell(5);
                        cell.setCellValue(failure[0]);
                        cell = row.createCell(6);
                        cell.setCellValue(failure[1]);
                        row = null;
                    }
                }
            }
        }
    }
    public void export(String filePath){
        try (FileOutputStream out = new FileOutputStream(filePath + File.separator + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) + ".xls");) {
            workbook.write(out);
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static class DetailBean{
        String method;
        long ranSeconds;
        int ranTimes;
        int failedTimes;
        List<String[]> failures;

        public void setMethod(String method) {
            this.method = method;
        }

        public void setRanSeconds(long ranSeconds) {
            this.ranSeconds = ranSeconds;
        }

        public void setRanTimes(int ranTimes) {
            this.ranTimes = ranTimes;
        }

        public void setFailedTimes(int failedTimes) {
            this.failedTimes = failedTimes;
        }

        public void setFailures(List<String[]> failures) {
            this.failures = failures;
        }
    }
}
