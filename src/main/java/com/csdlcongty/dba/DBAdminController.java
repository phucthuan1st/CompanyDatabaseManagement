/**
 * @project CompanyDatabaseOperation
 * @system_one: DBA
 * @author 20H3T-02
 */
package com.csdlcongty.dba;

import com.csdlcongty.DBManager;
import java.sql.SQLException;
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;

public class DBAdminController extends JFrame implements ActionListener {

    // Controller properties
    private final DBManager dbm;
    private ResultSet result;
    private final JFrame father;

    // GUI Components
    private final JSplitPane panes;

    private final JButton logOutButton;

    private final JButton showUserListButton;
    private final JButton showRoleListButton;
    private final JButton showTableListButton;
    private final JButton showViewListButton;

    private final JButton showPrivilegeButton;

    private final JButton createNewUserButton;
    private final JButton createNewRoleButton;
    private final JButton createNewTableButton;

    private final JButton grantPrivilegeButton;
    private final JButton revokePrivilegeButton;
    private JButton grantRevokeButton;

    private final JButton grantRoleToUserButton;
    private final JButton revokeRoleFromUserButton;

    public DBAdminController(String password, JFrame father) throws ClassNotFoundException, SQLException {
        this.dbm = new DBManager("sys as SYSDBA", password);
        this.father = father;

        // Set window properties
        this.setTitle("Database Administration");
        this.setSize(1280, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        /*
         * split view into 2-pane:
         * a panel contains button and a scrollable list to display result
         */
        this.panes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        this.panes.setResizeWeight(0.5);
        this.panes.setDividerLocation(0.5);

        GridBagLayout layout = new GridBagLayout();
        JPanel leftPanel = new JPanel(layout);

        JScrollPane rightPane = new JScrollPane();

        this.panes.setLeftComponent(leftPanel);
        this.panes.setRightComponent(rightPane);

        this.add(this.panes);

        // components constraints
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.insets = new Insets(10, 0, 10, 5);
        constraint.anchor = GridBagConstraints.CENTER;

        // declare component in left panel
        logOutButton = new JButton("Logout");
        constraint.gridx = 0;
        constraint.gridy = 0;
        leftPanel.add(logOutButton, constraint);
        constraint.gridheight = 1;

        showUserListButton = new JButton("Show user list");
        constraint.gridx = 1;
        constraint.gridy = 1;
        leftPanel.add(showUserListButton, constraint);

        showRoleListButton = new JButton("Show role list");
        constraint.gridx = 2;
        constraint.gridy = 1;
        leftPanel.add(showRoleListButton, constraint);

        showTableListButton = new JButton("Show table list");
        constraint.gridx = 1;
        constraint.gridy = 2;
        leftPanel.add(showTableListButton, constraint);

        showViewListButton = new JButton("Show view list");
        constraint.gridx = 2;
        constraint.gridy = 2;
        leftPanel.add(showViewListButton, constraint);

        showPrivilegeButton = new JButton("Show user or role privilege");
        constraint.gridx = 2;
        constraint.gridy = 3;
        leftPanel.add(showPrivilegeButton, constraint);

        createNewUserButton = new JButton("Create new user");
        constraint.gridx = 1;
        constraint.gridy = 4;
        leftPanel.add(createNewUserButton, constraint);

        createNewRoleButton = new JButton("Create new role");
        constraint.gridx = 2;
        constraint.gridy = 4;
        leftPanel.add(createNewRoleButton, constraint);

        createNewTableButton = new JButton("Create new table");
        constraint.gridx = 3;
        constraint.gridy = 4;
        leftPanel.add(createNewTableButton, constraint);

        grantPrivilegeButton = new JButton("Grant privilege");
        constraint.gridx = 1;
        constraint.gridy = 5;
        leftPanel.add(grantPrivilegeButton, constraint);

        revokePrivilegeButton = new JButton("Revoke privilege");
        constraint.gridx = 2;
        constraint.gridy = 5;
        leftPanel.add(revokePrivilegeButton, constraint);

        grantRoleToUserButton = new JButton("Grant role to user");
        constraint.gridx = 1;
        constraint.gridy = 6;
        leftPanel.add(grantRoleToUserButton, constraint);

        revokeRoleFromUserButton = new JButton("Revoke role from user");
        constraint.gridx = 2;
        constraint.gridy = 6;
        leftPanel.add(revokeRoleFromUserButton, constraint);

        // initilize action on left component
        this.setVisible(true);
        initialActionListener();
    }

    // update this method every times you need to add action for a components
    private void initialActionListener() {

        // Set action listeners for buttons
        logOutButton.addActionListener(this);

        showUserListButton.addActionListener(this);
        showRoleListButton.addActionListener(this);
        showTableListButton.addActionListener(this);
        showViewListButton.addActionListener(this);

        showPrivilegeButton.addActionListener(this);

        createNewUserButton.addActionListener(this);
        createNewRoleButton.addActionListener(this);
        createNewTableButton.addActionListener(this);

        grantPrivilegeButton.addActionListener(this);
        revokePrivilegeButton.addActionListener(this);

        grantRoleToUserButton.addActionListener(this);
        revokeRoleFromUserButton.addActionListener(this);
    }

    // Helper method to convert a ResultSet to a TableModel
    private TableModel buildTableModel(ResultSet rs, int numRows) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // Get dataLengthField of columns
        int columns = metaData.getColumnCount();

        // Create columnNameField names array
        String[] columnNames = new String[columns];
        for (int i = 1; i <= columns; i++) {
            columnNames[i - 1] = metaData.getColumnName(i);
        }

        // Create data array
        Object[][] data = new Object[numRows][columns];
        int row = 0;
        while (rs.next() && row < numRows) {
            for (int i = 1; i <= columns; i++) {
                data[row][i - 1] = rs.getObject(i);
            }
            row++;
        }

        return new DefaultTableModel(data, columnNames);
    }

