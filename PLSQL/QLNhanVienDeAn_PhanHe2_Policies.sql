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

/*
CS#4: Những người dùng có VAITRO là “Tài chính” cho biết đó là một nhân viên phụ trách công
tác tài chính tiền lương của công ty. Một người dùng có vai trò là “Tài chính” có quyền được mô tả
như sau:
− Có quyền như là một nhân viên thông thường (vai trò “Nhân viên”).
− Xem trên toàn bộ quan hệ NHANVIEN và PHANCONG, có thể sửa trên thuộc tính LUONG
và PHUCAP (thừa hành ban giám đốc).
*/
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
-------------------------------------------------------------------------------------------------------------------------------------

/*
CS#6: Những người dùng có VAITRO là “Trưởng đề án” cho biết đó là nhân viên là trưởng các
đề án. Một người dùng là “Trưởng đề án” có quyền được mô tả như sau:
− Có quyền như là một nhân viên thông thường (vai trò “Nhân viên”).
− Được quyền thêm, xóa, cập nhật trên quan hệ ĐEAN.
*/