package walker.mybatis.daoj.core;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import walker.mybatis.daoj.DaojApp;
import walker.mybatis.daoj.utils.ExcelUtil;
import walker.mybatis.daoj.utils.MappingUtil;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;


/**
 * @author HuQingmiao
 */
public class Generator {
    private static final Logger LOGGER = LogManager.getLogger(Generator.class);

    private String configFilename = "mybatis-daoj.xml";

    public Generator() {
    }

    public Generator(String configFilename) {
        this.configFilename = configFilename;
    }

    @Deprecated
    public void generator() {
        this.generateCode();
    }

    @Deprecated
    public void generate() {
        this.generateCode();
    }

    public void generateCode() {
        //代码生成的输出目录
        String outputDircName = ConfigLoader.getInstance(this.configFilename).getOutputDirc();

        //列出要生成的表名
        String[] tables = ConfigLoader.getInstance(this.configFilename).getTables();

        File outputDirc = new File(outputDircName);
        File poPackageDirc = new File(outputDirc, "dpo");
        poPackageDirc.mkdirs();
        File daoPackageDirc = new File(outputDirc, "dao");
        daoPackageDirc.mkdirs();
        File mapperPackageDirc = new File(outputDirc, "mapper");
        mapperPackageDirc.mkdirs();

        for (int i = 0; i < tables.length; i++) {
            try {
                LOGGER.info("\n>> 对 {} 表 生成代码 ...", tables[i]);
                CodeBuilder codeBuilder = CodeBuilder.getInstance(this.configFilename, tables[i]);

                //生成实体类
                String codeStr = codeBuilder.buildEntitySource();
                this.createFile(codeStr, poPackageDirc.getCanonicalPath()
                        + File.separator + MappingUtil.getEntityName(tables[i]) + ".java");

                //生成DAO类
                codeStr = codeBuilder.buildDaoSource();
                this.createFile(codeStr, daoPackageDirc.getCanonicalPath()
                        + File.separator + MappingUtil.getEntityName(tables[i]) + "Dao.java");

                //生成MAPPER
                codeStr = codeBuilder.buildMapperSource();
                this.createFile(codeStr, mapperPackageDirc.getCanonicalPath()
                        + File.separator + MappingUtil.getEntityName(tables[i]) + "Mapper.xml");

                //复制DAO/VO基类
                InputStream is = Generator.class.getClassLoader().getResourceAsStream("BasicDao.java");
                File basicDaoFile = new File(outputDirc, "BasicDao.java");
                this.writeToFile(is, basicDaoFile);

                is = Generator.class.getClassLoader().getResourceAsStream("BasicPo.java");
                File basicPoFile = new File(outputDirc, "BasicPo.java");
                this.writeToFile(is, basicPoFile);

            } catch (Exception e) {
                LOGGER.error(">> ......................................................... Error! ", e);
                throw new RuntimeException(e);
            }
        }
        try {
            LOGGER.info(">> 已成功生成代码到: " + outputDirc.getCanonicalPath());
        } catch (IOException e) {
            LOGGER.error("",e);
        }
    }

