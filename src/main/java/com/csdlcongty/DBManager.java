package com.csdlcongty;

import com.csdlcongty.helper.CryptographyUtilities;
import com.csdlcongty.helper.NhanVienRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.csdlcongty.helper.MockGenerator.*;

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

    protected PreparedStatement prt;
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
        } catch (SQLException ex) {
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
        String sql = "SELECT * FROM COMPANY_PUBLIC." + tableName;
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

    public ResultSet selectLuongPhuCap() throws SQLException {
        String sql = String.format("SELECT LUONG, PHUCAP FROM COMPANY_PUBLIC.NHANVIEN");
        ResultSet result;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }

    public ResultSet getPersonalInfomation(String id) throws SQLException {
        String sql = String.format("SELECT * FROM COMPANY_PUBLIC.NHANVIEN WHERE MANV = '%s'", id);
        ResultSet result;

        st = cnt.createStatement();
        result = st.executeQuery(sql);

        previousStatement = String.format(DBManager.COUNTSQL, sql);
        return result;
    }


    public void insertMockRecordToNhanVien() throws NoSuchAlgorithmException {
        var data = generateNVRecords(300);
        data.addAll(generateQLRecords(20));
        data.addAll(generateTPRecords(8));
        data.addAll(generateTCRecords(5));
        data.addAll(generateNhanSuRecords(5));
        data.addAll(generateTruongDeAnRecords(3));

        String key = CryptographyUtilities.hashMD5("secret");

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
                String encryptedLuong = CryptographyUtilities.encryptAES(record.LUONG, key);
                String encryptedPhuCap = CryptographyUtilities.encryptAES(record.PHUCAP, key);

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
                cst.setString(2, key);
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

    public void insertPhanCongRecord(String maNV, String maDA, String thoiGian) throws SQLException, ParseException {
        String sql = "INSERT INTO COMPANY_PUBLIC.PHANCONG(MANV, MADA, THOIGIAN) VALUES (?, ?, ?)";
        prt = cnt.prepareStatement(sql);

        prt.setString(1, maNV);
        prt.setString(2, maDA);
        prt.setDate(3, new Date((new SimpleDateFormat("dd/MM/yyyy")).parse(thoiGian).getTime()));

        prt.execute();
        commit();
    }

    public void deletePhanCongRecord(String maNV, String maDA) throws SQLException {
        String sql = "DELETE FROM COMPANY_PUBLIC.PHANCONG WHERE MANV = ? AND MADA = ?";
        prt = cnt.prepareStatement(sql);

        prt.setString(1, maNV);
        prt.setString(2, maDA);

        prt.execute();
        commit();
    }

    public void updatePhanCongRecord(String oldMaNV, String oldMaDA, String newMaNV, String newMaDA, String newThoiGian) throws SQLException, ParseException {
        String sql = """
                                UPDATE COMPANY_PUBLIC.PHANCONG
                                SET MANV = ?, MADA = ?, THOIGIAN = ?
                                WHERE MANV = ? AND MADA = ?
                """;

        prt = cnt.prepareStatement(sql);

        prt.setString(1, newMaNV);
        prt.setString(2, newMaDA);
        prt.setDate(3, new Date((new SimpleDateFormat("dd/MM/yyyy")).parse(newThoiGian).getTime()));
        prt.setString(4, oldMaNV);
        prt.setString(5, oldMaDA);

        prt.execute();
        commit();
    }

    public void insertPhongBanRecord(String maPB, String tenPB, String trPhg) throws SQLException {
        String sql = "INSERT INTO COMPANY_PUBLIC.PHONGBAN(MAPB, TENPB, TRPHG) VALUES (?, ?, ?)";
        prt = cnt.prepareStatement(sql);

        prt.setString(1, maPB);
        prt.setString(2, tenPB);
        prt.setString(3, trPhg);

        prt.execute();
        commit();
    }

    public void updatePhongBanRecord(String oldMaPB, String maPB, String tenPB, String trPhg) throws SQLException {
        String sql = """
                                UPDATE COMPANY_PUBLIC.PHONGBAN
                                SET MAPB = ?, TENPB = ?, TRPHG = ?
                                WHERE MAPB = ?
                """;

        prt = cnt.prepareStatement(sql);

        prt.setString(1, maPB);
        prt.setString(2, tenPB);
        prt.setString(3, trPhg);
        prt.setString(4, oldMaPB);

        prt.execute();
        commit();
    }

    public void insertDeAnRecord(String maDA, String tenDA, String ngayBD, String phong, String truongDeAn) throws SQLException, ParseException {
        String sql = "INSERT INTO COMPANY_PUBLIC.DEAN(MADA, TENDA, NGAYBD, PHONG, TRUONGDEAN) VALUES (?, ?, ?, ?, ?)";
        prt = cnt.prepareStatement(sql);

        prt.setString(1, maDA);
        prt.setString(2, tenDA);
        prt.setDate(3, new Date((new SimpleDateFormat("dd/MM/yyyy")).parse(ngayBD).getTime()));
        prt.setString(4, phong);
        prt.setString(5, truongDeAn);

        prt.execute();
        commit();
    }

    public void updateDeAnRecord(String oldMaDA, String maDA, String tenDA, String ngayBD, String phong, String truongDeAn) throws SQLException, ParseException {
        String sql = """
                                UPDATE COMPANY_PUBLIC.DEAN
                                SET MADA = ?, TENDA = ?, NGAYBD = ?, PHONG = ?, TRUONGDEAN = ?
                                WHERE MADA = ?
                """;

        prt = cnt.prepareStatement(sql);

        prt.setString(1, maDA);
        prt.setString(2, tenDA);
        prt.setDate(3, new Date((new SimpleDateFormat("dd/MM/yyyy")).parse(ngayBD).getTime()));
        prt.setString(4, phong);
        prt.setString(5, truongDeAn);
        prt.setString(6, oldMaDA);

        prt.execute();
        commit();
    }

    public void deleteDeAnRecord(String oldMaDA) throws SQLException {
        String sql = "DELETE FROM COMPANY_PUBLIC.DEAN WHERE MADA = ?";
        prt = cnt.prepareStatement(sql);

        prt.setString(1, oldMaDA);

        prt.execute();
        commit();
    }

    public void updatePersonalInfoRecord(String newNgaySinh, String newDiaChi, String newSoDT) throws SQLException, ParseException {
        String sql = """
                                UPDATE COMPANY_PUBLIC.NHANVIEN
                                SET NGAYSINH = ?, DIACHI = ?, SODT = ?
                                WHERE MANV = SYS_CONTEXT('USERENV', 'SESSION_USER')
                """;

        prt = cnt.prepareStatement(sql);

        prt.setDate(1, new Date((new SimpleDateFormat("dd/MM/yyyy")).parse(newNgaySinh).getTime()));
        prt.setString(2, newDiaChi);
        prt.setString(3, newSoDT);

        prt.execute();
        commit();
    }

    public void insertNhanVienRecord(String newMaNV, String newTenNV, String phai, String newNgaySinh, String newDiaChi, String newSoDT, String newMaNQL, String newPhong, String newVaiTro) throws SQLException, ParseException {
        String sql = "INSERT INTO COMPANY_PUBLIC.NHANVIEN(MANV, TENNV, PHAI, NGAYSINH, DIACHI, SODT, MANQL, PHG)"
                + " VALUES(?, ? , ? , ? , ? , ? , ? , ?);";

        prt = cnt.prepareStatement(sql);

        prt.setString(1, newMaNV);
        prt.setString(2, newTenNV);
        prt.setString(3, phai);
        prt.setDate(4, new Date((new SimpleDateFormat("dd/MM/yyyy")).parse(newNgaySinh).getTime()));
        prt.setString(5, newDiaChi);
        prt.setString(6, newSoDT);
        prt.setString(7, newMaNQL);
        prt.setString(8, newPhong);
        prt.execute();
        commit();
    }

    public void updateSalaryAndAllowance(String maNV, String newLuong, String newPhuCap) throws Exception {
        String sql = """
                                UPDATE COMPANY_PUBLIC.NHANVIEN
                                SET LUONG = ?, PHUCAP = ?
                                WHERE MANV = ?
                """;

        prt = cnt.prepareStatement(sql);

        prt.setString(3, maNV);

        ResultSet result = this.selectFromTable("LUUTRU");
        String key;
        if (result.next())
            key = result.getString("SECRET_KEY");
        else
            throw new Exception("Cannot access the secret key");

        String encryptedLuong = CryptographyUtilities.encryptAES(newLuong, key);
        String encryptedPhuCap = CryptographyUtilities.encryptAES(newPhuCap, key);

        prt.setString(1, encryptedLuong);
        prt.setString(2, encryptedPhuCap);
        prt.setString(3, maNV);

        prt.execute();
        commit();
    }

    public ResultSet showSystemAudit() throws SQLException {
        String sql = "SELECT TIMESTAMP, DB_USER, SQL_TEXT FROM DBA_FGA_AUDIT_TRAIL WHERE OBJECT_SCHEMA=? and policy_name=?";
        prt = cnt.prepareStatement(sql);

        String schema = "COMPANY_PUBLIC";
        String policy_name = "AUDIT_LOGFILE_DATA";
        prt.setString(1, schema);
        prt.setString(2, policy_name);

        ResultSet resultSet = prt.executeQuery();
        previousStatement = String.format(DBManager.COUNTSQL, sql);

        return resultSet;
    }

    public ResultSet showInvalidUpdateAuditOnNhanVien() throws SQLException {
        String sql = "SELECT TIMESTAMP, DB_USER, SQL_TEXT FROM DBA_FGA_AUDIT_TRAIL WHERE OBJECT_SCHEMA=? and policy_name=?";
        ;
        prt = cnt.prepareStatement(sql);

        String schema = "COMPANY_PUBLIC";
        String policy_name = "AUDIT_UPDATE_LUONG_PHUCAP";
        prt.setString(1, schema);
        prt.setString(2, policy_name);

        ResultSet resultSet = prt.executeQuery();
        previousStatement = String.format(DBManager.COUNTSQL, sql);

        return resultSet;
    }

    public ResultSet ShowReadAuditOnNhanVien() throws SQLException {
        String sql = "SELECT TIMESTAMP, DB_USER, SQL_TEXT FROM DBA_FGA_AUDIT_TRAIL WHERE OBJECT_SCHEMA=? and policy_name=?";
        ;
        prt = cnt.prepareStatement(sql);

        String schema = "COMPANY_PUBLIC";
        String policy_name = "AUDIT_NHANVIEN";
        prt.setString(1, schema);
        prt.setString(2, policy_name);

        ResultSet resultSet = prt.executeQuery();
        previousStatement = String.format(DBManager.COUNTSQL, sql);

        return resultSet;
    }

    public ResultSet showUpdateAuditInPhanCong() throws SQLException {
        String sql = "SELECT TIMESTAMP, DB_USER, SQL_TEXT FROM DBA_FGA_AUDIT_TRAIL WHERE OBJECT_SCHEMA=? and policy_name=?";

        prt = cnt.prepareStatement(sql);

        String schema = "COMPANY_PUBLIC";
        String policy_name = "AUDIT_PHANCONG";
        prt.setString(1, schema);
        prt.setString(2, policy_name);

        ResultSet resultSet = prt.executeQuery();
        previousStatement = String.format(DBManager.COUNTSQL, sql);

        return resultSet;
    }

    public Boolean updateSecretKey(String username, String currentKey, String newKey) throws Exception {
        String sql = "UPDATE COMPANY_PUBLIC.LUUTRU SET SECRET_KEY = ? WHERE SYS_CONTEXT('USERENV', 'SESSION_USER') = MANV";
        String currentKeyHashed = CryptographyUtilities.hashMD5(currentKey);

        ResultSet result = this.selectFromTable("LUUTRU");
        String savedKey;
        if (result.next())
            savedKey = result.getString("SECRET_KEY");
        else
            throw new Exception("Cannot access the secret key");

        if (savedKey.equals(currentKeyHashed)) {
            String newHashedKey = CryptographyUtilities.hashMD5(newKey);
            prt = cnt.prepareStatement(sql);
            prt.setString(1, newHashedKey);

            String LUONG, PHUCAP;
            LUONG = CryptographyUtilities.decryptAES(result.getString("LUONG"), currentKeyHashed);
            PHUCAP = CryptographyUtilities.decryptAES(result.getString("PHUCAP"), currentKeyHashed);

            prt.execute();
            commit();

            updateSalaryAndAllowance(username, LUONG, PHUCAP);
            return true;
        }

        return false;
    }
}
