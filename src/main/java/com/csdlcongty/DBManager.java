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

/*
   Read note in Resources folder to learn more about ojdbc driver
 */
// Create connect to Oracle database using jdbc
public class DBManager {

    private static final String IP = "localhost";
    private static final String PORT = "1521";
    protected static final String DBURL = String.format("jdbc:oracle:thin:@%s:%s:xe", IP, PORT);
    protected Connection cnt;
    protected Statement st;
    protected CallableStatement cst;
    protected PreparedStatement pst;
    protected String previousStatement;

    private static final String COUNTSQL = "SELECT COUNT(*) FROM (%s)";

    private void commit() throws SQLException {
        this.st = cnt.createStatement();
        st.execute("COMMIT");
    }

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
        String sql = "SELECT * FROM USER_LIST";
        ResultSet result = null;
        try {
            st = cnt.createStatement();
            result = st.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }

    public ResultSet getRoleList() throws SQLException {
        String sql = "SELECT * FROM ROLE_LIST";
        ResultSet result = null;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }

    public ResultSet getTableList() throws SQLException {
        String sql = "SELECT * FROM TABLE_LIST ORDER BY TABLE_NAME ASC";
        ResultSet result = null;
        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }

    public ResultSet getViewList() throws SQLException {
        String sql = "SELECT * FROM VIEW_LIST";
        ResultSet result = null;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }

    public ResultSet getTablePrivilegesOfRoleOrUser(String name) throws SQLException {
        String sql = "SELECT GRANTEE, GRANTOR, TABLE_NAME, GRANTABLE, PRIVILEGE FROM TABLE_PRIVILEGES WHERE GRANTEE = '"
                + name.toUpperCase() + "'";

        ResultSet result;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }

    public ResultSet getRoleOfUser(String name) throws SQLException {
        String sql = "SELECT GRANTEE, GRANTED_ROLE, ADMIN_OPTION FROM ROLE_PRIVILEGES WHERE GRANTEE = '"
                + name.toUpperCase() + "'";

        ResultSet result = null;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }

    public int createNewUser(String usernm, String pass) throws SQLException {
        String sql = "{call CREATE_USER(?, ?)}";
        int successful = 0;

        cst = cnt.prepareCall(sql);
        cst.setString(1, usernm);
        cst.setString(2, pass);
        cst.execute();
        commit();
        successful = 1;

        return successful;
    }

    public int createNewRole(String roleName) throws SQLException {
        String sql = "{call CREATE_ROLE(?)}";

        int successful = 0;

        cst = cnt.prepareCall(sql);
        cst.setString(1, roleName);
        cst.execute();
        commit();
        successful = 1;

        return successful;
    }

    public int createTable(String tableName, String columnNames, String dataTypes, String primaryKey, String notNullColumn) {
        try {
            cst = cnt.prepareCall("{call create_table(?,?,?,?,?)}");
            cst.setString(1, tableName);
            cst.setString(2, columnNames);
            cst.setString(3, dataTypes);
            cst.setString(4, primaryKey);
            cst.setString(5, notNullColumn);
            cst.execute();
            this.commit();

            return 1;
        } catch (SQLException e) {
            Logger.getLogger("DBManager").log(Level.SEVERE, e.getMessage());
            return 0;
        }
    }

    public ResultSet getColumnsOfTable(String tableName) throws SQLException {
        String sql = "SELECT COLUMN_NAME FROM GET_TABLE_COLUMNS('" + tableName + "')";
        ResultSet result = null;

        st = cnt.createStatement();
        st.execute(sql);
        result = st.getResultSet();

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }

    public String entityTypeIdentify(String entityName) throws SQLException {
        String entityType = "";
        String sql = "{? = call IDENTIFY_ENTITY_TYPE(?)}";
        cst = cnt.prepareCall(sql);
        cst.setString(2, entityName.toUpperCase());
        cst.registerOutParameter(1, java.sql.Types.LONGNVARCHAR);
        cst.execute();

        entityType = cst.getString(1);

        return entityType;
    }

    public void grantPrivilegesOnTable(String permission, String table, String roleOrUserName, boolean grantable)
            throws SQLException {
        String sql = "GRANT " + permission + " ON " + table + " TO " + roleOrUserName;
        String entityType = entityTypeIdentify(roleOrUserName);

        if (grantable && entityType.equals("USER")) {
            sql += " WITH GRANT OPTION";
        }

        st = cnt.createStatement();
        st.executeUpdate(sql);
        commit();
    }

    public void grantRoleToUser(String userName, String roleName) throws SQLException {
        String sql = "{call GRANT_ROLE_TO_USER(?, ?)}";
        cst = cnt.prepareCall(sql);
        cst.setString(1, userName.toUpperCase());
        cst.setString(2, roleName.toUpperCase());

        cst.execute();
    }

    public void revokePrivilegesOnTable(String permission, String table, String roleOrUserName)
            throws SQLException {
        String sql = "REVOKE " + permission + " ON " + table + " FROM " + roleOrUserName;
        st = cnt.createStatement();
        st.executeUpdate(sql);
        commit();
    }

    public void revokeRoleFromUser(String userName, String roleName) throws SQLException, Exception {
        String sql = "{call REVOKE_ROLE_FROM_USER(?, ?)}";
        cst = cnt.prepareCall(sql);
        cst.setString(1, userName.toUpperCase());
        cst.setString(2, roleName.toUpperCase());

        cst.execute();
    }
}
