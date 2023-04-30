package org.example.config;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.mysql.cj.jdbc.MysqlPooledConnection;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {
    private static ConnectionManager instance = null;
    private final DataSource dataSource;

    private ConnectionManager() throws SQLException {
        MysqlDataSource ds = new MysqlDataSource();
        ds.setServerName("localhost");
        ds.setDatabaseName("university");
        ds.setUser("root");
        ds.setPassword("3011");
        ds.setUseSSL(false);
        ds.setAllowPublicKeyRetrieval(true);
        dataSource = ds;
    }
    public  static  ConnectionManager getInstance() throws SQLException {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
