package com.github.walker.mybatis.daoj.core;


import com.github.walker.mybatis.daoj.utils.MappingUtil;

import java.util.*;

/**
 * 代码构建
 *
 * @author HuQingmiao
 */
class CodeBuilder {

    protected String tableName;

    protected Map<String, MetaDataDescr> colNameMetaMap;

    protected CodeBuilder(String tableName) {
        this.tableName = tableName.trim().toLowerCase();
        this.colNameMetaMap = new MetaMapping(tableName).getColNameMetaMap();
    }


    /**
     * 构造实体类的源码
     *
     * @param entityPackageName 实体类源码的包名
     * @return
     */
    protected String buildEntitySource(String basicVoName, String entityPackageName) throws Exception {

        StringBuffer buff = new StringBuffer();

        buff.append("package ");
        buff.append(entityPackageName + "; \n\n");

        buff.append("import " + basicVoName + "; \n");
        buff.append("\n");

        // public class AA {
        buff.append("public class " + MappingUtil.getEntityName(tableName));
        buff.append(" extends BasicVo {\n");
        buff.append("    private static final long serialVersionUID = 1L;\n\n");


        //生成属性  private String xxx;
        for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
            String colName = it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
            String filedName = md.getFieldName();
            Class fieldType = md.getFieldType();
            String fieldTypeName = fieldType.getName();
            System.out.println(">>" + fieldType.getName());
            if (fieldType.getName().contains("java.lang") || fieldType.getName().startsWith("[")) {
                fieldTypeName = fieldType.getSimpleName();
            }
            buff.append("    private " + fieldTypeName + " " + filedName + ";\n");
        }
        buff.append("\n\n");

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

