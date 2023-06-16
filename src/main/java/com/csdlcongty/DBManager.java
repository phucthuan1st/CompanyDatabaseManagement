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
import java.sql.SQLWarning;
import com.csdlcongty.CryptographyUtilities;
import static com.csdlcongty.MockGenerator.generateRecords;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/*
   Read note in Resources folder to learn more about ojdbc driver
 */
// Create connect to Oracle database using jdbc
public class DBManager {

    private static final String IP = "localhost";
    private static final String PORT = "1521";
    protected static final String DBURL = String.format("jdbc:oracle:thin:@%s:%s/COMPANY", IP, PORT);
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
        updateStatisticsForSchema();
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

    public int createNewUser(String usernm, String pass) {
        String sql = "{call CREATE_USER(?, ?)}";

        try {
            cst = cnt.prepareCall(sql);
            cst.setString(1, usernm);
            cst.setString(2, pass);
            cst.execute();
        
//            sql = "{call INSERT_DANGNHAP_RECORD(?, ?, ?)";
//            cst = cnt.prepareCall(sql);
//            String salt = CryptographyUtilities.generateSalt(16);
//            
//            cst.setString(1, usernm);
//            cst.setString(2, salt);
//            cst.setString(3, CryptographyUtilities.hashSHA1(pass, salt));
//            cst.execute();
            commit();
            
            return 1;
        }
        catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
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
    
    private int updateStatisticsForSchema() {
        try {
            CallableStatement stmt = cnt.prepareCall("{call gather_statistics_for_schema}");
            stmt.execute();
            return 1; // Successful update
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0; // Failed update
    }

    public int createTable(String tableName, String columnNames, String dataTypes, String primaryKey, String notNullColumn) {
        try {
            CallableStatement cstmt = cnt.prepareCall("{call create_table(?, ?, ?, ?, ?)}");
            
            cstmt.setString(1, tableName);
            cstmt.setString(2, columnNames);
            cstmt.setString(3, dataTypes);
            cstmt.setString(4, primaryKey);
            cstmt.setString(5, notNullColumn);
            
            cstmt.execute();
            
            // Check if any error occurred during table creation
            SQLWarning warning = cstmt.getWarnings();
            if (warning != null) {
                System.out.println("Error creating table: " + warning.getMessage());
                return 0;
            }
            
            System.out.println("Table created successfully.");
            commit();
            return 1;
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
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
    
    public void insertMockRecordToNhanVien() {
        var data = generateRecords(300);

        String sqlNhanVien = "{call INSERT_NHANVIEN_RECORD(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try {
            cst = cnt.prepareCall(sqlNhanVien);

            for (NhanVienRecord record : data) {
                cst.setString(1, record.MANV);
                cst.setString(2, record.TENNV);
                cst.setString(3, record.PHAI);
                cst.setDate(4, new java.sql.Date(record.NGAYSINH.getTime()));
                cst.setString(5, record.DIACHI);
                cst.setString(6, record.SODT);

                // Encrypt LUONG and PHUCAP using AES in CryptographyUtilities
                String md5Hash = CryptographyUtilities.hashMD5(record.SODT);
                String encryptedLuong = CryptographyUtilities.encryptAES(record.LUONG, md5Hash);
                String encryptedPhuCap = CryptographyUtilities.encryptAES(record.PHUCAP, md5Hash);

                cst.setString(7, encryptedLuong);
                cst.setString(8, encryptedPhuCap);
                cst.setString(9, record.VAITRO);
                cst.setString(10, record.MANQL);
                cst.setString(11, record.PHG);

                cst.execute();
            }

            commit();
            System.out.println("Records inserted successfully.");
        } catch (SQLException | NoSuchAlgorithmException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
//        sqlNhanVien = "{call INSERT_LUUTRU_RECORD(?, ?, ?)}";
//
//        try {
//            cst = cnt.prepareCall(sqlNhanVien);
//
//            for (NhanVienRecord record : data) {
//                String salt = CryptographyUtilities.generateSalt(16);
//                cst.setString(1, record.MANV);
//                cst.setString(2, salt);
//                cst.setString(3,  CryptographyUtilities.hashMD5(record.SODT));
//                cst.execute();
//            }
//
//            commit();
//            System.out.println("Records inserted successfully.");
//        } catch (SQLException | NoSuchAlgorithmException ex) {
//            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    public List<NhanVienRecord> getDecryptedNhanVienRecords() {
        List<NhanVienRecord> decryptedRecords = new ArrayList<>();

        try {
            String query = "SELECT MANV, TENNV, PHAI, NGAYSINH, DIACHI, SODT, LUONG, PHUCAP, VAITRO, MANQL, PHG FROM NHANVIEN";
            Statement statement = cnt.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String MANV = resultSet.getString("MANV");
                String TENNV = resultSet.getString("TENNV");
                String PHAI = resultSet.getString("PHAI");
                java.sql.Date NGAYSINH = resultSet.getDate("NGAYSINH");
                String DIACHI = resultSet.getString("DIACHI");
                String SODT = resultSet.getString("SODT");
                String encryptedLUONG = resultSet.getString("LUONG");
                String encryptedPHUCAP = resultSet.getString("PHUCAP");
                String VAITRO = resultSet.getString("VAITRO");
                String MANQL = resultSet.getString("MANQL");
                String PHG = resultSet.getString("PHG");

                String key = CryptographyUtilities.hashMD5(SODT);
                String LUONG = CryptographyUtilities.decryptAES(encryptedLUONG, key);
                String PHUCAP = CryptographyUtilities.decryptAES(encryptedPHUCAP, key);

                NhanVienRecord record = new NhanVienRecord(MANV, TENNV, PHAI, NGAYSINH, DIACHI, SODT, LUONG, PHUCAP, VAITRO, MANQL, PHG);
                decryptedRecords.add(record);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return decryptedRecords;
    }

    public void writeNhanVienRecordsToFile(List<NhanVienRecord> records, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.write("MANV,TENNV,PHAI,NGAYSINH,DIACHI,SODT,LUONG,PHUCAP,VAITRO,MANQL,PHG\n");

            // Write records
            for (NhanVienRecord record : records) {
                String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        record.MANV, record.TENNV, record.PHAI, record.NGAYSINH,
                        record.DIACHI, record.SODT, record.LUONG, record.PHUCAP,
                        record.VAITRO, record.MANQL, record.PHG);
                writer.write(line);
            }

            System.out.println("Records written to file: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
