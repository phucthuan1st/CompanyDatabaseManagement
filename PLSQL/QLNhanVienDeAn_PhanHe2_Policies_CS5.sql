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
CS#5: Những người dùng có VAITRO là “Nhân sự” cho biết đó là nhân viên phụ trách công tác
nhân sự trong công ty. Một người dùng có VAITRO là “Nhân sự” có quyền được mô tả như sau:
− Có quyền như là một nhân viên thông thường (vai trò “Nhân viên”).
− Được quyền thêm, cập nhật trên quan hệ PHONGBAN.
− Thêm, cập nhật dữ liệu trong quan hệ NHANVIEN với giá trị các trường LUONG, PHUCAP
là mang giá trị mặc định là NULL, không được xem LUONG, PHUCAP của người khác và
không được cập nhật trên các trường LUONG, PHUCAP.
*/

--tạo role NHAN_SU
BEGIN
    EXECUTE IMMEDIATE 'CREATE ROLE NHAN_SU';
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Role already exists');
END;
/
--GRANT QUYỀN CHO ROLE NHAN_SU (DÙNG RBAC)
GRANT SELECT, INSERT, UPDATE    ON COMPANY_PUBLIC.NHANVIEN TO NHAN_SU;
GRANT SELECT, INSERT, UPDATE    ON COMPANY_PUBLIC.PHONGBAN TO NHAN_SU;
GRANT SELECT                    ON COMPANY_PUBLIC.DEAN TO NHAN_SU;
GRANT SELECT                    ON COMPANY_PUBLIC.PHANCONG TO NHAN_SU;

------------------------------------------
--KẾT HỢP VỚI VDP

CREATE OR REPLACE FUNCTION a_CS_NHANSU_1( 
P_SCHEMA IN VARCHAR2,
P_OBJ  IN VARCHAR2)
RETURN VARCHAR2
IS  
    ROL VARCHAR2(20);
    USR VARCHAR2(100);
    STR VARCHAR2(100);
BEGIN
    USR:= SYS_CONTEXT('USERENV', 'SESSION_USER');
    SELECT NV.VAITRO INTO ROL FROM COMPANY_PUBLIC.VAITRO_NHANVIEN NV WHERE MANV= USR;
    
    IF ROL ='Nhân sự' THEN
        IF (P_OBJ = 'NHANVIEN' OR P_OBJ = 'PHANCONG') THEN
            STR := 'MANV = SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
            RETURN STR;
        END IF;
    END IF;
    RETURN NULL;
END;
/

CREATE OR REPLACE FUNCTION a_CS_NHANSU_2( 
P_SCHEMA IN VARCHAR2,
P_OBJ  IN VARCHAR2)
RETURN VARCHAR2
IS  
    ROL VARCHAR2(20);
    USR VARCHAR2(100);
    STR VARCHAR2(100);
    SAL VARCHAR2(100);
    ALL VARCHAR2(100);
BEGIN
    USR:= SYS_CONTEXT('USERENV', 'SESSION_USER');
    SELECT NV.VAITRO INTO ROL FROM COMPANY_PUBLIC.VAITRO_NHANVIEN NV WHERE MANV= USR;
    SELECT NV.LUONG INTO SAL FROM COMPANY_PUBLIC.VAITRO_NHANVIEN NV WHERE MANV= USR;
    SELECT NV.PHUCAP INTO ALL FROM COMPANY_PUBLIC.VAITRO_NHANVIEN NV WHERE MANV= USR;
    
    IF ROL ='Nhân sự' THEN
        IF P_OBJ = 'NHANVIEN' and SAL='NULL' and ALL= 'NULL' THEN
            STR := 'MANV = SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
            RETURN STR;
        END IF;
    END IF;
    RETURN NULL;
END;
/

/* CREATE OR REPLACE FUNCTION a_CS_NHANSU_3( 
P_SCHEMA IN VARCHAR2,
P_OBJ  IN VARCHAR2)
RETURN VARCHAR2
IS  
    ROL VARCHAR2(20);
    USR VARCHAR2(100);
    STR VARCHAR2(100);
BEGIN
    USR:= SYS_CONTEXT('USERENV', 'SESSION_USER');
    SELECT NV.VAITRO INTO ROL FROM COMPANY_PUBLIC.VAITRO_NHANVIEN NV WHERE MANV= USR;
    
    IF ROL ='Nhân sự' THEN
        IF P_OBJ = 'NHANVIEN' THEN
            STR := 'PHUCAP = NULL';
            RETURN STR;
        END IF;
    END IF;
    RETURN NULL;
END; */
/

-- *NOTE: đối với hàm chính sách, return NULL để có vô hiệu hóa điều kiện, để có thể gắn thêm các chính sách khác (các CS2 --> 6)

BEGIN
    --quyền được sửa trên NGAYSINH, DIACHI, SODT dòng của chính mình trong bảng NHANVIEN
    DBMS_RLS.ADD_POLICY(
        OBJECT_SCHEMA => 'COMPANY_PUBLIC',
        OBJECT_NAME => 'NHANVIEN',
        POLICY_NAME => 'NHANSU_UPDATE_NHANSU_PC',
        POLICY_FUNCTION => 'a_CS_NHANSU_1',
        STATEMENT_TYPES => 'UPDATE',
        SEC_RELEVANT_COLS=> 'NGAYSINH, DIACHI, SODT',
        UPDATE_CHECK => TRUE, 
        ENABLE => TRUE
        );

    -- nhân sự có quyền xem trên bảng nhân viên, có thể xem lương và phụ cấp của chính nhân sự đó, nhưng không 
    --xem được lương và phụ cấp của người khác
    DBMS_RLS.ADD_POLICY(
            OBJECT_SCHEMA => 'COMPANY_PUBLIC',
            OBJECT_NAME => 'NHANVIEN',
            POLICY_NAME => 'NHANSU_SELECT_NHANVIEN_PC',
            POLICY_FUNCTION => 'a_CS_NHANSU_1',
            STATEMENT_TYPES => 'SELECT',
            SEC_RELEVANT_COLS=> 'LUONG, PHUCAP' ,
            UPDATE_CHECK => TRUE, 
            ENABLE => TRUE
            );
    --Xem phân công của chính mình trên bảng PHANCONG như CS1
     DBMS_RLS.ADD_POLICY(
            OBJECT_SCHEMA => 'COMPANY_PUBLIC',
            OBJECT_NAME => 'PHANCONG',
            POLICY_NAME => 'NHANSU_SELECT_PHANCONG_PC',
            POLICY_FUNCTION => 'a_CS_NHANSU_1',
            STATEMENT_TYPES => 'SELECT',
            UPDATE_CHECK => TRUE, 
            ENABLE => TRUE
            );
    --update trên bảng NHANVIEN các thuộc tính MANV, TENNV, PHAI, NGAYSINH, DIACHI, SDT, MANQL, PHG các thuộc tính nếu LUONG = NULL
 
     DBMS_RLS.ADD_POLICY(
        OBJECT_SCHEMA => 'COMPANY_PUBLIC',
        OBJECT_NAME => 'NHANVIEN',
        POLICY_NAME => 'NHANSU_UPDATE_NHANVIEN_PC',
        POLICY_FUNCTION => 'a_CS_NHANSU_2',
        STATEMENT_TYPES => 'UPDATE, INSERT',
        SEC_RELEVANT_COLS=> 'MANV, TENNV, PHAI, NGAYSINH, DIACHI, SDT, MANQL, PHG' ,
        UPDATE_CHECK => TRUE, 
        ENABLE => TRUE
        );

END;
/
-------------------------------------------------------------------------------------------------------------------------------------

