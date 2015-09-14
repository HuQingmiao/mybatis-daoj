package com.github.walker.mybatis.daoj.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * PO 基类
 * <p/>
 * Created by HuQingmiao on 2015-5-13.
 */

import org.json.JSONObject;
import java.util.*;

/**
 * PO 基类
 * <p/>
 * Created by HuQingmiao on 2015-5-13.
 */
public abstract class BasicVo implements Serializable {

    private static Logger log = LoggerFactory.getLogger(BasicVo.class);

    //存放当前实体类的属性及类型
    private HashMap<String, Class<?>> fieldNameTypeMap = new HashMap<String, Class<?>>(10);


    public BasicVo() {

        //将属性及其类型存入map(fieldName,fieldType)
        Field[] fields = this.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            if ("serialVersionUID".equals(fields[i].getName())) {
                continue;
            }
            fieldNameTypeMap.put(fields[i].getName(), fields[i].getType());
        }
    }


    /**
     * 根据属性名取值
     *
     * @param propertyName 属性名
     * @return 属性值
     * @throws Exception
     */
    public Object get(String propertyName) throws Exception {

        String methodName = "get";
        if (propertyName.length() > 1 && Character.isUpperCase(propertyName.charAt(1))) {
            methodName += propertyName;
        } else {
            methodName += (Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1));
        }

        try {
            //builds the method name, such as: "getXX"
            Method method = this.getClass().getMethod(methodName,
                    new Class[]{});

            //Getting value of the field by the method.
            Object fieldValue = method.invoke(this, new Object[]{});

            return fieldValue;

        } catch (Exception e) {
            log.error("", e);
            throw e;
        }
    }


    /**
     * 对指定属性设置值
     *
     * @param propertyName
     * @param value
     * @throws Exception
     */
    public void set(String propertyName, Object value) throws Exception {

        String methodName = "set";
        if (propertyName.length() > 1 && Character.isUpperCase(propertyName.charAt(1))) {
            methodName += propertyName;
        } else {
            methodName += (Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1));
        }

        try {
            Method method = this.getClass().getMethod(methodName,
                    new Class[]{(Class<?>) fieldNameTypeMap.get(propertyName)});

            //Setting value of the field by the method.
            method.invoke(this, new Object[]{value});

        } catch (NoSuchMethodException e) {
            log.error("", e);
            throw e;
        }
    }


    public HashMap<String, Class<?>> fieldNameTypeMap() {
        return this.fieldNameTypeMap;
    }


    @Override
    public int hashCode() {
        int hashCode = 1;
        Map<String, Object> keyObjectMap = new HashMap<String, Object>();
        try {
            for (Iterator<String> it = fieldNameTypeMap.keySet().iterator(); it.hasNext(); ) {
                String filedName = it.next();
                Object obj = this.get(filedName);
                hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        keyObjectMap.clear();
        return hashCode;
    }

    @Override
    public String toString() {
        Map<String, Object> keyObjectMap = new HashMap<String, Object>();
        try {
            for (Iterator<String> it = fieldNameTypeMap.keySet().iterator(); it.hasNext(); ) {
                String filedName = it.next();
                keyObjectMap.put(filedName, this.get(filedName));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String str = JSONObject.valueToString(keyObjectMap);
        keyObjectMap.clear();
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BasicVo)) {
            return false;
        }

        BasicVo vo2= ((BasicVo) o);
        HashMap<String, Class<?>> ftMap2 =vo2.fieldNameTypeMap();
        if (fieldNameTypeMap.size() != ftMap2.size()) {
            return false;
        }

        try {
            for (Iterator<String> it = fieldNameTypeMap.keySet().iterator(); it.hasNext(); ) {
                String filedName = it.next();
                Object obj1 = this.get(filedName);
                Object obj2 = vo2.get(filedName);
                if (!(obj1 == null ? obj2 == null : obj1.equals(obj2))) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

}
