package walker.mybatis.daoj.core;

import walker.mybatis.daoj.utils.MappingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.sql.*;
import java.util.*;

/**
 * 元数据映射类
 * <p/>
 * Created by HuQingmiao on 2015-6-2.
 */
class MetaMapping {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    //表名
    private String tableName;

    //表的各列及元数据
    private Map<String, MetaDataDescr> colNameMetaMap = new LinkedHashMap<String, MetaDataDescr>();


    protected MetaMapping(String tableName) {
        try {
            this.tableName = tableName;
            this.parseMetaData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Map<String, MetaDataDescr> getColNameMetaMap() {
        return this.colNameMetaMap;
    }

    /**
     * 取得表的元数据，即取得各列名及类型
     *
     * @return 列名及其列类型：LinkedHashMap<String, MyMetaData> map
     * @throws Exception
     */
    protected void parseMetaData()
            throws Exception {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBResource.getConnection();

            //定位主键字段
            Set<String> keySet = new HashSet<String>();

            // log.info(">>>>"+conn.isClosed());
            //log.info(">>>>"+conn.getCatalog());

            rs = conn.getMetaData().getPrimaryKeys(conn.getCatalog(), null
                    , tableName.toUpperCase());
            for (; rs.next(); ) {
                keySet.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
            rs.close();

            //获取列元数据
            String sql = "SELECT * FROM " + tableName + " WHERE 1=2";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String colName = rsmd.getColumnName(i).toLowerCase();
                log.debug(colName + ": " + rsmd.getColumnType(i) + "("
                        + rsmd.getColumnTypeName(i) + "), " + rsmd.getPrecision(i) + "(精确度), " + rsmd.getScale(i) + "(小数点后位数)");

                MetaDataDescr md = new MetaDataDescr();
                md.setColName(colName);
                md.setColType(rsmd.getColumnType(i));
                md.setPrecision(rsmd.getPrecision(i));
                md.setScale(rsmd.getScale(i));

                if (keySet.contains(colName)) {
                    md.setPk(true);
                } else {
                    md.setPk(false);
                }

                String fileldName = MappingUtil.getFieldName(colName);
                md.setFieldName(fileldName);

                //把列类型映射为类属性类型
                md.setFieldType(reflectToFieldType(md.getColType(), md.getScale()));

                colNameMetaMap.put(colName, md);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                DBResource.freeConnection(conn);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 把列类型映射为类属性类型
     *
     * @param colType
     * @return
     * @throws Exception
     */
    private Class reflectToFieldType(int colType, int scale) throws Exception {

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
                return Double.class;
            case Types.DOUBLE:
                return Double.class;
            case Types.NUMERIC:
                if (scale == 0) {
                    return Long.class;
                } else {
                    return java.math.BigDecimal.class;
                }
            case Types.DECIMAL:
                if (scale == 0) {
                    return Long.class;
                } else {
                    return java.math.BigDecimal.class;
                }
            case Types.CHAR:
                return String.class;
            case Types.VARCHAR:
                return String.class;
            case Types.LONGVARCHAR:
                return String.class;

            case Types.DATE:
                return java.sql.Date.class;
            case Types.TIME:
                return java.sql.Time.class;
            case Types.TIMESTAMP:
                return java.sql.Timestamp.class;

            case Types.BINARY:
                return byte[].class;
            case Types.VARBINARY:
                return byte[].class;
            case Types.LONGVARBINARY:
                return byte[].class;

            case Types.BLOB:
                return byte[].class;
            case Types.CLOB:
                return byte[].class;
        }

        throw new Exception("不能识别的列类型:" + colType);
    }

    public static void main(String[] args) {
        System.out.println(Byte[].class.getName());
        System.out.println(Byte[].class.getSimpleName());

        System.out.println(byte[].class.getName());
        System.out.println(byte[].class.getSimpleName());
    }
}
