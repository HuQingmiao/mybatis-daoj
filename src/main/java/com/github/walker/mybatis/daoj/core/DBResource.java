package com.github.walker.mybatis.daoj.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by HuQingmiao on 2015-6-2.
 */
class DBResource {

    private static Logger log = LoggerFactory.getLogger(DBResource.class);

    protected static Connection getConnection() throws Exception {

        String driverStr = ConfigLoader.getJdbcDriver();
        String dataSource = ConfigLoader.getJdbcUrl();
        String username = ConfigLoader.getUsername();
        String password = ConfigLoader.getPassword();

        Class.forName(driverStr);

        return java.sql.DriverManager.getConnection(dataSource, username,
                password);
    }


    protected static void freeConnection(Connection conn) throws Exception {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }
}
