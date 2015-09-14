package com.github.walker.mybatis.daoj.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * 对XML文档进行操作的工具类, 包括文档的输入输出, 格式化, 转码等
 *
 * @author HuQingmiao
 */
public class XmlUtil {

    // 美化的采用GBK编码的格式
    public static final OutputFormat FORMAT_PRETTY_GBK = OutputFormat.createPrettyPrint();

    // 美化的采用UTF-8编码的格式
    public static final OutputFormat FORMAT_PRETTY_UTF = OutputFormat.createPrettyPrint();

    // 紧凑的采用GBK编码的格式
    public static final OutputFormat FORMAT_COMPACT_GBK = OutputFormat.createCompactFormat();

    // 紧凑的采用UTF-8编码的格式
    public static final OutputFormat FORMAT_COMPACT_UTF = OutputFormat.createCompactFormat();

    static {
        FORMAT_PRETTY_GBK.setEncoding("GBK");
        FORMAT_PRETTY_UTF.setEncoding("UTF-8");

        FORMAT_COMPACT_GBK.setEncoding("GBK");
        FORMAT_COMPACT_UTF.setEncoding("UTF-8");
    }

    /**
     * 将XML文档写入指定的文件.
     *
     * @param document XML文档
     * @param fileName 文件名
     * @throws java.io.IOException
     */
    public static void write(Document document, String fileName) throws IOException {

        FileWriter out = new FileWriter(fileName);
        document.write(out);
        out.flush();
        out.close();
    }

    /**
     * 将XML文档写入指定的文件.
     *
     * @param document XML文档
     * @param fileName 文件名
     * @param format   输出格式, 参考常量XmlUtil.FORMAT_GBK_PRETTY,
     *                 XmlUtil.FORMAT_UTF_PRETTY等
     * @throws java.io.IOException
     */
    public static void write(Document document, String fileName, OutputFormat format) throws IOException {

        XMLWriter writer = new XMLWriter(new FileWriter(fileName), format);
        writer.write(document);
        writer.flush();
        writer.close();
    }

    /**
     * 从指定的URL读取XML，返回XML文档
     *
     * @param url
     * @return
     * @throws org.dom4j.DocumentException
     */
    public static Document read(URL url) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(url);
        return document;
    }

    /**
     * 从指定的文件读取XML，返回XML文档
     *
     * @param fileName 文件名
     * @return
     * @throws org.dom4j.DocumentException
     */
    public static Document read(String fileName) throws DocumentException {
        File file = new File(fileName);
        return read(file);
    }

    public static Document read(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        return document;
    }

    /**
     * XML转字符串
     *
     * @param document
     * @return
     * @throws org.dom4j.DocumentException
     */
    public static String toText(Document document) {
        return document.asXML();
    }

    /**
     * 字符串转XML
     *
     * @param text
     * @return
     * @throws org.dom4j.DocumentException
     */
    public static Document toXml(String text) throws DocumentException {
        return DocumentHelper.parseText(text);
    }

    public static void main(String[] args) {

    }
}
