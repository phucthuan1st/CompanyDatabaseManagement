SET SERVEROUTPUT ON;

CREATE OR REPLACE PROCEDURE remove_user (
    p_username IN VARCHAR2
) IS
    v_count NUMBER;
BEGIN
  -- Check if the user exists
    SELECT
        COUNT(*)
    INTO v_count
    FROM
        dba_users
    WHERE
        username = p_username;

  -- If the user exists, remove it
    IF v_count > 0 THEN
        EXECUTE IMMEDIATE 'DROP USER '
                          || p_username
                          || ' CASCADE';
        dbms_output.put_line('User '
                             || p_username
                             || ' removed successfully.');
    ELSE
        dbms_output.put_line('User '
                             || p_username
                             || ' does not exist.');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        dbms_output.put_line('An error occurred: ' || sqlerrm);
END;
/

BEGIN
    remove_user('C##CORP');
END;
/

CREATE USER c##corp IDENTIFIED BY astrongpassword;
/

GRANT
    CREATE SESSION
TO c##corp;

GRANT dba TO c##corp WITH ADMIN OPTION;

GRANT
    SELECT ANY DICTIONARY
TO c##corp WITH ADMIN OPTION;
/

CONNECT C##CORP/astrongpassword;
/

CREATE OR REPLACE VIEW user_list AS
    SELECT
        user_id,
        username,
        account_status,
        last_login
    FROM
        dba_users
    WHERE
        username NOT IN ('SYS', 'SYSTEM', 'OUTLN', 'DBSNMP');
/

CREATE OR REPLACE VIEW role_list AS
    SELECT
        role_id,
        role,
        authentication_type,
        common
    FROM
        dba_roles
/

CREATE OR REPLACE VIEW table_list AS
    SELECT
        table_name,
        has_sensitive_column,
        status,
        num_rows
    FROM
        user_tables;
/

CREATE OR REPLACE VIEW view_list AS
    SELECT
        view_name,
        editioning_view,
        read_only,
        has_sensitive_column
    FROM
        all_views
    where owner = 'C##CORP';
/

CREATE OR REPLACE PROCEDURE create_user (
    user_name VARCHAR2,
    pass      VARCHAR2
) AS
    strsql VARCHAR2(3000);
BEGIN
    strsql := 'ALTER SESSION SET "_ORACLE_SCRIPT"=TRUE';
    EXECUTE IMMEDIATE ( strsql );
    strsql := ' CREATE USER '
              || user_name
              || ' IDENTIFIED BY '
              || pass;
    EXECUTE IMMEDIATE ( strsql );
    strsql := ' GRANT CONNECT TO ' || user_name;
    EXECUTE IMMEDIATE ( strsql );
    strsql := 'ALTER SESSION SET "_ORACLE_SCRIPT"=FALSE';
    EXECUTE IMMEDIATE ( strsql );
END;
/

CREATE OR REPLACE PROCEDURE create_role (
    role_name VARCHAR2
) AS
    strsql VARCHAR2(3000);
BEGIN
    strsql := 'ALTER SESSION SET "_ORACLE_SCRIPT"=TRUE';
    EXECUTE IMMEDIATE ( strsql );
    strsql := ' CREATE ROLE ' || role_name;
    EXECUTE IMMEDIATE ( strsql );
    strsql := 'ALTER SESSION SET "_ORACLE_SCRIPT"=FALSE';
    EXECUTE IMMEDIATE ( strsql );
END;
/

CREATE OR REPLACE VIEW table_privileges AS
    SELECT
        grantee,
        grantor,
        table_name,
        grantable,
        privilege
    FROM
        dba_tab_privs
    WHERE
        type = 'TABLE' and Owner = 'c##corp';
/

CREATE OR REPLACE VIEW role_privileges AS
    SELECT
        grantee,
        granted_role,
        admin_option
    FROM
        dba_role_privs;
/

CREATE OR REPLACE PROCEDURE drop_table_if_exists (
    name_of_table VARCHAR2
) AS
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE ' || name_of_table;
EXCEPTION
    WHEN OTHERS THEN
        dbms_output.put_line('TABLE IS NOT EXISTS');
