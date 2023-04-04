package com.csdlcongty;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
   Read note in Resources folder to learn more about ojdbc driver
*/

// Create connect to Oracle database using jdbc
public class DBManager {
    protected static final String DBURL = "jdbc:oracle:thin:@localhost:1521:xe";
    protected Connection cnt;
    protected ResultSet result;
    protected Statement st;

    public DBManager(String username, String password) throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        cnt = DriverManager.getConnection(DBURL, username, password);
    }
    
    public ResultSet getUserList() {
        String sql = "SELECT * FROM DBA_USER_LIST";
        try {
            st = cnt.createStatement();
            result = st.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    }
}
