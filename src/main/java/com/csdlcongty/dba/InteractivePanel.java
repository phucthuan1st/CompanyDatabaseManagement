/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.csdlcongty.dba;

import com.csdlcongty.DBManager;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author pthuan
 */
public class InteractivePanel extends JPanel {
    final JCheckBox selectPermissionsCheckBox = new JCheckBox("SELECT");
    final JCheckBox insertPermissionsCheckBox = new JCheckBox("INSERT");
    final JCheckBox deletePermissionsCheckBox = new JCheckBox("DELETE");
    final JCheckBox updatePermissionsCheckBox = new JCheckBox("UPDATE");
    final JComboBox<String> columnsOfTable = new JComboBox<>();
    final JLabel tableNameLabel = new JLabel("Table Name");
    final JLabel columnNameLabel = new JLabel("Column Name");
    final JComboBox<String> tableNameComboBox = new JComboBox<>();
    final JCheckBox grantOptionCheckBox = new JCheckBox("WITH GRANT OPTION");
    final JLabel userOrRoleNameLabel = new JLabel("Name of Role/User");
    final JTextField userOrRoleTextField = new JTextField();
    final JButton grantRevokeButton = new JButton("GRANT");

    final JPanel panel = new JPanel(new GridBagLayout());
    final GridBagConstraints constraints = new GridBagConstraints();

    public InteractivePanel(DBManager dbm) {

        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;

        constraints.gridx = 1;
        constraints.gridy = 1;
        panel.add(selectPermissionsCheckBox, constraints);

        constraints.gridx = 2;
        constraints.gridy = 1;
        panel.add(insertPermissionsCheckBox, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        panel.add(deletePermissionsCheckBox, constraints);

        constraints.gridx = 2;
        constraints.gridy = 2;
        panel.add(updatePermissionsCheckBox, constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        panel.add(tableNameLabel, constraints);

        constraints.gridx = 2;
        constraints.gridy = 3;
        String[] tableNames = new String[0];
        try {
            ResultSet tableNameSet = dbm.getTableList();
            int num_rows = dbm.getNumberOfRowsInLastQuery();

            tableNames = new String[num_rows];
            int i = 0;
            while (tableNameSet.next()) {
                tableNames[i] = tableNameSet.getString(1);
                i++;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }

        tableNameComboBox.setModel(new DefaultComboBoxModel<>(tableNames));

        tableNameComboBox.addActionListener((ActionEvent e) -> {
            try {
                ResultSet resultSet = dbm.getColumnsOfTable(tableNameComboBox.getSelectedItem().toString());
                int numRows = dbm.getNumberOfRowsInLastQuery() + 1;

                String[] columnNames = new String[numRows];
                int i = 0;
                columnNames[i++] = "";
                while (resultSet.next()) {
                    columnNames[i] = resultSet.getString("COLUMN_NAME");
                    i++;
                }
                columnsOfTable.setModel(new DefaultComboBoxModel<>(columnNames));
                columnsOfTable.setSelectedItem(0);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(tableNameComboBox, constraints);

        constraints.gridx = 1;
        constraints.gridy = 4;
        panel.add(columnNameLabel, constraints);

        constraints.gridx = 2;
        constraints.gridy = 4;
        panel.add(columnsOfTable, constraints);

        constraints.gridwidth = 1;
        constraints.gridx = 2;
        constraints.gridy = 5;
        panel.add(grantOptionCheckBox, constraints);

        constraints.gridx = 1;
        constraints.gridy = 6;
        panel.add(userOrRoleNameLabel, constraints);

        constraints.gridx = 2;
        constraints.gridy = 6;
        userOrRoleTextField.setColumns(20);
        panel.add(userOrRoleTextField, constraints);

        constraints.gridx = 2;
        constraints.gridy = 7;
        panel.add(grantRevokeButton, constraints);

        this.add(panel);
    }
}
