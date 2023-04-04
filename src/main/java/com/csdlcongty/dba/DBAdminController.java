/**
 * @project CompanyDatabaseOperation
 * @system_one: DBA
 * @author 20H3T-02
 */
package com.csdlcongty.dba;

import com.csdlcongty.DBConnection;
import java.sql.SQLException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

public class DBAdminController extends JFrame implements ActionListener {
    private final DBConnection dbc;
    private final JSplitPane panes;
    private final JFrame father;
    private final JButton logOutButton;
    
    public DBAdminController(String password, JFrame father) throws ClassNotFoundException, SQLException {
        dbc = new DBConnection("sys as SYSDBA", password);
        this.father = father;
        
        // Set window properties
        this.setTitle("Login");
        this.setSize(1024, 768);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        /* split view into 2-pane: 
         * a panel contains button and a scrollable list to display result
        */
        panes = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        JPanel leftPanel = new JPanel(new GridBagLayout());
        JScrollPane rightPane = new JScrollPane();
        panes.setLeftComponent(leftPanel);
        panes.setRightComponent(rightPane);
        this.add(panes);
        
        // declare component
        GridBagConstraints constraint = new GridBagConstraints();
        logOutButton = new JButton("Logout");
        
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.dispose();
    }
}
