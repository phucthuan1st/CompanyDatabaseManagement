package com.csdlcongty;

import com.csdlcongty.dba.DBAdminController;
import com.csdlcongty.users.NormalUserController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

// include Login screen and handling for pressing Login button
public class LoginController extends JFrame implements ActionListener {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;

    public LoginController(int wWidth, int wHeight) throws IOException {
        // Set window properties
        setTitle("Login");
        setSize(wWidth, wHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        this.setResizable(false);

        // Initialize components
        URL url = new URL("https://raw.githubusercontent.com/phucthuan1st/CompanyDatabaseManagement/master/Resources/Logo.png");
        JLabel loginLabel;
        try {
            Icon icon = new ImageIcon(url);
            loginLabel = new JLabel(icon);
        } catch (Exception ex) {
            loginLabel = new JLabel("LOGIN");
        }

        JLabel companyLogo = loginLabel;
        companyLogo.setVisible(true);

        // Declare components
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");

        // Add components to the window
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.gridx = 4;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        panel.add(companyLogo, constraints);

        constraints.gridheight = 1;
        constraints.gridwidth = 1;

        constraints.gridx = 3;
        constraints.gridy = 3;
        panel.add(usernameLabel, constraints);

        constraints.gridx = 4;
        panel.add(usernameField, constraints);

        constraints.gridx = 3;
        constraints.gridy = 4;
        panel.add(passwordLabel, constraints);

        constraints.gridx = 4;
        panel.add(passwordField, constraints);

        constraints.gridx = 4;
        constraints.gridy = 5;
        panel.add(loginButton, constraints);

        add(panel);

        initialActionListener();
        setVisible(true);
    }

    private void initialActionListener() {
        // Set action listeners for buttons
        loginButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            authenticateLogin();
        }
    }

    private void authenticateLogin() {
        // Implement login logic
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String role;
        boolean accessGranted = false;

        // normalize username
        username = username.toUpperCase();
        if ("COMPANY_PUBLIC".equals(username)) {
            role = "DBA";
        } else {
            role = "normal";
        }

        // try to connect to database to check if credentials is true
        try {
            DBManager dbc = new DBManager(username, password);
            accessGranted = true;
            JOptionPane.showMessageDialog(this, "Access Granted");

                /*
                    Nếu trong máy chưa có record nào trong bảng NHANVIEN thì bỏ comment dòng bên dưới
                */
            //dbc.insertMockRecordToNhanVien();
            //dbc.writeNhanVienRecordsToFile(dbc.getDecryptedNhanVienRecords(), "NhanVienRecord.csv");

            dbc.cnt.close();
        } catch (SQLException ex) {
            String cause = ex.getMessage();
            String message;
            if (cause.contains("could not establish the connection")) {
                message = "Cannot access database: database might offline!";
            } else {
                message = "Username or password is not correct";
            }
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Unexpected error!", "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (accessGranted) {

            try {
                if ("DBA".equals(role)) {
                    DBAdminController adminSession = new DBAdminController(username, password, this);
                    changeTo(adminSession);
                } else {
                    NormalUserController userSession = new NormalUserController(username, password, this);
                    changeTo(userSession);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Cannot connect to database!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Unexpected error!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    public void changeTo(JFrame other) {
        this.setVisible(false);
        other.setVisible(true);
    }
}
