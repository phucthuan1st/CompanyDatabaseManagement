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
BEGIN
    EXECUTE IMMEDIATE 'CREATE ROLE QL_TRUC_TIEP';
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Role already exists');
END;
/

-- Grant SELECT privilege on NHANVIEN and PHANCONG tables
GRANT SELECT ON COMPANY_PUBLIC.NHANVIEN TO QL_TRUC_TIEP;
GRANT SELECT ON COMPANY_PUBLIC.PHANCONG TO QL_TRUC_TIEP;
GRANT SELECT ON COMPANY_PUBLIC.VAITRO_NHANVIEN TO QL_TRUC_TIEP;

-- Grant UPDATE privilege on NGAYSINH, DIACHI, and SODT columns of NHANVIEN table
GRANT UPDATE(NGAYSINH, DIACHI, SODT) ON COMPANY_PUBLIC.NHANVIEN TO QL_TRUC_TIEP;

-- Grant SELECT privilege on PHONGBAN and DEAN tables
GRANT SELECT ON COMPANY_PUBLIC.PHONGBAN TO QL_TRUC_TIEP;
GRANT SELECT ON COMPANY_PUBLIC.DEAN TO QL_TRUC_TIEP;

-------------------------------------------------------------------------------------------------------------------------------------
-- chính sách trên dòng của nhân viên đó và các nhân viên mà người đó quản lí
CREATE OR REPLACE FUNCTION QLTT_PERMISSION_CONSTRAINTS (
    schema_name IN VARCHAR2,
    object_name IN VARCHAR2
) RETURN VARCHAR2 AS
    v_predicate VARCHAR2(4000);
    v_session_user VARCHAR2(100);
    v_vaitro VARCHAR2(20);
BEGIN
    -- Get the session user name
    v_session_user := SYS_CONTEXT('USERENV', 'SESSION_USER');
    
    -- Lay vai tro cua user
    SELECT VAITRO INTO v_vaitro FROM COMPANY_PUBLIC.VAITRO_NHANVIEN WHERE MANV = v_session_user;
    
    -- Kiểm tra vai trò của user
    IF schema_name = 'COMPANY_PUBLIC' AND v_vaitro = 'QL trực tiếp' THEN
        IF object_name = 'NHANVIEN' THEN
            -- Trả về ràng buộc MANV = user hoặc MANQL = user
            v_predicate := 'MANV = ''' || v_session_user || ''' OR MANQL = ''' || v_session_user || '''';
        ELSIF object_name = 'PHANCONG' THEN
            -- Trả về ràng buộc MANV = user hoặc MANV thuộc danh sách nhân viên do user quản lý trực tiếp
            v_predicate := 'MANV = ''' || v_session_user || ''' OR MANV IN (SELECT MANV FROM COMPANY_PUBLIC.NHANVIEN WHERE MANQL = ''' || v_session_user || ''')';
        ELSIF object_name = 'PHONGBAN' OR object_name = 'DEAN' THEN
            -- Không áp dụng ràng buộc cho PHONGBAN và DEAN
            v_predicate := '1 = 1';
        ELSE
            -- Không áp dụng ràng buộc cho các bảng khác
            v_predicate := '1=0';
        END IF;
    ELSE
        -- Không áp dụng ràng buộc cho các bảng khác
        v_predicate := NULL;
    END IF;
    
    -- Trả về ràng buộc (predicate) được sinh ra
    RETURN v_predicate;
END;
/

-- chính sách trên dòng của nhân viên đó
CREATE OR REPLACE FUNCTION QLTT_SELF_PERMISSION_CONSTRAINTS (
    schema_name IN VARCHAR2,
    object_name IN VARCHAR2
) RETURN VARCHAR2 AS
    v_predicate    VARCHAR2(4000);
    v_session_user VARCHAR2(100);
    v_vaitro       VARCHAR2(20);
BEGIN
    -- Get the session user name
    v_session_user := SYS_CONTEXT('USERENV', 'SESSION_USER');
    
    -- Lay vai tro cua user
    SELECT VAITRO INTO v_vaitro FROM COMPANY_PUBLIC.VAITRO_NHANVIEN WHERE MANV = v_session_user;
    
    -- Kiểm tra vai trò của user
    IF schema_name = 'COMPANY_PUBLIC' AND v_vaitro = 'QL trực tiếp' THEN
        IF object_name = 'NHANVIEN' THEN
            -- Trả về ràng buộc MANV = user
            v_predicate := 'MANV = ''' || v_session_user || '''';
        ELSE
            -- Không áp dụng ràng buộc cho các bảng khác
            v_predicate := '1 = 1';
        END IF;
    ELSE
        -- Không áp dụng ràng buộc cho các bảng khác
        v_predicate := NULL;
    END IF;
    
    -- Trả về ràng buộc (predicate) được sinh ra
    RETURN v_predicate;
END;
/

-- truy cap cac ham muc tieu
GRANT EXECUTE ON QLTT_PERMISSION_CONSTRAINTS TO QL_TRUC_TIEP;
GRANT EXECUTE ON QLTT_SELF_PERMISSION_CONSTRAINTS TO QL_TRUC_TIEP;

-----------------------------------------------------------------------------------------------------------------------------

-- được xem dòng của nhân viên đó và các nhân viên mà nhân viên đó quản lí
BEGIN
        dbms_rls.add_policy(
           object_schema   => 'COMPANY_PUBLIC',
           object_name     => 'NHANVIEN',
           policy_name     => 'CS2_QL_SELECT_NHANVIEN_POLICY',
           policy_function => 'QLTT_PERMISSION_CONSTRAINTS',
           statement_types => 'SELECT',
           enable          => true
        );
END;
/

-- chỉ được xem lương và phụ cấp của bản thân
-- masking cột LUONG và PHUCAP
BEGIN
        dbms_rls.add_policy(
           object_schema   => 'COMPANY_PUBLIC',
           object_name     => 'NHANVIEN',
           policy_name     => 'CS2_QL_SELECT_SELF_NHANVIEN_POLICY',
           policy_function => 'QLTT_SELF_PERMISSION_CONSTRAINTS',
           statement_types => 'SELECT',
           sec_relevant_cols => 'LUONG, PHUCAP',
           sec_relevant_cols_opt => DBMS_RLS.ALL_ROWS,
           enable          => true
        );
END;
/

-- Có thể xem tất cả trên quan hệ phân công của chính nhân viên đó
-- và các nhân viên mà nhân viên đó quản lí
BEGIN
        dbms_rls.add_policy(
                   object_schema   => 'COMPANY_PUBLIC',
                   object_name     => 'PHANCONG',
                   policy_name     => 'CS2_QL_SELECT_PHANCONG_POLICY',
                   policy_function => 'QLTT_PERMISSION_CONSTRAINTS',
                   statement_types => 'SELECT',
                   enable          => true
        );
END;
/

-- Có thể sửa trên các thuộc tính NGAYSINH, DIACHI, SODT liên quan đến chính nhân viên đó
BEGIN
        dbms_rls.add_policy(
                   object_schema     => 'COMPANY_PUBLIC',
                   object_name       => 'NHANVIEN',
                   policy_name       => 'CS2_QL_UPDATE_NHANVIEN_POLICY',
                   policy_function   => 'QLTT_UPDATE_SELF_PERMISSION_CONSTRAINTS',
                   statement_types   => 'UPDATE',
                   sec_relevant_cols => 'NGAYSINH, DIACHI, SODT',
                   update_check      => true,
                   enable            => true
        );
END;
/

COMMIT;