package com.csdlcongty;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    protected final String DBURL = "jdbc:oracle:thin:@localhost:1521:xe";
    protected Connection cnt;

    public DBConnection(String username, String password) throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        cnt = DriverManager.getConnection(DBURL, username, password);
    }
}
