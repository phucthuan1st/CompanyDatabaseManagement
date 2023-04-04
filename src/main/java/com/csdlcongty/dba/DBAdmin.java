/**
 * @project CompanyDatabaseOperation
 * @author 20H3T-02
 */
package com.csdlcongty.dba;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import com.csdlcongty.DBConnection;
import java.sql.SQLException;

public class DBAdmin extends JFrame implements ActionListener {
    private final DBConnection dbc;
    
    public DBAdmin(String password) throws ClassNotFoundException, SQLException {
        dbc = new DBConnection("sys as SYSDBA", password);
        
        // Set window properties
        setTitle("Login");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.dispose();
    }
}
