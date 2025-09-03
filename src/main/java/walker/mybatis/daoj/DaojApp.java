package walker.mybatis.daoj;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import walker.mybatis.daoj.core.Generator;

/**
 * mybatis-daoj的 Main主类
 *
 * @author HuQingmiao
 *
 * 使用说明:
 * 1. 用idea打开mybatis-daoj项目，修改resources/mybatis-daoj.xml，配置数据库连接、表名、输出目录。
 * 2. 编译、运行DaojApp.java，该程序会为每个表生成1个po实体类、1个dao接口类、1个mapper.xml。
 * 3. 将生成的代码复制到你的工程目录，确保代码中的包名正确。 接下来就可以在业务层代码中调用Dao接口类了。
 */
public class DaojApp {

    private static final Logger LOGGER = LogManager.getLogger(DaojApp.class);

    public static void main(String[] args) {
        try {
            // 生成dao层代码
            new Generator("mybatis-daoj-test.xml").generateCode();

            // 生成表结构文档
            new Generator("mybatis-daoj-test.xml").generateDoc();

//            // 生成数据字典
       //    String sql = "SELECT kind, kind_desc, code, code_desc, value1 FROM ark_dict ORDER BY kind, code, order_by";
     //      new Generator("mybatis-daoj.xml").generateDict(sql);

        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
}
