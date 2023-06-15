CONNECT COMPANY_PUBLIC/astrongpassword@localhost:1521/COMPANY;
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

CREATE OR REPLACE VIEW user_list AS
    SELECT
        user_id,
        username,
        account_status,
        last_login
    FROM
        dba_users
    WHERE
        account_status = 'OPEN'
        AND authentication_type = 'PASSWORD'
        AND username NOT IN ( 'SYS', 'SYSTEM', 'OUTLN', 'DBSNMP' );
/

CREATE OR REPLACE VIEW role_list AS
    SELECT
        role_id,
        role,
        authentication_type,
        common
    FROM
        dba_roles
    WHERE COMMON = 'NO'
/

CREATE OR REPLACE PROCEDURE gather_statistics_for_schema AS
    v_owner VARCHAR2(30) := 'COMPANY_PUBLIC';
BEGIN
    FOR t IN (SELECT table_name FROM all_tables WHERE owner = v_owner) LOOP
        BEGIN
            DBMS_STATS.GATHER_TABLE_STATS(
                ownname => v_owner,
                tabname => t.table_name,
                estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE,
                method_opt => 'FOR ALL COLUMNS SIZE AUTO'
            );
            DBMS_OUTPUT.PUT_LINE('Statistics gathered for table: ' || t.table_name);
        EXCEPTION
            WHEN OTHERS THEN
                DBMS_OUTPUT.PUT_LINE('Error gathering statistics for table: ' || t.table_name || ' - ' || SQLERRM);
        END;
    END LOOP;
END;
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
    where owner = 'COMPANY_PUBLIC';
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
        type = 'TABLE' and Owner = 'COMPANY_PUBLIC';
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

-- Procedure to create a table
CREATE OR REPLACE PROCEDURE create_table(
    table_name IN VARCHAR2,
    column_list IN VARCHAR2, -- comma-separated column names
    data_types IN VARCHAR2, -- comma-separated data types
    primary_key_col IN VARCHAR2, -- primary key column
    not_null_cols IN VARCHAR2 -- comma-separated not null columns
) AS
    query_string VARCHAR2(4000);
BEGIN
    -- Generate the CREATE TABLE statement
    query_string := 'CREATE TABLE ' || table_name || ' (';
    
    -- Process the column list and data types
    FOR i IN 1..REGEXP_COUNT(column_list, ',') + 1 LOOP
        query_string := query_string || TRIM(REGEXP_SUBSTR(column_list, '[^,]+', 1, i)) || ' ' ||
                        TRIM(REGEXP_SUBSTR(data_types, '[^,]+', 1, i));
        
        -- Add NOT NULL constraint if applicable
        IF REGEXP_INSTR(not_null_cols, TRIM(REGEXP_SUBSTR(column_list, '[^,]+', 1, i))) > 0 THEN
            query_string := query_string || ' NOT NULL';
        END IF;
        
        query_string := query_string || ', ';
    END LOOP;
    
    query_string := RTRIM(query_string, ', ') || ', CONSTRAINT pk_' || table_name ||
                    ' PRIMARY KEY (' || primary_key_col || '))';
    
    -- Execute the CREATE TABLE statement
    EXECUTE IMMEDIATE query_string;
    
    DBMS_OUTPUT.PUT_LINE('Table ' || table_name || ' created successfully.');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error creating table: ' || SQLERRM);
END;
/

SET SERVEROUTPUT ON

DECLARE
  v_service_name VARCHAR2(100);
BEGIN
  SELECT SYS_CONTEXT('USERENV', 'SERVICE_NAME') INTO v_service_name FROM DUAL;
  DBMS_OUTPUT.PUT_LINE('Service Name: ' || v_service_name);
END;
/

COMMIT;