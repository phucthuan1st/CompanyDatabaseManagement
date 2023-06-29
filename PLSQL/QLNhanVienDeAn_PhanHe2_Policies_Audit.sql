CONNECT COMPANY_PUBLIC/astrongpassword@localhost:1521/COMPANY;

SET SERVEROUTPUT ON

DECLARE
  v_service_name VARCHAR2(100);
BEGIN
  SELECT SYS_CONTEXT('USERENV', 'SERVICE_NAME') INTO v_service_name FROM DUAL;
  DBMS_OUTPUT.PUT_LINE('Service Name: ' || v_service_name);
END;
/

GRANT SELECT ON NHANVIEN TO COMPANY_PUBLIC;
GRANT SELECT ON PHANCONG TO COMPANY_PUBLIC;
GRANT SELECT ON DEAN TO COMPANY_PUBLIC;
GRANT SELECT ON PHONGBAN TO COMPANY_PUBLIC;

-----------------------------------------------------------------AUDIT------------------------------------------------------
--ALTER SYSTEM SET audit_trail = 'DB' SCOPE=SPFILE;
--SHUTDOWN IMMEDIATE;
--STARTUP;

--Tạo bảng audit_thoigian để ghi lại thông tin audit
CREATE TABLE COMPANY_PUBLIC.audit_thoigian (
  username       VARCHAR2(100),
  object_name    VARCHAR2(100),
  policy_name    VARCHAR2(100),
  statement_type VARCHAR2(100),
  new_thoigian   TIMESTAMP,
  old_thoigian   TIMESTAMP
);
/

--Tạo bảng audit_luongpc để ghi lại thông tin audit
CREATE TABLE COMPANY_PUBLIC.audit_luongpc (
  username       VARCHAR2(100),
  object_name    VARCHAR2(100),
  policy_name    VARCHAR2(100),
  statement_type VARCHAR2(100)
);
/

--Tạo bảng audit_updateluongpc để ghi lại thông tin audit
CREATE TABLE COMPANY_PUBLIC.audit_updateluongpc (
    username     VARCHAR2(100),
    object_name  VARCHAR2(100),
    policy_name  VARCHAR2(100),
    statement_type VARCHAR2(100)
);
/

----Tạo bảng audit_log để ghi lại thông tin audit
CREATE TABLE COMPANY_PUBLIC.audit_log (
  username        VARCHAR2(100),
  object_name     VARCHAR2(100),
  policy_name     VARCHAR2(100),
  statement_type  VARCHAR2(100),
  timestamp       TIMESTAMP DEFAULT SYSTIMESTAMP
);
/

BEGIN
  FOR policy IN (SELECT object_name, policy_name
                 FROM dba_audit_policies
                 WHERE policy_owner = 'COMPANY_PUBLIC') -- Replace 'COMPANY_PUBLIC' with the appropriate schema name
  LOOP
    DBMS_FGA.DROP_POLICY(
      object_schema => 'COMPANY_PUBLIC', -- Replace 'COMPANY_PUBLIC' with the appropriate schema name
      object_name   => policy.object_name,
      policy_name   => policy.policy_name
    );
  END LOOP;
END;
/


/*
    Những nguời dã cập nhật trường THOIGIAN trong quan hệ PHANCONG.
*/
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
    INSERT INTO COMPANY_PUBLIC.audit_thoigian (username, object_name, policy_name, statement_type, new_thoigian, old_thoigian)
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
    handler_module  => 'audit_1',
    enable          => TRUE,
    statement_types => 'UPDATE'
  );
END;
/
  
/* 
  Những người đã đọc trên trường LUONG và PHUCAP của người khác
*/

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
    INSERT INTO COMPANY_PUBLIC.audit_luongpc (username, object_name, policy_name, statement_type)
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
    handler_module  => 'audit_2',
    enable          => TRUE,
    statement_types => 'SELECT'
  );
END;
/

/*
  Một người không thuộc vai trò “Tài chính” nhưng đã cập nhật thành công trên trường LUONG và PHUCAP.
*/
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
    INSERT INTO COMPANY_PUBLIC.audit_updateluongpc (username, object_name, policy_name, statement_type)
    VALUES (SYS_CONTEXT('USERENV', 'SESSION_USER'), object_name, policy_name, statement_type);
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
    handler_module  => 'audit_3',
    enable          => TRUE,
    statement_types => 'UPDATE'
  );
