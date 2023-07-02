CONNECT COMPANY_PUBLIC/astrongpassword@localhost:1521/COMPANY;

SET SERVEROUTPUT ON

DECLARE
  v_service_name VARCHAR2(100);
BEGIN
  SELECT SYS_CONTEXT('USERENV', 'SERVICE_NAME') INTO v_service_name FROM DUAL;
  DBMS_OUTPUT.PUT_LINE('Service Name: ' || v_service_name);
END;
/

-------------------------------------------------------------------------------------------------------------------------------------
---------------------------------------------OLS----------------------------------------
--Tạo chính sách
BEGIN
    SA_SYSDBA.CREATE_POLICY (
        policy_name => 'OLS_POLICY'
        column_name => 'OLS_COLUMN'
    );
END;
/
----------------------------------------
--GÁN QUYỀN
GRANT OLS_POLICY_DBA TO COMPANY_PUBLIC;
GRANT EXECUTE ON SA_COMPONENTS TO COMPANY_PUBLIC;
GRANT EXECUTE ON SA_LABEL_ADMIN TO COMPANY_PUBLIC;
GRANT EXECUTE ON SA_POLICY_ADMIN TO COMPANY_PUBLIC;
GRANT EXECUTE ON SA_USER_ADMIN TO COMPANY_PUBLIC;
GRANT EXECUTE ON CHAR_TO_LABEL TO COMPANY_PUBLIC;

--Tạo level theo 3 mức độ Giám đốc, Trường phòng và Nhân viên
BEGIN
    SA_COMPONENTS.CREATE_LEVEL (
        policy_name => 'OLS_POLICY'
        long_name => 'NHANVIEN'
        short_name =>'NV'
        level_num => 1000
    );
END;
/

BEGIN
    SA_COMPONENTS.CREATE_LEVEL (
        policy_name => 'OLS_POLICY'
        long_name => 'TRUONGPHONG'
        short_name =>'TP'
        level_num => 2000
    );
END;
/

BEGIN
    SA_COMPONENTS.CREATE_LEVEL (
        policy_name => 'OLS_POLICY'
        long_name => 'GIAMDOC'
        short_name =>'GD'
        level_num => 3000
    );
END;
/

--Tạo compartments theo 3 ngành nghề Mua bán, Sản xuẩ và Gia công
BEGIN
    SA_COMPONENTS.CREATE_COMPARTMENT(
        policy_name => 'OLS_POLICY'
        long_name => 'MUABAN'
        short_name => 'MB'
        comp_num => 100
    );
END;
/

BEGIN
    SA_COMPONENTS.CREATE_COMPARTMENT(
        policy_name => 'OLS_POLICY'
        long_name => 'SANXUAT'
        short_name => 'SX'
        comp_num => 200
    );
END;
/

BEGIN
    SA_COMPONENTS.CREATE_COMPARTMENT(
        policy_name => 'OLS_POLICY'
        long_name => 'GIACONG'
        short_name => 'GC'
        comp_num => 300
    );
END;
/
--Tạo group theo 3 miền Bắc, Trung, Nam là con của công ty chủ
BEGIN
    SA_COMPONENTS.CREATE_GROUP (
        policy_name => 'OLS_POLICY'
        long_name => 'CONGTY'
        short_name =>'CTY'
        group_num => 10
        parent_name => NULL
    );
END;
/

BEGIN
    SA_COMPONENTS.CREATE_GROUP (
        policy_name => 'OLS_POLICY'
        long_name => 'BAC'
        short_name =>'BA'
        group_num => 11
        parent_name => 'CTY'
    );
END;
/

BEGIN
    SA_COMPONENTS.CREATE_GROUP (
        policy_name => 'OLS_POLICY'
        long_name => 'TRUNG'
        short_name =>'TR'
        group_num => 12
        parent_name => 'CTY'
    );
END;
/

BEGIN
    SA_COMPONENTS.CREATE_GROUP (
        policy_name => 'OLS_POLICY'
        long_name => 'NAM'
        short_name =>'NA'
        group_num => 13
        parent_name => 'CTY'
    );
END;
/

COMMIT;
