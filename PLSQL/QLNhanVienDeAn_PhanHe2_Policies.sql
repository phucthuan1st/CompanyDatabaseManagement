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
 * CS#1: Những người dùng có thuộc tính VAITRO là “Nhân viên” cho biết đó là một nhân viên thông thường, không kiêm nhiệm công việc nào khác. 
Những người dùng có VAITRO là “Nhân viên” có quyền được mô tả như sau:
− Có quyền xem tất cả các thuộc tính trên quan hệ NHANVIEN và PHANCONG liên quan đến chính nhân viên đó.
− Có thể sửa trên các thuộc tính NGAYSINH, DIACHI, SODT liên quan đến chính nhân viên đó.
− Có thể xem dữ liệu của toàn bộ quan hệ PHONGBAN và DEAN.
 */

-- 1. Quyền SELECT
CREATE OR REPLACE FUNCTION NHANVIEN_PERMISSION_CONSTRAINTS (
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
        IF vaitro = 'Nhân viên' THEN
             RETURN 'MANV = SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
        END IF;
    
    -- Truy cập dòng liên quan đến nhân viên đó
    ELSIF object_name = 'PHANCONG' THEN
        IF vaitro = 'Nhân viên' THEN
             RETURN 'MANV = SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
        END IF;
    
    -- tùy ý truy cập phòng ban và đề án
    ELSIF object_name = 'PHONGBAN' or object_name = 'DEAN' THEN
        IF vaitro = 'Nhân viên' THEN
            RETURN '1=1'
        END IF;
    END IF;
  
  RETURN NULL;
END;
/

-- *NOTE: đối với hàm chính sách, return NULL để có vô hiệu hóa điều kiện, để có thể gắn thêm các chính sách khác (các CS2 --> 6)

BEGIN
    -- Có quyền xem tất cả các thuộc tính trên quan hệ NHANVIEN và PHANCONG
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'NHANVIEN',
        policy_name      => 'NHANVIEN_SELECT_NHANVIEN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'NHANVIEN_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );
    
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'PHANCONG',
        policy_name      => 'NHANVIEN_SELECT_PHANCONG_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'NHANVIEN_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );
    
    -- Có thể xem dữ liệu của toàn bộ quan hệ PHONGBAN và DEAN.
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'PHONGBAN',
        policy_name      => 'NHANVIEN_SELECT_PHONGBAN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'NHANVIEN_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );
    
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'DEAN',
        policy_name      => 'NHANVIEN_SELECT_DEAN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'NHANVIEN_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );
    ----------------------------------------------------------------------------------------
    -- Có thể sửa trên các thuộc tính NGAYSINH, DIACHI, SODT liên quan đến chính nhân viên đó.
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'NHANVIEN',
        policy_name      => 'NHANVIEN_UPDATE_NHANVIEN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'NHANVIEN_PERMISSION_CONSTRAINTS',
        statement_types  => 'UPDATE',
        sec_relevant_cols => 'NGAYSINH, DIACHI, SODT'
    );
END;
/
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
            RETURN '1=1'
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
            RETURN '1=1'
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

/*
CS#5: Những người dùng có VAITRO là “Nhân sự” cho biết đó là nhân viên phụ trách công tác
nhân sự trong công ty. Một người dùng có VAITRO là “Nhân sự” có quyền được mô tả như sau:
− Có quyền như là một nhân viên thông thường (vai trò “Nhân viên”).
− Được quyền thêm, cập nhật trên quan hệ PHONGBAN.
− Thêm, cập nhật dữ liệu trong quan hệ NHANVIEN với giá trị các trường LUONG, PHUCAP
là mang giá trị mặc định là NULL, không được xem LUONG, PHUCAP của người khác và
không được cập nhật trên các trường LUONG, PHUCAP.
*/
-- 1. Quyền SELECT
CREATE OR REPLACE FUNCTION NHANSU_PERMISSION_CONSTRAINTS (
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
        IF vaitro = 'Nhân sự' THEN
             RETURN 'MANV = SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
        END IF;
    
    -- Truy cập dòng liên quan đến nhân viên đó
    ELSIF object_name = 'PHANCONG' THEN
        IF vaitro = 'Nhân sự' THEN
             RETURN 'MANV = SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
        END IF;
    
    -- tùy ý truy cập phòng ban và đề án
    ELSIF object_name = 'PHONGBAN' or object_name = 'DEAN' THEN
        IF vaitro = 'Nhân sự' THEN
            RETURN '1=1'
        END IF;
    END IF;
  
  RETURN NULL;
END;
/

-- *NOTE: đối với hàm chính sách, return NULL để có vô hiệu hóa điều kiện, để có thể gắn thêm các chính sách khác (các CS2 --> 6)

BEGIN
    -- Có quyền xem trên quan hệ NHANVIEN của chính nhân viên đó
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'NHANVIEN',
        policy_name      => 'NHANSU_SELECT_NHANVIEN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'NHANSU_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );
    --Có quyền thêm, chỉnh sửa tất cả các thuộc tính trừ LUONG, PHUCAP trên quan hệ NHANVIEN 
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'NHANVIEN',
        policy_name      => 'NHANSU_INSERT_UPDATE_NHANVIEN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'NHANSU_PERMISSION_CONSTRAINTS',
        statement_types  => 'INSERT,UPDATE'
        sec_relevant_cols => 'MANV,TENNV,PHAI,NGAYSINH,DIACHI,SDT,VAITRO,MANQL,PHG'
    );
    
    -- Có quyền xem tất cả các thuộc tính trên quan hệ PHANCONG của chính nhân viên đó
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'PHANCONG',
        policy_name      => 'NHANSU_SELECT_PHANCONG_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'NHANSU_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );
    
    -- Có thể xem, thêm, cập nhật dữ liệu của toàn bộ quan hệ PHONGBAN
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'PHONGBAN',
        policy_name      => 'NHANSU_SELECT_PHONGBAN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'NHANSU_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT,INSERT,UPDATE'
    );
    
    --Có thể xem dữ liệu của toàn bộ quan hệ DEAN
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'DEAN',
        policy_name      => 'NHANSU_SELECT_DEAN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'NHANSU_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );
    ----------------------------------------------------------------------------------------
    -- Có thể sửa trên các thuộc tính NGAYSINH, DIACHI, SODT liên quan đến chính nhân viên đó.
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'NHANVIEN',
        policy_name      => 'NHANVIEN_UPDATE_NHANVIEN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'NHANVIEN_PERMISSION_CONSTRAINTS',
        statement_types  => 'UPDATE',
        sec_relevant_cols => 'NGAYSINH, DIACHI, SODT'
    );
END;
/
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
            RETURN '1=1'
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
