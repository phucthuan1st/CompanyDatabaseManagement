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
    protected Statement st;
    protected String previousStatement;

    public DBManager(String username, String password) throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        cnt = DriverManager.getConnection(DBURL, username, password);
    }
    
    public int getNumberRowsOf(String entity) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + entity;
        ResultSet resultSet = null;
        int result = 0;
        try {
            st = cnt.createStatement();
            resultSet = st.executeQuery(sql);
            resultSet.next();
            result = resultSet.getInt("COUNT(*)");
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    }
    
    public int getNumberOfRowsInLastQuery() {
        ResultSet resultSet = null;
        int result = 0;
        
        try {
            st = cnt.createStatement();
            resultSet = st.executeQuery(previousStatement);
            resultSet.next();
            result = resultSet.getInt("COUNT(*)");
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    }
   
    public ResultSet getUserList() {
        String sql = "SELECT USER_ID, USERNAME, ACCOUNT_STATUS, LAST_LOGIN FROM USER_LIST";
        ResultSet result = null;
        try {
            st = cnt.createStatement();
            result = st.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        previousStatement = String.format("SELECT COUNT(*) FROM (%s)", sql);
        return result;
    }

    public ResultSet getRoleList() {
        String sql = "SELECT ROLE_ID, ROLE, AUTHENTICATION_TYPE, COMMON FROM ROLE_LIST";
        ResultSet result = null;
        try {
            st = cnt.createStatement();
            result = st.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        previousStatement = String.format("SELECT COUNT(*) FROM (%s)", sql);
        return result;
    }
    
    public ResultSet getTableList() {
        String sql = "SELECT TABLE_NAME, OWNER, STATUS, NUM_ROWS FROM TABLE_LIST";
        ResultSet result = null;
        try {
            st = cnt.createStatement();
            result = st.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        previousStatement = String.format("SELECT COUNT(*) FROM (%s)", sql);
        return result;
    }

    public ResultSet getViewList() {
        String sql = "SELECT OWNER, VIEW_NAME, EDITIONING_VIEW, READ_ONLY, HAS_SENSITIVE_COLUMN FROM VIEW_LIST";
        ResultSet result = null;
        try {
            st = cnt.createStatement();
            result = st.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        previousStatement = String.format("SELECT COUNT(*) FROM (%s)", sql);
        return result;
    }
   
    public ResultSet getTablePrivilegesOfRoleOrUser(String name) {
        String sql = "SELECT GRANTEE, GRANTOR, TABLE_NAME, GRANTABLE, PRIVILEGE FROM DBA_TAB_PRIVS WHERE GRANTEE = '" + name + "' AND TYPE='TABLE'";
        
        ResultSet result = null;
        try {
            st = cnt.createStatement();
            result =st.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        previousStatement = String.format("SELECT COUNT(*) FROM (%s)", sql);
        return result;
    }
     
    public ResultSet getRoleOfUser(String name) {
        String sql = "SELECT GRANTEE, GRANTED_ROLE, ADMIN_OPTION FROM DBA_ROLE_PRIVS WHERE GRANTEE = '" + name + "'";
        
        ResultSet result = null;
        try {
            st = cnt.createStatement();
            result =st.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        previousStatement = String.format("SELECT COUNT(*) FROM (%s)", sql);
        return result;
    }
    
}
