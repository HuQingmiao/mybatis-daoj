package walker.mybatis.daoj.core;

/**
 * 本自定义元数据的描述类
 * <p/>
 * @author HuQingmiao
 */
class MetaDataDescr {

    private String colName;   //列名

    private String fieldName; //映射的属性名
    private Class fieldType;  //映射的属性名的类型, 如：String

    private int colType;        //列类型, 参考java.sql.Types
    private String colTypeDesc; // number(12,2)

    private int colSize;        //最大长度或精度
    private int decimalDigits;  //小数点后长度
    private int maxByteLen;     //小数点后长度

    private boolean isPk;        //是主键吗?
    private String  udxName;     //归属的唯一约束
    private boolean nullable;    //可空吗?
    private boolean autoIncreased;//自增吗?

    private String comment; // 注释


    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
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

    public int getColType() {
        return colType;
    }

    public void setColType(int colType) {
        this.colType = colType;
    }

    public String getColTypeDesc() {
        return colTypeDesc;
    }

    public void setColTypeDesc(String colTypeDesc) {
        this.colTypeDesc = colTypeDesc;
    }

    public int getColSize() {
        return colSize;
    }

    public void setColSize(int colSize) {
        this.colSize = colSize;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    public int getMaxByteLen() {
        return maxByteLen;
    }

    public void setMaxByteLen(int maxByteLen) {
        this.maxByteLen = maxByteLen;
    }

    public boolean isPk() {
        return isPk;
    }

    public void setPk(boolean pk) {
        isPk = pk;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isAutoIncreased() {
        return autoIncreased;
    }

    public void setAutoIncreased(boolean autoIncreased) {
        this.autoIncreased = autoIncreased;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUdxName() {
        return udxName;
    }

    public void setUdxName(String udxName) {
        this.udxName = udxName;
    }
}
