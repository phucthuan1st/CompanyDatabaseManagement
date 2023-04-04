package com.csdlcongty;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
   Read note in Resources folder to learn more about ojdbc driver
*/

// Create connect to Oracle database using jdbc
public class DBConnection {
    protected static final String DBURL = "jdbc:oracle:thin:@localhost:1521:xe";
    protected Connection cnt;

    public DBConnection(String username, String password) throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        cnt = DriverManager.getConnection(DBURL, username, password);
    }
}
