package your.project.demo.common;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Service 基类, 可在此提供Service层的公共方法，如获取序列号、生成主键等
 * <p/>
 * Created by Huqingmiao on 2015-5-16.
 */
public abstract class BasicService {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected static String res = "mybatis.xml";

    protected static SqlSessionFactory sessionFactory = null;


    static {
        try {
            sessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader(res));
        } catch (IOException e) {
            Logger log2 = LoggerFactory.getLogger(BasicService.class);
            log2.error(e.getMessage(), e);
        }
    }

}
