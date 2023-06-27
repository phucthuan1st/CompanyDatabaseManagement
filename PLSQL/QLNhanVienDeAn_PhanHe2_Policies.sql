CONNECT COMPANY_PUBLIC/astrongpassword@localhost:1521/COMPANY;

SET SERVEROUTPUT ON

DECLARE
  v_service_name VARCHAR2(100);
BEGIN
  SELECT SYS_CONTEXT('USERENV', 'SERVICE_NAME') INTO v_service_name FROM DUAL;
  DBMS_OUTPUT.PUT_LINE('Service Name: ' || v_service_name);
END;
/
-------------------------------------------------------------------------------------------------------------------------------------
---------------------------------------------OLS----------------------------------------
--Tạo chính sách
BEGIN
    SA_SYSDBA.CREATE_POLICY (
        policy_name => 'OLS_POLICY'
        column_name => 'OLS_COLUMN'
    );
END;
/

--Tạo level theo 3 mức độ Giám đốc, Trường phòng và Nhân viên
BEGIN
    SA_COMPONENTS.CREATE_LEVEL (
        policy_name => 'OLS_POLICY'
        long_name => 'NHANVIEN'
        short_name =>'NV'
        level_num => 1000
    );
END;
/

BEGIN
    SA_COMPONENTS.CREATE_LEVEL (
        policy_name => 'OLS_POLICY'
        long_name => 'TRUONGPHONG'
        short_name =>'TP'
        level_num => 2000
    );
END;
/

BEGIN
    SA_COMPONENTS.CREATE_LEVEL (
        policy_name => 'OLS_POLICY'
        long_name => 'GIAMDOC'
        short_name =>'GD'
        level_num => 3000
    );
END;
/

--Tạo compartments theo 3 ngành nghề Mua bán, Sản xuẩ và Gia công
BEGIN
    SA_COMPONENTS.CREATE_COMPARTMENT(
        policy_name => 'OLS_POLICY'
        long_name => 'MUABAN'
        short_name => 'MB'
        comp_num => 100
    );
END;
/

BEGIN
    SA_COMPONENTS.CREATE_COMPARTMENT(
        policy_name => 'OLS_POLICY'
        long_name => 'SANXUAT'
        short_name => 'SX'
        comp_num => 200
    );
END;
/

BEGIN
    SA_COMPONENTS.CREATE_COMPARTMENT(
        policy_name => 'OLS_POLICY'
        long_name => 'GIACONG'
        short_name => 'GC'
        comp_num => 300
    );
END;
/
--Tạo group theo 3 miền Bắc, Trung, Nam là con của công ty chủ
BEGIN
    SA_COMPONENTS.CREATE_GROUP (
        policy_name => 'OLS_POLICY'
        long_name => 'CONGTY'
        short_name =>'CTY'
        group_num => 10
        parent_name => NULL
    );
END;
/

BEGIN
    SA_COMPONENTS.CREATE_GROUP (
        policy_name => 'OLS_POLICY'
        long_name => 'BAC'
        short_name =>'BA'
        group_num => 11
        parent_name => 'CTY'
    );
END;
/

BEGIN
    SA_COMPONENTS.CREATE_GROUP (
        policy_name => 'OLS_POLICY'
        long_name => 'TRUNG'
        short_name =>'TR'
        group_num => 12
        parent_name => 'CTY'
    );
END;
/

BEGIN
    SA_COMPONENTS.CREATE_GROUP (
        policy_name => 'OLS_POLICY'
        long_name => 'NAM'
        short_name =>'NA'
        group_num => 13
        parent_name => 'CTY'
    );
END;
/
  
-----------------------------------------------------------------AUDIT------------------------------------------------------
ALTER SYSTEM SET audit_trail = 'DB' SCOPE=SPFILE;
SHUTDOWN IMMEDIATE;
STARTUP;

/*
    Những nguời dã cập nhật trường THOIGIAN trong quan hệ PHANCONG.
*/
--Tạo bảng audit_thoigian để ghi lại thông tin audit
CREATE TABLE audit_thoigian (
  username       VARCHAR2(100),
  object_name    VARCHAR2(100),
  policy_name    VARCHAR2(100),
  statement_type VARCHAR2(100),
  new_thoigian   TIMESTAMP,
  old_thoigian   TIMESTAMP
);
/
--Tạo function để ghi thông tin audit vào bảng audit_thoigian
CREATE OR REPLACE FUNCTION audit_1 (
  object_schema IN VARCHAR2,
  object_name   IN VARCHAR2,
  policy_name   IN VARCHAR2,
  statement_type IN VARCHAR2,
  audit_column  IN VARCHAR2,
  new_value     IN VARCHAR2,
  old_value     IN VARCHAR2
) RETURN VARCHAR2 AS
BEGIN
    -- Ghi thông tin audit vào bảng audit_thoigian
    INSERT INTO audit_thoigian (username, object_name, policy_name, statement_type, new_thoigian, old_thoigian)
    VALUES (SYS_CONTEXT('USERENV', 'SESSION_USER'), object_name, policy_name, statement_type, TO_TIMESTAMP(new_value), TO_TIMESTAMP(old_value));
    
    RETURN NULL;
END;
/
--Thiết lập FGA cho quan hệ "PHANCONG" và trường "THOIGIAN"
BEGIN
  DBMS_FGA.ADD_POLICY(
    object_schema   => 'COMPANY_PUBLIC', 
    object_name     => 'PHANCONG',
    policy_name     => 'AUDIT_THOIGIAN_UPDATE',
    audit_column    => 'THOIGIAN',
    handler_schema  => 'COMPANY_PUBLIC', 
    handler_module  => 'audit_1',
    enable          => TRUE,
    statement_types => 'UPDATE'
  );