        //生成各字段名拼接的字符串
        buff.append("/*List columns as follows:\n");
        int i = 0;
        for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
            String colName = it.next();
            buff.append("\"" + colName.toLowerCase() + "\", ");
            i++;
            if (i % 7 == 0) {
                buff.append("\n");
            }
        }
        buff.delete(buff.lastIndexOf(","), buff.length());
        buff.append("\n*/");

        return buff.toString();
    }


    /**
     * 构造DAO类的源码
     *
     * @param daoPackageName DAO类源码的包名
     * @return
     */
    protected String buildDaoSource(String basicDaoName, String daoPackageName) throws Exception {

        StringBuffer buff = new StringBuffer();

        buff.append("package ");
        buff.append(daoPackageName + "; \n\n");

        buff.append("import " + basicDaoName + "; \n\n");

        // public class AA {
        buff.append("public interface ");
        buff.append(MappingUtil.getEntityName(tableName) + "Dao");
        buff.append(" extends BasicDao {\n}\n");

        return buff.toString();
    }


    /**
     * 构造MAPPER的源码
     *
     * @param daoPackageName DAO类源码的包名
     * @return
     */
    protected String buildMapperSource(String daoPackageName) throws Exception {
        final String head = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<!DOCTYPE mapper PUBLIC  \"-//mybatis.org//DTD Mapper 3.0//EN\"  \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n\n";

        StringBuffer buff = new StringBuffer(head);
        //buff.append("<!-- 本XML文件相当于对DAO接口的实现; 属性namespace对应DAO接口名称. -->\n");

        buff.append("<mapper namespace=\"" + daoPackageName + "." + MappingUtil.getEntityName(tableName) + "Dao\">\n");


        //获取主键列
        Map<String, String> pkColFieldMap = new HashMap<String, String>();
        Iterator it = colNameMetaMap.keySet().iterator();
        for (; it.hasNext(); ) {
            String colName = (String) it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);

            if (md.isPk()) {
                pkColFieldMap.put(colName, md.getFieldName());
            }
        }

        ///////////////////// save
        buff.append("\n    <!-- ============================= INSERT ============================= -->\n");

        if (!pkColFieldMap.isEmpty() && pkColFieldMap.size() == 1) {
            String keyField = pkColFieldMap.entrySet().iterator().next().getValue();
            buff.append("    <insert id=\"save\" useGeneratedKeys=\"true\" keyProperty=\"" + keyField + "\" >\n");
        } else {
            buff.append("    <insert id=\"save\">\n");
        }

        buff.append("        INSERT INTO ");
        buff.append(tableName + "( ");

        StringBuffer valuesStr = new StringBuffer();
        it = colNameMetaMap.keySet().iterator();

        int i = 0;
        for (; it.hasNext(); ) {
            String colName = (String) it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
//            if (md.isPk()) {
//                continue;
//            }
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


        ///////////////////// saveBatch
        buff.append("\n    <!-- batch insert for mysql -->\n");
        buff.append("    <insert id=\"saveBatch\">\n");
        buff.append("        INSERT INTO " + tableName + "( ");

        it = colNameMetaMap.keySet().iterator();
        i = 0;
        for (; it.hasNext(); ) {
            String colName = (String) it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
//            if (md.isPk()) {
//                continue;
//            }
            buff.append(colName + ",");
            valuesStr.append("#{item." + md.getFieldName() + "},");

            i++;
            if (i % 7 == 0) {
                buff.append("\n                          ");
                valuesStr.append("\n              ");
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

        buff.append("        VALUES \n");
        buff.append("        <foreach collection=\"list\" item=\"item\" index=\"index\" separator=\",\">\n");
        buff.append("            ( " + valuesStr.toString() + " )\n");
        buff.append("        </foreach>\n");
        buff.append("    </insert>\n\n");
        valuesStr.delete(0, valuesStr.length());


        buff.append("\n    <!-- batch insert for oracle -->\n");
        buff.append("    <!--\n");
        buff.append("    <insert id=\"saveBatch\">\n");
        buff.append("        INSERT INTO " + tableName + "( ");

        it = colNameMetaMap.keySet().iterator();
        i = 0;
        for (; it.hasNext(); ) {
            String colName = (String) it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
//            if (md.isPk()) {
//                continue;
//            }
            buff.append(colName + ",");
            valuesStr.append("#{item." + md.getFieldName() + "},");

            i++;
            if (i % 7 == 0) {
                buff.append("\n                          ");
                valuesStr.append("\n              ");
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

        buff.append("        <foreach collection=\"list\" item=\"item\" index=\"index\" separator=\"UNION ALL\">\n");
        buff.append("            SELECT " + valuesStr.toString() + " \n");
        buff.append("              FROM DUAL \n");
        buff.append("        </foreach>\n");
        buff.append("    </insert>\n\n");
        valuesStr.delete(0, valuesStr.length());

        buff.append("    -->\n");


        buff.append("\n    <!-- ============================= UPDATE ============================= -->\n");
        buff.append("    <update id=\"update\">\n");

        buff.append("        UPDATE " + tableName + "\n");
        buff.append("        <set>\n");
        it = colNameMetaMap.keySet().iterator();
        for (; it.hasNext(); ) {
            String colName = (String) it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
            if (md.isPk()) {
                continue;
            }
            buff.append("            " + colName + "=#{" + md.getFieldName() + "},\n");
        }
        buff.append("        </set>\n");

        buff.append("        WHERE ");

        Iterator<String> keyIt = pkColFieldMap.keySet().iterator();
        for (; keyIt.hasNext(); ) {
            String colName = keyIt.next();
            buff.append(colName + "=#{" + pkColFieldMap.get(colName) + "} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");
        buff.append("    </update>\n\n");


        buff.append("    <update id=\"updateIgnoreNull\">\n");

        buff.append("        UPDATE " + tableName + "\n");
        buff.append("        <set>\n");
        it = colNameMetaMap.keySet().iterator();
        for (; it.hasNext(); ) {
            String colName = (String) it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
            if (md.isPk()) {
                continue;
            }
            buff.append("            <if test=\"" + md.getFieldName() + "!= null\">" + colName + "=#{" + md.getFieldName() + "},</if>\n");
        }
        buff.append("        </set>\n");

        buff.append("        WHERE ");

        keyIt = pkColFieldMap.keySet().iterator();
        for (; keyIt.hasNext(); ) {
            String colName = keyIt.next();
            buff.append(colName + "=#{" + pkColFieldMap.get(colName) + "} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");
        buff.append("    </update>\n\n");


        ///////////////////// updateBatch
        if (!pkColFieldMap.isEmpty() && pkColFieldMap.size() == 1) {
            String pkColName = pkColFieldMap.keySet().iterator().next();
            String pkField = pkColFieldMap.get(pkColName);

            buff.append("    <update id=\"updateBatch\" parameterType=\"java.util.List\">\n");
            buff.append("        UPDATE " + tableName + "\n");
            buff.append("        <set>\n");

            it = colNameMetaMap.keySet().iterator();
            for (; it.hasNext(); ) {
                String colName = (String) it.next();
                MetaDataDescr md = colNameMetaMap.get(colName);
                if (md.isPk()) {
                    continue;
                }
                buff.append("            <foreach collection=\"list\" item=\"item\" index=\"index\" open=\""+colName+"= CASE "+pkColName+"\" close=\"END\" separator=\" \" >\n");
                buff.append("                WHEN #{item."+pkField+"} THEN #{item."+md.getFieldName()+"}\n");
                buff.append("            </foreach>,\n");
            }

            buff.append("        </set>\n");

            buff.append("        WHERE \n");
            buff.append("            <foreach collection=\"list\" separator=\"or\" item=\"item\" index=\"index\">\n");
            buff.append("                "+pkColName+"=#{item."+pkField+"}\n");
            buff.append("            </foreach>\n");

            buff.append("    </update>\n\n");
        }


        buff.append("\n    <!-- ============================= DELETE ============================= -->\n");
        buff.append("    <delete id=\"delete\">\n");
        buff.append("        DELETE FROM " + tableName + "\n");
        buff.append("        WHERE ");
        keyIt = pkColFieldMap.keySet().iterator();
        for (; keyIt.hasNext(); ) {
            String colName = keyIt.next();
            buff.append(colName + "=#{" + pkColFieldMap.get(colName) + "} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");
        buff.append("    </delete>\n\n");


        buff.append("    <delete id=\"deleteBatch\">\n");
        buff.append("        DELETE FROM " + tableName + "\n");
        buff.append("        WHERE\n");
        buff.append("        <foreach collection=\"list\" item=\"item\" index=\"index\" open=\"(\" separator=\"OR\" close=\")\">\n");
        buff.append("            ");
        keyIt = pkColFieldMap.keySet().iterator();
        for (; keyIt.hasNext(); ) {
            String colName = keyIt.next();
            buff.append(colName + "=#{item." + pkColFieldMap.get(colName) + "} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");
        buff.append("        </foreach>\n");
        buff.append("    </delete>\n\n");

        buff.append("    <delete id=\"deleteByPK\">\n");
        buff.append("        DELETE FROM " + tableName + "\n");
        buff.append("        WHERE ");
        keyIt = pkColFieldMap.keySet().iterator();
        for (; keyIt.hasNext(); ) {
            String colName = keyIt.next();
            buff.append(colName + "=#{" + pkColFieldMap.get(colName) + "} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");
        buff.append("    </delete>\n\n");

/*
        buff.append("    <delete id=\"deleteByPKs\">\n");
        buff.append("        DELETE FROM " + tableName + "\n");
        buff.append("        WHERE \n");
        buff.append("        <foreach collection=\"list\" item=\"item\" index=\"index\" open=\"(\" separator=\"OR\" close=\")\">\n");
        buff.append("            (");
        keyIt = pkColFieldMap.keySet().iterator();
        for (; keyIt.hasNext(); ) {
            String colName = keyIt.next();
            buff.append(colName + "=#{item} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append(")\n");

        buff.append("        </foreach>\n");
        buff.append("    </delete>\n\n");
*/

        buff.append("    <delete id=\"deleteAll\">\n");
        buff.append("        DELETE FROM " + tableName + "\n");
        buff.append("    </delete>\n\n");


        buff.append("\n    <!-- ============================= SELECT ============================= -->\n");
        buff.append("    <select id=\"count\" resultType=\"java.lang.Long\">\n");
        buff.append("        SELECT COUNT(*) FROM " + tableName + "\n");
        buff.append("    </select>\n\n");


        buff.append("    <select id=\"findByPK\" resultType=\"" + MappingUtil.getEntityName(tableName) + "\">\n");
        buff.append("        SELECT * FROM " + tableName + "\n");
        buff.append("        WHERE ");

        keyIt = pkColFieldMap.keySet().iterator();
        for (; keyIt.hasNext(); ) {
            String colName = keyIt.next();
            buff.append(colName + "=#{" + pkColFieldMap.get(colName) + "} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");
        buff.append("    </select>\n\n");


        buff.append("    <select id=\"find\" resultType=\"" + MappingUtil.getEntityName(tableName) + "\">\n");
        buff.append("        SELECT ");

        it = colNameMetaMap.keySet().iterator();
        String colName = (String) it.next();
        buff.append(colName);

        i = 0;
        for (; it.hasNext(); ) {
            colName = (String) it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
            buff.append("," + colName);
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
        buff.append("\n");

        buff.append("         FROM " + tableName + "\n");
        buff.append("        <where>\n");

        it = colNameMetaMap.keySet().iterator();
        for (; it.hasNext(); ) {
            colName = (String) it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);

            String filedName = md.getFieldName();
            if (md.getFieldType().equals("String")) {
                buff.append("            <if test=\"" + filedName + "!= null and " + filedName + "!=''\">\n");
            } else {
                buff.append("            <if test=\"" + filedName + "!= null\">\n");
            }
            if (md.getFieldType().equals("String")) {
                buff.append("               AND " + colName + " = #{" + filedName + "}\n");
            } else if (md.getFieldType().equals("Double") || md.getFieldType().equals("Float")) {
                buff.append("               AND <![CDATA[ " + colName + " >= #{" + filedName + "} ]]>\n");
            } else if (md.getFieldType().equals("Date")) {
                buff.append("               AND DATE_FORMAT(" + colName + ",'%Y-%m-%d') = #{" + filedName + "}\n");
            } else {
                buff.append("               AND " + colName + " = #{" + filedName + "}\n");
            }
            buff.append("            </if>\n");
        }

        buff.append("        </where>\n");
        buff.append("    </select>\n\n");


        buff.append("</mapper>\n");
        return buff.toString();
    }
}
