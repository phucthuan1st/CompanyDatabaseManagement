package com.csdlcongty.users;

import com.csdlcongty.DBManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.*;

public class NormalUserController extends JFrame implements ActionListener {
     private final DBManager dbc;
     private final JFrame father;

     private JSplitPane mainSplitPane;

     private JPanel leftPanel;

     private JButton showDepartmentInfo;

     private JButton showSchemeInfo;

     private JButton showAssignmentInfo;

     private JButton showRelevantInfo;

     private JButton showSalaryAndAllowance;

     private JButton changeSecretKey;

     private JButton forgetSecretKey;

     private JButton showAdminMessages;

     private JButton updatePersonalInfo;

     private JButton modifyAsignment;

     private JButton updateSalaryAndAllowance;

     private JButton updateEmployeeInfo;

     private JButton insertNewEmployee;

     private JButton modifyDepartment;

     private JButton modifyScheme;

    public NormalUserController(String username, String password, JFrame father)
            throws ClassNotFoundException, SQLException {

        this.dbc = new DBManager(username, password);
        this.father = father;
        this.initComponents();
    }

    private void initComponents() {
        // Set window properties
        this.setTitle("Dashboard");
        this.setSize(1024, 768);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        this.mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        GridBagLayout layout = new GridBagLayout();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        this.leftPanel = new JPanel(layout);

        constraints.gridx = 0;
        constraints.gridy = 0;

        // Thông điệp
        this.showAdminMessages = new JButton("Xem tin nhắn từ người quản trị");

        // Khóa
        this.changeSecretKey = new JButton("Đổi khóa bí mật mới");
        this.forgetSecretKey = new JButton("Quên khóa bí mật?");

        // Phòng ban
        this.showDepartmentInfo = new JButton("Xem thông tin các phòng ban");
        this.modifyDepartment = new JButton("Thay đổi thông tin các Phòng ban");

        // Đề án
        this.showSchemeInfo = new JButton("Xem các đề án hiện có");
        this.modifyScheme = new JButton("Thay đổi các đề án");

        // Nhân viên
        this.showSalaryAndAllowance = new JButton("Xem Lương và Phụ cấp");
        this.showRelevantInfo = new JButton("Xem thông tin các Nhân viên đang quản lí");
        this.updateSalaryAndAllowance = new JButton("Cập nhật Lương và Phụ cấp");
        this.updateEmployeeInfo = new JButton("Cập nhật thông tin nhân viên");
        this.updatePersonalInfo = new JButton("Cập nhật thông tin cá nhân");
        this.insertNewEmployee = new JButton("Thêm nhân viên mới");

        // Phân công
        this.showAssignmentInfo = new JButton("Xem phân công trên các đề án");
        this.modifyAsignment = new JButton("Cập nhật phân công");

        this.mainSplitPane.setLeftComponent(this.leftPanel);

        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
