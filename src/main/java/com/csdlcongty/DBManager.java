package com.csdlcongty;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList; 
/*
   Read note in Resources folder to learn more about ojdbc driver
*/

// Create connect to Oracle database using jdbc
public class DBManager {
    protected static final String DBURL = "jdbc:oracle:thin:@localhost:1521:xe";
    protected Connection cnt;
    protected Statement st;
    protected CallableStatement  cst;
    protected PreparedStatement  pst;
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
    
    public int  createNewUser(String usernm, String pass) {
        String sql = "{call CREATE_USER(?, ?)}";
        int resultt= 0;
        try {
            cst = cnt.prepareCall(sql);
            cst.setString(1,usernm);
            cst.setString(2,pass);
            cst.execute();
            resultt=1;
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return resultt;
    }
    public int  createNewRole(String roleName) {
        String sql = "{call CREATE_ROLE(?)}";
        int hasResult=0;
        try {
            cst = cnt.prepareCall(sql);
            cst.setString(1,roleName);
            cst.execute();
            hasResult =1;
            //System.out.println("Record insert: ");
        
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return hasResult;
    }
    public int  createNewTable(String nametable, int numbercolumn, ArrayList<String> varname, ArrayList<String> vartype, ArrayList<String> varnumber, ArrayList<String> varvalue) {
        String sql = "CREATE TABLE " +nametable +"( ";
        
        for (int i=0;i<numbercolumn; i++)
        {
        sql= sql+ varname.get(0);
        varname.remove(0);
        sql+= vartype.get(0);
        if(vartype.get(0)== " VARCHAR " || vartype.get(0)== " NVARCHAR " || vartype.get(0)== " VARCHAR2 " || vartype.get(0)== " NVARCHAR2 ")
        {   
            sql+= "("+ varnumber.get(0)+")";
            varnumber.remove(0);
        }
        
        vartype.remove(0);
        if(varvalue.get(0) != " PRIMARY KEY ")
        {
            sql+= varvalue.get(0);
        }
        else
        {
            sql+= " CONSTRAINT table_pk " + varvalue.get(0);
        }
        
        varvalue.remove(0);
        if(i<numbercolumn-1)
        {
            sql+= ", ";
        }
        }
        sql+=")";
        int hasResult=0;
        try {
            pst = cnt.prepareStatement(sql);
            hasResult =pst.executeUpdate();
            //System.out.println("Record insert: ");
            hasResult=1;
            
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return hasResult;
    }
}
