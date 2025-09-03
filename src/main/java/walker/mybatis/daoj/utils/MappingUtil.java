package walker.mybatis.daoj.utils;


/**
 * 提提对类名/属性名与数据库表/列的映射方法
 *
 * @author HuQingmiao
 */
public class MappingUtil {

    /**
     * 根据驼峰规则将表名转为PO类名
     *
     * @param tableName
     * @return
     */
    public static String getEntityName(String tableName) {

        StringBuffer buff = new StringBuffer(tableName.toLowerCase());

        // the first character of class name is upper case
        buff.replace(0, 1, String.valueOf(Character.toUpperCase(tableName.charAt(0))));

        // delete character '_', and convert the next character to uppercase
        for (int i = 1, length = buff.length(); i < length; ) {

            char lastCh = buff.charAt(i - 1);// the last character
            char ch = buff.charAt(i); // the current character

            // if this character is a letter, and the last character is '_'
            if (Character.isLetter(ch) && lastCh == '_') {
                buff.replace(i - 1, i, String.valueOf(Character.toUpperCase(ch)));

                buff.deleteCharAt(i);
                length--;
            } else {
                i++;
            }
        }

        return buff.toString();
    }

    /**
     * 根据驼峰规则将列名转为PO类的属性名
     *
     * @param columnName
     * @return
     */
    public static String getFieldName(String columnName) {

        StringBuffer buff = new StringBuffer(columnName.toLowerCase());

        // delete character '_', and convert the next character to uppercase
        for (int i = 1, length = buff.length(); i < length; ) {

            char lastCh = buff.charAt(i - 1);// the last character
            char ch = buff.charAt(i); // the current character

            // if this character is a letter, and the last character is '_'
            if (Character.isLetter(ch) && lastCh == '_') {
                buff.replace(i - 1, i, String.valueOf(Character.toUpperCase(ch)));

                buff.deleteCharAt(i);
                length--;
            } else {
                i++;
            }
        }

        return buff.toString();
    }


    public static void main(String[] args) {
//        String table1 = "abc_d";
//        String table2 = "abc_2";
//        String table3 = "abc3";
//        String table4 = "a_bc";
//
//        System.out.println(getEntityName(table1));
//        System.out.println(getEntityName(table2));
//        System.out.println(getEntityName(table3));
//        System.out.println(getEntityName(table4));
    }
}