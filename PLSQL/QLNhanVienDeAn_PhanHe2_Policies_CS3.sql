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
CS#3: Những người dùng có VAITRO là “Trưởng phòng” cho biết đó là một nhân viên kiêm
nhiệm thêm vai trò trưởng phòng. Một người dùng T có VAITRO là “Trưởng phòng” có quyền
được mô tả như sau:
− T có quyền như là một nhân viên thông thường (vai trò “Nhân viên”). Ngoài ra, với các dòng
trong quan hệ NHANVIEN liên quan đến các nhân viên thuộc phòng ban mà T làm trưởng
phòng thì T có quyền xem tất cả các thuộc tính, trừ thuộc tính LUONG và PHUCAP.
− Có thể thêm, xóa, cập nhật trên quan hệ PHANCONG liên quan đến các nhân viên thuộc
phòng ban mà T làm trưởng phòng.
*/
-------------------------------------------------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION TRUONGPHONG_PERMISSION_CONSTRAINTS (
  schema_name   IN VARCHAR2,
  object_name   IN VARCHAR2
)
RETURN VARCHAR2
IS
  vaitro NVARCHAR2(20);
BEGIN
    -- Lấy vai trò của user hiện tại
    SELECT VAITRO INTO vaitro FROM COMPANY_PUBLIC.NHANVIEN WHERE MANV = SYS_CONTEXT('USERENV', 'SESSION_USER');

    -- Truy cập dòng liên quan đến nhân viên đó và liên quan đến các nhân viên thuộc phòng ban mà T làm trưởng phòng
    IF object_name = 'NHANVIEN' THEN
        IF vaitro = 'Trưởng phòng' THEN
            RETURN 'MANV = SYS_CONTEXT(''USERENV'', ''SESSION_USER'') OR (MANV IN (SELECT MANV FROM COMPANY_PUBLIC.NHANVIEN WHERE PHG = (SELECT PHG FROM COMPANY_PUBLIC.NHANVIEN WHERE MANV = SYS_CONTEXT(''USERENV'', ''SESSION_USER''))) AND COLUMN_NAME NOT IN (''LUONG'', ''PHUCAP''))';
        END IF;
    
    -- Truy cập dòng liên quan đến nhân viên đó và liên quan đến các nhân viên thuộc phòng ban mà T làm trưởng phòng.
    ELSIF object_name = 'PHANCONG' THEN
        IF vaitro = 'Trưởng phòng' THEN
            RETURN 'MANV = SYS_CONTEXT(''USERENV'', ''SESSION_USER'') OR (MANV IN (SELECT MANV FROM COMPANY_PUBLIC.NHANVIEN WHERE PHG = (SELECT PHG FROM COMPANY_PUBLIC.NHANVIEN WHERE MANV = SYS_CONTEXT(''USERENV'', ''SESSION_USER''))))';
        END IF;
    
    -- tùy ý truy cập phòng ban và đề án
    ELSIF object_name = 'PHONGBAN' or object_name = 'DEAN' THEN
        IF vaitro = 'Trưởng phòng' THEN
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
        policy_name      => 'TRUONGPHONG_SELECT_PHONGBAN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TRUONGPHONG_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );

    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'DEAN',
        policy_name      => 'TRUONGPHONG_SELECT_DEAN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TRUONGPHONG_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );

    -- Có thể xem dữ liệu của quan hệ NHANVIEN và PHANCONG liên quan đến nhân viên đó và liên quan đến các nhân viên thuộc phòng ban mà T làm trưởng phòng
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'NHANVIEN',
        policy_name      => 'TRUONGPHONG_SELECT_NHANVIEN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TRUONGPHONG_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );

    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'PHANCONG',
        policy_name      => 'TRUONGPHONG_SELECT_PHANCONG_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TRUONGPHONG_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );
END;
/

BEGIN
    -- Có thể thêm,xoá,cập nhật trên quan hệ PHANCONG liên quan đến các nhân viên thuộc phòng ban mà T làm trưởng phòng.
    DBMS_RLS.ADD_POLICY(
    object_schema    => 'COMPANY_PUBLIC',
    object_name      => 'PHANCONG',
    policy_name      => 'TRUONGPHONG_PHANCONG_POLICY',
    function_schema  => 'COMPANY_PUBLIC',
    policy_function  => 'TRUONGPHONG_PERMISSION_CONSTRAINTS',
    statement_types  => 'INSERT, DELETE, UPDATE'
  );
END;
/
-------------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------------