    private void writeToFile(InputStream is, File file) throws IOException {
        try (FileOutputStream os = new FileOutputStream(file);) {
            byte[] bytes = new byte[5 * 1024];
            int len = 0;
            while ((len = is.read(bytes)) > 0) {
                os.write(bytes, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    /**
     * 将文本内容写入指定的文件
     *
     * @param fileContent
     * @param fileName
     */
    private void createFile(String fileContent, String fileName) throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8")) {
            osw.write(fileContent, 0, fileContent.length());
            osw.flush();
        } catch (IOException e) {
            LOGGER.error("", e);
            throw e;
        }
    }


    public void generateDoc() {
        // 文档生成的输出目录
        String outputDircName = ConfigLoader.getInstance(this.configFilename).getOutputDirc();

        //列出要生成的表名
        String[] tables = ConfigLoader.getInstance(this.configFilename).getTables();

        DbPattern dbPattern = ConfigLoader.getInstance(this.configFilename).getDbPattern();
        String jdbcDriver = ConfigLoader.getInstance(this.configFilename).getJdbcDriver();

        File outputDirc = new File(outputDircName, "doc");
        outputDirc.mkdirs();
        String filename = "db_table_design_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";
        try {
            String outfilename = new File(outputDirc, filename).getCanonicalPath();
            HSSFWorkbook workbook = new HSSFWorkbook();

            LinkedHashMap<String, HSSFCellStyle> firstPageTitleStyleMap = this.creatFirstPageTitleStyle(workbook); // 首页(目录页)标题样式
            LinkedHashMap<String, HSSFCellStyle> firstPageHeadStyleMap = this.creatFirstPageHeadStyle(workbook);   // 首页(目录页)表头样式
            LinkedHashMap<String, HSSFCellStyle> firstPageBodyStyleMap = this.createFirstPageBodyStyle(workbook);  // 首页(目录页)表体样式

            HashMap<String, String> tabCommentMap = new HashMap<String, String>();
            if (DbPattern.oracle.equals(dbPattern)) {
                tabCommentMap = this.fetchTabCommentsForOracle();
            } else if (DbPattern.mysql.equals(dbPattern) && !jdbcDriver.contains("postgresql.")) {
                tabCommentMap = this.fetchTabCommentsForMysql();
            }
            ArrayList<LinkedHashMap<String, Object>> bodyColValueMapList0 = new ArrayList<LinkedHashMap<String, Object>>();
            for (int i = 0; i < tables.length; i++) {
                String tabName = tables[i].toLowerCase();
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("表名", tabName);
                map.put("描述", tabCommentMap.get(tabName));
                map.put("备注", "");
                bodyColValueMapList0.add(map);
            }

            // 目录链接
            ArrayList<String> hyperlinkList = new ArrayList();
            for (String tableName : tables) {
                hyperlinkList.add(tableName + "!A1");
            }
            int[] colwidthArray = {32, 80, 60};
            ExcelUtil.createWorkbook(workbook, "目录", colwidthArray, firstPageTitleStyleMap, firstPageHeadStyleMap, firstPageBodyStyleMap, bodyColValueMapList0, hyperlinkList, null, null);
            hyperlinkList.clear();

            LinkedHashMap<String, HSSFCellStyle> dbTabHeadStyleMap = this.createDBTabHeadStyle(workbook); // 库表页表头样式
            LinkedHashMap<String, HSSFCellStyle> dbTabBodyStyleMap = this.createDBTabBodyStyle(workbook); // 库表页表体样式
            // 为每个库表生成一个sheet
            for (int i = 0; i < tables.length; i++) {
                String tableName = tables[i].toLowerCase();
                String tableComment = tabCommentMap.get(tableName);
                LOGGER.info("\n>> table name: " + tableName);
                LinkedHashMap<String, HSSFCellStyle> dbTabTitleStyleMap = this.createDBTabTitleStyle(workbook, tableName, tableComment); // 库表页标题样式

                CodeBuilder codeBuilder = CodeBuilder.getInstance(this.configFilename, tableName);
                ArrayList<MetaDataDescr> mdList = codeBuilder.fetchTableStructure();
                ArrayList<LinkedHashMap<String, Object>> bodyColValueMapList = new ArrayList<LinkedHashMap<String, Object>>();
                for (MetaDataDescr md : mdList) {
                    LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                    map.put("字段名", md.getColName());
                    map.put("字段描述", md.getComment());
                    map.put("数据类型", md.getColTypeDesc());
                    map.put("最大字节数", String.class.equals(md.getFieldType()) ? String.valueOf(md.getMaxByteLen()) : "");
                    map.put("是否主键", md.isPk() ? "是" : "");
                    map.put("非空", md.isNullable() ? "" : "是");
                    map.put("归属的唯一约束", md.getUdxName());
                    bodyColValueMapList.add(map);
                }
                int[] colwidthArray2 = {32, 72, 15, 10, 10, 10, 30};
                ExcelUtil.createWorkbook(workbook, tableName, colwidthArray2, dbTabTitleStyleMap, dbTabHeadStyleMap, dbTabBodyStyleMap, bodyColValueMapList, null, "返回目录页", "目录!A1");
            }

            ExcelUtil.writeWorkbook(workbook, outfilename);
            LOGGER.info(">> 已成功生成表结构到: " + outfilename);

        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }


    public HashMap<String, String> fetchTabComments() throws SQLException, ClassNotFoundException {
        HashMap<String, String> tabCommentMap = new HashMap();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBResource.getConnection(this.configFilename);
            DatabaseMetaData metaData = conn.getMetaData();
            rs = metaData.getTables(null, null, null, null);
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME").toLowerCase();
                String comment = rs.getString("REMARKS");  // 对于oracle模式，这种方式是取不到注释的
                tabCommentMap.put(tableName, comment);
            }
            rs.close();
            return tabCommentMap;
        } finally {
            DBResource.freeConnection(conn);
        }
    }

    private HashMap<String, String> fetchTabCommentsForOracle() throws SQLException, ClassNotFoundException {
        HashMap<String, String> tabCommentMap = new HashMap();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBResource.getConnection(this.configFilename);
            String sql = "select table_name, comments from user_tab_comments";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String tabName = rs.getString(1).toLowerCase();
                String comment = rs.getString(2);
                tabCommentMap.put(tabName, comment);
            }
            rs.close();
            stmt.close();
            return tabCommentMap;
        } finally {
            DBResource.freeConnection(conn);
        }
    }

    private HashMap<String, String> fetchTabCommentsForMysql() throws SQLException, ClassNotFoundException {
        HashMap<String, String> tabCommentMap = new HashMap();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBResource.getConnection(this.configFilename);
            String sql = "select table_name, table_comment from information_schema.tables ";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String tabName = rs.getString(1).toLowerCase();
                String comment = rs.getString(2);
                tabCommentMap.put(tabName, comment);
            }
            rs.close();
            stmt.close();
            return tabCommentMap;
        } finally {
            DBResource.freeConnection(conn);
        }
    }

    // 首页(目录页)标题行样式
    private LinkedHashMap<String, HSSFCellStyle> creatFirstPageTitleStyle(HSSFWorkbook workbook) {
        HSSFCellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setWrapText(true); // 自动换行
        titleStyle.setAlignment(HorizontalAlignment.CENTER);         // 横向居中
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);   // 垂直居中
        titleStyle.setBorderLeft(BorderStyle.NONE);
        titleStyle.setBorderRight(BorderStyle.NONE);
        titleStyle.setBorderTop(BorderStyle.THIN);
        titleStyle.setBorderBottom(BorderStyle.THIN);
        titleStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        titleStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        titleStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        titleStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        titleStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.index);
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        HSSFFont titleFont0 = workbook.createFont();    // 表头字体
        titleFont0.setFontName("微软雅黑");              //设置字体
        titleFont0.setCharSet(HSSFFont.DEFAULT_CHARSET); //设置编码
        titleFont0.setFontHeightInPoints((short) 11);     //字体大小
        titleFont0.setBold(Boolean.TRUE);
        titleStyle.setFont(titleFont0);

        LinkedHashMap<String, HSSFCellStyle> titleColStyleMap = new LinkedHashMap<String, HSSFCellStyle>();
        titleColStyleMap.put("目录", titleStyle);
        return titleColStyleMap;
    }

    // 数据库表的标题行样式
    private LinkedHashMap<String, HSSFCellStyle> createDBTabTitleStyle(HSSFWorkbook workbook, String tableName, String tableComment) {
        HSSFCellStyle[] titleStyle = new HSSFCellStyle[3];
        titleStyle[0] = workbook.createCellStyle();
        titleStyle[0].setWrapText(true); // 自动换行
        titleStyle[0].setAlignment(HorizontalAlignment.CENTER);         // 横向居中
        titleStyle[0].setVerticalAlignment(VerticalAlignment.CENTER);   // 垂直居中
        titleStyle[0].setBorderLeft(BorderStyle.NONE);
        titleStyle[0].setBorderRight(BorderStyle.NONE);
        titleStyle[0].setBorderTop(BorderStyle.THIN);
        titleStyle[0].setBorderBottom(BorderStyle.THIN);
        titleStyle[0].setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        titleStyle[0].setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        titleStyle[0].setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        titleStyle[0].setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        titleStyle[0].setFillForegroundColor(IndexedColors.SKY_BLUE.index);
        titleStyle[0].setFillPattern(FillPatternType.SOLID_FOREGROUND);
        HSSFFont titleFont0 = workbook.createFont();    // 表头字体
        titleFont0.setFontName("微软雅黑");              //设置字体
        titleFont0.setCharSet(HSSFFont.DEFAULT_CHARSET); //设置编码
        titleFont0.setFontHeightInPoints((short) 10);     //字体大小
        titleFont0.setBold(Boolean.TRUE);
        titleStyle[0].setFont(titleFont0);

        titleStyle[1] = workbook.createCellStyle();
        titleStyle[1].setWrapText(true); // 自动换行
        titleStyle[1].setAlignment(HorizontalAlignment.CENTER);         // 横向居中
        titleStyle[1].setVerticalAlignment(VerticalAlignment.CENTER);   // 垂直居中
        titleStyle[1].setBorderLeft(BorderStyle.NONE);
        titleStyle[1].setBorderRight(BorderStyle.NONE);
        titleStyle[1].setBorderTop(BorderStyle.THIN);
        titleStyle[1].setBorderBottom(BorderStyle.THIN);
        titleStyle[1].setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        titleStyle[1].setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        titleStyle[1].setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        titleStyle[1].setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        titleStyle[1].setFillForegroundColor(IndexedColors.SKY_BLUE.index);
        titleStyle[1].setFillPattern(FillPatternType.SOLID_FOREGROUND);
        HSSFFont titleFont1 = workbook.createFont();    // 表头字体
        titleFont1.setFontName("Consolas");              //设置字体
        titleFont1.setCharSet(HSSFFont.DEFAULT_CHARSET); //设置编码
        titleFont1.setFontHeightInPoints((short) 10);     //字体大小
        titleFont1.setBold(Boolean.TRUE);
        titleStyle[1].setFont(titleFont1);

//        titleStyle[2] = workbook.createCellStyle();
//        titleStyle[2].setWrapText(true); // 自动换行
//        titleStyle[2].setAlignment(HorizontalAlignment.LEFT);
//        titleStyle[2].setVerticalAlignment(VerticalAlignment.CENTER);   // 垂直居中
//        titleStyle[2].setBorderLeft(BorderStyle.NONE);
//        titleStyle[2].setBorderRight(BorderStyle.NONE);
//        titleStyle[2].setBorderTop(BorderStyle.THIN);
//        titleStyle[2].setBorderBottom(BorderStyle.THIN);
//        titleStyle[2].setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
//        titleStyle[2].setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
//        titleStyle[2].setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
//        titleStyle[2].setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
//        titleStyle[2].setFillForegroundColor(IndexedColors.SKY_BLUE.index);
//        titleStyle[2].setFillPattern(FillPatternType.SOLID_FOREGROUND);
//        HSSFFont titleFont2 = workbook.createFont();    // 表头字体
//        titleFont2.setFontName("微软雅黑");              //设置字体
//        titleFont2.setCharSet(HSSFFont.DEFAULT_CHARSET); //设置编码
//        titleFont2.setFontHeightInPoints((short) 11);     //字体大小
//        titleFont2.setBold(Boolean.TRUE);
//        titleStyle[2].setFont(titleFont2);

        LinkedHashMap<String, HSSFCellStyle> titleColStyleMap = new LinkedHashMap<String, HSSFCellStyle>();
        titleColStyleMap.put("表名:", titleStyle[0]);
        if (tableComment == null || tableComment.equalsIgnoreCase(tableName)) {
            tableComment = tableName + "(待补充中文注释)";
        }
        titleColStyleMap.put(tableName+" ("+tableComment+")", titleStyle[1]);
        return titleColStyleMap;
    }

    // 首页(目录页)表头样式
    private LinkedHashMap<String, HSSFCellStyle> creatFirstPageHeadStyle(HSSFWorkbook workbook) {
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

        LinkedHashMap<String, HSSFCellStyle> headColStyleMap = new LinkedHashMap<String, HSSFCellStyle>();
        headColStyleMap.put("表名", headStyle);
        headColStyleMap.put("描述", headStyle);
        headColStyleMap.put("备注", headStyle);
        return headColStyleMap;
    }

    // 库表表头样式
    private LinkedHashMap<String, HSSFCellStyle> createDBTabHeadStyle(HSSFWorkbook workbook) {
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

        LinkedHashMap<String, HSSFCellStyle> headColStyleMap = new LinkedHashMap<String, HSSFCellStyle>();
        headColStyleMap.put("字段名", headStyle);
        headColStyleMap.put("字段描述", headStyle);
        headColStyleMap.put("数据类型", headStyle);
        headColStyleMap.put("最大字节数", headStyle);
        headColStyleMap.put("是否主键", headStyle);
        headColStyleMap.put("非空", headStyle);
        headColStyleMap.put("归属的唯一约束", headStyle);
        return headColStyleMap;
    }


    // 首页(目录页)表体样式
    private LinkedHashMap<String, HSSFCellStyle> createFirstPageBodyStyle(HSSFWorkbook workbook) {
        HSSFCellStyle enStyle = workbook.createCellStyle();
        enStyle.setWrapText(true); // 自动换行
        enStyle.setAlignment(HorizontalAlignment.LEFT);         // 横向靠左
        enStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        enStyle.setBorderLeft(BorderStyle.THIN);
        enStyle.setBorderRight(BorderStyle.THIN);
        enStyle.setBorderTop(BorderStyle.THIN);
        enStyle.setBorderBottom(BorderStyle.THIN);
        enStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        enStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        enStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        enStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        HSSFFont enFont = workbook.createFont();     // 表体字体
        enFont.setFontName("Consolas");              // 设置字体
        enFont.setCharSet(HSSFFont.DEFAULT_CHARSET); // 设置编码
        enFont.setFontHeightInPoints((short) 10);    // 字体大小
        enFont.setBold(Boolean.FALSE);
        enStyle.setFont(enFont);

        HSSFCellStyle chStyle = workbook.createCellStyle();
        chStyle.setWrapText(true); // 自动换行
        chStyle.setAlignment(HorizontalAlignment.LEFT);       // 横向靠左
        chStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        chStyle.setBorderLeft(BorderStyle.THIN);
        chStyle.setBorderRight(BorderStyle.THIN);
        chStyle.setBorderTop(BorderStyle.THIN);
        chStyle.setBorderBottom(BorderStyle.THIN);
        chStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        chStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        chStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        chStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        HSSFFont chFont = workbook.createFont();     // 表体字体
        chFont.setFontName("微软雅黑");              // 设置字体
        chFont.setCharSet(HSSFFont.DEFAULT_CHARSET); // 设置编码
        chFont.setFontHeightInPoints((short) 10);    // 字体大小
        chFont.setBold(Boolean.FALSE);
        chStyle.setFont(chFont);

        LinkedHashMap<String, HSSFCellStyle> bodyColStyleMap = new LinkedHashMap<String, HSSFCellStyle>();
        bodyColStyleMap.put("表名", enStyle);
        bodyColStyleMap.put("描述", chStyle);
        bodyColStyleMap.put("备注", chStyle);
        return bodyColStyleMap;
    }

    // 库表表体样式
    private LinkedHashMap<String, HSSFCellStyle> createDBTabBodyStyle(HSSFWorkbook workbook) {
        HSSFCellStyle enStyle = workbook.createCellStyle();
        enStyle.setWrapText(true); // 自动换行
        enStyle.setAlignment(HorizontalAlignment.LEFT);         // 横向靠左
        enStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        enStyle.setBorderLeft(BorderStyle.THIN);
        enStyle.setBorderRight(BorderStyle.THIN);
        enStyle.setBorderTop(BorderStyle.THIN);
        enStyle.setBorderBottom(BorderStyle.THIN);
        enStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        enStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        enStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        enStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        HSSFFont enFont = workbook.createFont();     // 表体字体
        enFont.setFontName("Consolas");              // 设置字体
        enFont.setCharSet(HSSFFont.DEFAULT_CHARSET); // 设置编码
        enFont.setFontHeightInPoints((short) 10);    // 字体大小
        enFont.setBold(Boolean.FALSE);
        enStyle.setFont(enFont);

        HSSFCellStyle chStyle = workbook.createCellStyle();
        chStyle.setWrapText(true); // 自动换行
        chStyle.setAlignment(HorizontalAlignment.LEFT);       // 横向靠左
        chStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        chStyle.setBorderLeft(BorderStyle.THIN);
        chStyle.setBorderRight(BorderStyle.THIN);
        chStyle.setBorderTop(BorderStyle.THIN);
        chStyle.setBorderBottom(BorderStyle.THIN);
        chStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        chStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        chStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        chStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        HSSFFont chFont = workbook.createFont();     // 表体字体
        chFont.setFontName("微软雅黑");              // 设置字体
        chFont.setCharSet(HSSFFont.DEFAULT_CHARSET); // 设置编码
        chFont.setFontHeightInPoints((short) 10);    // 字体大小
        chFont.setBold(Boolean.FALSE);
        chStyle.setFont(chFont);

        LinkedHashMap<String, HSSFCellStyle> bodyColStyleMap = new LinkedHashMap<String, HSSFCellStyle>();
        bodyColStyleMap.put("字段名", enStyle);
        bodyColStyleMap.put("字段描述", chStyle);
        bodyColStyleMap.put("数据类型", enStyle);
        bodyColStyleMap.put("最大字节数", enStyle);
        bodyColStyleMap.put("是否主键", chStyle);
        bodyColStyleMap.put("非空", chStyle);
        bodyColStyleMap.put("归属的唯一约束", enStyle);
        return bodyColStyleMap;
    }


    public void generateDict(String sql) {
        // 文档生成的输出目录
        String outputDircName = ConfigLoader.getInstance(this.configFilename).getOutputDirc();
        File outputDirc = new File(outputDircName, "doc");
        outputDirc.mkdirs();
        String filename = "db_dict_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";
        try {
            String outfilename = new File(outputDirc, filename).getCanonicalPath();
            HSSFWorkbook workbook = new HSSFWorkbook();

            LinkedHashMap<String, HSSFCellStyle> dbTabHeadStyleMap = this.createDictHeadStyle(workbook); // 数据字典表头样式
            LinkedHashMap<String, HSSFCellStyle> dbTabBodyStyleMap = this.createDictBodyStyle(workbook); // 数据字典表体样式

            ArrayList<LinkedHashMap<String, Object>>  bodyColValueMapList = this.fetchData(sql.toLowerCase());
            int[] colwidthArray = {28, 75, 30, 55};
            ExcelUtil.createWorkbook(workbook, "数据字典(ark_dict)", colwidthArray, null, dbTabHeadStyleMap, dbTabBodyStyleMap, bodyColValueMapList, null, null, null);

            ExcelUtil.writeWorkbook(workbook, outfilename);
            LOGGER.info(">> 已成功生成数据字典到: " + outfilename);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private ArrayList<LinkedHashMap<String, Object>> fetchData(String sql) throws SQLException, ClassNotFoundException {
        LOGGER.info("\n>> {} ", sql);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBResource.getConnection(this.configFilename);
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            int colCnt = rs.getMetaData().getColumnCount();

            ArrayList<LinkedHashMap<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                for (int i = 1; i <= colCnt; i++) {
                    LOGGER.info("  {} : {}", rs.getMetaData().getColumnLabel(i), rs.getString(i));
                    map.put(rs.getMetaData().getColumnLabel(i).toLowerCase(), rs.getString(i));
                }
                list.add(map);
            }
            rs.close();
            stmt.close();
            return list;
        } finally {
            DBResource.freeConnection(conn);
        }
    }


    // 数据字典表头样式
    private LinkedHashMap<String, HSSFCellStyle> createDictHeadStyle(HSSFWorkbook workbook) {
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

        LinkedHashMap<String, HSSFCellStyle> headColStyleMap = new LinkedHashMap<String, HSSFCellStyle>();
        headColStyleMap.put("kind", headStyle);
        headColStyleMap.put("kind_desc", headStyle);
        headColStyleMap.put("code", headStyle);
        headColStyleMap.put("code_desc", headStyle);

        return headColStyleMap;
    }


    // 数据字典表体样式
    private LinkedHashMap<String, HSSFCellStyle> createDictBodyStyle(HSSFWorkbook workbook) {
        HSSFCellStyle enStyle = workbook.createCellStyle();
        enStyle.setWrapText(true); // 自动换行
        enStyle.setAlignment(HorizontalAlignment.LEFT);         // 横向靠左
        enStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        enStyle.setBorderLeft(BorderStyle.THIN);
        enStyle.setBorderRight(BorderStyle.THIN);
        enStyle.setBorderTop(BorderStyle.THIN);
        enStyle.setBorderBottom(BorderStyle.THIN);
        enStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        enStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        enStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        enStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        HSSFFont enFont = workbook.createFont();     // 表体字体
        enFont.setFontName("Consolas");              // 设置字体
        enFont.setCharSet(HSSFFont.DEFAULT_CHARSET); // 设置编码
        enFont.setFontHeightInPoints((short) 10);    // 字体大小
        enFont.setBold(Boolean.FALSE);
        enStyle.setFont(enFont);

        HSSFCellStyle chStyle = workbook.createCellStyle();
        chStyle.setWrapText(true); // 自动换行
        chStyle.setAlignment(HorizontalAlignment.LEFT);       // 横向靠左
        chStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        chStyle.setBorderLeft(BorderStyle.THIN);
        chStyle.setBorderRight(BorderStyle.THIN);
        chStyle.setBorderTop(BorderStyle.THIN);
        chStyle.setBorderBottom(BorderStyle.THIN);
        chStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        chStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        chStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        chStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        HSSFFont chFont = workbook.createFont();     // 表体字体
        chFont.setFontName("微软雅黑");               // 设置字体
        chFont.setCharSet(HSSFFont.DEFAULT_CHARSET); // 设置编码
        chFont.setFontHeightInPoints((short) 10);    // 字体大小
        chFont.setBold(Boolean.FALSE);
        chStyle.setFont(chFont);

        LinkedHashMap<String, HSSFCellStyle> bodyColStyleMap = new LinkedHashMap<>();
        bodyColStyleMap.put("kind", enStyle);
        bodyColStyleMap.put("kind_desc", chStyle);
        bodyColStyleMap.put("code", enStyle);
        bodyColStyleMap.put("code_desc", chStyle);
        return bodyColStyleMap;
    }
}