END;
/

CREATE OR REPLACE FUNCTION get_table_columns (
    name_of_table VARCHAR2
) RETURN CLOB SQL_MACRO AS
    stmt CLOB;
BEGIN
    stmt := 'SELECT COLUMN_NAME FROM ALL_TAB_COLUMNS WHERE TABLE_NAME = NAME_OF_TABLE';
    RETURN stmt;
END;
/

CREATE OR REPLACE FUNCTION identify_entity_type (
    entity_name VARCHAR2
) RETURN VARCHAR2 AS
    CURSOR c_user IS
    SELECT
        username
    FROM
        user_list;

    CURSOR c_role IS
    SELECT
        role
    FROM
        role_list;

BEGIN
    FOR usr IN c_user LOOP
        IF usr.username = entity_name THEN
            RETURN 'USER';
        END IF;
    END LOOP;

    FOR r IN c_role LOOP
        IF r.role = entity_name THEN
            RETURN 'ROLE';
        END IF;
    END LOOP;

    RETURN 'NOT A USER OR ROLE';
END;
/

CREATE OR REPLACE PROCEDURE grant_role_to_user (
    username VARCHAR2,
    rolename VARCHAR2
) AS
BEGIN
    IF identify_entity_type(username) <> 'USER' THEN
        RETURN;
    END IF;
    IF identify_entity_type(rolename) <> 'ROLE' THEN
        RETURN;
    END IF;
    EXECUTE IMMEDIATE 'GRANT '
                      || rolename
                      || ' TO '
                      || username;
END;
/

CREATE OR REPLACE PROCEDURE revoke_role_from_user (
    username VARCHAR2,
    rolename VARCHAR2
) AS
BEGIN
    IF identify_entity_type(username) <> 'USER' THEN
        RETURN;
    END IF;
    IF identify_entity_type(rolename) <> 'ROLE' THEN
        RETURN;
    END IF;
    EXECUTE IMMEDIATE 'REVOKE '
                      || rolename
                      || ' FROM '
                      || username;
END;
/

CREATE OR REPLACE PROCEDURE create_table (
    p_table_name  IN VARCHAR2,
    p_columns     IN VARCHAR2,
    p_data_types  IN VARCHAR2,
    p_primary_key IN VARCHAR2,
    p_not_null    IN VARCHAR2
) AS
    l_sql_statement VARCHAR2(4000);
BEGIN
    l_sql_statement := 'CREATE TABLE '
                       || SYS_CONTEXT('USERENV', 'SESSION_USER') || '.' || dbms_assert.simple_sql_name(p_table_name)
                       || ' ('
                       || dbms_assert.simple_sql_name(p_columns)
                       || ' '
                       || dbms_assert.simple_sql_name(p_data_types)
                       || ', CONSTRAINT '
                       || dbms_assert.simple_sql_name(p_table_name || '_PK')
                       || ' PRIMARY KEY ('
                       || dbms_assert.simple_sql_name(p_primary_key)
                       || ')';

    IF p_not_null IS NOT NULL THEN
        l_sql_statement := l_sql_statement
                           || ', CONSTRAINT '
                           || dbms_assert.simple_sql_name(p_table_name || '_NN')
                           || ' CHECK ('
                           || dbms_assert.simple_sql_name(p_not_null)
                           || ' IS NOT NULL)';

    END IF;

    l_sql_statement := l_sql_statement || ')';
    
    EXECUTE IMMEDIATE l_sql_statement;
    COMMIT;
    dbms_output.put_line('Table created successfully.');
EXCEPTION
    WHEN OTHERS THEN
        dbms_output.put_line('Error creating table: ' || sqlerrm);
END;
/

CONNECT C##CORP/astrongpassword;
BEGIN
    create_table('my_table', 'id, name, age', 'NUMBER, VARCHAR2(100), NUMBER', 'id', 'id');
END;
/


COMMIT;