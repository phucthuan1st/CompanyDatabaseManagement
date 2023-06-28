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
CS#6: Những người dùng có VAITRO là “Trưởng đề án” cho biết đó là nhân viên là trưởng các
đề án. Một người dùng là “Trưởng đề án” có quyền được mô tả như sau:
− Có quyền như là một nhân viên thông thường (vai trò “Nhân viên”).
− Được quyền thêm, xóa, cập nhật trên quan hệ ĐEAN.
*/

BEGIN
    EXECUTE IMMEDIATE 'CREATE ROLE TRUONG_DEAN';
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Role already exists');
END;
/
-------------------------------------------------------
--GRANT QUYỀN CHO ROLE TRƯỞNG ĐỀ ÁN (DÙNG RBAC)
GRANT SELECT, UPDATE                    ON COMPANY_PUBLIC.NHANVIEN TO TRUONG_DEAN;
GRANT SELECT                            ON COMPANY_PUBLIC.PHONGBAN TO TRUONG_DEAN;
GRANT SELECT                            ON COMPANY_PUBLIC.PHANCONG TO TRUONG_DEAN;
GRANT SELECT, INSERT, UPDATE, DELETE    ON COMPANY_PUBLIC.DEAN TO TRUONG_DEAN;

-----------------------------------------------------
---CẤP CHÍNH SÁCH VDP TRÊN 1 SỐ YÊU CẦU KAHSC
--function dùng cho TAICHINH có quyền update trên chính dòng của họ như Nhân viên
CREATE OR REPLACE FUNCTION a_CS_TRUONGDEAN_1( 
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
    
    IF ROL ='Trưởng đề án' THEN
        IF (P_OBJ = 'NHANVIEN' OR P_OBJ ='PHANCONG') THEN
            STR := 'MANV= SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
            RETURN STR;
        ELSIF (P_OBJ='DEAN') THEN
            STR:= 'TRUONGDEAN = SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
            RETURN STR;
        END IF;
    END IF;
    RETURN NULL;
END;
/
-- *NOTE: đối với hàm chính sách, return NULL để có vô hiệu hóa điều kiện, để có thể gắn thêm các chính sách khác (các CS2 --> 6)

BEGIN
-------------------------------------------------------------------
-- QUYỀN NHƯ NHÂN VIÊN
    -- có quyền xem thông tin của mình trong quan hệ NHANVIEN
        DBMS_RLS.ADD_POLICY(
            OBJECT_SCHEMA => 'COMPANY_PUBLIC',
            OBJECT_NAME => 'NHANVIEN',
            POLICY_NAME => 'TRUONGDEAN_SELECT_NHANVIEN_PC',
            POLICY_FUNCTION => 'a_CS_TRUONGDEAN_1',
            STATEMENT_TYPES => 'SELECT',
            UPDATE_CHECK => TRUE, 
            ENABLE => TRUE
            );
END;
/
BEGIN
     DBMS_RLS.ADD_POLICY(
            OBJECT_SCHEMA => 'COMPANY_PUBLIC',
            OBJECT_NAME => 'NHANVIEN',
            POLICY_NAME => 'TRUONGDEAN_UPDATE_TRUONGDEAN_PC',
            POLICY_FUNCTION => 'a_CS_TRUONGDEAN_1',
            STATEMENT_TYPES => 'UPDATE',
            UPDATE_CHECK => TRUE, 
            SEC_RELEVANT_COLS=> 'NGAYSINH, DIACHI, SODT', 
            ENABLE => TRUE
            );
END;
/
BEGIN            
      DBMS_RLS.ADD_POLICY(
            OBJECT_SCHEMA => 'COMPANY_PUBLIC',
            OBJECT_NAME => 'PHANCONG',
            POLICY_NAME => 'TRUONGDEAN_SELECT_PHANCONG_PC',
            POLICY_FUNCTION => 'a_CS_TRUONGDEAN_1',
            STATEMENT_TYPES => 'SELECT',
            UPDATE_CHECK => TRUE, 
            ENABLE => TRUE
            );      
END;
/
--------------------------------------------------------------------
-- trưởng đề án chỉ được xóa, cập nhật những đề án của chính đề án.
BEGIN
      DBMS_RLS.ADD_POLICY(
            OBJECT_SCHEMA => 'COMPANY_PUBLIC',
            OBJECT_NAME => 'DEAN',
            POLICY_NAME => 'TRUONGDEAN_UPDATE_DEL_DEAN_PC',
            POLICY_FUNCTION => 'a_CS_TRUONGDEAN_1',
            STATEMENT_TYPES => 'UPDATE, DELETE',
            UPDATE_CHECK => TRUE, 
            ENABLE => TRUE
            );    
END;
/            
--------------------------------------------------------------------------