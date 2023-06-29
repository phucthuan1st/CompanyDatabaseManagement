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
-- Tạo role TRUONG_PHONG
BEGIN
        EXECUTE IMMEDIATE 'CREATE ROLE TRUONG_PHONG';
EXCEPTION
        WHEN OTHERS THEN
                DBMS_OUTPUT.PUT_LINE('ROLE TRUONG_PHONG FOUND!!');
END;
/

-- Cấp quyền cho role TRUONG_PHONG trên bảng PHANCONG
GRANT SELECT, INSERT, UPDATE, DELETE ON COMPANY_PUBLIC.PHANCONG TO TRUONG_PHONG;
GRANT SELECT ON COMPANY_PUBLIC.VAITRO_NHANVIEN TO TRUONG_PHONG;

-- Cấp quyền cho role TRUONG_PHONG với vai trò giống "Nhân viên" trên bảng NHANVIEN
GRANT SELECT, UPDATE(NGAYSINH, DIACHI, SODT) ON COMPANY_PUBLIC.NHANVIEN TO TRUONG_PHONG;

-- Grant SELECT privilege on PHONGBAN and DEAN tables
GRANT SELECT ON COMPANY_PUBLIC.PHONGBAN TO TRUONG_PHONG;
GRANT SELECT ON COMPANY_PUBLIC.DEAN TO TRUONG_PHONG;
GRANT SELECT ON COMPANY_PUBLIC.NHANVIEN_PHONG TO TRUONG_PHONG;

/*
        Các hàm chính sách
*/
-- được xem dòng của nhân viên đó và các nhân viên mà trong phòng ban người đó quản lí
CREATE OR REPLACE FUNCTION TRUONGPHONG_PERMISSION_CONSTRAINTS (
    schema_name IN VARCHAR2,
    object_name IN VARCHAR2
) RETURN VARCHAR2 AS
    v_predicate VARCHAR2(4000);
    v_session_user VARCHAR2(100);
    v_vaitro VARCHAR2(20);
    v_phg VARCHAR2(10);
