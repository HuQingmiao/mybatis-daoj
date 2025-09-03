package walker.mybatis.daoj.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import walker.mybatis.daoj.utils.XmlUtil;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * 载入配置信息
 *
 * @author HuQingmiao
 */
public class ConfigLoader {

    private static final Logger LOGGER = LogManager.getLogger(ConfigLoader.class);

    private static final HashMap<String, ConfigLoader> instanceMap = new HashMap<>(4);

    private String configFilename;

    private final Map<String, String> configMap = new HashMap<String, String>();


    private ConfigLoader(String configFilename) {
        this.configFilename = configFilename;
        this.load();
    }

    public synchronized static ConfigLoader getInstance(String configFilename) {
        if (!instanceMap.containsKey(configFilename)) {
            ConfigLoader instance = new ConfigLoader(configFilename);
            instanceMap.put(configFilename, instance);
        }
        return instanceMap.get(configFilename);
    }

    public String getJdbcDriver() {
        return configMap.get("jdbcDriver");
    }

    public String getJdbcUrl() {
        return configMap.get("jdbcUrl");
    }

    public String getUsername() {
        return configMap.get("username");
    }

    public String getPassword() {
        return configMap.get("password");
    }

    public DbPattern getDbPattern() {
        String jdbcDriver = configMap.get("jdbcDriver");
        String pattern = configMap.get("pattern");
        // LOGGER.info(">> jdbcDriver: " + jdbcDriver);
        // LOGGER.info(">> pattern: " + pattern);
        if (jdbcDriver.contains("oracle.") || jdbcDriver.contains("dm.")) {
            return DbPattern.oracle;
        }
        if (jdbcDriver.contains("mysql.") || jdbcDriver.contains("postgresql.")) {
            return DbPattern.mysql;
        }
        if (jdbcDriver.contains("oceanbase.") || jdbcDriver.contains("tdsql.")) {
            if (pattern != null && !pattern.trim().equals("")) {
                return DbPattern.valueOf(pattern.toLowerCase());
            } else {
                return DbPattern.mysql;
            }
        }
        LOGGER.warn(" 数据库的类型不明确, 已默认按mysql类型生成代码！");
        return DbPattern.mysql;
    }


    public String getOutputDirc() {
        return configMap.get("outputDirc");
    }

    public String getBasicPo() {
        return configMap.get("basicPo");
    }

    public String getBasicDao() {
        return configMap.get("basicDao");
    }

    public String getPoPackage() {
        return configMap.get("poPackage");
    }

    public String getDaoPackage() {
        return configMap.get("daoPackage");
    }

    public Boolean getPkAutoIncreased() {
        // 这项仅对oracle,Oceanbase的oracle模式有效。对于oracle, 默认采用sequence做主键列自增长.
        String tmp = configMap.get("pkAutoIncreased");
        if (tmp == null || tmp.trim().equals("")) {
            return Boolean.TRUE;
        }
        return Boolean.valueOf(tmp);
    }

    public String[] getTables() {
        String str = configMap.get("tables");
        StringTokenizer tokenizer = new StringTokenizer(str, ",");

        List tableList = new ArrayList<String>();
        while (tokenizer.hasMoreElements()) {
            String tableName = (String) tokenizer.nextElement();
            tableList.add(tableName.trim().toLowerCase());
        }
        return (String[]) tableList.toArray(new String[0]);
    }


    //从data-handler装载handlers类
    private void load() {
        LOGGER.info("Load {} begin... ", this.configFilename);
        Document doc = null;
        try {
            File file = new File(this.configFilename);
            if (!file.exists()) {
                URL url = ConfigLoader.class.getClassLoader().getResource(this.configFilename);
                file = new File(url.getPath());
            }
            doc = XmlUtil.read(file);

            Element root = doc.getRootElement();
            Element dsElement = root.element("dataSource");
            //LOGGER.debug(dsElement.getName());

            for (Iterator<Element> it = dsElement.elementIterator(); it.hasNext(); ) {
                Element e = it.next();
                configMap.put(e.attribute("name").getValue(), e.attribute("value").getValue().trim());
            }

            Element psElement = root.element("params");
            //LOGGER.debug(psElement.getName());

            for (Iterator<Element> it = psElement.elementIterator(); it.hasNext(); ) {
                Element e = it.next();
                configMap.put(e.attribute("name").getValue(), e.attribute("value").getValue().trim());
            }

            LOGGER.info("Load {} successful! \n", this.configFilename);

        } catch (Exception e) {
            LOGGER.error("Load {} failed! \n", this.configFilename, e);
        } finally {
            if (doc != null) {
                doc.clearContent();
            }
        }
    }

    public static void main(String[] args) {

        String a = "book,editor,book_editor";


        LOGGER.info(">>" + a.split(",").length);

        LOGGER.info(">>" + ConfigLoader.getInstance("mybatis-daoj.xml").getTables().length);
        String[] b = ConfigLoader.getInstance("mybatis-daoj.xml").getTables();
        for (int i = 0; i < b.length; i++) {
            LOGGER.info(">> {}", b[i]);
        }
    }

}
