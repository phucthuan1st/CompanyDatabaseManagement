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
    private final DBManager dbm;
    private final JSplitPane panes;
    private final JFrame father;
    private final JButton logOutButton;
    private final JButton showUserListButton;
    private ResultSet result;
    
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
        
        JPanel leftPanel = new JPanel(new GridBagLayout());
        JScrollPane rightPane = new JScrollPane();
        panes.setLeftComponent(leftPanel);
        panes.setRightComponent(rightPane);
        this.add(panes);
        
        // components constraints
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.insets = new Insets(10, 10, 10, 10);
        // declare component
        logOutButton = new JButton("Logout");
        constraint.gridx = 0;
        constraint.gridy = 0;
        leftPanel.add(logOutButton, constraint);
        
        showUserListButton = new JButton("Get user list in database");
        constraint.gridx = 0;
        constraint.gridy = 1;
        leftPanel.add(showUserListButton, constraint);
        
        this.setVisible(true);
        initialActionListener();
    }
    
    private void initialActionListener() {
        // Set action listeners for buttons
        logOutButton.addActionListener(this);
        showUserListButton.addActionListener(this);
    }
    
    // Helper method to convert a ResultSet to a TableModel
    private static TableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // Get number of columns
        int columns = metaData.getColumnCount();

        // Create column names array
        String[] columnNames = new String[columns];
        for (int i = 1; i <= columns; i++) {
            columnNames[i - 1] = metaData.getColumnName(i);
        }

        // Create data array
        Object[][] data = new Object[100][columns];
        int row = 0;
        while (rs.next() && row < 100) {
            for (int i = 1; i <= columns; i++) {
                data[row][i - 1] = rs.getObject(i);
            }
            row++;
        }

        return new DefaultTableModel(data, columnNames);
    }
    
    void showUserList() throws SQLException {
        result = dbm.getUserList();
        JTable table = new JTable(buildTableModel(result));
        JScrollPane rightPane = new JScrollPane(table);
        panes.setRightComponent(rightPane);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == logOutButton) {
            this.dispose();
            this.father.setVisible(true);
        }
        if (e.getSource() == showUserListButton) {
            try {
                showUserList();
            } catch(SQLException ex) {
                String message = "Cannot retrieve data from database";
                JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        }
    }
}