END;
/

------------------------------------------------------------------------------
------------------------ Fine Grained Audit-----------------------------------
------------------------------------------------------------------------------
/*
    Kiểm tra nhật ký hệ thống
*/
--Tạo function để ghi thông tin audit vào bảng audit_log:
CREATE OR REPLACE FUNCTION audit_4 (
  object_schema   IN VARCHAR2,
  object_name     IN VARCHAR2,
  policy_name     IN VARCHAR2,
  statement_type  IN VARCHAR2
) RETURN BOOLEAN AS
BEGIN
  -- Insert the audit information into the audit_log table
  INSERT INTO COMPANY_PUBLIC.audit_log (username, object_name, policy_name, statement_type)
  VALUES (SYS_CONTEXT('USERENV', 'SESSION_USER'), object_name, policy_name, statement_type);

  RETURN TRUE;
END;
/

-- Thiết lập FGA để kích hoạt kiểm tra nhật ký hệ thống
BEGIN
  DBMS_FGA.ADD_POLICY(
    object_schema     => 'COMPANY_PUBLIC',
    object_name       => 'NHANVIEN',
    policy_name       => 'SYSTEM_AUDIT_NHANVIEN_POLICY',
    handler_module    => 'audit_4',
    enable            => TRUE,
    statement_types   => 'SELECT, INSERT, UPDATE, DELETE'
  );
END;
/

-- Thiết lập FGA để kích hoạt kiểm tra nhật ký hệ thống
BEGIN

  DBMS_FGA.ADD_POLICY(
    object_schema     => 'COMPANY_PUBLIC', 
    object_name       => 'PHANCONG',
    policy_name       => 'SYSTEM_AUDIT_PHANCONG_POLICY',
    handler_module    => 'audit_4',
    enable            => TRUE,
    statement_types   => 'SELECT, INSERT, UPDATE, DELETE'
  );
END;
/

-- Thiết lập FGA để kích hoạt kiểm tra nhật ký hệ thống
BEGIN
  
  DBMS_FGA.ADD_POLICY(
    object_schema     => 'COMPANY_PUBLIC', 
    object_name       => 'PHONGBAN',
    policy_name       => 'SYSTEM_AUDIT_PHONGBAN_POLICY',
    handler_module    => 'audit_4',
    enable            => TRUE,
    statement_types   => 'SELECT, INSERT, UPDATE, DELETE'
  );
END;
/

-- Thiết lập FGA để kích hoạt kiểm tra nhật ký hệ thống
BEGIN

  DBMS_FGA.ADD_POLICY(
    object_schema     => 'COMPANY_PUBLIC', 
    object_name       => 'LUUTRU',
    policy_name       => 'SYSTEM_AUDIT_LUUTRU_POLICY',
    handler_module    => 'audit_4',
    enable            => TRUE,
    statement_types   => 'SELECT, INSERT, UPDATE, DELETE'
  );
END;
/

-- Thiết lập FGA để kích hoạt kiểm tra nhật ký hệ thống
BEGIN

  DBMS_FGA.ADD_POLICY(
    object_schema     => 'COMPANY_PUBLIC', 
    object_name       => 'DEAN',
    policy_name       => 'SYSTEM_AUDIT_DEAN_POLICY',
    handler_module    => 'audit_4',
    enable            => TRUE,
    statement_types   => 'SELECT, INSERT, UPDATE, DELETE'
  );
END;
/

-- Thiết lập FGA để kích hoạt kiểm tra nhật ký hệ thống
BEGIN
  DBMS_FGA.ADD_POLICY(
    object_schema     => 'COMPANY_PUBLIC', 
    object_name       => 'THONGDIEP',
    policy_name       => 'SYSTEM_AUDIT_THONGDIEP_POLICY',
    handler_module    => 'audit_4',
    enable            => TRUE,
    statement_types   => 'SELECT, INSERT, UPDATE, DELETE'
  );
END;
/