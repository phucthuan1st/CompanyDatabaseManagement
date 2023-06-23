CONNECT COMPANY_PUBLIC/astrongpassword@localhost:1521/COMPANY;

SET SERVEROUTPUT ON

DECLARE
  v_service_name VARCHAR2(100);
BEGIN
  SELECT SYS_CONTEXT('USERENV', 'SERVICE_NAME') INTO v_service_name FROM DUAL;
  DBMS_OUTPUT.PUT_LINE('Service Name: ' || v_service_name);
END;
/

/* =========== CÀI ĐẶT CÁC CHÍNH SÁCH DÙNG VPD ============= */

-------------------------------------------------------------------------------------------------------------------------------------
/*
CS#2: Những người dùng có VAITRO là “QL trực tiếp” nếu họ phụ trách quản lý trực tiếp nhân
viên khác. Nhân viên Q là quản lý trực tiếp nhân viên N, có quyền được mô tả như sau:
− Q có quyền như là một nhân viên thông thường (vai trò “Nhân viên”). Ngoài ra, với các dòng 
dữ liệu trong quan hệ NHANVIEN liên quan đến các nhân viên N mà Q quản lý trực tiếp thì
Q được xem tất cả các thuộc tính, trừ thuộc tính LUONG và PHUCAP.
− Có thể xem các dòng trong quan hệ PHANCONG liên quan đến chính Q và các nhân viên N
được quản lý trực tiếp bởi Q.
*/
CREATE OR REPLACE FUNCTION QLTRUCTIEP_PERMISSION_CONSTRAINTS(
P_SCHEMA IN VARCHAR2,
P_OBJ IN VARCHAR2
)
RETURN VARCHAR2
AS
vaitro  VARCHAR2(100);
BEGIN 
    SELECT VAITRO INTO vaitro FROM COMPANY_PUBLIC.NHANVIEN WHERE MANV = SYS_CONTEXT('USERENV', 'SESSION_USER');
    
    IF P_OBJ = 'NHANVIEN' THEN
        IF vaitro = 'QL trực tiếp' THEN 
            RETURN ' MANQL =SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
        END IF;
    ELSIF P_OBJ = 'PHANCONG' THEN
        IF vaitro= 'QL trực tiếp' THEN 
            RETURN 'MANV =SYS_CONTEXT(''USERENV'', ''SESSION_USER'') OR MANV IN (SELECT MANV FROM NHANVIEN WHERE MANQL= SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
        END IF;
    END IF;
END;
/
BEGIN 
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'NHANVIEN',
        policy_name      => 'QLTRUCTIEP_SELECT_NHANVIEN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'QLTRUCTIEP_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT',
        sec_relevant_cols=> ' MANV, TENNV, PHAI, NGAYSINH, DIACHI, SODT, VAITRO, MANQL, PHG '
    );

   DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'PHANCONG',
        policy_name      => 'QLTRUCTIEP_SELECT_PHANCONG_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'QLTRUCTIEP_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );     
END;
/
-------------------------------------------------------------------------------------------------------------------------------------
