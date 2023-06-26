CONNECT COMPANY_PUBLIC/astrongpassword@localhost:1521/COMPANY;
GRANT NHAN_VIEN to NV001;
GRANT QL_TRUC_TIEP TO QL001;
GRANT TRUONG_PHONG TO TP001;

CONNECT TP001/password@localhost:1521/COMPANY;
SET SERVEROUTPUT ON;
BEGIN
    DBMS_OUTPUT.PUT_LINE(COMPANY_PUBLIC.TRUONGPHONG_PERMISSION_CONSTRAINTS('COMPANY_PUBLIC', 'PHANCONG'));
END;
/
SELECT * FROM COMPANY_PUBLIC.PHANCONG;

select OBJECT_NAME, POLICY_NAME, ENABLE 
from user_policies 
order by POLICY_NAME asc;

DECLARE
  v_sql VARCHAR2(4000);
BEGIN
  FOR p IN (SELECT OBJECT_NAME, POLICY_NAME, ENABLE
            FROM USER_POLICIES
            ORDER BY POLICY_NAME ASC)
  LOOP
    IF p.ENABLE = 'YES' THEN
      v_sql := 'BEGIN
                  DBMS_RLS.DROP_POLICY(
                    object_schema => ''' || 'COMPANY_PUBLIC' || ''',
                    object_name => ''' || p.OBJECT_NAME || ''',
                    policy_name => ''' || p.POLICY_NAME || '''
                  );
                END;';
      DBMS_OUTPUT.PUT_LINE(v_sql);
      EXECUTE IMMEDIATE v_sql;
    END IF;
  END LOOP;
END;
/