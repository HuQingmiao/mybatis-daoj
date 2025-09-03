package walker.mybatis.daoj.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import walker.mybatis.daoj.utils.MappingUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.Date;

/**
 * 代码构建
 *
 * @author HuQingmiao
 */
public abstract class CodeBuilder {
    private static final Logger LOGGER = LogManager.getLogger(CodeBuilder.class);

    // 默认行版本号的列的定义只能是： row_version BIGINT(12)  DEFAULT 1 COMMENT '行版本号'
    protected final String ROW_VERSION_COLUMNNAME = "row_version";

    // 通常，批量更新时不应该更改创建时间
    protected final String CREATE_TIME_COLUMNNAME = "create_time";
    protected final String CREATE_DATE_COLUMNNAME = "create_date";

    protected HashSet<Integer> bigDataTypes;

    protected String configFilename;

    protected String tableName;

    //列描述 Map<列名, 列元数据>
    protected Map<String, MetaDataDescr> colNameMetaMap;

    //主键列 Set<列名>
    protected Set<String> pkColSet;

    protected CodeBuilder(String configFilename, String tableName) throws SQLException, ClassNotFoundException {
        this.configFilename = configFilename;
        this.tableName = tableName.trim().toLowerCase();
        this.colNameMetaMap = this.parseMetaData(this.tableName);

        this.bigDataTypes = new HashSet<>();
        bigDataTypes.add(Types.BINARY);
        bigDataTypes.add(Types.VARBINARY);
        bigDataTypes.add(Types.LONGVARBINARY);
        bigDataTypes.add(Types.BLOB);
        bigDataTypes.add(Types.CLOB);
        bigDataTypes.add(Types.NCLOB);

        this.pkColSet = new HashSet();
        for (Iterator it = this.colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
            MetaDataDescr md = colNameMetaMap.get(it.next());
            if (md.isPk()) {
                pkColSet.add(md.getColName());
            }
        }
    }

    public static CodeBuilder getInstance(String configFilename, String tableName) throws SQLException, ClassNotFoundException {
        DbPattern dbPattern = ConfigLoader.getInstance(configFilename).getDbPattern();
        if (DbPattern.mysql.equals(dbPattern)) {
            return new CodeBuilderForMysql(configFilename, tableName);
        }
        if (DbPattern.oracle.equals(dbPattern)) {
            return new CodeBuilderForOracle(configFilename, tableName);
        }
        return null;
    }

    /**
     * 构造实体类的源码
     *
     * @return
     */
    public String buildEntitySource() {
        StringBuffer buff = new StringBuffer();

        buff.append("package ");
        buff.append(ConfigLoader.getInstance(this.configFilename).getPoPackage() + "; \n\n");

        buff.append("import " + ConfigLoader.getInstance(this.configFilename).getBasicPo() + "; \n");
        buff.append("\n");

        // public class AA {
        buff.append("public class " + MappingUtil.getEntityName(tableName));
        buff.append(" extends BasicPo {\n");
        buff.append("    private static final long serialVersionUID = 1L;\n\n");

        buff.append("    public static final String $TABLE_NAME = \"" + tableName.toUpperCase() + "\";\n\n");
        //对各字段名生成静态属性
        for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
            String colName = it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
            String filedName = md.getFieldName();
            buff.append("    public static final String $" + colName.toUpperCase() + " = \"" + filedName + "\";\n");
        }
        buff.append("\n");

        //生成属性  private String xxx;
        for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
            String colName = it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
            String filedName = md.getFieldName();
            Class fieldType = md.getFieldType();
            String fieldTypeName = fieldType.getName();

            if (fieldType.getName().contains("java.lang") || fieldType.getName().startsWith("[")) {
                fieldTypeName = fieldType.getSimpleName();
            }

//            if(java.sql.Date.class.getName().equals(fieldType.getName())
//                    || java.sql.Timestamp.class.getName().equals(fieldType.getName())){
//                fieldTypeName = java.sql.Date.class.getName();
//            }

            if (StringUtils.isNotBlank(md.getComment())) {
                buff.append("    /** " + md.getComment() + " */\n");
            }
            buff.append("    private " + fieldTypeName + " " + filedName + ";\n");
        }
        buff.append("\n");

        buff.append("    public " + MappingUtil.getEntityName(tableName) + " () {");
        buff.append("\n    }\n\n");

        //生成方法  public String getXXX();
        for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
            String colName = it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
            String fieldName = md.getFieldName();
            Class fieldType = md.getFieldType();
            String fieldTypeName = fieldType.getName();

            if (fieldType.getName().contains("java.lang") || fieldType.getName().startsWith("[")) {
                fieldTypeName = fieldType.getSimpleName();
            }
