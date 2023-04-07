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

public class DBAdminController extends JFrame implements ActionListener {
    
    // Controller properties
    private final DBManager dbm;
    private ResultSet result;
    
    // GUI Components
    private final JSplitPane panes;
    private final JFrame father;
    private final JButton logOutButton;
    private final JButton showUserListButton;
    private final JButton showRoleListButton;
    private final JButton showTableListButton;
    private final JButton showViewListButton;
    private final JButton showPrivilegeButton;
    
    public DBAdminController(String password, JFrame father) throws ClassNotFoundException, SQLException {
        dbm = new DBManager("sys as SYSDBA", password);
        this.father = father;
        
        // Set window properties
        this.setTitle("Login");
        this.setSize(1366, 768);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        /* split view into 2-pane: 
         * a panel contains button and a scrollable list to display result
        */
        panes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        panes.setResizeWeight(0.6);
        panes.setDividerLocation(0.6);
        
        GridBagLayout layout = new GridBagLayout();
        JPanel leftPanel = new JPanel(layout);
        JScrollPane rightPane = new JScrollPane();
        panes.setLeftComponent(leftPanel);
        panes.setRightComponent(rightPane);
        this.add(panes);
        
        // components constraints
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.insets = new Insets(10, 10, 10, 10);
        constraint.anchor = GridBagConstraints.CENTER;
        
        // declare component in left panel
        logOutButton = new JButton("Logout");
        constraint.gridx = 0;
        constraint.gridy = 0;
        leftPanel.add(logOutButton, constraint);
        
        showUserListButton = new JButton("Show user list");
        constraint.gridx = 0;
        constraint.gridy = 1;
        leftPanel.add(showUserListButton, constraint);
        
        showRoleListButton = new JButton("Show role list");
        constraint.gridx = 1;
        constraint.gridy = 1;
        leftPanel.add(showRoleListButton, constraint);
        
        showTableListButton = new JButton("Show table list");
        constraint.gridx = 0;
        constraint.gridy = 2;
        leftPanel.add(showTableListButton, constraint);
        
        showViewListButton = new JButton("Show view list");
        constraint.gridx = 1;
        constraint.gridy = 2;
        leftPanel.add(showViewListButton, constraint);
        
        showPrivilegeButton = new JButton("Show user or role privilege");
        constraint.gridx = 0;
        constraint.gridy = 3;
        leftPanel.add(showPrivilegeButton, constraint);
        
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
        // Set action for other compunents
    }
    
    // Helper method to convert a ResultSet to a TableModel
    private TableModel buildTableModel(ResultSet rs, int numRows) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // Get number of columns
        int columns = metaData.getColumnCount();

        // Create column names array
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
        var ratio = (float)(upperNumRows) / (float)(upperNumRows + lowerNumRows);
        //rightPane.setDividerLocation(ratio);
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
            }
            // Handle Show role list button
            else if (e.getSource() == showRoleListButton) {
                result = dbm.getRoleList();
                int num_rows = dbm.getNumberRowsOf("ROLE_LIST");
                displayRightPaneTable(num_rows);
            }
            // Handle Show table list button
            else if (e.getSource() == showTableListButton) {
                result = dbm.getTableList();
                int num_rows = dbm.getNumberRowsOf("TABLE_LIST");
                displayRightPaneTable(num_rows);
            }
            // Handle Show view list button
            else if (e.getSource() == showViewListButton) {
                result = dbm.getViewList();
                int num_rows = dbm.getNumberRowsOf("VIEW_LIST");
                displayRightPaneTable(num_rows);
            }
            else if(e.getSource() == showPrivilegeButton) {
                String[] option = {"User", "Role"};
                String message = "Show either role or user?";
                int choose = JOptionPane.showOptionDialog(this, message,"Selection", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, option, null);
                String entityType = option[choose];
                
                message = "Enter name of " + entityType;
                String nameOfEntity = JOptionPane.showInputDialog(this, message, entityType + " name", JOptionPane.QUESTION_MESSAGE);
                
                result = dbm.getTablePrivilegesOfRoleOrUser(nameOfEntity);
                int numRows = dbm.getNumberOfRowsInLastQuery();
                
                if (numRows == 0) {
                    JOptionPane.showMessageDialog(this, nameOfEntity + " is not exist", "Error", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    if ("Role".equals(entityType))
                        this.displayRightPaneTable(numRows);
                    else if ("User".equals(entityType)) {
                        ResultSet roleSet = dbm.getRoleOfUser(nameOfEntity);
                        int lowerNumRows = dbm.getNumberOfRowsInLastQuery();
                        this.displayTwoRightPaneTable(numRows, roleSet, lowerNumRows);
                    }
                }
            }
        } catch(SQLException ex) {
            String message = "Cannot retrieve data from database: " + ex.getMessage();
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            String message = "Unexpected error occured: " + ex.getMessage();
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
