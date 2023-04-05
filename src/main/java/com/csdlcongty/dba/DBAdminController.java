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
    
    public DBAdminController(String password, JFrame father) throws ClassNotFoundException, SQLException {
        dbm = new DBManager("sys as SYSDBA", password);
        this.father = father;
        
        // Set window properties
        this.setTitle("Login");
        this.setSize(1024, 768);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        /* split view into 2-pane: 
         * a panel contains button and a scrollable list to display result
        */
        panes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        panes.setResizeWeight(0.6);
        
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
        
        showUserListButton = new JButton("Get user list");
        constraint.gridx = 0;
        constraint.gridy = 1;
        leftPanel.add(showUserListButton, constraint);
        
        showRoleListButton = new JButton("Get role list");
        constraint.gridx = 1;
        constraint.gridy = 1;
        leftPanel.add(showRoleListButton, constraint);
        
        showTableListButton = new JButton("Get table list");
        constraint.gridx = 0;
        constraint.gridy = 2;
        leftPanel.add(showTableListButton, constraint);
        
        showViewListButton = new JButton("Get view list");
        constraint.gridx = 1;
        constraint.gridy = 2;
        leftPanel.add(showViewListButton, constraint);
        
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
        
        // Set action for other compunents
    }
    
    // Helper method to convert a ResultSet to a TableModel
    private static TableModel buildTableModel(ResultSet rs, int num_rows) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // Get number of columns
        int columns = metaData.getColumnCount();

        // Create column names array
        String[] columnNames = new String[columns];
        for (int i = 1; i <= columns; i++) {
            columnNames[i - 1] = metaData.getColumnName(i);
        }

        // Create data array
        Object[][] data = new Object[num_rows][columns];
        int row = 0;
        while (rs.next() && row < num_rows) {
            for (int i = 1; i <= columns; i++) {
                data[row][i - 1] = rs.getObject(i);
            }
            row++;
        }

        return new DefaultTableModel(data, columnNames);
    }
    
    // Update display on right pane with table view
    void updateRightPaneTable(int num_rows) throws SQLException {
        JTable table = new JTable(buildTableModel(result, num_rows));
        JScrollPane rightPane = new JScrollPane(table);
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
                updateRightPaneTable(num_rows);
            }
            // Handle Show role list button
            else if (e.getSource() == showRoleListButton) {
                result = dbm.getRoleList();
                int num_rows = dbm.getNumberRowsOf("ROLE_LIST");
                updateRightPaneTable(num_rows);
            }
            // Handle Show table list button
            else if (e.getSource() == showTableListButton) {
                result = dbm.getTableList();
                int num_rows = dbm.getNumberRowsOf("TABLE_LIST");
                updateRightPaneTable(num_rows);
            }
            // Handle Show view list button
            else if (e.getSource() == showViewListButton) {
                result = dbm.getViewList();
                int num_rows = dbm.getNumberRowsOf("VIEW_LIST");
                updateRightPaneTable(num_rows);
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