    // Update display on right pane with table view
    void displayRightPaneTable(int numRows) throws SQLException {
        JTable table = new JTable(buildTableModel(this.result, numRows)) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JScrollPane rightPane = new JScrollPane(table);
        panes.setRightComponent(rightPane);
    }

    // Update display on right pane with table view
    void displayTwoRightPaneTable(int upperNumRows, ResultSet lowerSet, int lowerNumRows) throws SQLException {
        JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        float ratio = (float) (upperNumRows) / (float) (upperNumRows + lowerNumRows);
        rightPane.setResizeWeight(ratio + 0.1);

        JTable upperTable = new JTable(buildTableModel(this.result, upperNumRows)) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JScrollPane upperPane = new JScrollPane(upperTable);
        rightPane.setTopComponent(upperPane);

        JTable lowerTable = new JTable(buildTableModel(lowerSet, lowerNumRows)) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JScrollPane lowerPane = new JScrollPane(lowerTable);
        rightPane.setBottomComponent(lowerPane);

        panes.setRightComponent(rightPane);
    }

    InteractivePanel setRightPanelToInteracionMode() {
        InteractivePanel panel = new InteractivePanel(this.dbm);
        this.panes.setRightComponent(panel);
        this.grantRevokeButton = panel.grantRevokeButton;
        this.grantRevokeButton.addActionListener(this);
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Define action for every component
        // Logout Button
        if (e.getSource() == logOutButton) {
            this.dispose();
            this.father.setVisible(true);
        }

        try {
            // Handle Show User list button
            if (e.getSource() == showUserListButton) {
                result = dbm.getUserList();
                int num_rows = dbm.getNumberRowsOf("USER_LIST");
                displayRightPaneTable(num_rows);
            } // Handle Show role list button
            else if (e.getSource() == showRoleListButton) {
                result = dbm.getRoleList();
                int num_rows = dbm.getNumberRowsOf("ROLE_LIST");
                displayRightPaneTable(num_rows);
            } // Handle Show table list button
            else if (e.getSource() == showTableListButton) {
                result = dbm.getTableList();
                int num_rows = dbm.getNumberRowsOf("TABLE_LIST");
                displayRightPaneTable(num_rows);
            } // Handle Show view list button
            else if (e.getSource() == showViewListButton) {
                result = dbm.getViewList();
                int num_rows = dbm.getNumberRowsOf("VIEW_LIST");
                displayRightPaneTable(num_rows);
            } else if (e.getSource() == showPrivilegeButton) {
                String[] option = { "User", "Role" };
                String message = "Show either role or user?";
                int choose = JOptionPane.showOptionDialog(this, message, "Selection", JOptionPane.DEFAULT_OPTION,
                        JOptionPane.PLAIN_MESSAGE, null, option, null);

                if (choose < 0) {
                    return;
                }

                String entityType = option[choose];
                message = "Enter name of " + entityType;

                String nameOfEntity = JOptionPane.showInputDialog(this, message, entityType + " name",
                        JOptionPane.QUESTION_MESSAGE);

                if (nameOfEntity.isBlank()) {
                    return;
                }

                result = dbm.getTablePrivilegesOfRoleOrUser(nameOfEntity);
                int numRows = dbm.getNumberOfRowsInLastQuery();

                if (numRows == 0) {
                    JOptionPane.showMessageDialog(this,
                            nameOfEntity + " is not exist or doesn't has any privileges yet!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    if ("Role".equals(entityType)) {
                        this.displayRightPaneTable(numRows);
                    } else if ("User".equals(entityType)) {
                        ResultSet roleSet = dbm.getRoleOfUser(nameOfEntity);
                        int lowerNumRows = dbm.getNumberOfRowsInLastQuery();
                        this.displayTwoRightPaneTable(numRows, roleSet, lowerNumRows);
                    }
                }
            } else if (e.getSource() == createNewUserButton) {
                JTextField username = new JTextField();
                JTextField password = new JPasswordField();

                Object[] message = {
                        "Username: ", username,
                        "Password: ", password
                };

                int choose = JOptionPane.showConfirmDialog(this, message, "Create new user",
                        JOptionPane.OK_CANCEL_OPTION);

                if (choose == JOptionPane.OK_OPTION) {
                    int successfull = dbm.createNewUser(username.getText(), password.getText());

                    if (successfull > 0) {
                        JOptionPane.showMessageDialog(this, "User " + username.getText() + " created", "Message",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "User " + username.getText() + " cannot create or exists in database", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else if (e.getSource() == createNewRoleButton) {
                JTextField rolename = new JTextField();

                Object[] message = { "Enter name of Role ", rolename };

                int choose = JOptionPane.showConfirmDialog(this, message, "Create new role",
                        JOptionPane.OK_CANCEL_OPTION);

                if (choose != JOptionPane.OK_OPTION) {
                    return;
                }

                String nameOfRole = rolename.getText();

                if (nameOfRole.isBlank()) {
                    return;
                }

                int resultQuery = dbm.createNewRole(nameOfRole);

                if (resultQuery > 0) {
                    JOptionPane.showMessageDialog(this, "Role " + nameOfRole + " created", "Message",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Role " + nameOfRole + " cannot create or exists in database",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }

            } else if (e.getSource() == createNewTableButton) {
                JTextField tableNameField = new JTextField();
                JTextField numberOfColumnsField = new JTextField();

                JTextField columnNameField = new JTextField();
                JTextField dataLengthField = new JTextField();

                // type of variable in oracle
                String[] valueTypeSelection = { " INT ", " FLOAT ", " VARCHAR2 ", " NVARCHAR2 ", " VARCHAR ",
                        " NVARCHAR ", " DATE " };
                String[] valueNullConstraint = { " NULL ", " NOT NULL ", " PRIMARY KEY " };

                JComboBox<String> valueTypeSelectionField = new JComboBox<>(valueTypeSelection);
                JComboBox<String> valueNullConstraintField = new JComboBox<>(valueNullConstraint);

                Object[] message = {
                        "Table Name:", tableNameField,
                        "Numbers of Column:", numberOfColumnsField
                };

                Object[] field = {
                        "Column Name:", columnNameField,
                        "Type of Column:", valueTypeSelectionField,
                        "Number:", dataLengthField,
                        "Is NULL:", valueNullConstraintField
                };

                int choose = JOptionPane.showConfirmDialog(this, message, "Create new table",
                        JOptionPane.OK_CANCEL_OPTION);

                if (choose != JOptionPane.OK_OPTION) {
                    return;
                }

                if (numberOfColumnsField.getText().isBlank() || tableNameField.getText().isBlank()) {
                    return;
                }
                ArrayList<String> nameOfColumns = new ArrayList<>();
                ArrayList<String> valueTypeOfColumns = new ArrayList<>();
                ArrayList<String> lengthOfDataInColumns = new ArrayList<>();
                ArrayList<String> nullConstraintOfColumns = new ArrayList<>();

                int numberOfColumns = Integer.parseInt(numberOfColumnsField.getText());

                for (int i = 0; i < numberOfColumns; i++) {
                    choose = JOptionPane.showConfirmDialog(this, field, "Field " + (i + 1),
                            JOptionPane.OK_CANCEL_OPTION);

                    if (choose != JOptionPane.OK_OPTION) {
                        return;
                    }

                    if (columnNameField.getText().isBlank()) {
                        return;
                    }

                    nameOfColumns.add(columnNameField.getText());
                    valueTypeOfColumns.add(valueTypeSelectionField.getSelectedItem().toString());

                    if (!dataLengthField.getText().isBlank()) {
                        lengthOfDataInColumns.add(dataLengthField.getText());
                    }

                    nullConstraintOfColumns.add(valueNullConstraintField.getSelectedItem().toString());

                    columnNameField.setText("");
                    dataLengthField.setText("");
                    valueTypeSelectionField.setSelectedIndex(0);
                    valueNullConstraintField.setSelectedIndex(0);
                }

                int resultQuery = dbm.createNewTable(tableNameField.getText(),
                        Integer.parseInt(numberOfColumnsField.getText()), nameOfColumns, valueTypeOfColumns,
                        lengthOfDataInColumns, nullConstraintOfColumns);
                if (resultQuery > 0) {
                    JOptionPane.showMessageDialog(this, "Table " + tableNameField.getText() + " created", "Message",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Table " + tableNameField.getText() + " cannot create or exists in database", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else if (e.getSource() == grantPrivilegeButton) {
                setRightPanelToInteracionMode();
                grantRevokeButton.setText("GRANT");

            } else if (e.getSource() == revokePrivilegeButton) {
                InteractivePanel panel = setRightPanelToInteracionMode();
                panel.grantOptionCheckBox.setVisible(false);
                grantRevokeButton.setText("REVOKE");
            } else if (e.getSource() == grantRevokeButton) {
                InteractivePanel panel = (InteractivePanel) this.panes.getRightComponent();
                String operator = panel.grantRevokeButton.getText();
                String permission = "";

                if (panel.selectPermissionsCheckBox.isSelected()) {
                    permission = "SELECT";
                }

                if (panel.insertPermissionsCheckBox.isSelected()) {
                    if (permission.isBlank()) {
                        permission = "INSERT";
                    } else {
                        permission += ", INSERT";
                    }
                }
                if (panel.updatePermissionsCheckBox.isSelected()) {
                    if (permission.isBlank()) {
                        permission = "UPDATE";
                    } else {
                        permission += ", UPDATE";
                    }
                }
                if (panel.deletePermissionsCheckBox.isSelected()) {
                    if (permission.isBlank()) {
                        permission = "DELETE";
                    } else {
                        permission += ", DELETE";
                    }
                }

                if (!permission.contains("INSERT") && !permission.contains("DELETE")) {
                    if (!panel.columnsOfTable.getSelectedItem().toString().isBlank()) {
                        permission += " (" + panel.columnsOfTable.getSelectedItem().toString() + ")";
                    }
                }

                String tableName = panel.tableNameComboBox.getSelectedItem().toString();
                String userOrRoleName = panel.userOrRoleTextField.getText();

                if (operator.equals("GRANT")) {
                    boolean grantable = panel.grantOptionCheckBox.isSelected();
                    dbm.grantPrivilegesOnTable(permission, tableName, userOrRoleName, grantable);
                } else {
                    dbm.revokePrivilegesOnTable(permission, tableName, userOrRoleName);
                }

                JOptionPane.showMessageDialog(this, operator + " successfullly", operator,
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (e.getSource() == grantRoleToUserButton) {
                JTextField username = new JTextField();
                JTextField rolename = new JTextField();

                Object[] message = {
                        "Username: ", username,
                        "Role: ", rolename
                };

                int choose = JOptionPane.showConfirmDialog(this, message, "Grant role to user",
                        JOptionPane.OK_CANCEL_OPTION);

                if (choose != JOptionPane.OK_OPTION)
                    return;

                if (username.getText().isBlank() || rolename.getText().isBlank())
                    return;

                dbm.grantRoleToUser(username.getText(), rolename.getText());
                JOptionPane.showMessageDialog(this, "Grant successfullly", "Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (e.getSource() == revokeRoleFromUserButton) {
                JTextField username = new JTextField();
                JTextField rolename = new JTextField();

                Object[] message = {
                        "Username: ", username,
                        "Role: ", rolename
                };

                int choose = JOptionPane.showConfirmDialog(this, message, "Revoke role from user",
                        JOptionPane.OK_CANCEL_OPTION);

                if (choose != JOptionPane.OK_OPTION)
                    return;

                if (username.getText().isBlank() || rolename.getText().isBlank())
                    return;

                dbm.revokeRoleFromUser(username.getText(), rolename.getText());
                JOptionPane.showMessageDialog(this, "Revoke successfullly", "Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            String message = "Cannot create new element: " + ex.getMessage();
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            String message = "Unexpected error occured: " + ex.getMessage();
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

}
