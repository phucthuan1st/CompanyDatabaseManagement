package com.csdlcongty;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.csdlcongty.helper.CryptographyUtilities;
import com.csdlcongty.helper.NhanVienRecord;

import static com.csdlcongty.helper.MockGenerator.generateQLRecords;
import static com.csdlcongty.helper.MockGenerator.generateNVRecords;
import static com.csdlcongty.helper.MockGenerator.generateTPRecords;

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
    public Connection cnt;
    protected Statement st;
    protected CallableStatement cst;
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

    public int getNumberRowsOf(String entity) {
        String sql = "SELECT COUNT(*) FROM " + entity;
        ResultSet resultSet;
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
        ResultSet resultSet;
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
        ResultSet result;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }

    public ResultSet getTableList() throws SQLException {
        updateStatisticsForSchema();
        String sql = "SELECT * FROM TABLE_LIST ORDER BY TABLE_NAME ASC";
        ResultSet result;
        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }

    public ResultSet getViewList() throws SQLException {
        String sql = "SELECT * FROM VIEW_LIST";
        ResultSet result;

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

        ResultSet result;

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

        int successful;

        cst = cnt.prepareCall(sql);
        cst.setString(1, roleName);
        cst.execute();
        commit();
        successful = 1;

        return successful;
    }
    
    private void updateStatisticsForSchema() {
        try {
            CallableStatement stmt = cnt.prepareCall("{call gather_statistics_for_schema}");
            stmt.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
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
        ResultSet result;

        st = cnt.createStatement();
        st.execute(sql);
        result = st.getResultSet();

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }

    public String entityTypeIdentify(String entityName) throws SQLException {
        String entityType;
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
    
    public ResultSet selectFromTable(String tableName) throws SQLException {
        String sql = "SELECT * FROM COMPANY_PUBLIC." +tableName;
        ResultSet result;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }

    public ResultSet selectLuongPhuCap(String id) throws SQLException {
        String sql = String.format("SELECT LUONG, PHUCAP FROM COMPANY_PUBLIC.NHANVIEN WHERE MANV = '%s'", id);
        ResultSet result;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }
    
    public int updatePHONGBAN(String oldma, String mapb, String tenpb, String tentrphg  ) throws SQLException{
        String sql = "UPDATE COMPANY_PUBLIC.PHONGBAN SET";
        int count=0;
        // kiểm tra
        try{
                if (mapb!= "")
            {
                sql= sql +"MAPB= ? ,";
                count++;
            }
            if (tenpb!="" )
            {
                sql= sql + "  TENPB= ? ,";
                count++;
            }  

            if (tentrphg!="")
            {
                sql= sql + "TRPHG= ? , ";
            }
            sql = sql.substring(0, sql.length() - 1);
            sql= sql+ " WHERE MAPB = ? ;";
            cst = cnt.prepareCall(sql);
            int parameterIndex=1;
            // gán giá trị
            if (mapb != "") {
            cst.setString(parameterIndex++, mapb);
            }
            if (tenpb != "") {
                cst.setString(parameterIndex++, tenpb);
            }
            if (tentrphg != "") {
                cst.setString(parameterIndex++, tentrphg);
            }
            cst.setString(parameterIndex, mapb);
            cst.execute();
                        SQLWarning warning = cst.getWarnings();
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
    
    
    
//    public void insertMockRecordToNhanVien() {
//        var data = generateNVRecords(300);
//        data.addAll(generateQLRecords(20));
//        data.addAll(generateTPRecords(8));
//
//        String sqlNhanVien = "{call INSERT_NHANVIEN_RECORD(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
//
//        try {
//            cst = cnt.prepareCall(sqlNhanVien);
//
//            for (NhanVienRecord record : data) {
//                cst.setString(1, record.MANV);
//                cst.setString(2, record.TENNV);
//                cst.setString(3, record.PHAI);
//                cst.setDate(4, new java.sql.Date(record.NGAYSINH.getTime()));
//                cst.setString(5, record.DIACHI);
//                cst.setString(6, record.SODT);
//
//                // Encrypt LUONG and PHUCAP using AES in CryptographyUtilities
//                String md5Hash = CryptographyUtilities.hashMD5(record.SODT);
//                String encryptedLuong = CryptographyUtilities.encryptAES(record.LUONG, md5Hash);
//                String encryptedPhuCap = CryptographyUtilities.encryptAES(record.PHUCAP, md5Hash);
//
//                cst.setString(7, encryptedLuong);
//                cst.setString(8, encryptedPhuCap);
//                cst.setString(9, record.VAITRO);
//                cst.setString(10, record.MANQL);
//                cst.setString(11, record.PHG);
//
//                cst.execute();
//            }
//
//            commit();
//            System.out.println("Records inserted to NHANVIEN successfully.");
//        } catch (SQLException | NoSuchAlgorithmException ex) {
//            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        sqlNhanVien = "{call INSERT_LUUTRU_RECORD(?, ?)}";
//        try {
//            cst = cnt.prepareCall(sqlNhanVien);
//
//            for (NhanVienRecord record : data) {
//                cst.setString(1, record.MANV);
//                cst.setString(2,  CryptographyUtilities.hashMD5(record.SODT));
//                cst.execute();
//            }
//
//            commit();
//            System.out.println("Records inserted to LUUTRU successfully.");
//        } catch (Exception ex) {
//            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }


    public ResultSet selectLuongPhuCap() throws SQLException {
        String sql = String.format("SELECT LUONG, PHUCAP FROM COMPANY_PUBLIC.NHANVIEN");
        ResultSet result;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }

    public ResultSet getPersonalInfomation(String id) throws SQLException{
        String sql = String.format("SELECT * FROM COMPANY_PUBLIC.NHANVIEN WHERE MANV = '%s'", id);
        System.out.println(sql);
        ResultSet result;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }


    public void insertMockRecordToNhanVien() {
        var data = generateNVRecords(300);
        data.addAll(generateQLRecords(20));
        data.addAll(generateTPRecords(8));

        String sqlNhanVien = "{call INSERT_NHANVIEN_RECORD(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try {
            cst = cnt.prepareCall(sqlNhanVien);

            // Enable DBMS_OUTPUT
            CallableStatement enableOutputStmt = cnt.prepareCall("BEGIN DBMS_OUTPUT.ENABLE(NULL); END;");
            enableOutputStmt.execute();

            for (NhanVienRecord record : data) {
                cst.setString(1, record.MANV);
                cst.setString(2, record.TENNV);
                cst.setString(3, record.PHAI);
                cst.setDate(4, new java.sql.Date(record.NGAYSINH.getTime()));
                cst.setString(5, record.DIACHI);
                cst.setString(6, record.SODT);

                // Encrypt LUONG and PHUCAP using AES in CryptographyUtilities
                String md5Hash = CryptographyUtilities.hashMD5("secret");
                String encryptedLuong = CryptographyUtilities.encryptAES(record.LUONG, md5Hash);
                String encryptedPhuCap = CryptographyUtilities.encryptAES(record.PHUCAP, md5Hash);

                cst.setString(7, encryptedLuong);
                cst.setString(8, encryptedPhuCap);
                cst.setString(9, record.VAITRO);
                cst.setString(10, record.MANQL);
                cst.setString(11, record.PHG);

                cst.execute();
            }

            // Retrieve DBMS_OUTPUT
            CallableStatement retrieveOutputStmt = cnt.prepareCall("BEGIN DBMS_OUTPUT.GET_LINE(?, ?); END;");
            retrieveOutputStmt.registerOutParameter(1, Types.VARCHAR);
            retrieveOutputStmt.registerOutParameter(2, Types.NUMERIC);

            int status = 0;
            while (status == 0) {
                retrieveOutputStmt.execute();
                String line = retrieveOutputStmt.getString(1);
                status = retrieveOutputStmt.getInt(2);
                if (line != null && status == 0) {
                    System.out.println(line);
                }
            }

            // Disable DBMS_OUTPUT
            CallableStatement disableOutputStmt = cnt.prepareCall("BEGIN DBMS_OUTPUT.DISABLE; END;");
            disableOutputStmt.execute();

            commit();
            System.out.println("Records inserted to NHANVIEN successfully.");
        } catch (SQLException | NoSuchAlgorithmException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        sqlNhanVien = "{call INSERT_LUUTRU_RECORD(?, ?)}";
        try {
            cst = cnt.prepareCall(sqlNhanVien);

            for (NhanVienRecord record : data) {
                cst.setString(1, record.MANV);
                cst.setString(2, CryptographyUtilities.hashMD5(record.SODT));
                cst.execute();
            }

            commit();
            System.out.println("Records inserted to LUUTRU successfully.");
        } catch (Exception ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public List<NhanVienRecord> getDecryptedNhanVienRecords() {
        List<NhanVienRecord> decryptedRecords = new ArrayList<>();

        try {
            String query = "SELECT MANV, TENNV, PHAI, NGAYSINH, DIACHI, SODT, LUONG, PHUCAP, MANQL, PHG FROM NHANVIEN";
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
                String MANQL = resultSet.getString("MANQL");
                String PHG = resultSet.getString("PHG");

                String key = CryptographyUtilities.hashMD5("secret");
                String LUONG = CryptographyUtilities.decryptAES(encryptedLUONG, key);
                String PHUCAP = CryptographyUtilities.decryptAES(encryptedPHUCAP, key);

                NhanVienRecord record = new NhanVienRecord(MANV, TENNV, PHAI, NGAYSINH, DIACHI, SODT, LUONG, PHUCAP, "", MANQL, PHG);
                decryptedRecords.add(record);
            }

            resultSet.close();
            statement.close();
        } catch (Exception ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return decryptedRecords;
    }

    public void writeNhanVienRecordsToFile(List<NhanVienRecord> records, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.write("MANV,TENNV,PHAI,NGAYSINH,DIACHI,SODT,LUONG,PHUCAP,MANQL,PHG\n");

            // Write records
            for (NhanVienRecord record : records) {
                String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        record.MANV, record.TENNV, record.PHAI, record.NGAYSINH,
                        record.DIACHI, record.SODT, record.LUONG, record.PHUCAP,
                        record.MANQL, record.PHG);
                writer.write(line);
            }

            System.out.println("Records written to file: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