//            if(java.sql.Date.class.getName().equals(fieldType.getName())
//                    || java.sql.Timestamp.class.getName().equals(fieldType.getName())){
//                fieldTypeName = java.sql.Date.class.getName();
//            }
            String firstChar = fieldName.substring(0, 1).toUpperCase();
            if (fieldName.length() > 1 && Character.isUpperCase(fieldName.charAt(1))) {
                firstChar = firstChar.toLowerCase();
            }
            buff.append("    public " + fieldTypeName + " get");
            buff.append(firstChar + fieldName.substring(1) + "() {\n");
            buff.append("        return " + fieldName + ";\n");
            buff.append("   }\n\n");

            buff.append("    public void set");
            buff.append(firstChar + fieldName.substring(1));
            buff.append("(" + fieldTypeName + " " + fieldName + ") {\n");
            buff.append("        this." + fieldName + " = " + fieldName + ";\n");
            buff.append("   }\n\n");
        }
        buff.append("}\n\n");
        return buff.toString();
    }


    /**
     * 构造DAO类的源码
     *
     * @return
     */
    public String buildDaoSource() {
        StringBuffer buff = new StringBuffer();

        buff.append("package ");
        buff.append(ConfigLoader.getInstance(this.configFilename).getDaoPackage() + "; \n\n");
        buff.append("import " + ConfigLoader.getInstance(this.configFilename).getBasicDao() + "; \n");
        buff.append("\n");

        // public class AA {
        buff.append("public interface ");
        buff.append(MappingUtil.getEntityName(tableName) + "Dao");
        buff.append(" extends BasicDao {\n");


        // 如果表中含有row_version字段，则多生成一个乐观锁并发更新方法
        if (colNameMetaMap.containsKey(ROW_VERSION_COLUMNNAME)) {
            buff.append("\n    /** ***************** daoj 自动生成的方法 BEGIN ***************** */\n");
            buff.append("\n    // 依赖row_version实现的并发更新");
            buff.append("\n    int updateWithOptiLock(BasicPo basicPo); \n");
            buff.append("\n    /** ***************** daoj 自动生成的方法 END. ***************** */\n\n\n");
        }

        buff.append("}\n");

        return buff.toString();
    }


    /**
     * 构造MAPPER的源码
     *
     * @return
     */
    public String buildMapperSource() {
        final String head = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<!DOCTYPE mapper PUBLIC  \"-//mybatis.org//DTD Mapper 3.0//EN\"  \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n\n";

        StringBuffer buff = new StringBuffer(head);
        buff.append("<mapper namespace=\"" + ConfigLoader.getInstance(this.configFilename).getDaoPackage() + "." + MappingUtil.getEntityName(tableName) + "Dao\">\n");

        buff.append("\n    <!-- ============================= INSERT ============================= -->\n");
        buff.append(this.buildMapperSave());
        buff.append(this.buildMapperSaveBatch());

        buff.append("\n    <!-- ============================= UPDATE ============================= -->\n");
        buff.append(this.buildMapperUpdate());
        // 如果表中含有row_version字段，则多生成一个乐观锁并发更新方法
        if (colNameMetaMap.containsKey(ROW_VERSION_COLUMNNAME)) {
            buff.append(buildMapperUpdateWithOptiLock());
        }
        buff.append(this.buildMapperUpdateIgnoreNull());
        buff.append(this.buildMapperUpdateBatch());

        buff.append("\n    <!-- ============================= DELETE ============================= -->\n");
        buff.append(this.buildMapperDelete());
        buff.append(this.buildMapperDeleteBatch());
        buff.append(this.buildMapperDeleteByPk());
        buff.append(this.buildMapperDeleteByPKs());
        buff.append(this.buildMapperDeleteAll());

        buff.append("\n    <!-- ============================= SELECT ============================= -->\n");
        buff.append(this.buildMapperCount());
        buff.append(this.buildMapperFindByPK());
        buff.append(this.buildMapperFindByPKs());
        buff.append(this.buildMapperFind());

        buff.append("</mapper>\n");
        return buff.toString();
    }


    protected String buildMapperSave() {
        StringBuffer buff = new StringBuffer();
        buff.append("    <insert id=\"save\">\n");

        buff.append("        INSERT INTO " + tableName + "( ");
        StringBuffer valuesStr = new StringBuffer();

        int i = 0;
        for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
            String colName = it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
            if (bigDataTypes.contains(md.getColType())) {
                continue;
            }
            //对于标识行版本号字段，在insert语句中不列出此字段
            if (ROW_VERSION_COLUMNNAME.equals(md.getColName().toLowerCase())) {
                continue;
            }

            buff.append(colName + ",");
            valuesStr.append("#{" + md.getFieldName() + "},");
            i++;
            if (i % 7 == 0) {
                buff.append("\n                         ");
                valuesStr.append("\n                 ");
            }
        }
        while (buff.charAt(buff.length() - 1) == ' ') {
            buff.deleteCharAt(buff.length() - 1);
        }
        while (valuesStr.charAt(valuesStr.length() - 1) == ' ') {
            valuesStr.deleteCharAt(valuesStr.length() - 1);
        }
        if (buff.charAt(buff.length() - 1) == '\n') {
            buff.deleteCharAt(buff.length() - 1);
        }
        if (buff.charAt(buff.length() - 1) == ',') {
            buff.deleteCharAt(buff.length() - 1);
        }
        if (valuesStr.charAt(valuesStr.length() - 1) == '\n') {
            valuesStr.deleteCharAt(valuesStr.length() - 1);
        }
        if (valuesStr.charAt(valuesStr.length() - 1) == ',') {
            valuesStr.deleteCharAt(valuesStr.length() - 1);
        }
        buff.append(" )\n");

        buff.append("        VALUES ");
        buff.append("( " + valuesStr.toString() + ")\n");
        buff.append("    </insert>\n\n");
        valuesStr.delete(0, valuesStr.length());

        return buff.toString();
    }


    protected abstract String buildMapperSaveBatch();

    protected String buildMapperUpdate() {
        if (pkColSet.isEmpty()) {
            LOGGER.warn("要生成完整正确的update语句，必须为表指定主键！");
            //return "";
        }

        StringBuffer buff = new StringBuffer();
        buff.append("    <update id=\"update\">\n");

        if (!pkColSet.isEmpty()) {
            buff.append("        UPDATE " + tableName + "\n");
            buff.append("        <set>\n");

            for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
                String colName = it.next();
                MetaDataDescr md = colNameMetaMap.get(colName);
                if (md.isPk() | bigDataTypes.contains(md.getColType())) {
                    continue;
                }
                //对于行版本号字段，设为更新时自动递增
                if (ROW_VERSION_COLUMNNAME.equals(md.getColName().toLowerCase())) {
                    buff.append("            " + ROW_VERSION_COLUMNNAME + " = " + ROW_VERSION_COLUMNNAME + " + 1,\n");
                } else {
                    buff.append("            " + colName + " = #{" + md.getFieldName() + "},\n");
                }
            }
            buff.append("        </set>\n");

            buff.append("        WHERE ");

            for (Iterator<String> keyIt = pkColSet.iterator(); keyIt.hasNext(); ) {
                String pkColName = keyIt.next();
                String pkFieldName = this.colNameMetaMap.get(pkColName).getFieldName();
                buff.append(pkColName + " = #{" + pkFieldName + "} AND ");
            }
            if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
                buff.delete(buff.length() - 4, buff.length());
            }
            buff.append("\n");
        }

        buff.append("    </update>\n\n");
        return buff.toString();
    }

    protected String buildMapperUpdateWithOptiLock() {
        String baseUpdateSql = this.buildMapperUpdate()
                .replaceAll("<update id=\"update\">", "<update id=\"updateWithOptiLock\">");

        StringBuffer buff = new StringBuffer(baseUpdateSql);
        buff.insert(buff.length() - 16, " and row_version = #{rowVersion} ");
        return buff.toString();
    }

    protected String buildMapperUpdateIgnoreNull() {
        if (pkColSet.isEmpty()) {
            LOGGER.warn("要生成完整正确的update语句，必须为表指定主键！");
            //return "";
        }

        StringBuffer buff = new StringBuffer();
        buff.append("    <update id=\"updateIgnoreNull\">\n");

        if (!pkColSet.isEmpty()) {
            buff.append("        UPDATE " + tableName + "\n");
            buff.append("        <set>\n");

            for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
                String colName = it.next();
                MetaDataDescr md = colNameMetaMap.get(colName);
                if (md.isPk() | bigDataTypes.contains(md.getColType())) {
                    continue;
                }
                //对于行版本号字段，设为更新时自动递增
                if (ROW_VERSION_COLUMNNAME.equals(md.getColName().toLowerCase())) {
                    buff.append("            " + ROW_VERSION_COLUMNNAME + " = " + ROW_VERSION_COLUMNNAME + " + 1,\n");
                } else {
                    buff.append("            <if test=\"" + md.getFieldName() + "!= null\">" + colName + " = #{" + md.getFieldName() + "},</if>\n");
                }
            }
            buff.append("        </set>\n");

            buff.append("        WHERE ");

            for (Iterator<String> keyIt = pkColSet.iterator(); keyIt.hasNext(); ) {
                String pkColName = keyIt.next();
                String pkFieldName = this.colNameMetaMap.get(pkColName).getFieldName();
                buff.append(pkColName + " = #{" + pkFieldName + "} AND ");
            }
            if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
                buff.delete(buff.length() - 4, buff.length());
            }
            buff.append("\n");
        }

        buff.append("    </update>\n\n");
        return buff.toString();
    }

    protected abstract String buildMapperUpdateBatch();


    protected String buildMapperDelete() {
        if (pkColSet.isEmpty()) {
            LOGGER.warn("要生成完整正确的delete语句，必须为表指定主键！");
            //return "";
        }

        StringBuffer buff = new StringBuffer();
        buff.append("    <delete id=\"delete\">\n");

        if (!pkColSet.isEmpty()) {
            buff.append("        DELETE FROM " + tableName + "\n");
            buff.append("        WHERE ");

            for (Iterator<String> keyIt = pkColSet.iterator(); keyIt.hasNext(); ) {
                String pkColName = keyIt.next();
                String pkFieldName = this.colNameMetaMap.get(pkColName).getFieldName();
                buff.append(pkColName + " = #{" + pkFieldName + "} AND ");
            }

            if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
                buff.delete(buff.length() - 4, buff.length());
            }
            buff.append("\n");
        }

        buff.append("    </delete>\n\n");
        return buff.toString();
    }


    protected String buildMapperDeleteBatch() {
        if (pkColSet.isEmpty()) {
            LOGGER.warn("要生成完整正确的delete语句，必须为表指定主键！");
            //return "";
        }

        StringBuffer buff = new StringBuffer();
        buff.append("    <delete id=\"deleteBatch\">\n");

        if (!pkColSet.isEmpty()) {
            buff.append("        DELETE FROM " + tableName + "\n");
            buff.append("        WHERE\n");
            buff.append("        <foreach collection=\"list\" item=\"item\" index=\"index\" open=\"(\" separator=\"OR\" close=\")\">\n");
            buff.append("            ");
            for (Iterator<String> keyIt = pkColSet.iterator(); keyIt.hasNext(); ) {
                String pkColName = keyIt.next();
                String pkFieldName = this.colNameMetaMap.get(pkColName).getFieldName();
                buff.append(pkColName + " = #{item." + pkFieldName + "} AND ");
            }
            if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
                buff.delete(buff.length() - 4, buff.length());
            }
            buff.append("\n");
            buff.append("        </foreach>\n");
        }
        buff.append("    </delete>\n\n");
        return buff.toString();
    }

    protected String buildMapperDeleteByPk() {
        if (pkColSet.isEmpty()) {
            LOGGER.warn("要生成完整正确的delete语句，必须为表指定主键！");
            //return "";
        }

        StringBuffer buff = new StringBuffer();
        buff.append("    <delete id=\"deleteByPK\">\n");

        if (!pkColSet.isEmpty()) {
            buff.append("        DELETE FROM " + tableName + "\n");
            buff.append("        WHERE ");
            for (Iterator<String> keyIt = pkColSet.iterator(); keyIt.hasNext(); ) {
                String pkColName = keyIt.next();
                String pkFieldName = this.colNameMetaMap.get(pkColName).getFieldName();
                buff.append(pkColName + " = #{" + pkFieldName + "} AND ");
            }
            if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
                buff.delete(buff.length() - 4, buff.length());
            }
            buff.append("\n");
        }
        buff.append("    </delete>\n\n");
        return buff.toString();
    }

    protected String buildMapperDeleteByPKs() {
        if (pkColSet.isEmpty()) {
            LOGGER.warn("要生成完整正确的delete语句，必须为表指定主键！");
            //return "";
        }

        StringBuffer buff = new StringBuffer();
        buff.append("    <delete id=\"deleteByPKs\">\n");

        if (!pkColSet.isEmpty()) {
            buff.append("        DELETE FROM " + tableName + "\n");
            buff.append("        WHERE \n ");

            buff.append("        <foreach collection=\"collection\" item=\"item\" index=\"index\" open=\"(\" separator=\"OR\" close=\")\">\n");
            buff.append("            ");
            for (Iterator<String> keyIt = pkColSet.iterator(); keyIt.hasNext(); ) {
                String pkColName = keyIt.next();
                buff.append(pkColName + " = #{item} AND ");
            }
            if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
                buff.delete(buff.length() - 4, buff.length());
            }
            buff.append("\n");
            buff.append("        </foreach>\n");
        }
        buff.append("    </delete>\n\n");
        return buff.toString();
    }

    protected String buildMapperDeleteAll() {
        StringBuffer buff = new StringBuffer();
        buff.append("    <delete id=\"deleteAll\">\n");
        buff.append("        DELETE FROM " + tableName + "\n");
        buff.append("    </delete>\n\n");
        return buff.toString();
    }

    protected String buildMapperCount() {
        StringBuffer buff = new StringBuffer();
        buff.append("    <select id=\"count\" resultType=\"java.lang.Long\">\n");
        buff.append("        SELECT COUNT(*) FROM " + tableName + "\n");
        buff.append("    </select>\n\n");
        return buff.toString();
    }

    protected String buildMapperFindByPK() {
        if (pkColSet.isEmpty()) {
            LOGGER.warn("要生成完整正确的findByPK语句，必须为表指定主键！");
            //return "";
        }

        StringBuffer buff = new StringBuffer();
        buff.append("    <select id=\"findByPK\" resultType=\"" + ConfigLoader.getInstance(this.configFilename).getPoPackage() + "." + MappingUtil.getEntityName(tableName) + "\">\n");
        buff.append("        SELECT ");

        int i = 0;
        for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
            String colName = it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
            if (bigDataTypes.contains(md.getColType())) {
                continue;
            }
            buff.append(colName + ", ");
            i++;
            if (i % 7 == 0) {
                buff.append("\n               ");
            }
        }
        while (buff.charAt(buff.length() - 1) == ' ') {
            buff.deleteCharAt(buff.length() - 1);
        }
        if (buff.charAt(buff.length() - 1) == '\n') {
            buff.deleteCharAt(buff.length() - 1);
        }
        while (buff.charAt(buff.length() - 1) == ' ') {
            buff.deleteCharAt(buff.length() - 1);
        }
        if (buff.charAt(buff.length() - 1) == ',') {
            buff.deleteCharAt(buff.length() - 1);
        }
        buff.append("\n");

        buff.append("         FROM " + tableName + "\n");
        buff.append("        WHERE ");

        for (Iterator<String> keyIt = pkColSet.iterator(); keyIt.hasNext(); ) {
            String pkColName = keyIt.next();
            String pkFieldName = this.colNameMetaMap.get(pkColName).getFieldName();
            buff.append(pkColName + " = #{" + pkFieldName + "} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");
        buff.append("    </select>\n\n");

        return buff.toString();
    }

    protected String buildMapperFindByPKs() {
        if (pkColSet.isEmpty()) {
            LOGGER.warn("要生成完整正确的findByPKs语句，必须为表指定主键！");
            //return "";
        }

        StringBuffer buff = new StringBuffer();
        buff.append("    <select id=\"findByPKs\" resultType=\"" + ConfigLoader.getInstance(this.configFilename).getPoPackage() + "." + MappingUtil.getEntityName(tableName) + "\">\n");
        buff.append("        SELECT ");

        int i = 0;
        for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
            String colName = it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
            if (bigDataTypes.contains(md.getColType())) {
                continue;
            }
            buff.append(colName + ", ");
            i++;
            if (i % 7 == 0) {
                buff.append("\n               ");
            }
        }
        while (buff.charAt(buff.length() - 1) == ' ') {
            buff.deleteCharAt(buff.length() - 1);
        }
        if (buff.charAt(buff.length() - 1) == '\n') {
            buff.deleteCharAt(buff.length() - 1);
        }
        while (buff.charAt(buff.length() - 1) == ' ') {
            buff.deleteCharAt(buff.length() - 1);
        }
        if (buff.charAt(buff.length() - 1) == ',') {
            buff.deleteCharAt(buff.length() - 1);
        }
        buff.append("\n");

        buff.append("         FROM " + tableName + "\n");
        buff.append("        WHERE \n");

        buff.append("        <foreach collection=\"collection\" item=\"item\" index=\"index\" open=\"(\" separator=\"OR\" close=\")\">\n");
        buff.append("            ");
        for (Iterator<String> keyIt = pkColSet.iterator(); keyIt.hasNext(); ) {
            String pkColName = keyIt.next();
            buff.append(pkColName + " = #{item} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");
        buff.append("        </foreach>\n");
        buff.append("    </select>\n\n");

        return buff.toString();
    }

    protected String buildMapperFind() {
        StringBuffer buff = new StringBuffer();
        buff.append("    <select id=\"find\" resultType=\"" + ConfigLoader.getInstance(this.configFilename).getPoPackage() + "." + MappingUtil.getEntityName(tableName) + "\">\n");
        buff.append("        SELECT ");

        int i = 0;
        for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
            String colName = it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
            if (bigDataTypes.contains(md.getColType())) {
                continue;
            }
            buff.append(colName + ", ");
            i++;
            if (i % 7 == 0) {
                buff.append("\n               ");
            }
        }
        while (buff.charAt(buff.length() - 1) == ' ') {
            buff.deleteCharAt(buff.length() - 1);
        }
        if (buff.charAt(buff.length() - 1) == '\n') {
            buff.deleteCharAt(buff.length() - 1);
        }
        while (buff.charAt(buff.length() - 1) == ' ') {
            buff.deleteCharAt(buff.length() - 1);
        }
        if (buff.charAt(buff.length() - 1) == ',') {
            buff.deleteCharAt(buff.length() - 1);
        }
        buff.append("\n");

        buff.append("         FROM " + tableName + "\n");
        buff.append("        <where>\n");
        for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
            String colName = it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);

            String filedName = md.getFieldName();
            buff.append("            <if test=\"" + filedName + "!= null\">\n");

            if (md.getFieldType().getName().equals(String.class.getName())) {
                buff.append("               AND " + colName + " like #{" + filedName + "}\n");
            } else if (md.getFieldType().getName().equals(Double.class.getName())
                    || md.getFieldType().getName().equals(Float.class.getName())
                    || md.getFieldType().getName().equals(BigDecimal.class.getName())) {
                buff.append("               <![CDATA[  AND " + colName + " >= #{" + filedName + "}  ]]> \n");
            } else if (md.getFieldType().getName().equals(Date.class.getName())) {
                buff.append("               AND DATE_FORMAT(" + colName + ",'%Y-%m-%d') = #{" + filedName + "}\n");
            } else {
                buff.append("               AND " + colName + " = #{" + filedName + "}\n");
            }
            buff.append("            </if>\n");
        }
        buff.append("        </where>\n");
        buff.append("    </select>\n\n");
        return buff.toString();
    }


    /**
     * 取得表的元数据，即取得各列名及类型
     *
     * @return 列名及其列类型：LinkedHashMap<String, MyMetaData> map
     * @throws Exception
     */
    private LinkedHashMap<String, MetaDataDescr> parseMetaData(String tableName) throws SQLException, ClassNotFoundException {
        LinkedHashMap<String, MetaDataDescr> cmap = new LinkedHashMap();
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = DBResource.getConnection(this.configFilename);
            DatabaseMetaData metaData = conn.getMetaData();

            //定位主键字段
            Set<String> keySet = new HashSet<String>();
            rs = metaData.getPrimaryKeys(null, null, tableName.toUpperCase());
            if (rs.next()) {
                String pk = rs.getString("COLUMN_NAME").toLowerCase();
                keySet.add(pk);
                for (; rs.next(); ) {
                    pk = rs.getString("COLUMN_NAME").toLowerCase();
                    keySet.add(pk);
                }
            } else {
                rs = metaData.getPrimaryKeys(null, null, tableName.toLowerCase());
                for (; rs.next(); ) {
                    String pk = rs.getString("COLUMN_NAME").toLowerCase();
                    keySet.add(pk);
                }
            }
            rs.close();


            DbPattern dbPattern = ConfigLoader.getInstance(this.configFilename).getDbPattern();
            String jdbcDriver = ConfigLoader.getInstance(this.configFilename).getJdbcDriver();

            //定位唯一约束字段
            HashMap<String, String> colUdxMap = new HashMap<String, String>();
            if (DbPattern.mysql.equals(dbPattern)) {
                rs = metaData.getIndexInfo(null, null, tableName.toUpperCase(), true, false);
                String udxName = null;
                String colName = null;
                for (; rs.next(); ) {
                    udxName = rs.getString("INDEX_NAME").toLowerCase();
                    if (!udxName.toLowerCase().equals("primary")) {  // 主键前面已识别，在此不再作为唯一约束来标识
                        colName = rs.getString("COLUMN_NAME").toLowerCase();
                        colUdxMap.put(colName, udxName);
                    }
                    LOGGER.info(">>upper {}: {}", udxName, colName);
                }
                rs.close();
            }

            HashMap<String, String> oracleColCommnetMap = null;
            String mysqlAutoIncreaedCol = null;
            if (DbPattern.oracle.equals(dbPattern)) {
                oracleColCommnetMap = this.fetchColumnCommentForOracle(tableName);
            }
            if (DbPattern.mysql.equals(dbPattern) && !jdbcDriver.contains("postgresql.")) {
                mysqlAutoIncreaedCol = this.fetchAutoIncreatedColumnForMysql(tableName);
            }

            // 查询列的元数据，获取列注释
            rs = metaData.getColumns(null, null, tableName.toUpperCase(), null);
            if (rs.next()) {
                this.postRs(rs, keySet, colUdxMap, mysqlAutoIncreaedCol, oracleColCommnetMap, cmap);
                for (; rs.next(); ) {
                    this.postRs(rs, keySet, colUdxMap, mysqlAutoIncreaedCol, oracleColCommnetMap, cmap);
                }
            } else {
                rs = metaData.getColumns(null, null, tableName.toLowerCase(), null);
                for (; rs.next(); ) {
                    this.postRs(rs, keySet, colUdxMap, mysqlAutoIncreaedCol, oracleColCommnetMap, cmap);
                }
            }
            rs.close();
            return cmap;
        } finally {
            DBResource.freeConnection(conn);
        }
    }

    private void postRs(ResultSet rs, Set<String> keySet, HashMap<String, String> colUdxMap, String mysqlAutoIncreaedCol, HashMap<String, String> oracleColCommnetMap, LinkedHashMap<String, MetaDataDescr> metaMap) throws SQLException {
        DbPattern dbPattern = ConfigLoader.getInstance(this.configFilename).getDbPattern();
        String jdbcDriver = ConfigLoader.getInstance(this.configFilename).getJdbcDriver();

        String columnName = rs.getString("COLUMN_NAME");
        columnName = columnName.toLowerCase();

        String tmp = rs.getString("DATA_TYPE");
        int dataType = Integer.parseInt(tmp);
        String typeName = rs.getString("TYPE_NAME");

        tmp = rs.getString("COLUMN_SIZE");
        int columnSize = Integer.parseInt(tmp);

        tmp = rs.getString("DECIMAL_DIGITS");
        int decimalDigits = tmp == null ? 0 : Integer.parseInt(tmp);

        tmp = rs.getString("CHAR_OCTET_LENGTH");
        int charByteLen = tmp == null ? 0 : Integer.parseInt(tmp);

        tmp = rs.getString("NULLABLE");
        boolean nullable = "1".equals(tmp) ? Boolean.TRUE : Boolean.FALSE;

        tmp = rs.getString("IS_AUTOINCREMENT");
        boolean autoIncrement = "1".equals(tmp) ? Boolean.TRUE : Boolean.FALSE;
        if (DbPattern.mysql.equals(dbPattern) && !jdbcDriver.contains("postgresql.")) {
            autoIncrement = columnName.equals(mysqlAutoIncreaedCol) ? true : false;
        }

        String comment = rs.getString("REMARKS"); // 对于oracle，这种方式是取不到注释的
        if (DbPattern.oracle.equals(dbPattern)) {
            comment = oracleColCommnetMap.get(columnName);
        }

        String colTypeDesc = typeName;
        if (dataType == Types.FLOAT || dataType == Types.REAL || dataType == Types.DOUBLE || dataType == Types.NUMERIC || dataType == Types.DECIMAL) {
            if (decimalDigits <= 0) {
                colTypeDesc = typeName + "(" + columnSize + ")";
            } else {
                colTypeDesc = typeName + "(" + columnSize + "," + decimalDigits + ")";
            }
        }
        if (dataType == Types.CHAR || dataType == Types.VARCHAR || dataType == Types.LONGVARCHAR) {
            colTypeDesc = typeName + "(" + columnSize + ")";
        }

        MetaDataDescr md = new MetaDataDescr();
        md.setColName(columnName);
        md.setColType(dataType);
        md.setColTypeDesc(colTypeDesc.toLowerCase());
        md.setColSize(columnSize);
        md.setDecimalDigits(decimalDigits);
        md.setMaxByteLen(charByteLen);

        md.setPk(keySet.contains(columnName) ? true : false);
        md.setUdxName(colUdxMap.containsKey(columnName) ? colUdxMap.get(columnName) : "");
        md.setNullable(nullable);
        md.setAutoIncreased(autoIncrement);

        md.setFieldName(MappingUtil.getFieldName(columnName));
        md.setFieldType(reflectToFieldType(dataType, columnSize, decimalDigits)); //把列类型映射为类属性类型
        md.setComment(comment);

        LOGGER.info("  " + md.getColName() + ": " + md.getColTypeDesc() + "  " + md.getComment() + "  " + md.isPk() + "(是否主键)  " + "  " + md.isAutoIncreased() + "(是否自增)  " + md.isNullable() + "(可空)  " + md.getMaxByteLen() + "(字符最大字节数) ");
        metaMap.put(columnName, md);
    }


    private HashMap<String, String> fetchColumnCommentForOracle(String tableName) throws SQLException, ClassNotFoundException {
        HashMap<String, String> colCommentMap = new HashMap<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBResource.getConnection(this.configFilename);
            String sql = "select column_name, comments from user_col_comments where lower(table_name)=? ";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, tableName.toLowerCase());
            rs = stmt.executeQuery();
            while (rs.next()) {
                String colName = rs.getString(1).toLowerCase();
                String comment = rs.getString(2);
                colCommentMap.put(colName.toLowerCase(), comment);
            }
            rs.close();
            stmt.close();
            return colCommentMap;
        } finally {
            DBResource.freeConnection(conn);
        }
    }

    private String fetchAutoIncreatedColumnForMysql(String tableName) throws SQLException, ClassNotFoundException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBResource.getConnection(this.configFilename);
            String sql = "select column_name from information_schema.columns where lower(table_name) = ? and extra='auto_increment' ";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, tableName.toLowerCase());
            rs = stmt.executeQuery();
            String colName = null;
            if (rs.next()) {
                colName = rs.getString(1).toLowerCase();
            }
            rs.close();
            stmt.close();
            return colName;
        } finally {
            DBResource.freeConnection(conn);
        }
    }

    private String fetchAutoIncreatedColumnForDM(String tableName) throws SQLException, ClassNotFoundException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBResource.getConnection(this.configFilename);
            String sql = "select a.name from sys.syscolumns a, all_tables b,sys.sysobjects c where a.info2 & 0x01 = 0x01 and a.id=c.id and c.name= b.table_name and lower(b.table_name)=? ";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, tableName.toLowerCase());
            rs = stmt.executeQuery();
            String colName = null;
            if (rs.next()) {
                colName = rs.getString(1).toLowerCase();
            }
            rs.close();
            stmt.close();
            return colName;
        } finally {
            DBResource.freeConnection(conn);
        }
    }


    /**
     * 把列类型映射为类属性类型
     *
     * @param colType
     * @return
     * @throws Exception
     */
    private Class reflectToFieldType(int colType, int precision, int scale) throws SQLException {
        switch (colType) {
            case Types.BIT:
                return Boolean.class;

            case Types.TINYINT:
                return Byte.class;
            case Types.SMALLINT:
                return Short.class;
            case Types.INTEGER:
                return Integer.class;
            case Types.BIGINT:
                return Long.class;

            case Types.FLOAT:
                return Float.class;
            case Types.REAL:
            case Types.DOUBLE:
                return Double.class;
            case Types.NUMERIC:
            case Types.DECIMAL:
                if (scale == 0) {
                    if (precision <= 9) {
                        return Integer.class;
                    }
                    if (precision <= 18) {
                        return Long.class;
                    }
                } else {
                    if (precision <= 6) {
                        return Float.class;
                    }
                    if (precision <= 15) {
                        return Double.class;
                    }
                }
                return BigDecimal.class;
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.VARCHAR:
                return String.class;

            case Types.DATE:
            case Types.TIMESTAMP:
                return java.util.Date.class;
            case Types.TIME:
                return java.util.Date.class;

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
            case Types.CLOB:
            case Types.NCLOB:
                return byte[].class;
            default:
                throw new SQLException("不能识别的列类型:" + colType);
        }
    }

    public ArrayList<MetaDataDescr> fetchTableStructure() {
        ArrayList<MetaDataDescr> mdList = new ArrayList<>();
        for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
            String colName = it.next();
            mdList.add(colNameMetaMap.get(colName));
        }
        return mdList;
    }
}