BEGIN
    -- Lấy tên người dùng của phiên làm việc
    v_session_user := SYS_CONTEXT('USERENV', 'SESSION_USER');
    
    IF v_session_user = 'COMPANY_PUBLIC' THEN
        RETURN '1=1';
    END IF;
    
    -- Lấy vai trò của người dùng
    SELECT VAITRO INTO v_vaitro FROM COMPANY_PUBLIC.VAITRO_NHANVIEN WHERE MANV = v_session_user;
    
    -- Kiểm tra vai trò của người dùng
    IF schema_name = 'COMPANY_PUBLIC' AND v_vaitro = 'Trưởng phòng' THEN
        -- Lấy thông tin phòng mà người này làm trưởng phòng
        SELECT MAPB INTO v_phg FROM COMPANY_PUBLIC.PHONGBAN WHERE TRPHG = v_session_user;
        
        IF object_name = 'NHANVIEN' THEN
            -- Trả về ràng buộc MANV = user hoặc PHG = phòng mà người này làm trưởng phòng
            v_predicate := 'MANV = ''' || v_session_user || ''' OR PHG = ''' || v_phg || '''';
        ELSIF object_name = 'PHANCONG' THEN
            -- Trả về ràng buộc MANV = user hoặc MANV thuộc danh sách nhân viên do người này làm trưởng phòng
            v_predicate := 'MANV = ''' || v_session_user || ''' OR MANV IN (SELECT MANV FROM COMPANY_PUBLIC.NHANVIEN_PHONG WHERE PHG = ''' || v_phg || ''')';
        ELSIF object_name = 'PHONGBAN' OR object_name = 'DEAN' THEN
            v_predicate := '1=1';
        END IF;
    ELSE
        -- Không áp dụng ràng buộc cho vai trò khác hoặc schema hoặc bảng khác
        v_predicate := '1=1';
    END IF;

    -- Trả về ràng buộc được sinh ra
    RETURN v_predicate;
END;
/

-- chỉ được xem dòng của nhân viên đó 
CREATE OR REPLACE FUNCTION TRUONGPHONG_SELF_PERMISSION_CONSTRAINTS (
    schema_name IN VARCHAR2,
    object_name IN VARCHAR2
) RETURN VARCHAR2 AS
    v_predicate    VARCHAR2(4000);
    v_session_user VARCHAR2(100);
    v_vaitro       VARCHAR2(20);
BEGIN
    -- Get the session user name
    v_session_user := SYS_CONTEXT('USERENV', 'SESSION_USER');
    
    IF v_session_user = 'COMPANY_PUBLIC' THEN
        RETURN '1=1';
    END IF;
    
    -- Lay vai tro cua user
    SELECT VAITRO INTO v_vaitro FROM COMPANY_PUBLIC.VAITRO_NHANVIEN WHERE MANV = v_session_user;
    
    -- Kiểm tra vai trò của user
    IF schema_name = 'COMPANY_PUBLIC' AND v_vaitro = 'Trưởng phòng' THEN
        IF object_name = 'NHANVIEN' THEN
            -- Trả về ràng buộc MANV = user
            v_predicate := 'MANV = ''' || v_session_user || '''';
        ELSE
            -- Không áp dụng ràng buộc cho các bảng khác
            v_predicate := '1 = 1';
        END IF;
    ELSE
        -- Không áp dụng ràng buộc cho các bảng khác
        v_predicate := '1=1';
    END IF;
    
    -- Trả về ràng buộc (predicate) được sinh ra
    RETURN v_predicate;
END;
/

-- truy cập đến các hàm chính sách
GRANT EXECUTE ON TRUONGPHONG_PERMISSION_CONSTRAINTS TO TRUONG_PHONG;
GRANT EXECUTE ON TRUONGPHONG_SELF_PERMISSION_CONSTRAINTS TO TRUONG_PHONG;
------------------------------------------------------------------------------------
-- *NOTE: đối với hàm chính sách, return NULL để có vô hiệu hóa điều kiện, để có thể gắn thêm các chính sách khác (các CS2 --> 6)
------------------------------------------------------------------------------------
-- Có thể xem dữ liệu của quan hệ NHANVIEN liên quan đến nhân viên đó 
-- và liên quan đến các nhân viên thuộc phòng ban mà T làm trưởng phòng
BEGIN
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'NHANVIEN',
        policy_name      => 'CS3_TRUONGPHONG_SELECT_NHANVIEN_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TRUONGPHONG_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );
END;
/

-- chỉ được xem lương và phụ cấp của bản thân
-- masking cột LUONG và PHUCAP
BEGIN
        dbms_rls.add_policy(
           object_schema   => 'COMPANY_PUBLIC',
           object_name     => 'NHANVIEN',
           policy_name     => 'CS3_TRUONGPHONG_SELECT_SELF_NHANVIEN_POLICY',
           policy_function => 'TRUONGPHONG_SELF_PERMISSION_CONSTRAINTS',
           statement_types => 'SELECT',
           sec_relevant_cols => 'LUONG, PHUCAP',
           sec_relevant_cols_opt => DBMS_RLS.ALL_ROWS,
           enable          => true
        );
END;
/

-- Có thể sửa trên các thuộc tính NGAYSINH, DIACHI, SODT liên quan đến chính nhân viên đó
BEGIN
        dbms_rls.add_policy(
                   object_schema     => 'COMPANY_PUBLIC',
                   object_name       => 'NHANVIEN',
                   policy_name       => 'CS3_TRUONGPHONG_UPDATE_NHANVIEN_POLICY',
                   policy_function   => 'TRUONGPHONG_SELF_PERMISSION_CONSTRAINTS',
                   statement_types   => 'UPDATE',
                   sec_relevant_cols => 'NGAYSINH, DIACHI, SODT',
                   enable            => true
        );
END;
/

-- Có thể xem dữ liệu của quan hệ PHANCONG liên quan đến nhân viên đó 
-- và liên quan đến các nhân viên thuộc phòng ban mà T làm trưởng phòng
BEGIN
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'PHANCONG',
        policy_name      => 'CS3_TRUONGPHONG_SELECT_PHANCONG_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TRUONGPHONG_PERMISSION_CONSTRAINTS',
        statement_types  => 'SELECT'
    );
END;
/

-- Có thể thêm,xoá,cập nhật trên quan hệ PHANCONG liên quan 
-- đến các nhân viên thuộc phòng ban mà T làm trưởng phòng.
BEGIN
    DBMS_RLS.ADD_POLICY(
        object_schema    => 'COMPANY_PUBLIC',
        object_name      => 'PHANCONG',
        policy_name      => 'CS3_TRUONGPHONG_MODIFY_PHANCONG_POLICY',
        function_schema  => 'COMPANY_PUBLIC',
        policy_function  => 'TRUONGPHONG_PERMISSION_CONSTRAINTS',
        statement_types  => 'INSERT,DELETE,UPDATE',
        update_check     => true,
        enable           => true
    );
END;
/

-------------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------------
COMMIT;