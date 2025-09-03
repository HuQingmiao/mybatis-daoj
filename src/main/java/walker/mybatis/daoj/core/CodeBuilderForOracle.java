package walker.mybatis.daoj.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Iterator;

public class CodeBuilderForOracle extends CodeBuilder {
    private static final Logger LOGGER = LogManager.getLogger(CodeBuilderForOracle.class);

    public CodeBuilderForOracle(String configFilename, String tableName) throws SQLException, ClassNotFoundException {
        super(configFilename, tableName);
    }

    @Override
    protected String buildMapperSave() {
        Boolean pkAutoIncreased = ConfigLoader.getInstance(configFilename).getPkAutoIncreased();
        String jdbcDriver = ConfigLoader.getInstance(configFilename).getJdbcDriver().toLowerCase();

        StringBuffer buff = new StringBuffer();
        buff.append("    <insert id=\"save\">\n");

        // 如果是主键列、且指定要依赖seq自增，则设置主键值为xx_seq.nextval
        if (!pkColSet.isEmpty()) {
            String pkColName = pkColSet.iterator().next();
            String pkFieldName = this.colNameMetaMap.get(pkColName).getFieldName();
            if (pkAutoIncreased) {
                buff.append("        <selectKey order=\"BEFORE\" keyProperty=\"" + pkFieldName + "\" resultType=\"java.lang.Long\">\n");
                buff.append("             select to_char(sysdate,'yymmddhh')||" + tableName + "_seq.nextval from dual\n");
                buff.append("        </selectKey>\n");
            }
        }

        buff.append("        INSERT INTO " + tableName + "( ");
        StringBuffer valuesStr = new StringBuffer();

        int i = 0;
        for (Iterator<String> it = colNameMetaMap.keySet().iterator(); it.hasNext(); ) {
            String colName = it.next();
            MetaDataDescr md = colNameMetaMap.get(colName);
            if (bigDataTypes.contains(md.getColType())) {
                continue;
            }
            if (jdbcDriver.contains("dm.")) {
                //对于达梦的自增长的列，在insert语句中不列出此字段
                if (md.isAutoIncreased()) {
                    continue;
                }
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

    @Override
    protected String buildMapperSaveBatch() {
        Boolean pkAutoIncreased = ConfigLoader.getInstance(configFilename).getPkAutoIncreased();
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
            if (jdbcDriver.contains("dm.")) {
                //对于采用自增长的列，在insert语句中不列出此字段
                if (md.isAutoIncreased()) {
                    continue;
                }
            }
            //对于标识行版本号字段，在insert语句中不列出此字段
            if (ROW_VERSION_COLUMNNAME.equals(md.getColName().toLowerCase())) {
                continue;
            }

            // 如果是主键列、且指定要依赖seq自增，在批量insert语句中列出此字段，后面会拼接seq主键值
            if (pkColSet.contains(colName) && pkAutoIncreased) {
                buff.append(colName + ",");
            } else {
                buff.append(colName + ",");
                valuesStr.append("#{item." + md.getFieldName() + "} as " + md.getFieldName() + ", ");
            }
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

        // 批量插入，如果指定依赖seq自增， 则要拼接 xx_seq 作为主键值
        if (pkAutoIncreased) {
            buff.append("        SELECT to_char(sysdate,'yymmddhh')||" + tableName + "_seq.nextval, a.* from ( \n");
        }
        buff.append("        <foreach collection=\"list\" item=\"item\" index=\"index\" separator=\"UNION ALL\">\n");
        buff.append("            SELECT " + valuesStr.toString() + " \n");
        buff.append("              FROM DUAL \n");
        buff.append("        </foreach>\n");
        if (pkAutoIncreased) {
            buff.append("        ) a \n");
        }
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
            buff.append("        <foreach collection=\"list\" item=\"item\" index=\"index\" separator=\";\" open=\"begin\" close=\";end;\">\n");
            buff.append("            UPDATE " + tableName + "\n");
            buff.append("            <set>\n");
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
                    buff.append("                " + colName + "=#{item." + md.getFieldName() + "},\n");
                }
            }
            buff.append("            </set>\n");

            buff.append("            WHERE ");

            for (Iterator<String> keyIt = pkColSet.iterator(); keyIt.hasNext(); ) {
                String pkColName = keyIt.next();
                String pkFieldName = this.colNameMetaMap.get(pkColName).getFieldName();
                buff.append(pkColName + "=#{item." + pkFieldName + "} AND ");
            }
            if (buff.substring(buff.length() - 4, buff.length()).equals("AND ")) {
                buff.delete(buff.length() - 4, buff.length());
            }
            buff.append("\n");
            buff.append("        </foreach>\n");
        }

        buff.append("    </update>\n\n");
        return buff.toString();
    }


    @Override
    protected String buildMapperFind() {
        String str = super.buildMapperFind();

        str = str.replaceAll("DATE_FORMAT", "TO_CHAR");
        str = str.replaceAll("%Y-%m-%d", "yyyy-mm-dd");
        return str;
    }
}
