package org.example.config;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public class UniversityDataSource {


    public static DataSource getDataSource() throws SQLException {
        MysqlDataSource ds = new MysqlDataSource();
        ds.setServerName("localhost");
        ds.setDatabaseName("university");
        ds.setUser("root");
        ds.setPassword("3011");
        ds.setUseSSL(false);
        ds.setAllowPublicKeyRetrieval(true);
        return ds;
    }
}
