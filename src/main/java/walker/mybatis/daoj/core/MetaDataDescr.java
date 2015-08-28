package walker.mybatis.daoj.core;

/**
 * 本自定义元数据的描述类
 * <p/>
 * Created by HuQingmiao on 2015-6-2.
 */
class MetaDataDescr {

    private String colName; //列名

    private int colType;//列类型, 参考java.sql.Types

    private boolean isPk;//是主键吗?

    private int precision;  // 精确度

    private int scale;      // 小数点后长度

    private String fieldName; //映射的属性名

    private Class fieldType; //映射的属性名的类型, 如：String


    public int getColType() {
        return colType;
    }

    public void setColType(int colType) {
        this.colType = colType;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public boolean isPk() {
        return isPk;
    }

    public void setPk(boolean isPk) {
        this.isPk = isPk;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Class getFieldType() {
        return fieldType;
    }

    public void setFieldType(Class fieldType) {
        this.fieldType = fieldType;
    }
}
