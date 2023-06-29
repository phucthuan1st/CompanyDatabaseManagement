CONNECT COMPANY_PUBLIC/astrongpassword@localhost:1521/COMPANY;
SET SERVEROUTPUT ON;

GRANT SELECT, UPDATE(SECRET_KEY) ON LUUTRU TO NHAN_VIEN;
GRANT SELECT, UPDATE(SECRET_KEY) ON LUUTRU TO QL_TRUC_TIEP;
GRANT SELECT, UPDATE(SECRET_KEY) ON LUUTRU TO TRUONG_PHONG;
GRANT SELECT, UPDATE(SECRET_KEY) ON LUUTRU TO TAI_CHINH;
GRANT SELECT, UPDATE(SECRET_KEY) ON LUUTRU TO NHAN_SU;
GRANT SELECT, UPDATE(SECRET_KEY) ON LUUTRU TO TRUONG_DEAN;

CREATE OR REPLACE FUNCTION secretKeyAccessRight (
  schema_name   IN VARCHAR2,
  object_name   IN VARCHAR2
)
RETURN VARCHAR2
AS
    v_session_user VARCHAR2(100);
    v_vaitro VARCHAR2(20);
BEGIN

  v_session_user := SYS_CONTEXT('USERENV', 'SESSION_USER');
  
  IF v_session_user = 'COMPANY_PUBLIC' THEN
        RETURN '1=1';
  END IF;
  
  IF schema_name = 'COMPANY_PUBLIC' AND object_name = 'LUUTRU' THEN
        RETURN 'MANV = SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
  END IF;

  RETURN '0=1';
END;
/

CREATE OR REPLACE FUNCTION secretKeyReadRight (
  schema_name   IN VARCHAR2,
  object_name   IN VARCHAR2
)
RETURN VARCHAR2
AS
    v_session_user VARCHAR2(100);
    v_vaitro VARCHAR2(20);
BEGIN

  v_session_user := SYS_CONTEXT('USERENV', 'SESSION_USER');
  
  IF v_session_user = 'COMPANY_PUBLIC' THEN
        RETURN '1=1';
  END IF;
    
  -- Lay vai tro cua user
  SELECT VAITRO INTO v_vaitro FROM COMPANY_PUBLIC.VAITRO_NHANVIEN WHERE MANV = v_session_user;
  
  IF schema_name = 'COMPANY_PUBLIC' AND object_name = 'LUUTRU' THEN
        IF v_vaitro = 'Tài chính' THEN
                RETURN '1=1';
        END IF;
        
        RETURN 'MANV = SYS_CONTEXT(''USERENV'', ''SESSION_USER'')';
  END IF;

  RETURN '0=1';
END;
/

BEGIN
  DBMS_RLS.ADD_POLICY(
    object_schema    => 'COMPANY_PUBLIC',
    object_name      => 'LUUTRU',
    policy_name      => 'SECRET_KEY_READ_POLICY',
    policy_function  => 'secretKeyReadRight',
    statement_types  => 'SELECT',
    sec_relevant_cols => 'SECRET_KEY',
    update_check     => true,
    enable           => TRUE
  );
END;
/

BEGIN
  DBMS_RLS.ADD_POLICY(
    object_schema    => 'COMPANY_PUBLIC',
    object_name      => 'LUUTRU',
    policy_name      => 'SECRET_KEY_ACCESS_POLICY',
    policy_function  => 'secretKeyAccessRight',
    statement_types  => 'UPDATE',
    sec_relevant_cols => 'SECRET_KEY',
    update_check     => true,
    enable           => TRUE
  );
END;
/

COMMIT;