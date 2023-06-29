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

BEGIN
    EXECUTE IMMEDIATE 'CREATE ROLE NHAN_VIEN';
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Role already exists');
END;
/
-- Grant SELECT privilege on NHANVIEN and PHANCONG tables
GRANT SELECT ON COMPANY_PUBLIC.NHANVIEN TO NHAN_VIEN;
GRANT SELECT ON COMPANY_PUBLIC.PHANCONG TO NHAN_VIEN;
GRANT SELECT ON COMPANY_PUBLIC.VAITRO_NHANVIEN TO NHAN_VIEN;

-- Grant UPDATE privilege on NGAYSINH, DIACHI, and SODT columns of NHANVIEN table
GRANT UPDATE(NGAYSINH, DIACHI, SODT) ON COMPANY_PUBLIC.NHANVIEN TO NHAN_VIEN;

-- Grant SELECT privilege on PHONGBAN and DEAN tables
GRANT SELECT ON COMPANY_PUBLIC.PHONGBAN TO NHAN_VIEN;
GRANT SELECT ON COMPANY_PUBLIC.DEAN TO NHAN_VIEN;

CREATE OR REPLACE FUNCTION NHANVIEN_PERMISSION_CONSTRAINTS (
  schema_name   IN VARCHAR2,
  object_name   IN VARCHAR2
)
RETURN VARCHAR2
IS
  vaitro VARCHAR2(20);
BEGIN
  IF SYS_CONTEXT('USERENV', 'SESSION_USER') = 'COMPANY_PUBLIC' THEN
        RETURN '1=1';
  END IF;


  -- Lấy vai trò của user hiện tại
  SELECT VAITRO INTO vaitro FROM COMPANY_PUBLIC.VAITRO_NHANVIEN WHERE MANV = SYS_CONTEXT('USERENV', 'SESSION_USER');

  
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
      RETURN '1=1';
    END IF;
  END IF;

  RETURN '1=1';
END;
/

GRANT EXECUTE ON NHANVIEN_PERMISSION_CONSTRAINTS TO NHAN_VIEN;

BEGIN
  -- Có quyền xem tất cả các thuộc tính trên quan hệ NHANVIEN và PHANCONG
  DBMS_RLS.ADD_POLICY(
    object_schema    => 'COMPANY_PUBLIC',
    object_name      => 'NHANVIEN',
    policy_name      => 'CS1_NHANVIEN_SELECT_NHANVIEN_POLICY',
    policy_function  => 'NHANVIEN_PERMISSION_CONSTRAINTS',
    statement_types  => 'SELECT',
    enable           => TRUE
  );
  
  DBMS_RLS.ADD_POLICY(
    object_schema    => 'COMPANY_PUBLIC',
    object_name      => 'PHANCONG',
    policy_name      => 'CS1_NHANVIEN_SELECT_PHANCONG_POLICY',
    policy_function  => 'NHANVIEN_PERMISSION_CONSTRAINTS',
    statement_types  => 'SELECT',
    enable           => TRUE
  );
END;
/

BEGIN
  -- Có thể sửa trên các thuộc tính NGAYSINH, DIACHI, SODT liên quan đến chính nhân viên đó
  DBMS_RLS.ADD_POLICY(
    object_schema    => 'COMPANY_PUBLIC',
    object_name      => 'NHANVIEN',
    policy_name      => 'CS1_NHANVIEN_UPDATE_NHANVIEN_POLICY',
    policy_function  => 'NHANVIEN_PERMISSION_CONSTRAINTS',
    statement_types  => 'UPDATE',
    sec_relevant_cols => 'NGAYSINH, DIACHI, SODT',
    update_check     => true,
    enable           => TRUE
  );
END;
/

COMMIT;