CONNECT COMPANY_PUBLIC/astrongpassword@localhost:1521/COMPANY;

SET SERVEROUTPUT ON

DECLARE
  v_service_name VARCHAR2(100);
BEGIN
  SELECT SYS_CONTEXT('USERENV', 'SERVICE_NAME') INTO v_service_name FROM DUAL;
  DBMS_OUTPUT.PUT_LINE('Service Name: ' || v_service_name);
END;
/
--------------------------cau a
--Những người đã cập nhật trường THOIGIAN trong quan hệ PHANCONG.

BEGIN 
    DBMS_FGA.ADD_POLICY(
        OBJECT_SCHEMA=> 'COMPANY_PUBLIC',
        OBJECT_NAME=> 'PHANCONG',
        POLICY_NAME=> 'AUDIT_PHANCONG',
        AUDIT_COLUMN=> 'THOIGIAN',
        ENABLE => TRUE,
        STATEMENT_TYPES => 'UPDATE'
       
    );

END;
/
----------------------------cau b
--Những người đã đọc trên trường LUONG và PHUCAP của người khác.
BEGIN
       DBMS_FGA.ADD_POLICY(
        OBJECT_SCHEMA=> 'COMPANY_PUBLIC',
        OBJECT_NAME=> 'NHANVIEN',
        POLICY_NAME=> 'AUDIT_NHANVIEN',
        AUDIT_CONDITION=> 'MANV != SYS_CONTEXT(''USERENV'', ''SESSION_USER'')',
        AUDIT_COLUMN=> 'LUONG, PHUCAP',
        ENABLE => TRUE,
        STATEMENT_TYPES => 'SELECT'
       
    );
END;
/
-------------------------------------câu c
--Một người không thuộc vai trò “Tài chính” nhưng đã cập nhật thành công trên trường LUONG và PHUCAP.

CREATE OR REPLACE FUNCTION checkrole
(maNV in nvarchar2)
RETURN VARCHAR2 
AS
    v_role VARCHAR2(100);
BEGIN
    SELECT VAITRO INTO v_role
    FROM COMPANY_PUBLIC.VAITRO_NHANVIEN nv
    WHERE nv.MANV = maNV AND ROWNUM = 1;
   return v_role;
END;
/

BEGIN
    DBMS_FGA.ADD_POLICY(
        OBJECT_SCHEMA=> 'COMPANY_PUBLIC',
        OBJECT_NAME=> 'NHANVIEN',
        POLICY_NAME=> 'AUDIT_UPDATE_LUONG_PHUCAP',
        AUDIT_CONDITION=> 'checkrole(SYS_CONTEXT(''USERENV'', ''SESSION_USER'')) != ''Tài chính''',
        AUDIT_COLUMN=> 'LUONG, PHUCAP',
        ENABLE => TRUE,
        STATEMENT_TYPES => 'UPDATE'       
    );
END;
/
--------------------------------cau d
--d. Kiểm tra nhật ký hệ thống.
CREATE VIEW AUDIT_LOGFILE AS
SELECT * FROM DBA_FGA_AUDIT_TRAIL WHERE OBJECT_SCHEMA='COMPANY_PUBLIC';

BEGIN 
    DBMS_FGA.ADD_POLICY(
        OBJECT_SCHEMA=> 'COMPANY_PUBLIC',
        OBJECT_NAME=> 'AUDIT_LOGFILE',
        POLICY_NAME=> 'AUDIT_LOGFILE_DATA',
        ENABLE => TRUE,
        STATEMENT_TYPES => 'SELECT'       
    );

END;
/