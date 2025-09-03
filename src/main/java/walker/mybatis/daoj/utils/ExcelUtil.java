package walker.mybatis.daoj.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import walker.mybatis.daoj.core.CodeBuilderForOracle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 描述：Excel写操作帮助类
 *
 * @author HuQingmiao
 */
public class ExcelUtil {
    private static final Logger LOGGER = LogManager.getLogger(ExcelUtil.class);

    /**
     * 将HSSFWorkbook写入Excel文件
     *
     * @param wb       HSSFWorkbook
     * @param fileName 写入完全路径 如: d:/test.xls
     */
    public static void writeWorkbook(HSSFWorkbook wb, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        wb.write(fos);
    }

    /**
     * 读取Excel文件，得到HSSFWorkbook
     *
     * @param fileName 写入完全路径 如:d:/est.xls
     */
    public static HSSFWorkbook readWorkbook(String fileName) throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        return new HSSFWorkbook(fis);
    }

    /**
     * 创建excel工作表
     *
     * @param workbook
     * @param sheetName
     * @param titleColStyleMap    标题行 Map<标题每列的文本内容, 标题行每列对应的样式>
     * @param headColStyleMap     表头行 Map<表头每列的文本内容, 表头行每列对应的样式>
     * @param bodyColStyleMap     表体行 Map<表头每列的文本内容, 表头每列的对应的样式>
     * @param bodyColValueMapList 表体行 Map<表头每列的文本内容, 表体每列的数值>
     * @return
     */
    public static HSSFWorkbook createWorkbook(HSSFWorkbook workbook, String sheetName,
                                              int[] colwidthArray,
                                              LinkedHashMap<String, HSSFCellStyle> titleColStyleMap,
                                              LinkedHashMap<String, HSSFCellStyle> headColStyleMap,
                                              LinkedHashMap<String, HSSFCellStyle> bodyColStyleMap,
                                              ArrayList<LinkedHashMap<String, Object>> bodyColValueMapList,
                                              ArrayList<String> hyperlinkList,
                                              String firstPageLinkName,
                                              String firstPageLinkUrl) {
        // 创建一张sheet
        HSSFSheet sheet = workbook.createSheet(sheetName);
        if (colwidthArray != null) {
            for (int i = 0; i < colwidthArray.length; i++) {
                sheet.setColumnWidth(i, 256 * colwidthArray[i] + 184); // 这是码友计算的字符长度与列宽的公式
            }
        } else {
            sheet.setDefaultColumnWidth(32); // 设置默认列宽, 但这里的默认宽度就指字符长度
        }

        int rownum = 0;
        if (titleColStyleMap != null && !titleColStyleMap.isEmpty()) {
            // 标题行
            HSSFRow titleRow = sheet.createRow(rownum++);
            int i = 0;
            String lstText = "";
            for (Iterator<String> it = titleColStyleMap.keySet().iterator(); it.hasNext(); ) {
                lstText = it.next();
                HSSFCell titleCell = titleRow.createCell(i++);
                titleCell.setCellStyle(titleColStyleMap.get(lstText));
                titleCell.setCellType(CellType.STRING);
                titleCell.setCellValue(new HSSFRichTextString(lstText));
            }
            // 将标题行的列数扩展到与表头、表体同宽
            for (int j = 0; (i + j) < headColStyleMap.size(); j++) {
                HSSFCell titleCell = titleRow.createCell(i + j);
                titleCell.setCellStyle(titleColStyleMap.get(lstText)); // 沿用标题前一列的样式
                titleCell.setCellType(CellType.STRING);
                titleCell.setCellValue(new HSSFRichTextString(""));

                // 在表头最后一列设置返回首页的链接
                if ((i + j) == headColStyleMap.size() - 1) {
                    if (firstPageLinkUrl != null && !"".equals(firstPageLinkUrl)) {
                        titleCell.setCellValue(new HSSFRichTextString(firstPageLinkName));
                        CreationHelper creationHelper = workbook.getCreationHelper();
                        Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.DOCUMENT);
                        hyperlink.setAddress(firstPageLinkUrl);
                        titleCell.setHyperlink(hyperlink);

                        HSSFCellStyle cs = workbook.createCellStyle();
                        cs.setWrapText(true); // 自动换行
                        cs.setAlignment(HorizontalAlignment.CENTER);         // 横向居中
                        cs.setVerticalAlignment(VerticalAlignment.CENTER);   // 垂直居中
                        cs.setBorderLeft(BorderStyle.NONE);
                        cs.setBorderRight(BorderStyle.NONE);
                        cs.setBorderTop(BorderStyle.THIN);
                        cs.setBorderBottom(BorderStyle.THIN);
                        cs.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
                        cs.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
                        cs.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
                        cs.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
                        cs.setFillForegroundColor(IndexedColors.SKY_BLUE.index);
                        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        HSSFFont tf = workbook.createFont();    // 表头字体
                        tf.setFontName("微软雅黑");              //设置字体
                        tf.setCharSet(HSSFFont.DEFAULT_CHARSET); //设置编码
                        tf.setFontHeightInPoints((short) 10);     //字体大小
                        tf.setBold(Boolean.TRUE);
                        tf.setColor(IndexedColors.BLUE1.getIndex());
                        cs.setFont(tf);
                        titleCell.setCellStyle(cs);
                    }
                }// end if((i + j)==headColStyleMap.size()){
            }// end for (int j = 0; (i
        }

        // 表头行
        HSSFRow headRow = sheet.createRow(rownum++);
        int i = 0;
        for (Iterator<String> it = headColStyleMap.keySet().iterator(); it.hasNext(); ) {
            String text = it.next();
            HSSFCell headCell = headRow.createCell(i++);
            headCell.setCellStyle(headColStyleMap.get(text));
            headCell.setCellType(CellType.STRING);
            headCell.setCellValue(new HSSFRichTextString(text));
        }

        // 表体
        for (i = 0; i < bodyColValueMapList.size(); i++) {
            Map<String, Object> bodyColValueMap = bodyColValueMapList.get(i);
            HSSFRow bodyRow = sheet.createRow(rownum++);
            String url = hyperlinkList == null ? null : hyperlinkList.get(i);
            boolean linkSetted = false;
            int j = 0;
            for (Iterator<String> it = bodyColValueMap.keySet().iterator(); it.hasNext(); ) {
                String text = it.next();
                String bodyCellVal = (String) bodyColValueMap.get(text);
                HSSFCell bodyCell = bodyRow.createCell(j++);
                bodyCell.setCellStyle(bodyColStyleMap.get(text));
                bodyCell.setCellType(CellType.STRING);
                bodyCell.setCellValue(bodyCellVal != null ? bodyCellVal : "");

                if (!linkSetted && url != null && !"".equals(url)) {
                    CreationHelper creationHelper = workbook.getCreationHelper();
                    Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.DOCUMENT);
                    hyperlink.setAddress(url);
                    bodyCell.setHyperlink(hyperlink);
                    HSSFFont linkFont = workbook.createFont();
                    linkFont.setFontName("Consolas");
                    linkFont.setColor(IndexedColors.BLUE1.getIndex());
                    bodyColStyleMap.get(text).setFont(linkFont);
                    linkSetted = true;
                }
            }
        }
        return workbook;
    }


    /**
     * 解析excel工作表
     *
     * @param workbook  工作表名称
     * @param sheetName sheet名称
     * @return
     * @throws IOException
     */
    public static ArrayList<LinkedHashMap<String, Object>> parseWorkbook(HSSFWorkbook workbook, String sheetName) {
        Sheet sheet = null;
        if (sheetName != null && !"".equals(sheetName.trim())) {
            sheet = workbook.getSheet(sheetName);
        } else {
            sheet = workbook.getSheetAt(0);
        }

        Iterator<Row> rowIt = sheet.rowIterator();

        //解析标题
        Row row = rowIt.next();
        List<String> titles = new ArrayList<String>(8);
        for (Iterator<Cell> cellIt = row.cellIterator(); cellIt.hasNext(); ) {
            Cell cell = cellIt.next();
            titles.add(cell.getStringCellValue());
        }

        //解析表体
        ArrayList<LinkedHashMap<String, Object>> rowList = new ArrayList();
        for (; rowIt.hasNext(); ) {
            row = rowIt.next();
            LinkedHashMap<String, Object> cellMap = new LinkedHashMap<String, Object>();
            int i = 0;
            for (Iterator<Cell> cellIt = row.cellIterator(); cellIt.hasNext(); ) {
                Cell cell = cellIt.next();
                // log.info(">>> "+getValue(cell));
                cellMap.put(titles.get(i++), getValue(cell));
            }
            rowList.add(cellMap);
        }
        return rowList;
    }

    private static String getValue(Cell cell) {
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return String.valueOf(new DecimalFormat("#0.#").format(cell.getNumericCellValue()));
        }
        return cell.getStringCellValue();
    }


    // 默认样式
    public static HSSFCellStyle createDefaultHeadStyle(HSSFWorkbook workbook) {
        HSSFCellStyle headStyle = workbook.createCellStyle();
        headStyle.setWrapText(true); // 自动换行
        headStyle.setAlignment(HorizontalAlignment.CENTER);         // 横向居中
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);   // 垂直居中
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);
        headStyle.setBorderTop(BorderStyle.THIN);
        headStyle.setBorderBottom(BorderStyle.THIN);
        headStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); // 背景
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);       // 这行也是控制背景
        HSSFFont headFont = workbook.createFont();    // 表头字体
        headFont.setFontName("微软雅黑");              //设置字体
        headFont.setCharSet(HSSFFont.DEFAULT_CHARSET); //设置编码
        headFont.setFontHeightInPoints((short) 10);     //字体大小
        headFont.setBold(Boolean.TRUE);
        headStyle.setFont(headFont);
        return headStyle;
    }

    public static void main(String[] args) {
        /**
         * 写excel
         */
        // 定义样式
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFCellStyle headStyle = workbook.createCellStyle();
        LinkedHashMap<String, HSSFCellStyle> headColStyleMap = new LinkedHashMap();
        headColStyleMap.put("姓名", headStyle);
        headColStyleMap.put("性别", headStyle);

        // 表格数据
        ArrayList<LinkedHashMap<String, Object>> dataList = new ArrayList();
        LinkedHashMap<String, Object> dataMap = new LinkedHashMap<String, Object>();
        dataMap.put("姓名", "walker");
        dataMap.put("性别", "male");
        dataList.add(dataMap);
        dataMap = new LinkedHashMap<String, Object>();
        dataMap.put("姓名", "ou");
        dataMap.put("性别", "female");
        dataList.add(dataMap);
        try {
            String outfilename = "d:/test.xls";
            HSSFWorkbook wb = ExcelUtil.createWorkbook(workbook, "sheet1", null,
                    null,
                    headColStyleMap,
                    headColStyleMap,
                    dataList,
                    null,
                    null, null);
            ExcelUtil.writeWorkbook(wb, outfilename);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String inputFilename = "d:/老核心表名.xls";
        ArrayList<LinkedHashMap<String, Object>> rsList = null;
        HashMap<String, String> enCnMap = new HashMap<>();
        try {
            HSSFWorkbook wb = ExcelUtil.readWorkbook(inputFilename);
            rsList = ExcelUtil.parseWorkbook(wb, "Sheet1");
            for (LinkedHashMap<String, Object> rsMap : rsList) {
                enCnMap.put(((String) rsMap.get("表名英文".trim())).toLowerCase(), (String) rsMap.get("表名中文注释".trim()));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /**
         * 读Excel
         */
        try {
            HSSFWorkbook wb = ExcelUtil.readWorkbook(inputFilename);
            rsList = ExcelUtil.parseWorkbook(wb, "老核心的表");
            for (LinkedHashMap<String, Object> rsMap : rsList) {
                String sourceName = ((String) rsMap.get("表名英文".trim()));
                if (sourceName != null) {
                    sourceName = sourceName.trim();
                }
                String cnName = ((String) rsMap.get("表名中文注释".trim()));
                if (cnName != null) {
                    cnName = cnName.trim();
                }
//                SourceTab sourceTab = new SourceTab(sourceName, cnName, null, null);
//                log.info(">> "+ JSONObject.toJSONString(sourceTab));
            }
            rsList.clear();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}

