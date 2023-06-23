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
CS#4: Những người dùng có VAITRO là “Tài chính” cho biết đó là một nhân viên phụ trách công
tác tài chính tiền lương của công ty. Một người dùng có vai trò là “Tài chính” có quyền được mô tả
như sau:
− Có quyền như là một nhân viên thông thường (vai trò “Nhân viên”).
− Xem trên toàn bộ quan hệ NHANVIEN và PHANCONG, có thể sửa trên thuộc tính LUONG
và PHUCAP (thừa hành ban giám đốc).
*/
-- 1. Quyền SELECT
CREATE OR REPLACE FUNCTION TAICHINH_PERMISSION_CONSTRAINTS (
  schema_name   IN VARCHAR2,
  object_name   IN VARCHAR2
)
RETURN VARCHAR2
IS
  vaitro NVARCHAR2(20);
BEGIN
        -- Lấy vai trò của user hiện tại
    SELECT VAITRO INTO vaitro FROM COMPANY_PUBLIC.NHANVIEN WHERE MANV = SYS_CONTEXT('USERENV', 'SESSION_USER');
    
    -- tùy ý truy cập phòng ban và đề án như nhân viên
    IF object_name = 'PHONGBAN' or object_name = 'DEAN' THEN
        IF vaitro = 'Tài chính' THEN
            RETURN '1=1'
        END IF;
    END IF;

    --tùy ý truy cập nhân viên, phân công
    ELSIF object_name = 'NHANVIEN' or object_name = 'PHANCONG' THEN
        IF vaitro = 'Tài chính' THEN
            RETURN '1=1';
        END IF;
    END IF;
  
  RETURN NULL;
END;
/

-- *NOTE: đối với hàm chính sách, return NULL để có vô hiệu hóa điều kiện, để có thể gắn thêm các chính sách khác (các CS2 --> 6)

BEGIN
    -- Có thể xem dữ liệu của toàn bộ quan hệ PHONGBAN và DEAN như nhân viên.
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'PHONGBAN',
        policy_name      => 'TAICHINH_SELECT_PHONGBAN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TAICHINH_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );
    
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'DEAN',
        policy_name      => 'TAICHINH_SELECT_DEAN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TAICHINH_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );

    ---------------------------------------------------------------------------------------------------------
    -- Có thể xem dữ liệu của toàn bộ quan hệ NHANVIEN và PHANCONG.
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'NHANVIEN',
        policy_name      => 'TAICHINH_SELECT_NHANVIEN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TAICHINH_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );
    
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'PHANCONG',
        policy_name      => 'TAICHINH_SELECT_PHANCONG_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TAICHINH_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );
    
    ----------------------------------------------------------------------------------------
    -- Có thể sửa trên các thuộc tính LUONG, PHUCAP.
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'NHANVIEN',
        policy_name      => 'TAICHINH_UPDATE_NHANVIEN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TAICHINH_PERMISSION_CONSTRAINTS',
        statement_types  => 'UPDATE',
        sec_relevant_cols => 'LUONG,PHUCAP'
    );
END;
/
-------------------------------------------------------------------------------------------------------------------------------------
