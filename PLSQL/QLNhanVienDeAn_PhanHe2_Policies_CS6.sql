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
CS#6: Những người dùng có VAITRO là “Trưởng đề án” cho biết đó là nhân viên là trưởng các
đề án. Một người dùng là “Trưởng đề án” có quyền được mô tả như sau:
− Có quyền như là một nhân viên thông thường (vai trò “Nhân viên”).
− Được quyền thêm, xóa, cập nhật trên quan hệ ĐEAN.
*/
CREATE OR REPLACE FUNCTION TRUONGDEAN_PERMISSION_CONSTRAINTS (
  schema_name   IN VARCHAR2,
  object_name   IN VARCHAR2
)
RETURN VARCHAR2
IS
  vaitro NVARCHAR2(20);
BEGIN
        -- Lấy vai trò của user hiện tại
    SELECT VAITRO INTO vaitro FROM COMPANY_PUBLIC.NHANVIEN WHERE MANV = SYS_CONTEXT('USERENV', 'SESSION_USER');

      -- Truy cập dòng liên quan đến nhân viên đó
    IF object_name = 'NHANVIEN' THEN
        IF vaitro = 'Trưởng đề án' THEN
             RETURN 'MANV = SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
        END IF;
    
    -- Truy cập dòng liên quan đến nhân viên đó
    ELSIF object_name = 'PHANCONG' THEN
        IF vaitro = 'Trưởng đề án' THEN
             RETURN 'MANV = SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
        END IF;
    
    -- tùy ý truy cập phòng ban và đề án
    ELSIF object_name = 'PHONGBAN' or object_name = 'DEAN' THEN
        IF vaitro = 'Trưởng đề án' THEN
            RETURN '1=1';
        END IF;
    END IF;
  
  RETURN NULL;
END;
/
-- *NOTE: đối với hàm chính sách, return NULL để có vô hiệu hóa điều kiện, để có thể gắn thêm các chính sách khác (các CS2 --> 6)

BEGIN
    -- Có thê xem dữ liệu của toàn bộ quan hệ PHONGBAN và DEAN như nhân viên.
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'PHONGBAN',
        policy_name      => 'TRUONGDEAN_SELECT_PHONGBAN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TRUONGDEAN_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );

    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'DEAN',
        policy_name      => 'TRUONGDEAN_SELECT_DEAN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TRUONGDEAN_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );
    
    -- Có thể xem dữ liệu của toàn bô quan hệ NHANVIEN và PHANCONG liên quan đến nhân viên đó
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'NHANVIEN',
        policy_name      => 'TRUONGDEAN_SELECT_NHANVIEN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TRUONGDEAN_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );

    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'PHANCONG',
        policy_name      => 'TRUONGDEAN_SELECT_PHANCONG_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TRUONGDEAN_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );

    -- Có thể thêm,xoá,cập nhật trên quan hệ DEAN
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'DEAN',
        policy_name      => 'TRUONGDEAN_INSERT_DELETE_UPDATE_DEAN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TRUONGDEAN_PERMISSION_CONSTRAINTS',
        statement_types  => 'INSERT,DELETE,UPDATE',
    );
END;
/
-------------------------------------------------------------------------------------------------------------------------------------
