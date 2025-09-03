package walker.mybatis.daoj.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author HuQingmiao
 */
class DBResource {

    private static final Logger LOGGER = LogManager.getLogger(DBResource.class);

    protected static Connection getConnection(String configFilename) throws ClassNotFoundException, SQLException {

        String driverStr = ConfigLoader.getInstance(configFilename).getJdbcDriver();
        String dataSource = ConfigLoader.getInstance(configFilename).getJdbcUrl();
        String username = ConfigLoader.getInstance(configFilename).getUsername();
        String password = ConfigLoader.getInstance(configFilename).getPassword();

        Class.forName(driverStr);

        return java.sql.DriverManager.getConnection(dataSource, username,
                password);
    }


    protected static void freeConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            LOGGER.error("", e);
        }
    }
}
