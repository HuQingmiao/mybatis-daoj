package walker.mybatis.daoj.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Iterator;

public class CodeBuilderForMysql extends CodeBuilder {
    private static final Logger LOGGER = LogManager.getLogger(CodeBuilderForMysql.class);

    public CodeBuilderForMysql(String configFilename, String tableName) throws SQLException, ClassNotFoundException {
        super(configFilename, tableName);
    }

    @Override
    protected String buildMapperSave() {
        String baseSaveSql = super.buildMapperSave();
        // 没有指定主键或有多个主键列
        if (pkColSet.isEmpty() || pkColSet.size() > 1) {
            return baseSaveSql;
        }

        String pkColName = pkColSet.iterator().next();
        String pkFieldName = this.colNameMetaMap.get(pkColName).getFieldName();
        if (!this.colNameMetaMap.get(pkColName).isAutoIncreased()) {
            return baseSaveSql;
        }

        // mysql的主键指定了自增，则：
        String mysqlInsertHead = "    <insert id=\"save\" useGeneratedKeys=\"true\" keyProperty=\"" + pkFieldName + "\" >\n";
        String baseInsertHead = "    <insert id=\"save\">\n";
        return baseSaveSql.replaceAll(baseInsertHead, mysqlInsertHead);
    }


    @Override
    protected String buildMapperSaveBatch() {
        String jdbcDriver = ConfigLoader.getInstance(configFilename).getJdbcDriver().toLowerCase();

        StringBuffer buff = new StringBuffer();
        buff.append("    <insert id=\"saveBatch\">\n");

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

        return buff.toString();
    }


    @Override
    protected String buildMapperUpdateBatch() {
        if (pkColSet.isEmpty()) {
            LOGGER.info("要生成完整正确的update语句，必须为表指定主键！");
            //return "";
        }

        StringBuffer buff = new StringBuffer();
        buff.append("    <update id=\"updateBatch\" parameterType=\"java.util.List\">\n");
        if (!pkColSet.isEmpty()) {
            String pkColName = pkColSet.iterator().next();
            String pkFieldName = this.colNameMetaMap.get(pkColName).getFieldName();

            buff.append("        UPDATE " + tableName + "\n");
            buff.append("        <set>\n");

            for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
                String colName = it.next();
                MetaDataDescr md = colNameMetaMap.get(colName);
                if (md.isPk() | bigDataTypes.contains(md.getColType())) {
                    continue;
                }

                if (CREATE_TIME_COLUMNNAME.equals(md.getColName().toLowerCase())) {
                    continue;
                }
                if (CREATE_DATE_COLUMNNAME.equals(md.getColName().toLowerCase())) {
                    continue;
                }

                //对于行版本号字段，设为更新时自动递增
                if (ROW_VERSION_COLUMNNAME.equals(md.getColName().toLowerCase())) {
                    buff.append("            " + ROW_VERSION_COLUMNNAME + " = " + ROW_VERSION_COLUMNNAME + " + 1,\n");
                } else {
                    buff.append("            <foreach collection=\"list\" item=\"item\" index=\"index\" open=\"" + colName + "= CASE " + pkColName + "\" close=\"END\" separator=\" \" >\n");
                    buff.append("                WHEN #{item." + pkFieldName + "} THEN #{item." + md.getFieldName() + "}\n");
                    buff.append("            </foreach>,\n");
                }
            }

            buff.append("        </set>\n");

            buff.append("        WHERE \n");
            buff.append("            <foreach collection=\"list\" separator=\"or\" item=\"item\" index=\"index\">\n");
            buff.append("                " + pkColName + " = #{item." + pkFieldName + "}\n");
            buff.append("            </foreach>\n");
        }

        buff.append("    </update>\n\n");
        return buff.toString();
    }


    @Override
    protected String buildMapperFind() {
        String str = super.buildMapperFind();

        String jdbcDriver = ConfigLoader.getInstance(configFilename).getJdbcDriver();
        if (jdbcDriver.contains("postgresql.")) {
            str = str.replaceAll("DATE_FORMAT", "TO_CHAR");
            str = str.replaceAll("%Y-%m-%d", "yyyy-mm-dd");
        }
        return str;
    }
}
