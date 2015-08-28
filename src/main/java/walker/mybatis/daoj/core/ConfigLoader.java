package walker.mybatis.daoj.core;

import walker.mybatis.daoj.utils.XmlUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * 载入配置信息
 * Created by HuQingmiao on 2015-6-4.
 */
public class ConfigLoader {

    private static Logger log = LoggerFactory.getLogger(ConfigLoader.class);

    private static final String CONFI_FILENAME = "mybatis-daoj.xml";

    private static Map<String, String> configMap = new HashMap<String, String>();

    static {
        load();
    }

    private ConfigLoader() {
    }

    public static String getJdbcDriver() {
        return configMap.get("jdbcDriver");
    }

    public static String getJdbcUrl() {
        return configMap.get("jdbcUrl");
    }

    public static String getUsername() {
        return configMap.get("username");
    }

    public static String getPassword() {
        return configMap.get("password");
    }


    public static String getOutputDirc() {
        return configMap.get("outputDirc");
    }

    public static String getBasicEntity() {
        return configMap.get("basicEntity");
    }

    public static String getBasicDao() {
        return configMap.get("basicDao");
    }

    public static String getEntityPackage() {
        return configMap.get("entityPackage");
    }

    public static String getDaoPackage() {
        return configMap.get("daoPackage");
    }

    public static String[] getTables() {

        String str = configMap.get("tables");
        StringTokenizer tokenizer =  new StringTokenizer(str,",");

        List tableList = new ArrayList<String>();
        while(tokenizer.hasMoreElements()){
            String tableName = (String)tokenizer.nextElement();
            tableList.add(tableName.trim());
        }
        return (String[])tableList.toArray(new String[0]);
    }


    //从data-handler装载handlers类
    private static void load() {

        log.info("Load {} begin... ", CONFI_FILENAME);

        Document doc = null;
        try {
            File file = new File(CONFI_FILENAME);
            if (!file.exists()) {
                URL url = ConfigLoader.class.getClassLoader().getResource(CONFI_FILENAME);
                file = new File(url.getPath());
            }
            doc = XmlUtil.read(file);

            Element root = doc.getRootElement();
            Element dsElement = root.element("dataSource");
            log.debug(dsElement.getName());

            for (Iterator<Element> it = dsElement.elementIterator(); it.hasNext(); ) {
                Element e = it.next();
                configMap.put(e.attribute("name").getValue(), e.attribute("value").getValue().trim());
            }

            Element psElement = root.element("params");
            log.debug(psElement.getName());

            for (Iterator<Element> it = psElement.elementIterator(); it.hasNext(); ) {
                Element e = it.next();
                configMap.put(e.attribute("name").getValue(), e.attribute("value").getValue().trim());
            }

            log.info("Load {} successful! ", CONFI_FILENAME);

        } catch (Exception e) {
            log.error("Load {} failed! ", CONFI_FILENAME, e);
        } finally {
            if (doc != null) {
                doc.clearContent();
            }
        }
    }

    public static void main(String[] args) {

        String a="ACTTACCF,ACTTACJN,ACTTACJNDT,ACTTACLG,ACTTACDT,ACTTINIF\n" +
                "ACTTINHI,ACTTINCN,ACTTINDT,ACTTITEM,ACTTITMR,ACTTORG,\n" +
                "ACTTGLBL,ACTTGLHI,ACTTGLTP,ACTTGLIN,ACTTVHHI,ACTTVOCH,\n" +
                "ACTTFRTACBL,ACTTFRTCDAT,ACTTFRTCHKCTL,ACTTFRTCHKJRNL,ACTTFRTCHKJNDT,ACTTVHIN,\n" +
                "ACTTCHKJRNL,ACTTCHKJNDT,ACTTCHKUNJRN,ACTTCHKUNJNDT,PUBTBSTS,PUBTHLP,\n" +
                "ACTTACBL,ACTTACJNDTTMP,ACTTACJNTMP,ACTTBFACC,ACTTVCH,ACTTINBL,\n" +
                "ACTTBFACCBSP,ACTTBFACCJNDT,ACTTBFACCJRNL,ACTTTURNCF,PUBBSPCTL,PUBLCKREC,\n" +
                "ACTTCDIN,ACTTCHKBSP,ACTTEACJN,ACTTFRTCHKJNDTHI,PUBPLTINF,PUBTBATCH,\n" +
                "ACTTFRTCHKJRNL,ACTTFRTCHKJRNLHI,ACTTINDT_ASYN,ACTTITEMTL,PUBTMSG,PUBTRSTS,\n" +
                "PUBTRSTSHIS,BUITACLG";


        log.info(">>"+a.split(",").length);

        log.info(">>" + ConfigLoader.getTables().length);
        String[] b= ConfigLoader.getTables();
        for(int i=0;i<b.length;i++){
            log.info(">>"+ b[i]+ "<<");
        }
    }

}
