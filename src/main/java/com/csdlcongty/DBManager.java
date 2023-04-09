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
import java.util.ArrayList;

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

    public ResultSet getRoleList() throws SQLException, Exception {
        String sql = "SELECT ROLE_ID, ROLE, AUTHENTICATION_TYPE, COMMON FROM ROLE_LIST";
        ResultSet result = null;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format("SELECT COUNT(*) FROM (%s)", sql);
        return result;
    }

    public ResultSet getTableList() throws SQLException, Exception {
        String sql = "SELECT TABLE_NAME, OWNER, STATUS, NUM_ROWS FROM TABLE_LIST ORDER BY TABLE_NAME ASC";
        ResultSet result = null;
        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format("SELECT COUNT(*) FROM (%s)", sql);
        return result;
    }

    public ResultSet getViewList() throws SQLException, Exception {
        String sql = "SELECT OWNER, VIEW_NAME, EDITIONING_VIEW, READ_ONLY, HAS_SENSITIVE_COLUMN FROM VIEW_LIST";
        ResultSet result = null;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format("SELECT COUNT(*) FROM (%s)", sql);
        return result;
    }

    public ResultSet getTablePrivilegesOfRoleOrUser(String name) throws SQLException, Exception {
        String sql = "SELECT GRANTEE, GRANTOR, TABLE_NAME, GRANTABLE, PRIVILEGE FROM TABLE_PRIVILEGES WHERE GRANTEE = '"
                + name.toUpperCase() + "'";

        ResultSet result = null;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format("SELECT COUNT(*) FROM (%s)", sql);
        return result;
    }

    public ResultSet getRoleOfUser(String name) throws SQLException, Exception {
        String sql = "SELECT GRANTEE, GRANTED_ROLE, ADMIN_OPTION FROM ROLE_PRIVILEGES WHERE GRANTEE = '"
                + name.toUpperCase() + "'";

        ResultSet result = null;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format("SELECT COUNT(*) FROM (%s)", sql);
        return result;
    }

    public int createNewUser(String usernm, String pass) throws SQLException, Exception {
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

    public int createNewRole(String roleName) throws SQLException, Exception {
        String sql = "{call CREATE_ROLE(?)}";

        int successful = 0;

        cst = cnt.prepareCall(sql);
        cst.setString(1, roleName);
        cst.execute();
        commit();
        successful = 1;

        return successful;
    }

    public int createNewTable(String nametable, int numbercolumn, ArrayList<String> varname, ArrayList<String> vartype,
            ArrayList<String> varnumber, ArrayList<String> varvalue) throws SQLException, Exception {
        String sql = "CREATE TABLE " + nametable + "( ";

        for (int i = 0; i < numbercolumn; i++) {

            sql = sql + varname.get(0);
            varname.remove(0);
            sql += vartype.get(0);

            if (" VARCHAR ".equals(vartype.get(0)) || " NVARCHAR ".equals(vartype.get(0))
                    || " VARCHAR2 ".equals(vartype.get(0)) || " NVARCHAR2 ".equals(vartype.get(0))) {
                sql += "(" + varnumber.get(0) + ")";
                varnumber.remove(0);
            }

            vartype.remove(0);
            if (!" PRIMARY KEY ".equals(varvalue.get(0))) {
                sql += varvalue.get(0);
            } else {
                sql += " CONSTRAINT table_pk " + varvalue.get(0);
            }

            varvalue.remove(0);

            if (i < numbercolumn - 1) {
                sql += ", ";
            }
        }
        sql += ")";

        int successful = 0;

        pst = cnt.prepareStatement(sql);
        pst.executeUpdate();
        commit();
        successful = 1;

        return successful;
    }

    public ResultSet getColumnsOfTable(String tableName) throws SQLException, Exception {
        String sql = "SELECT COLUMN_NAME FROM GET_TABLE_COLUMNS('" + tableName + "')";
        ResultSet result = null;

        st = cnt.createStatement();
        st.execute(sql);
        result = st.getResultSet();

        previousStatement = String.format("SELECT COUNT(*) FROM (%s)", sql);
        return result;
    }

    public String entityTypeIdentify(String entityName) throws SQLException, Exception {
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
            throws SQLException, Exception {
        String sql = "GRANT " + permission + " ON " + table + " TO " + roleOrUserName;
        String entityType = entityTypeIdentify(roleOrUserName);

        if (grantable && entityType.equals("USER")) {
            sql += " WITH GRANT OPTION";
        }

        st = cnt.createStatement();
        st.executeUpdate(sql);
        commit();
    }

    public void grantRoleToUser(String userName, String roleName) throws SQLException, Exception {
        String sql = "{call GRANT_ROLE_TO_USER(?, ?)}";
        cst = cnt.prepareCall(sql);
        cst.setString(1, userName.toUpperCase());
        cst.setString(2, roleName.toUpperCase());

        cst.execute();
    }

    public void revokePrivilegesOnTable(String permission, String table, String roleOrUserName)
            throws SQLException, Exception {
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
