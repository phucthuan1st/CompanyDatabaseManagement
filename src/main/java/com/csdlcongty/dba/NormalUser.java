package com.csdlcongty.dba;

import com.csdlcongty.DBConnection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.JFrame;

public class NormalUser extends JFrame implements ActionListener {
    private final DBConnection dbc;
    
    public NormalUser(String username, String password) throws ClassNotFoundException, SQLException {
        dbc = new DBConnection(username, password);
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