END;
/
  
/* 
  Những người đã đọc trên trường LUONG và PHUCAP của người khác
*/
--Tạo bảng audit_luongpc để ghi lại thông tin audit
CREATE TABLE audit_luongpc (
  username       VARCHAR2(100),
  object_name    VARCHAR2(100),
  policy_name    VARCHAR2(100),
  statement_type VARCHAR2(100)
);
/
--Tạo function để ghi thông tin audit vào bảng audit_luongpc
CREATE OR REPLACE FUNCTION audit_2 (
  object_schema  IN VARCHAR2,
  object_name    IN VARCHAR2,
  policy_name    IN VARCHAR2,
  statement_type IN VARCHAR2,
  audit_column   IN VARCHAR2
) RETURN VARCHAR2 AS
BEGIN
    -- Ghi thông tin audit vào bảng audit_luongpc
    INSERT INTO audit_luongpc (username, object_name, policy_name, statement_type)
    VALUES (SYS_CONTEXT('USERENV', 'SESSION_USER'), object_name, policy_name, statement_type);
    
    RETURN NULL;
END;
/
--Thiết lập FGA cho quan hệ "NHANVIEN" và trường "LUONG" và "PHUCAP"
BEGIN
  DBMS_FGA.ADD_POLICY(
    object_schema   => 'COMPANY_PUBLIC', 
    object_name     => 'NHANVIEN',
    policy_name     => 'AUDIT_NHANVIEN_READ',
    audit_column    => 'LUONG,PHUCAP',
    handler_schema  => 'COMPANY_PUBLIC', 
    handler_module  => 'audit_2',
    enable          => TRUE,
    statement_types => 'SELECT'
  );
END;
/

/*
  Một người không thuộc vai trò “Tài chính” nhưng đã cập nhật thành công trên trường LUONG và PHUCAP.
*/
--Tạo bảng audit_updateluongpc để ghi lại thông tin audit
CREATE TABLE audit_updateluongpc (
    username     VARCHAR2(100),
    object_name  VARCHAR2(100),
    policy_name  VARCHAR2(100),
    statement_type VARCHAR2(100)
);
/
--Tạo function để ghi thông tin audit vào bảng audit_updateluongpc
CREATE OR REPLACE FUNCTION audit_3 (
    object_schema IN VARCHAR2,
    object_name   IN VARCHAR2,
    policy_name   IN VARCHAR2,
    statement_type IN VARCHAR2
) RETURN VARCHAR2 AS
  vaitro VARCHAR2(100);
BEGIN
  -- Kiểm tra vai trò của người dùng
  SELECT VAITRO INTO vaitro
  FROM COMPANY_PUBLIC.VAITRO_NHANVIEN
  WHERE MANV = SYS_CONTEXT('USERENV', 'SESSION_USER');
  
  -- Kiểm tra nếu người dùng không thuộc vai trò "Tài chính" 
  IF vaitro <> 'Tài chính' THEN
    -- Ghi thông tin audit vào bảng audit_updateluongpc
    INSERT INTO audit_updateluongpc (username, object_name, policy_name, statement_type, timestamp)
    VALUES (SYS_CONTEXT('USERENV', 'SESSION_USER'), object_name, policy_name, statement_type, SYSTIMESTAMP);
  END IF;
  
  RETURN NULL;
END;
/
--Thiết lập FGA cho quan hệ "NHANVIEN" và trường "LUONG" và "PHUCAP"
BEGIN
  DBMS_FGA.ADD_POLICY(
    object_schema   => 'COMPANY_PUBLIC',
    object_name     => 'NHANVIEN',
    policy_name     => 'AUDIT_LUONG_PHUCAP',
    audit_column    => 'LUONG,PHUCAP',
    handler_schema  => 'COMPANY_PUBLIC',
    handler_module  => 'audit_3',
    enable          => TRUE,
    statement_types => 'UPDATE'
  );
END;
/
  
/*
    Kiểm tra nhật ký hệ thống
*/
----Tạo bảng audit_log để ghi lại thông tin audit
CREATE TABLE audit_log (
  username        VARCHAR2(100),
  object_name     VARCHAR2(100),
  policy_name     IN VARCHAR2,
  statement_type  VARCHAR2(100)
);
/
--Tạo function để ghi thông tin audit vào bảng audit_log:
CREATE OR REPLACE FUNCTION audit_4 (
  object_schema   IN VARCHAR2,
  object_name     IN VARCHAR2,
  policy_name     IN VARCHAR2,
  statement_type  IN VARCHAR2
) RETURN BOOLEAN AS
BEGIN
  -- Ghi thông tin audit vào bảng audit_log
  INSERT INTO audit_log (username, object_name, policy_name, statement_type)
  VALUES (SYS_CONTEXT('USERENV', 'SESSION_USER'), object_name, policy_name, statement_type);
  
  RETURN TRUE;
END;
/
-- Thiết lập FGA để kích hoạt kiểm tra nhật ký hệ thống
BEGIN
  DBMS_FGA.ADD_POLICY(
    object_schema     => 'COMPANY_PUBLIC', 
    object_name       => NULL,
    policy_name       => 'SYSTEM_AUDIT_POLICY',
    audit_condition   => NULL,
    audit_column      => NULL,
    handler_schema    => 'COMPANY_PUBLIC', 
    handler_module    => 'audit_4',
    enable            => TRUE,
    statement_types   => 'SELECT, INSERT, UPDATE, DELETE'
  );
END;
/
