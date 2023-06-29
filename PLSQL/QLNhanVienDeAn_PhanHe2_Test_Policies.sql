CONNECT COMPANY_PUBLIC/astrongpassword@localhost:1521/COMPANY;

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