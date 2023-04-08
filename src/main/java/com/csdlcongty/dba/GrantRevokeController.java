package com.csdlcongty.dba;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.SQLException;


// include Login screen and handling for pressing Login button
public class GrantRevokeController extends JFrame implements ActionListener {
    
    // Declare components
    private final JLabel permissionLabel;
    private final JLabel tableLabel;
    private final JLabel roleUserLabel;
    protected final JCheckBox SELECT;
    protected final JCheckBox INSERT;
    protected final JCheckBox UPDATE;
    protected final JTextField tableField;
    protected final JTextField roleUserField;
    private final JButton acceptButton;
    private String[] result={"a","b","c"};
    protected String permission="";
    protected String table;
    protected String roleUser;

    public GrantRevokeController(int wWidth, int wHeight) throws IOException {
        // Set window properties
        setTitle("Grand/Revoke role/user");
        setSize(wWidth, wHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        this.setResizable(false);

        permissionLabel = new JLabel("Permission:");
        tableLabel = new JLabel("Table:");
        roleUserLabel = new JLabel("Role/User:");
        SELECT= new JCheckBox("SELECT",false);
        INSERT= new JCheckBox("INSERT",false);
        UPDATE= new JCheckBox("UPDATE",false);
        tableField = new JTextField(20);   
        roleUserField = new JTextField(20); 
        acceptButton = new JButton("OK");

         // Add components to the window
         JPanel panel = new JPanel(new GridBagLayout());
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.insets = new Insets(10, 10, 10, 10);
         
         constraints.gridheight = 1;
         constraints.gridwidth = 1; 
         constraints.gridx = 4;
         constraints.gridy = 0;
         constraints.anchor = GridBagConstraints.CENTER;
         
         constraints.gridheight = 1;
         constraints.gridwidth = 1; 
         
         constraints.gridx = 3;
         constraints.gridy = 3;
         panel.add(permissionLabel, constraints);
         
         constraints.gridx = 4;
         panel.add(SELECT, constraints);

         constraints.gridx = 5;
         panel.add(INSERT, constraints);

         constraints.gridx = 6;
         panel.add(UPDATE, constraints);
         
         constraints.gridx = 3;
         constraints.gridy = 4;
         panel.add(tableLabel, constraints);
         
         constraints.gridx = 4;
         constraints.gridwidth=10;
         panel.add(tableField, constraints);

         constraints.gridwidth=1;
         constraints.gridx = 3;
         constraints.gridy = 5;
         panel.add(roleUserLabel, constraints);
         
         constraints.gridwidth=10;
         constraints.gridx = 4;
         panel.add(roleUserField, constraints);
         
         constraints.gridwidth=1;
         constraints.gridx = 3;
         constraints.gridy = 6;
         panel.add(acceptButton, constraints);
         
         add(panel);
         
         initialActionListener();
         setVisible(true);
    }

    private void initialActionListener() {
        // Set action listeners for buttons
        acceptButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == acceptButton) {
            
            // Implement login logic
            //String permission = permissionField.getText();
            if (SELECT.isSelected()) permission="SELECT";
            if (INSERT.isSelected())
            {
                if (permission=="") permission="INSERT";
                else permission+=", INSERT";
            }
            if (UPDATE.isSelected())
            {
                if (permission=="") permission="UPDATE";
                else permission=", UPDATE";
            }
            table = tableField.getText();
            roleUser = roleUserField.getText();
        }
    }
    
    public void changeTo(JFrame other) {
        this.setVisible(false);
        other.setVisible(true);
    }

}