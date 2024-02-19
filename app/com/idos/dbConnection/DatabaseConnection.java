package com.idos.dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class DatabaseConnection {
    private static final String dbClassName = ConfigFactory.load().getString("db.default.driver");
    private static final String db_url = ConfigFactory.load().getString("db.default.url");
    private static final String db_user = ConfigFactory.load().getString("db.default.username");
    private static final String db_pwd = ConfigFactory.load().getString("db.default.password");

    private static Connection conn = null;

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName(dbClassName);
            connection = DriverManager.getConnection(db_url, db_user, db_pwd);
            return connection;
        } catch (Exception ex) {
            // Log the exception for debugging
            ex.printStackTrace();
            return null;
        }
    }
}
