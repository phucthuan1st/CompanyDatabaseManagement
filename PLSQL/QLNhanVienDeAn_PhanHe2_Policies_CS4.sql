CONNECT COMPANY_PUBLIC/astrongpassword@localhost:1521/COMPANY;

SET SERVEROUTPUT ON

DECLARE
  v_service_name VARCHAR2(100);
BEGIN
  SELECT SYS_CONTEXT('USERENV', 'SERVICE_NAME') INTO v_service_name FROM DUAL;
  DBMS_OUTPUT.PUT_LINE('Service Name: ' || v_service_name);
END;
/


/* =========== CÀI ĐẶT CÁC CHÍNH SÁCH DÙNG VPD VÀ RBAC ============= */

-------------------------------------------------------------------------------------------------------------------------------------
/*
CS#4: Những người dùng có VAITRO là “Tài chính” cho biết đó là một nhân viên phụ trách công
tác tài chính tiền lương của công ty. Một người dùng có vai trò là “Tài chính” có quyền được mô tả
như sau:
− Có quyền như là một nhân viên thông thường (vai trò “Nhân viên”).
− Xem trên toàn bộ quan hệ NHANVIEN và PHANCONG, có thể sửa trên thuộc tính LUONG
và PHUCAP (thừa hành ban giám đốc).
*/
--tạo role TAI_CHINH
BEGIN
    EXECUTE IMMEDIATE 'CREATE ROLE TAI_CHINH';
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Role already exists');
END;
/
-------------------------------------------------------
--GRANT QUYỀN CHO ROLE TÀI CHÍNH (DÙNG RBAC)
--xem được trên cả 4 bảng
GRANT SELECT ON COMPANY_PUBLIC.NHANVIEN TO TAI_CHINH;
GRANT SELECT ON COMPANY_PUBLIC.PHONGBAN TO TAI_CHINH;
GRANT SELECT ON COMPANY_PUBLIC.PHANCONG TO TAI_CHINH;
GRANT SELECT ON COMPANY_PUBLIC.DEAN TO TAI_CHINH;

--GRANT UPDATE PRIV
GRANT UPDATE ON COMPANY_PUBLIC.NHANVIEN TO TAI_CHINH;
--CHECK LAI
/
-------------------------------------------------------
--TẠO POLICY (DÙNG VDP)
--function dùng cho TAICHINH có quyền update trên chính dòng của họ như Nhân viên
CREATE OR REPLACE FUNCTION a_CS_TAICHINH_1( 
P_SCHEMA IN VARCHAR2,
P_OBJ  IN VARCHAR2)
RETURN VARCHAR2
IS  
    ROL VARCHAR2(20);
    USR VARCHAR2(100);
    STR VARCHAR2(100);
BEGIN
    USR:= SYS_CONTEXT('USERENV', 'SESSION_USER');
    SELECT NV.VAITRO INTO ROL FROM COMPANY_PUBLIC.NHANVIEN NV WHERE MANV= SYS_CONTEXT('USERENV', 'SESSION_USER');
    
    IF ROL ='Tài chính' THEN
        IF P_OBJ = 'NHANVIEN' THEN
            STR := 'MANV= SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
            RETURN STR;
        END IF;
    END IF;
    RETURN NULL;
END;
/
--function cho policy TAICHINH có thể sửa LUONG và PHUCAP trên NHANVIEN của tất cả nhân viên
CREATE OR REPLACE FUNCTION a_CS_TAICHINH_2 (
P_SCHEMA IN VARCHAR2,
P_OBJ  IN VARCHAR2)
RETURN VARCHAR2
IS  
    ROL VARCHAR2(20);
    USR VARCHAR2(100);
BEGIN
    USR:= SYS_CONTEXT('USERENV', 'SESSION_USER');
    SELECT NV.VAITRO INTO ROL FROM COMPANY_PUBLIC.NHANVIEN NV WHERE MANV= USR;
    IF ROL ='Tài chính' THEN
        IF P_OBJ = 'NHANVIEN' THEN
            RETURN '1=1';
        END IF;
    END IF;
    RETURN NULL;
END;
/
-- *NOTE: đối với hàm chính sách, return NULL để có vô hiệu hóa điều kiện, để có thể gắn thêm các chính sách khác (các CS2 --> 6)

BEGIN
    --có quyền như NHAN_VIEN
        --QUYEN UPDATE trên NGAYSINH, DIACHI, SODT
    DBMS_RLS.ADD_POLICY(
        OBJECT_SCHEMA => 'COMPANY_PUBLIC',
        OBJECT_NAME => 'NHANVIEN',
        POLICY_NAME => 'TAICHINH_UPDATE_TAICHINH_PC',
        POLICY_FUNCTION => 'a_CS_TAICHINH_1',
        STATEMENT_TYPES => 'UPDATE',
        SEC_RELEVANT_COLS=> 'NGAYSINH, DIACHI, SODT',
        UPDATE_CHECK => TRUE, 
        ENABLE => TRUE
        );
END;
/
    -- quyền được update trên LUONG, PHUCAP của cả bảng NHANVIEN
BEGIN
     DBMS_RLS.ADD_POLICY(
        OBJECT_SCHEMA => 'COMPANY_PUBLIC',
        OBJECT_NAME => 'NHANVIEN',
        POLICY_NAME => 'TAICHINH_UPDATE_NHANVIEN_PC',
        POLICY_FUNCTION => 'a_CS_TAICHINH_2',
        STATEMENT_TYPES => 'UPDATE',
        SEC_RELEVANT_COLS=> 'LUONG, PHUCAP' ,
        UPDATE_CHECK => TRUE, 
        ENABLE => TRUE
        );
END;
/
-------------------------------------------------------------------------------------------------------------------------------------
