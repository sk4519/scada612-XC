package cn.bp.scada.common.utils.excel;

import cn.bp.scada.mapper.scada.Pack;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


@Slf4j
@Component
public class ExcelUtils {
    @Autowired
    private  Pack packs;
    /**
     * 导出excel2007版本 模板
     *
     * @param titles
     *            表头集合
     * @param sheetNames
     *            sheet名称
     * @param datas
     *            数据集合
     * @param fileName
     *            文件名字
     * @param response
     *            输出流
     * @return String
     */
    public static int exportExcel07(HttpServletResponse response,String[] cellTitle,List<String[]> dataList) throws IOException{
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        String now = dateFormat.format(new Date());
        //导出文件路径
        //文件名
        String exportFileName = "模板"+now+".xlsx";

        // 声明一个工作薄
        XSSFWorkbook workBooks = null;
        workBooks = new XSSFWorkbook();

        // 生成一个表格
        XSSFSheet sheet = workBooks.createSheet();
        workBooks.setSheetName(0,"点检项目模板");

        //第一行模板说明
        Font font1 = workBooks.createFont();
        font1.setBold(true);
        font1.setFontHeightInPoints((short)14);
        XSSFCellStyle cellStyle2 = workBooks.createCellStyle();
        cellStyle2.setVerticalAlignment(VerticalAlignment.CENTER); //垂直居中
        font1.setColor(IndexedColors.RED.index);
        cellStyle2.setFont(font1);
        XSSFRow titleRow1 = sheet.createRow(0);
        titleRow1.setHeight((short)(70 *12)); //设置行高
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,10));
         XSSFCell cell1 = titleRow1.createCell(0);
        cell1.setCellValue("参照下列内容填写数据,模板内容数据需删除,注意时间格式");
        cell1.setCellStyle(cellStyle2);

        // 设置单元格格式居中
        XSSFCellStyle cellStyle = workBooks.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        //创建一个字体
        XSSFFont font = workBooks.createFont();
        font.setBold(true); //加粗
        // font.setStrikeout(true); //删除线
        cellStyle.setFont(font);
        XSSFRow titleRow = sheet.createRow(1);
        for(int i=0;i<cellTitle.length;i++){
             XSSFCell cell = titleRow.createCell(i);
            cell.setCellValue(cellTitle[i]);
            cell.setCellStyle(cellStyle);
        }

        XSSFCellStyle cellStyle1 = workBooks.createCellStyle();
        cellStyle1.setAlignment(HorizontalAlignment.CENTER);
        //插入需导出的数据
        for(int i=0;i<dataList.size();i++){
            XSSFRow row = sheet.createRow(i+2);
            for(int j =0; j<cellTitle.length; j++) {
                 XSSFCell cell = row.createCell(j);
                cell.setCellValue(dataList.get(i)[j]);
                cell.setCellStyle(cellStyle1);
            }

        }
        //设置自动列宽
        for (int i = 0; i < cellTitle.length; i++) {
            sheet.autoSizeColumn(i,true);
            sheet.setColumnWidth(i,sheet.getColumnWidth(i)*17/10);
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workBooks.write(os);
        byte[] content = os.toByteArray();
        InputStream is = new ByteArrayInputStream(content);
        // 设置response参数，可以打开下载页面
        response.reset();
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename="
                + new String((exportFileName ).getBytes(), "iso-8859-1"));
        ServletOutputStream out = response.getOutputStream();
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(is);
            bos = new BufferedOutputStream(out);
            byte[] buff = new byte[2048];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            if (bis != null)
                bis.close();
            if (bos != null)
                bos.close();
            if(workBooks != null)
                workBooks.close();
        }
        return 0;
    }

    /**
     * 读取excel内容
     * @param file
     * @return
     * @throws IOException
     */
    public  HashMap<String, ArrayList<Map>> analysisFile(MultipartFile file) throws IOException {
        HashMap<String, ArrayList<Map>> hashMap = new HashMap<>();
        //获取workbook对象
        Workbook workbook = null;
        String filename = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();
        //根据后缀名是否excel文件
        if(filename.endsWith("xls")){
            //2003
            workbook = new HSSFWorkbook(inputStream);
        }else if(filename.endsWith("xlsx")){
            //2007
            workbook = new XSSFWorkbook(inputStream);
        }

         Sheet sheetAt = workbook.getSheetAt(0);
         boolean flag = checkModel(sheetAt.getRow(0));
        if(flag) {
            String[] strArr = {"CK_CD","CK_NM", "ET_NM", "CK_TY_NM", "CK_SORT", "CUS_RM", "USE_YN", "IS_UR", "CRT_ID", "CRT_DT"};
            //创建对象，把每一行作为一个String数组，所以数组存到集合中
            ArrayList<Map> arrayList = new ArrayList<>();
            if (workbook != null) {
                //循环sheet,现在是单sheet
                for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
                    //获取第一个sheet
                    Sheet sheet = workbook.getSheetAt(sheetNum);
                    if (sheet == null) {
                        hashMap.put("文件sheet为空!", arrayList);
                        return hashMap;
                    }
                    //获取当前sheet开始行和结束行
                    int firstRowNum = sheet.getFirstRowNum();
                    int lastRowNum = sheet.getLastRowNum();
                    //循环开始，除了前两行
                    for (int rowNum = firstRowNum + 2; rowNum <= lastRowNum; rowNum++) {
                        //获取当前行
                        Row row = sheet.getRow(rowNum);
                        if (row == null) {
                            break;
                        }
                        //获取当前行的开始列和结束列
                        short firstCellNum = row.getFirstCellNum();
                        short lastCellNum = row.getLastCellNum();

                        //获取总行数
                        int lastCellNum2 = row.getPhysicalNumberOfCells();
                        Map<String, String> map = new HashMap<>();
                        //循环当前行
                        for (int cellNum = firstCellNum; cellNum < lastCellNum; cellNum++) {
                            Cell cell = row.getCell(cellNum);
                            String cellValue = "";
                            cellValue = getCellValue(cell);
                            if(cellNum == 2) {
                                cellValue = packs.queryDevice(cellValue);
                            } else if(cellNum ==3) {
                                cellValue = packs.queryCheckType(cellValue);
                            }
                            map.put(strArr[cellNum], cellValue);
                        }
                        arrayList.add(map);

                    }
                }
            }
            inputStream.close();
            hashMap.put("dataList", arrayList);
        } else {
            return null;
        }
        return hashMap;
    }

    /**
     * 校验模板第一行是否是制定的模板
     * @param row
     * @return
     */
    public static boolean checkModel(@NotNull Row row) {
        return row.getCell(0).toString().equals("参照下列内容填写数据,模板内容数据需删除,注意时间格式") ? true : false;
    }

    /**
     * 获取所有列的值，将cell转为string
     * @param cell 列对象传进来
     * @return
     */
    public static String getCellValue(Cell cell) {
        String cellValue = "";
        if(cell == null){
            return cellValue;
        }
        //把数字转换成string，防止12.0这种情况
        if(cell.getCellType() == cell.CELL_TYPE_NUMERIC){
            cell.setCellType(cell.CELL_TYPE_STRING);
        }
        //判断数据的类型
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC: //数字0
                cellValue = String.valueOf(cell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING: //字符串1
                cellValue = String.valueOf(cell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_BOOLEAN: //Boolean
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA: //公式
                //cellValue = String.valueOf(cell.getCellFormula());
                try {
                    cellValue = String.valueOf(cell.getNumericCellValue());
                } catch (IllegalStateException e) {
                    cellValue = String.valueOf(cell.getRichStringCellValue());
                }
                break;
            case Cell.CELL_TYPE_BLANK: //空值
                cellValue = "";
                break;
            case Cell.CELL_TYPE_ERROR: //故障
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }

    //转换excel导入之后时间变为数字,年月日时间
/*    public static String getCorrectDay(int i){
        calendar.set(1900,0,-1,0,0,0);
        calendar.add(calendar.DATE,i);
        Date time = calendar.getTime();
        String s = simpleDateFormat3.format(time);
        return s;
    }*/

    /**
     * //判断row是否为空
     * @param row 传入行进来
     * @return
     */
    public static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
                return false;
            }
        }
        return true;
    }

    //读取excel
    private static Workbook readExcel(String fileType, InputStream is) throws IOException {
        if ("xls".equals(fileType)) {
            return new HSSFWorkbook(is);
        } else if ("xlsx".equals(fileType)) {
            return new XSSFWorkbook(is);
        } else {
            throw new IllegalArgumentException("不支持的文件类型，仅支持xls和xlsx");
        }
    }

    /**
     * 去掉字符串右边的空格
     *
     * @param str 要处理的字符串
     * @return 处理后的字符串
     */
    private static String rightTrim(String str) {
        if (str == null) {
            return "";
        }
        int length = str.length();
        for (int i = length - 1; i >= 0; i--) {
            if (str.charAt(i) != 0x20) {
                break;
            }
            length--;
        }
        return str.substring(0, length);
    }
}
