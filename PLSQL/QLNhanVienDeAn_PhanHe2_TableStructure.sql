CONNECT COMPANY_PUBLIC/astrongpassword@localhost:1521/COMPANY;

/*
    Check connection availability
*/
SET SERVEROUTPUT ON

DECLARE
  v_service_name VARCHAR2(100);
BEGIN
  SELECT SYS_CONTEXT('USERENV', 'SERVICE_NAME') INTO v_service_name FROM DUAL;
  DBMS_OUTPUT.PUT_LINE('Service Name: ' || v_service_name);
END;
/

/*
    Create database's table
*/
CREATE TABLE NHANVIEN (
    MANV     VARCHAR2(10),
    TENNV     VARCHAR2(50),
    PHAI      VARCHAR2(10),
    NGAYSINH  DATE,
    DIACHI    VARCHAR2(100),
    SODT      VARCHAR2(20),
    LUONG     VARCHAR2(100),
    PHUCAP    VARCHAR2(100),
    VAITRO    VARCHAR2(20),
    MANQL     VARCHAR2(10),
    PHG       VARCHAR2(10),
    CONSTRAINT PK_NHANVIEN PRIMARY KEY (MANV)
);

CREATE TABLE PHONGBAN (
    MAPB   VARCHAR2(10),
    TENPB  VARCHAR2(100),
    TRPHG  VARCHAR2(10),
    CONSTRAINT PK_PHONGBAN PRIMARY KEY (MAPB)
);

CREATE TABLE DEAN (
    MADA    VARCHAR2(10),
    TENDA   VARCHAR2(100),
    NGAYBD  DATE,
    PHONG   VARCHAR2(10),
    CONSTRAINT PK_DEAN PRIMARY KEY (MADA)
);

CREATE TABLE PHANCONG (
    MANV      VARCHAR2(10),
    MADA      VARCHAR2(10),
    THOIGIAN  DATE,
    CONSTRAINT PK_PHANCONG PRIMARY KEY (MANV, MADA)
);

-- ten DN la MANV, khoa Bi Mat dung de ma hoa LUONG va PHUCAP
-- Create the DANGNHAP table
CREATE TABLE LUUTRU (
      MANV VARCHAR2(10),
      SALT VARCHAR2(100),
      SECRET_KEY VARCHAR2(100),
      CONSTRAINT PK_DANGNHAP PRIMARY KEY ( MANV )
);

SELECT * FROM NHANVIEN;
-- -----------------------------------------------------------------------------------------------------
SELECT * FROM PHANCONG;
-- -----------------------------------------------------------------------------------------------------
SELECT * FROM PHONGBAN;
-- -----------------------------------------------------------------------------------------------------
SELECT * FROM DEAN;
-- -----------------------------------------------------------------------------------------------------
SELECT * FROM LUUTRU;
-- -----------------------------------------------------------------------------------------------------

COMMIT;