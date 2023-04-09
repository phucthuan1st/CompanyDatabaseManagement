package com.csdlcongty.users;

// import com.csdlcongty.DBManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.JFrame;

public class NormalUserController extends JFrame implements ActionListener {
    // private final DBManager dbc;
    // private final JFrame father;

    public NormalUserController(String username, String password, JFrame father)
            throws ClassNotFoundException, SQLException {
        // dbc = new DBManager(username, password);
        // this.father = father;

        // Set window properties
        this.setTitle("Login");
        this.setSize(1024, 768);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
