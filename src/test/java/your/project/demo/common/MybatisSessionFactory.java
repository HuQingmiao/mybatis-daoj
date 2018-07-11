package your.project.demo.common;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class MybatisSessionFactory {

	private final static Logger log = LoggerFactory.getLogger(MybatisSessionFactory.class);

	private final static String res = "mybatis.xml";

	private static SqlSessionFactory sessionFactory = null;

	private MybatisSessionFactory() {
	}

	static {
		try {
			sessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader(res));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public static SqlSessionFactory getSqlSessionFactory() {
		return sessionFactory;
	}
}
