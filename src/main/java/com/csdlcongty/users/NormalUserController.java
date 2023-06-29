package com.csdlcongty.users;

import com.csdlcongty.DBManager;
import com.csdlcongty.helper.CryptographyUtilities;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;

public class NormalUserController extends JFrame implements ActionListener {
    private final DBManager dbc;
    private ResultSet result;
    private final JFrame father;
    private JSplitPane subRightSplits;
    private JPanel rightPanel;

    // Employee data fields
    private final JTextField luongField = new JTextField(60);
    private final JTextField phucapField = new JTextField(60);
    private final JTextField manvField = new JTextField(10);
    private final JTextField tennvField = new JTextField(35);
    private final JTextField phaiField = new JTextField(10);
    private final JTextField ngaysinhField = new JTextField(10);
    private final JTextField diachiField = new JTextField(60);
    private final JTextField sodtField = new JTextField(20);
    private final JTextField manqlField = new JTextField(10);
    private final JTextField phgField = new JTextField(10);
    private final JTextField vaitroField = new JTextField(20);

    public NormalUserController(String username, String password, JFrame father)
            throws ClassNotFoundException, SQLException {

        this.dbc = new DBManager(username, password);
        this.father = father;
        this.initComponents();
        this.fetchPersonalInformation(username);
    }

    private void initComponents() {
        // Set window properties
        // Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        this.setTitle("Dashboard");
        this.setSize(1600, 900);
        //this.setSize(r.width, r.height);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Create left panel
        JPanel leftPanel = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 10, 5, 10);
        constraints.anchor = GridBagConstraints.WEST;

        // Group 1 (Left column)
        JPanel group1Panel = new JPanel(new GridBagLayout());
        JLabel group1Label = new JLabel("Phòng ban và đề án");
        JButton showDepartmentInfoButton = new JButton("Xem thông tin các phòng ban");
        showDepartmentInfoButton.addActionListener(this);
        JButton modifyDepartmentButton = new JButton("Thay đổi thông tin các Phòng ban");
        modifyDepartmentButton.addActionListener(this);
        JButton showSchemeInfoButton = new JButton("Xem các đề án hiện có");
        showSchemeInfoButton.addActionListener(this);
        JButton modifySchemeButton = new JButton("Thay đổi các đề án");
        modifySchemeButton.addActionListener(this);
        applyLineBorder(group1Panel);
        constraints.gridx = 0;
        constraints.gridy = 0;
        group1Panel.add(group1Label, constraints);
        constraints.gridy++;
        group1Panel.add(showDepartmentInfoButton, constraints);
        constraints.gridy++;
        group1Panel.add(modifyDepartmentButton, constraints);
        constraints.gridy++;
        group1Panel.add(showSchemeInfoButton, constraints);
        constraints.gridy++;
        group1Panel.add(modifySchemeButton, constraints);

        // Group 2 (Right column)
        JPanel group2Panel = new JPanel(new GridBagLayout());
        JLabel group2Label = new JLabel("Phân công");
        JButton showAssignmentInfoButton = new JButton("Xem phân công trên các đề án");
        showAssignmentInfoButton.addActionListener(this);
        JButton modifyAssignmentButton = new JButton("Cập nhật phân công");
        modifyAssignmentButton.addActionListener(this);
        applyLineBorder(group2Panel);
        constraints.gridx = 1;
        constraints.gridy = 0;
        group2Panel.add(group2Label, constraints);
        constraints.gridy++;
        group2Panel.add(showAssignmentInfoButton, constraints);
        constraints.gridy++;
        group2Panel.add(modifyAssignmentButton, constraints);

        // Group 3 (Left column)
        JPanel group3Panel = new JPanel(new GridBagLayout());
        JLabel group3Label = new JLabel("Quản lí nhân sự");
        JButton insertNewEmployeeButton = new JButton("Thêm nhân viên mới");
        insertNewEmployeeButton.addActionListener(this);
        JButton updateEmployeeInfoButton = new JButton("Cập nhật thông tin nhân viên");
        updateEmployeeInfoButton.addActionListener(this);
        applyLineBorder(group3Panel);
        constraints.gridx = 0;
        constraints.gridy = 0;
        group3Panel.add(group3Label, constraints);
        constraints.gridy++;
        JButton showRelevantInfoButton = new JButton("Xem thông tin các Nhân viên đang quản lí");
        showRelevantInfoButton.addActionListener(this);
        group3Panel.add(showRelevantInfoButton, constraints);
        constraints.gridy++;
        group3Panel.add(insertNewEmployeeButton, constraints);
        constraints.gridy++;
        group3Panel.add(updateEmployeeInfoButton, constraints);

        // Group 4 (Right column)
        JPanel group4Panel = new JPanel(new GridBagLayout());
        JLabel group4Label = new JLabel("Lương và phụ cấp");
        JButton showSalaryAndAllowanceButton = new JButton("Xem Lương và Phụ cấp");
        showSalaryAndAllowanceButton.addActionListener(this);
        JButton updateSalaryAndAllowanceButton = new JButton("Cập nhật Lương và Phụ cấp");
        updateSalaryAndAllowanceButton.addActionListener(this);
        applyLineBorder(group4Panel);
        constraints.gridx = 1;
        constraints.gridy = 0;
        group4Panel.add(group4Label, constraints);
        constraints.gridy++;
        group4Panel.add(showSalaryAndAllowanceButton, constraints);
        constraints.gridy++;
        group4Panel.add(updateSalaryAndAllowanceButton, constraints);

        // Group 5 (Left column)
        JPanel group5Panel = new JPanel(new GridBagLayout());
        JLabel group5Label = new JLabel("Cá nhân hóa");
        applyLineBorder(group5Panel);
        constraints.gridx = 0;
        constraints.gridy = 0;
        group5Panel.add(group5Label, constraints);
        constraints.gridy++;
        JButton showAdminMessagesButton = new JButton("Xem tin nhắn từ người quản trị");
        showAdminMessagesButton.addActionListener(this);
        group5Panel.add(showAdminMessagesButton, constraints);
        constraints.gridy++;
        JButton updatePersonalInfoButton = new JButton("Cập nhật thông tin cá nhân");
        updatePersonalInfoButton.addActionListener(this);
        group5Panel.add(updatePersonalInfoButton, constraints);

        // Group 6 (Right column)
        JPanel group6Panel = new JPanel(new GridBagLayout());
        JLabel group6Label = new JLabel("Quản lí khóa");
        applyLineBorder(group6Panel);
        constraints.gridx = 1;
        constraints.gridy = 0;
        group6Panel.add(group6Label, constraints);
        JButton changeSecretKeyButton = new JButton("Đổi khóa bí mật mới");
        JButton forgetSecretKeyButton = new JButton("Quên khóa bí mật?");
        constraints.gridy++;
        group6Panel.add(changeSecretKeyButton, constraints);
        constraints.gridy++;
        group6Panel.add(forgetSecretKeyButton, constraints);

        // Add groups to the left panel
        constraints.gridx = 0;
        constraints.gridy = 0;
        leftPanel.add(group1Panel, constraints);
        constraints.gridy++;
        leftPanel.add(group3Panel, constraints);
        constraints.gridy++;
        leftPanel.add(group5Panel, constraints);

        // Add groups to the right panel
        constraints.gridx = 1;
        constraints.gridy = 0;
        leftPanel.add(group2Panel, constraints);
        constraints.gridy++;
        leftPanel.add(group4Panel, constraints);
        constraints.gridy++;
        leftPanel.add(group6Panel, constraints);

        // Add a logout button
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.addActionListener(this);
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        leftPanel.add(logoutButton, constraints);

        // create right panel
        this.rightPanel = new JPanel();

        this.subRightSplits = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel upperPanel = new JPanel(new GridBagLayout());

        constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 10, 5, 10);
        constraints.anchor = GridBagConstraints.WEST;

        constraints.gridx = 1;
        constraints.gridy = 0;
        upperPanel.add(new JLabel("Mã số"), constraints);
        constraints.gridx++;
        upperPanel.add(manvField, constraints);

        constraints.gridy++;
        constraints.gridx = 1;
        upperPanel.add(new JLabel("Họ tên"), constraints);
        constraints.gridx++;
        upperPanel.add(tennvField, constraints);

        constraints.gridx = 1;
        constraints.gridy++;
        upperPanel.add(new JLabel("Ngày sinh"), constraints);
        constraints.gridx++;
        upperPanel.add(ngaysinhField, constraints);

        constraints.gridx = 1;
        constraints.gridy++;
        upperPanel.add(new JLabel("Giới tính"), constraints);
        constraints.gridx++;
        upperPanel.add(phaiField, constraints);

        constraints.gridx = 1;
        constraints.gridy++;
        upperPanel.add(new JLabel("Chức vụ"), constraints);
        constraints.gridx++;
        upperPanel.add(vaitroField, constraints);

        constraints.gridx = 1;
        constraints.gridy++;
        upperPanel.add(new JLabel("Địa chỉ"), constraints);
        constraints.gridx++;
        upperPanel.add(diachiField, constraints);

        constraints.gridx = 1;
        constraints.gridy++;
        upperPanel.add(new JLabel("Số điện thoại"), constraints);
        constraints.gridx++;
        upperPanel.add(sodtField, constraints);

        constraints.gridx = 1;
        constraints.gridy++;
        upperPanel.add(new JLabel("Mã phòng ban"), constraints);
        constraints.gridx++;
        upperPanel.add(phgField, constraints);

        constraints.gridx = 1;
        constraints.gridy++;
        upperPanel.add(new JLabel("Mã người quản lí"), constraints);
        constraints.gridx++;
        upperPanel.add(manqlField, constraints);

        constraints.gridx = 1;
        constraints.gridy++;
        upperPanel.add(new JLabel("Lương"), constraints);
        constraints.gridx++;
        upperPanel.add(luongField, constraints);

        constraints.gridx = 1;
        constraints.gridy++;
        upperPanel.add(new JLabel("Phụ cấp"), constraints);
        constraints.gridx++;
        upperPanel.add(phucapField, constraints);

        // Set the fields as uneditable
        luongField.setEditable(false);
        phucapField.setEditable(false);
        manvField.setEditable(false);
        tennvField.setEditable(false);
        phaiField.setEditable(false);
        ngaysinhField.setEditable(false);
        diachiField.setEditable(false);
        sodtField.setEditable(false);
        manqlField.setEditable(false);
        phgField.setEditable(false);
        vaitroField.setEditable(false);

        subRightSplits.setTopComponent(upperPanel);

        this.rightPanel.add(subRightSplits);

        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(this.rightPanel);

        mainSplitPane.setResizeWeight(0.5);
        mainSplitPane.setDividerLocation(0.5);

        this.add(mainSplitPane);
        this.setVisible(true);
    }

    private TableModel buildTableModel(ResultSet rs, int numRows) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // Get dataLengthField of columns
        int columns = metaData.getColumnCount();

        // Create columnNameField names array
        String[] columnNames = new String[columns];
        for (int i = 1; i <= columns; i++) {
            columnNames[i - 1] = metaData.getColumnName(i);
        }

        // Create data array
        Object[][] data = new Object[numRows][columns];
        int row = 0;
        while (rs.next() && row < numRows) {
            for (int i = 1; i <= columns; i++) {
                data[row][i - 1] = rs.getObject(i);
            }
            row++;
        }

        return new DefaultTableModel(data, columnNames);
    }

    private void displayLowerPanelTable(int numRows) throws SQLException {
        JTable table = new JTable(buildTableModel(this.result, numRows)) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Set autoResizeMode
        JScrollPane bottomPane = new JScrollPane(table);
        this.subRightSplits.setBottomComponent(bottomPane);

        this.rightPanel.revalidate();
        this.rightPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        String command = button.getText();

        if ("Đăng xuất".equals(command)) {
            father.setVisible(true); // Display the father frame
            this.dispose(); // Dispose the current frame
        }

        try {
            if ("Xem thông tin các phòng ban".equals(command)) {
                handleShowPHONGBAN();
            } else if ("Xem các đề án hiện có".equals(command)) {
                handleShowDEAN();
            } else if ("Xem phân công trên các đề án".equals(command)) {
                handleShowPHANCONG();
            } else if ("Xem thông tin các Nhân viên đang quản lí".equals(command)) {
                handleShowNHANVIEN();
            } else if ("Xem Lương và Phụ cấp".equals(command)) {
                handleShowLUONGPHUCAP();
            } else if ("Thay đổi thông tin các Phòng ban".equals(command)) {
                handleModifyPHONGBAN();
            } else if ("Thay đổi các đề án".equals(command)) {
                handleModifyDEAN();
            } else if ("Cập nhật phân công".equals(command)) {
                handleModifyPHANCONG();
            } else if ("Cập nhật thông tin cá nhân".equals(command)) {
                handleUpdatePersonalInfomation();
            } else if ("Xem tin nhắn từ người quản trị".equals(command)) {
                handleShowAdminMessage();
            } else if ("Cập nhật Lương và Phụ cấp".equals(command)) {
                handleUpdateSalaryAndAllowanceButton();
            } else if("Thêm nhân viên mới".equals(command)){
                handleModifyNHANVIEN("Thêm");
            } else if("Cập nhật thông tin nhân viên".equals(command))
            {
                handleModifyNHANVIEN("Cập nhật");
            }
        } catch (SQLException ex) {
            String message = "Error when communicate with database: " + ex.getMessage();
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void handleUpdateSalaryAndAllowanceButton() {
        JTextField curentMaNVField = new JTextField(10);
        JTextField newLuongField = new JTextField(20);
        JTextField newPhuCapField = new JTextField(20);

        JButton button = new JButton("Cập nhật");
        this.subRightSplits.setBottomComponent(new JPanel() {
            {
                setLayout(new GridBagLayout());

                GridBagConstraints constraints = new GridBagConstraints();
                constraints.insets = new Insets(5, 10, 5, 10);
                constraints.anchor = GridBagConstraints.WEST;

                constraints.gridx = 0;
                constraints.gridy = 0;

                JPanel newInfoPanel = new JPanel();
                newInfoPanel.setBorder(BorderFactory.createTitledBorder("Thông tin mới"));

                newInfoPanel.setLayout(new GridBagLayout());
                GridBagConstraints newInfoConstraints = new GridBagConstraints();

                newInfoConstraints.insets = new Insets(5, 10, 5, 10);
                newInfoConstraints.anchor = GridBagConstraints.WEST;

                newInfoConstraints.gridx = 0;
                newInfoConstraints.gridy++;
                newInfoPanel.add(new JLabel("Mã nhân viên cần cập nhật"), newInfoConstraints);
                newInfoConstraints.gridx++;
                newInfoPanel.add(curentMaNVField, newInfoConstraints);

                newInfoConstraints.gridx = 0;
                newInfoConstraints.gridy++;
                newInfoPanel.add(new JLabel("Lương"), newInfoConstraints);
                newInfoConstraints.gridx++;
                newInfoPanel.add(newLuongField, newInfoConstraints);

                newInfoConstraints.gridx = 0;
                newInfoConstraints.gridy++;
                newInfoPanel.add(new JLabel("Phụ cấp"), newInfoConstraints);
                newInfoConstraints.gridx++;
                newInfoPanel.add(newPhuCapField, newInfoConstraints);

                constraints.gridx = 0;
                constraints.gridy++;
                constraints.gridwidth = 2;

                add(newInfoPanel, constraints);
                constraints.gridy++;
                constraints.gridwidth = 1;

                constraints.gridx = 1;
                constraints.gridy++;
                add(button, constraints);
                button.addActionListener(this::buttonActionPerformed);
            }

            private void buttonActionPerformed(ActionEvent e) {
                JButton button = (JButton) e.getSource();
                String command = button.getText();

                if ("Cập nhật".equals(command)) {
                    String maNV = curentMaNVField.getText();
                    String newLuong = newLuongField.getText();
                    String newPhuCap = newPhuCapField.getText();

                    try {
                        dbc.updateSalaryAndAllowance(maNV, newLuong, newPhuCap);
                        JOptionPane.showMessageDialog(this, "Cập nhật thông tin cá nhân thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        fetchPersonalInformation(manvField.getText());
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Sai khóa bí mật hoặc dữ liệu đã bị hỏng", "Lỗi mã hóa", JOptionPane.ERROR_MESSAGE);
                    }
                }

            }
        });

        this.rightPanel.revalidate();
        this.rightPanel.repaint();
    }

    private void handleShowAdminMessage() throws SQLException {
        result = dbc.selectFromTable("THONGDIEP");
        int num_rows = dbc.getNumberRowsOf("COMPANY_PUBLIC.THONGDIEP");
        displayLowerPanelTable(num_rows);
    }

    private void handleShowPHONGBAN() throws SQLException {
        result = dbc.selectFromTable("PHONGBAN");
        int num_rows = dbc.getNumberRowsOf("COMPANY_PUBLIC.PHONGBAN");
        displayLowerPanelTable(num_rows);
    }

    private void handleShowDEAN() throws SQLException {
        result = dbc.selectFromTable("DEAN");
        int num_rows = dbc.getNumberRowsOf("COMPANY_PUBLIC.DEAN");
        displayLowerPanelTable(num_rows);
    }

    private void handleShowPHANCONG() throws SQLException {
        result = dbc.selectFromTable("PHANCONG");
        int num_rows = dbc.getNumberRowsOf("COMPANY_PUBLIC.PHANCONG");
        displayLowerPanelTable(num_rows);
    }

    private void handleShowNHANVIEN() throws SQLException {
        result = dbc.selectFromTable("NHANVIEN");
        int num_rows = dbc.getNumberRowsOf("COMPANY_PUBLIC.NHANVIEN");
        displayLowerPanelTable(num_rows);
    }

    private void handleShowLUONGPHUCAP() throws Exception {

        String luong = this.luongField.getText();
        String phucap = this.phucapField.getText();
        String secretKey = JOptionPane.showInputDialog(this, "Nhập khóa bí mật", "Thông báo", JOptionPane.QUESTION_MESSAGE);

        String key = CryptographyUtilities.hashMD5(secretKey);
        luong = CryptographyUtilities.decryptAES(luong, key);
        phucap = CryptographyUtilities.decryptAES(phucap, key);

        this.luongField.setText(luong);
        this.phucapField.setText(phucap);

    }

    private void fetchPersonalInformation(String id) throws SQLException {
        ResultSet resultSet = dbc.getPersonalInfomation(id);

        if (resultSet.next()) {
            String luong = resultSet.getString("LUONG");
            String phucap = resultSet.getString("PHUCAP");
            String manv = resultSet.getString("MANV");
            String tennv = resultSet.getString("TENNV");
            String phai = resultSet.getString("PHAI");
            Date ngaysinh = resultSet.getDate("NGAYSINH");
            String diachi = resultSet.getString("DIACHI");
            String sodt = resultSet.getString("SODT");
            String manql = resultSet.getString("MANQL");
            String phg = resultSet.getString("PHG");

            luongField.setText(luong);
            phucapField.setText(phucap);
            manvField.setText(manv);
            tennvField.setText(tennv);
            phaiField.setText(phai);
            ngaysinhField.setText(ngaysinh.toString());
            diachiField.setText(diachi);
            sodtField.setText(sodt);
            manqlField.setText(manql);
            phgField.setText(phg);
        }

        resultSet.close();

    }

    private void handleModifyDEAN() throws SQLException {
        var option = new String[]{"Thêm", "Cập nhật", "Xóa"};

        int choose = JOptionPane.showOptionDialog(this, "Chọn thao tác?", "Selection", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, option, null);

        if (choose < 0) {
            return;
        }

        String operationType = option[choose];

        JTextField oldMADAField = new JTextField(10);
        JTextField newMADAField = new JTextField(10);
        JTextField newTenDAField = new JTextField(30);
        JTextField newNgayBDField = new JTextField(10);
        JTextField newPhongField = new JTextField(10);
        JTextField newTruongDeanField = new JTextField(10);

        JButton button = new JButton(operationType);

        this.subRightSplits.setBottomComponent(new JPanel() {
            {
                setLayout(new GridBagLayout());

                GridBagConstraints constraints = new GridBagConstraints();
                constraints.insets = new Insets(5, 10, 5, 10);
                constraints.anchor = GridBagConstraints.WEST;

                constraints.gridx = 0;
                constraints.gridy = 0;

                if ("Cập nhật".equals(operationType) || "Xóa".equals(operationType)) {
                    JPanel oldInfoPanel = new JPanel();
                    oldInfoPanel.setBorder(BorderFactory.createTitledBorder("Đề án cũ"));

                    oldInfoPanel.setLayout(new GridBagLayout());
                    GridBagConstraints oldInfoConstraints = new GridBagConstraints();
                    oldInfoConstraints.insets = new Insets(5, 10, 5, 10);
                    oldInfoConstraints.anchor = GridBagConstraints.WEST;

                    oldInfoConstraints.gridx = 0;
                    oldInfoConstraints.gridy = 0;
                    oldInfoPanel.add(new JLabel(String.format("Mã Đề án cần %s", operationType)), oldInfoConstraints);
                    oldInfoConstraints.gridx++;
                    oldInfoPanel.add(oldMADAField, oldInfoConstraints);
                    oldInfoConstraints.gridy++;

                    add(oldInfoPanel, constraints);
                    constraints.gridy++;
                }


                if (!"Xóa".equals(operationType)) {
                    JPanel newInfoPanel = new JPanel();
                    newInfoPanel.setBorder(BorderFactory.createTitledBorder("Đề án mới"));

                    newInfoPanel.setLayout(new GridBagLayout());
                    GridBagConstraints newInfoConstraints = new GridBagConstraints();

                    newInfoConstraints.insets = new Insets(5, 10, 5, 10);
                    newInfoConstraints.anchor = GridBagConstraints.WEST;

                    newInfoConstraints.gridx = 0;
                    newInfoConstraints.gridy = 0;
                    newInfoPanel.add(new JLabel("Mã đề án"), newInfoConstraints);
                    newInfoConstraints.gridx++;
                    newInfoPanel.add(newMADAField, newInfoConstraints);

                    newInfoConstraints.gridx = 0;
                    newInfoConstraints.gridy++;
                    newInfoPanel.add(new JLabel("Tên đề án"), newInfoConstraints);
                    newInfoConstraints.gridx++;
                    newInfoPanel.add(newTenDAField, newInfoConstraints);

                    newInfoConstraints.gridx = 0;
                    newInfoConstraints.gridy++;
                    newInfoPanel.add(new JLabel("Ngày bắt đầu đề án"), newInfoConstraints);
                    newInfoConstraints.gridx++;
                    newInfoPanel.add(newNgayBDField, newInfoConstraints);

                    newInfoConstraints.gridx = 0;
                    newInfoConstraints.gridy++;
                    newInfoPanel.add(new JLabel("Phòng ban quản lí"), newInfoConstraints);
                    newInfoConstraints.gridx++;
                    newInfoPanel.add(newPhongField, newInfoConstraints);

                    newInfoConstraints.gridx = 0;
                    newInfoConstraints.gridy++;
                    newInfoPanel.add(new JLabel("Trưởng đề án"), newInfoConstraints);
                    newInfoConstraints.gridx++;
                    newInfoPanel.add(newTruongDeanField, newInfoConstraints);

                    constraints.gridx = 0;
                    constraints.gridy++;
                    constraints.gridwidth = 2;
                    add(newInfoPanel, constraints);
                    constraints.gridy++;
                    constraints.gridwidth = 1;
                }

                constraints.gridx = 1;
                constraints.gridy++;
                add(button, constraints);
                button.addActionListener(this::buttonActionPerformed);
            }

            private void buttonActionPerformed(ActionEvent e) {
                JButton button = (JButton) e.getSource();
                String command = button.getText();

                switch (command) {
                    case "Thêm" -> {
                        String maDA = newMADAField.getText();
                        String tenDA = newTenDAField.getText();
                        String ngayBD = newNgayBDField.getText();
                        String phong = newPhongField.getText();
                        String truongDeAn = newTruongDeanField.getText();

                        try {
                            dbc.insertDeAnRecord(maDA, tenDA, ngayBD, phong, truongDeAn);
                            JOptionPane.showMessageDialog(this, "Đã thêm đề án: " + tenDA, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                            handleShowPHONGBAN();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
                        } catch (ParseException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage() + ": Date must be format dd/MM/yyyy", "Date Error", JOptionPane.ERROR_MESSAGE);
                        }

                    }
                    case "Cập nhật" -> {
                        String oldMaDA = oldMADAField.getText();
                        String maDA = newMADAField.getText();
                        String tenDA = newTenDAField.getText();
                        String ngayBD = newNgayBDField.getText();
                        String phong = newPhongField.getText();
                        String truongDeAn = newTruongDeanField.getText();

                        try {
                            dbc.updateDeAnRecord(oldMaDA, maDA, tenDA, ngayBD, phong, truongDeAn);
                            JOptionPane.showMessageDialog(this, "Đã cập nhật đề án: " + tenDA, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                            handleShowPHONGBAN();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
                        } catch (ParseException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage() + ": Date must be format dd/MM/yyyy", "Date Error", JOptionPane.ERROR_MESSAGE);
                        }

                    }
                    case "Xóa" -> {
                        String oldMaDA = oldMADAField.getText();

                        try {
                            dbc.deleteDeAnRecord(oldMaDA);
                            JOptionPane.showMessageDialog(this, "Đã xóa đề án: " + oldMaDA, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                            handleShowPHONGBAN();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
                        }

                    }
                    default ->
                            JOptionPane.showMessageDialog(this, "Invalid operation", "Operation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        this.rightPanel.revalidate();
        this.rightPanel.repaint();
    }

    private void handleModifyPHONGBAN() throws SQLException {
        var option = new String[]{"Thêm", "Cập nhật"};
        String message = "Chọn thao tác?";

        int choose = JOptionPane.showOptionDialog(this, message, "Selection", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, option, null);

        if (choose < 0) {
            return;
        }

        String operationType = option[choose];

        JTextField oldMAPBField = new JTextField(10);
        JTextField newMAPBField = new JTextField(10);
        JTextField newTenPBField = new JTextField(30);
        JTextField newTRPHGField = new JTextField(10);

        JButton button = new JButton(operationType);

        this.subRightSplits.setBottomComponent(new JPanel() {
            {
                setLayout(new GridBagLayout());

                GridBagConstraints constraints = new GridBagConstraints();
                constraints.insets = new Insets(5, 10, 5, 10);
                constraints.anchor = GridBagConstraints.WEST;

                constraints.gridx = 0;
                constraints.gridy = 0;

                if ("Cập nhật".equals(operationType)) {
                    JPanel oldInfoPanel = new JPanel();
                    oldInfoPanel.setBorder(BorderFactory.createTitledBorder("Phòng ban cũ"));

                    oldInfoPanel.setLayout(new GridBagLayout());
                    GridBagConstraints oldInfoConstraints = new GridBagConstraints();
                    oldInfoConstraints.insets = new Insets(5, 10, 5, 10);
                    oldInfoConstraints.anchor = GridBagConstraints.WEST;

                    oldInfoConstraints.gridx = 0;
                    oldInfoConstraints.gridy = 0;
                    oldInfoPanel.add(new JLabel(String.format("Mã Phòng ban cần %s", operationType)), oldInfoConstraints);
                    oldInfoConstraints.gridx++;
                    oldInfoPanel.add(oldMAPBField, oldInfoConstraints);
                    oldInfoConstraints.gridy++;

                    add(oldInfoPanel, constraints);
                    constraints.gridy++;
                }


                JPanel newInfoPanel = new JPanel();
                newInfoPanel.setBorder(BorderFactory.createTitledBorder("Phòng ban mới"));

                newInfoPanel.setLayout(new GridBagLayout());
                GridBagConstraints newInfoConstraints = new GridBagConstraints();

                newInfoConstraints.insets = new Insets(5, 10, 5, 10);
                newInfoConstraints.anchor = GridBagConstraints.WEST;

                newInfoConstraints.gridx = 0;
                newInfoConstraints.gridy = 0;
                newInfoPanel.add(new JLabel("Mã phòng ban"), newInfoConstraints);
                newInfoConstraints.gridx++;
                newInfoPanel.add(newMAPBField, newInfoConstraints);

                newInfoConstraints.gridx = 0;
                newInfoConstraints.gridy++;
                newInfoPanel.add(new JLabel("Tên phòng ban"), newInfoConstraints);
                newInfoConstraints.gridx++;
                newInfoPanel.add(newTenPBField, newInfoConstraints);

                newInfoConstraints.gridx = 0;
                newInfoConstraints.gridy++;
                newInfoPanel.add(new JLabel("Mã trưởng phòng"), newInfoConstraints);
                newInfoConstraints.gridx++;
                newInfoPanel.add(newTRPHGField, newInfoConstraints);

                constraints.gridx = 0;
                constraints.gridy++;
                constraints.gridwidth = 2;
                add(newInfoPanel, constraints);
                constraints.gridy++;
                constraints.gridwidth = 1;

                constraints.gridx = 1;
                constraints.gridy++;
                add(button, constraints);
                button.addActionListener(this::buttonActionPerformed);
            }

            private void buttonActionPerformed(ActionEvent e) {
                JButton button = (JButton) e.getSource();
                String command = button.getText();

                String MaPB = newMAPBField.getText();
                String TenPB = newTenPBField.getText();
                String TrPhg = newTRPHGField.getText();

                switch (command) {
                    case "Thêm" -> {
                        try {
                            dbc.insertPhongBanRecord(MaPB, TenPB, TrPhg);
                            JOptionPane.showMessageDialog(this, "Đã thêm phòng " + TenPB, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                            handleShowPHONGBAN();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
                        }

                    }
                    case "Cập nhật" -> {
                        String oldMaPB = oldMAPBField.getText();

                        try {
                            dbc.updatePhongBanRecord(oldMaPB, MaPB, TenPB, TrPhg);
                            JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin phòng " + TenPB, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                            handleShowPHONGBAN();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
                        }

                    }
                    default ->
                            JOptionPane.showMessageDialog(this, "Invalid operation", "Operation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        this.rightPanel.revalidate();
        this.rightPanel.repaint();
    }

    private void handleModifyPHANCONG() {
        var option = new String[]{"Thêm", "Xóa", "Cập nhật"};
        String message = "Chọn thao tác?";

        int choose = JOptionPane.showOptionDialog(this, message, "Selection", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, option, null);

        if (choose < 0) {
            return;
        }

        String operationType = option[choose];

        JTextField oldMaNVField = new JTextField(10);
        JTextField oldMaDaField = new JTextField(10);
        JTextField newMaNVField = new JTextField(10);
        JTextField newMaDaField = new JTextField(10);
        JTextField newThoiGianField = new JTextField(10);

        JButton button = new JButton(operationType);

        this.subRightSplits.setBottomComponent(new JPanel() {
            {
                setLayout(new GridBagLayout());

                GridBagConstraints constraints = new GridBagConstraints();
                constraints.insets = new Insets(5, 10, 5, 10);
                constraints.anchor = GridBagConstraints.WEST;

                constraints.gridx = 0;
                constraints.gridy = 0;

                if ("Cập nhật".equals(operationType) || "Xóa".equals(operationType)) {
                    JPanel oldInfoPanel = new JPanel();
                    oldInfoPanel.setBorder(BorderFactory.createTitledBorder("Phân công cũ"));

                    oldInfoPanel.setLayout(new GridBagLayout());
                    GridBagConstraints oldInfoConstraints = new GridBagConstraints();
                    oldInfoConstraints.insets = new Insets(5, 10, 5, 10);
                    oldInfoConstraints.anchor = GridBagConstraints.WEST;

                    oldInfoConstraints.gridx = 0;
                    oldInfoConstraints.gridy = 0;
                    oldInfoPanel.add(new JLabel(String.format("Mã Nhân viên cần %s", operationType)), oldInfoConstraints);
                    oldInfoConstraints.gridx++;
                    oldInfoPanel.add(oldMaNVField, oldInfoConstraints);
                    oldInfoConstraints.gridy++;
                    oldInfoConstraints.gridx = 0;
                    oldInfoPanel.add(new JLabel(String.format("Mã Đề án cần %s", operationType)), oldInfoConstraints);
                    oldInfoConstraints.gridx++;
                    oldInfoPanel.add(oldMaDaField, oldInfoConstraints);
                    oldInfoConstraints.gridy++;

                    add(oldInfoPanel, constraints);
                    constraints.gridy++;
                }

                if (!"Xóa".equals(operationType)) {
                    JPanel newInfoPanel = new JPanel();
                    newInfoPanel.setBorder(BorderFactory.createTitledBorder("Phân công mới"));

                    newInfoPanel.setLayout(new GridBagLayout());
                    GridBagConstraints newInfoConstraints = new GridBagConstraints();

                    newInfoConstraints.insets = new Insets(5, 10, 5, 10);
                    newInfoConstraints.anchor = GridBagConstraints.WEST;

                    newInfoConstraints.gridx = 0;
                    newInfoConstraints.gridy = 0;
                    newInfoPanel.add(new JLabel("Mã nhân viên"), newInfoConstraints);
                    newInfoConstraints.gridx++;
                    newInfoPanel.add(newMaNVField, newInfoConstraints);

                    newInfoConstraints.gridx = 0;
                    newInfoConstraints.gridy++;
                    newInfoPanel.add(new JLabel("Mã đề án"), newInfoConstraints);
                    newInfoConstraints.gridx++;
                    newInfoPanel.add(newMaDaField, newInfoConstraints);

                    newInfoConstraints.gridx = 0;
                    newInfoConstraints.gridy++;
                    newInfoPanel.add(new JLabel("Thời gian"), newInfoConstraints);
                    newInfoConstraints.gridx++;
                    newInfoPanel.add(newThoiGianField, newInfoConstraints);

                    constraints.gridx = 0;
                    constraints.gridy++;
                    constraints.gridwidth = 2;
                    add(newInfoPanel, constraints);
                    constraints.gridy++;
                    constraints.gridwidth = 1;
                }

                constraints.gridx = 1;
                constraints.gridy++;
                add(button, constraints);
                button.addActionListener(this::buttonActionPerformed);
            }

            private void buttonActionPerformed(ActionEvent e) {
                JButton button = (JButton) e.getSource();
                String command = button.getText();

                switch (command) {
                    case "Thêm" -> {
                        String MaNV = newMaNVField.getText();
                        String MaDA = newMaDaField.getText();
                        String ThoiGian = newThoiGianField.getText();

                        try {
                            dbc.insertPhanCongRecord(MaNV, MaDA, ThoiGian);
                            JOptionPane.showMessageDialog(this, "Đã thêm 01 phân công mới", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                            handleShowPHANCONG();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
                        } catch (ParseException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage() + ": Date must be format dd/MM/yyyy", "Date Error", JOptionPane.ERROR_MESSAGE);
                        }

                    }
                    case "Cập nhật" -> {
                        String oldMaNV = oldMaNVField.getText();
                        String oldMaDA = oldMaDaField.getText();
                        String newMaNV = newMaNVField.getText();
                        String newMaDA = newMaDaField.getText();
                        String newThoiGian = newThoiGianField.getText();

                        try {
                            dbc.updatePhanCongRecord(oldMaNV, oldMaDA, newMaNV, newMaDA, newThoiGian);
                            JOptionPane.showMessageDialog(this, "Cập nhật phân công thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                            handleShowPHANCONG();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
                        } catch (ParseException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage() + ": Date must be format dd/MM/yyyy", "Date Error", JOptionPane.ERROR_MESSAGE);
                        }

                    }
                    case "Xóa" -> {
                        String oldMaNV = oldMaNVField.getText();
                        String oldMaDA = oldMaDaField.getText();

                        try {
                            dbc.deletePhanCongRecord(oldMaNV, oldMaDA);
                            JOptionPane.showMessageDialog(this, "Đã xóa 01 phân công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                            handleShowPHANCONG();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(this, ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
                        }

                    }
                    default ->
                            JOptionPane.showMessageDialog(this, "Invalid operation", "Operation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        this.rightPanel.revalidate();
        this.rightPanel.repaint();
    }

    private void handleUpdatePersonalInfomation() {
        JTextField newNgaySinhField = new JTextField(10);
        JTextField newDiaChiField = new JTextField(30);
        JTextField newSoDTField = new JTextField(20);

        JButton button = new JButton("Cập nhật");

        this.subRightSplits.setBottomComponent(new JPanel() {
            {
                setLayout(new GridBagLayout());

                GridBagConstraints constraints = new GridBagConstraints();
                constraints.insets = new Insets(5, 10, 5, 10);
                constraints.anchor = GridBagConstraints.WEST;

                constraints.gridx = 0;
                constraints.gridy = 0;

                JPanel newInfoPanel = new JPanel();
                newInfoPanel.setBorder(BorderFactory.createTitledBorder("Thông tin mới"));

                newInfoPanel.setLayout(new GridBagLayout());
                GridBagConstraints newInfoConstraints = new GridBagConstraints();

                newInfoConstraints.insets = new Insets(5, 10, 5, 10);
                newInfoConstraints.anchor = GridBagConstraints.WEST;

                newInfoConstraints.gridx = 0;
                newInfoConstraints.gridy = 0;
                newInfoPanel.add(new JLabel("Ngày sinh"), newInfoConstraints);
                newInfoConstraints.gridx++;
                newInfoPanel.add(newNgaySinhField, newInfoConstraints);

                newInfoConstraints.gridx = 0;
                newInfoConstraints.gridy++;
                newInfoPanel.add(new JLabel("Địa chỉ"), newInfoConstraints);
                newInfoConstraints.gridx++;
                newInfoPanel.add(newDiaChiField, newInfoConstraints);

                newInfoConstraints.gridx = 0;
                newInfoConstraints.gridy++;
                newInfoPanel.add(new JLabel("Số điện thoại"), newInfoConstraints);
                newInfoConstraints.gridx++;
                newInfoPanel.add(newSoDTField, newInfoConstraints);

                constraints.gridx = 0;
                constraints.gridy++;
                constraints.gridwidth = 2;

                add(newInfoPanel, constraints);
                constraints.gridy++;
                constraints.gridwidth = 1;

                constraints.gridx = 1;
                constraints.gridy++;
                add(button, constraints);
                button.addActionListener(this::buttonActionPerformed);
            }

            private void buttonActionPerformed(ActionEvent e) {
                JButton button = (JButton) e.getSource();
                String command = button.getText();

                if ("Cập nhật".equals(command)) {
                    String newNgaySinh = newNgaySinhField.getText();
                    String newDiaChi = newDiaChiField.getText();
                    String newSoDT = newSoDTField.getText();

                    try {
                        dbc.updatePersonalInfoRecord(newNgaySinh, newDiaChi, newSoDT);
                        JOptionPane.showMessageDialog(this, "Cập nhật thông tin cá nhân thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        fetchPersonalInformation(manvField.getText());
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
                    } catch (ParseException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage() + ": Date must be format dd/MM/yyyy", "Date Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

            }

        });

        this.rightPanel.revalidate();
        this.rightPanel.repaint();
    }
    
    private void handleModifyNHANVIEN(String operation){
        JTextField manvOldField = new JTextField(10);
        JTextField manvField = new JTextField(10);
        JTextField tennvField = new JTextField(35);
        JTextField phaiField = new JTextField(10);
        JTextField ngaysinhField = new JTextField(10);
        JTextField diachiField = new JTextField(60);
        JTextField sodtField = new JTextField(20);
        JTextField manqlField = new JTextField(10);
        JTextField phgField = new JTextField(10);
        JTextField vaitroField = new JTextField(20);
        JButton insertButton = new JButton("Thêm");
        JButton updateButton = new JButton("Cập nhật");
        this.subRightSplits.setBottomComponent(new JPanel() {
        {
            setLayout(new GridBagLayout());
            GridBagConstraints constraints=new GridBagConstraints();
                constraints.insets = new Insets(5, 10, 5, 10);
                constraints.anchor = GridBagConstraints.WEST;


                constraints.gridx = 0;
                constraints.gridy = 0;
            JPanel nhanvienPanel= new JPanel();
            nhanvienPanel.setBorder(BorderFactory.createTitledBorder("Thông tin mới"));

            nhanvienPanel.setLayout(new GridBagLayout());
            GridBagConstraints newconstraints = new GridBagConstraints();

            newconstraints.insets = new Insets(5, 10, 5, 10);
            newconstraints.anchor = GridBagConstraints.WEST;

            if(operation== "Cập nhật")
            {
                    JPanel oldInfoPanel = new JPanel();
                    oldInfoPanel.setBorder(BorderFactory.createTitledBorder("Nhân viên cần cập nhật"));

                    oldInfoPanel.setLayout(new GridBagLayout());
                    GridBagConstraints oldInfoConstraints = new GridBagConstraints();
                    oldInfoConstraints.insets = new Insets(5, 10, 5, 10);
                    oldInfoConstraints.anchor = GridBagConstraints.WEST;

                    oldInfoConstraints.gridx = 1;
                    oldInfoConstraints.gridy = 0;
                    oldInfoPanel.add(new JLabel(String.format("Mã Nhân viên cần %s", operation)), oldInfoConstraints);
                    oldInfoConstraints.gridx++;
                    oldInfoPanel.add(manvOldField, oldInfoConstraints);
                    oldInfoConstraints.gridy++;

                    add(oldInfoPanel, constraints);
                    constraints.gridy++;
            
            }
            newconstraints.gridx = 1;
            newconstraints.gridy = 0;
            nhanvienPanel.add(new JLabel("Mã số"), newconstraints);
            newconstraints.gridx++;
            nhanvienPanel.add(manvField, newconstraints);

            newconstraints.gridy++;
            newconstraints.gridx = 1;
            nhanvienPanel.add(new JLabel("Họ tên"), newconstraints);
            newconstraints.gridx++;
            nhanvienPanel.add(tennvField, newconstraints);

            newconstraints.gridx = 1;
            newconstraints.gridy++;
            nhanvienPanel.add(new JLabel("Ngày sinh"), newconstraints);
            newconstraints.gridx++;
            nhanvienPanel.add(ngaysinhField, newconstraints);

            newconstraints.gridx = 1;
            newconstraints.gridy++;
            nhanvienPanel.add(new JLabel("Giới tính"), newconstraints);
            newconstraints.gridx++;
            nhanvienPanel.add(phaiField, newconstraints);

            newconstraints.gridx = 1;
            newconstraints.gridy++;
            nhanvienPanel.add(new JLabel("Chức vụ"), newconstraints);
            newconstraints.gridx++;
            nhanvienPanel.add(vaitroField, newconstraints);

            newconstraints.gridx = 1;
            newconstraints.gridy++;
            nhanvienPanel.add(new JLabel("Địa chỉ"), newconstraints);
            newconstraints.gridx++;
            nhanvienPanel.add(diachiField, newconstraints);

            newconstraints.gridx = 1;
            newconstraints.gridy++;
            nhanvienPanel.add(new JLabel("Số điện thoại"), newconstraints);
            newconstraints.gridx++;
            nhanvienPanel.add(sodtField, newconstraints);

            newconstraints.gridx = 1;
            newconstraints.gridy++;
            nhanvienPanel.add(new JLabel("Mã phòng ban"), newconstraints);
            newconstraints.gridx++;
            nhanvienPanel.add(phgField, newconstraints);

            newconstraints.gridx = 1;
            newconstraints.gridy++;
            nhanvienPanel.add(new JLabel("Mã người quản lí"), newconstraints);
            newconstraints.gridx++;
            nhanvienPanel.add(manqlField, newconstraints);
            
            if(operation =="Cập nhật"){
                newconstraints.gridx = 2;
                newconstraints.gridy++;
                nhanvienPanel.add(updateButton, newconstraints);
                updateButton.addActionListener(this::buttonUpdateActionPerformed);
            }else
            {
                newconstraints.gridx = 2;
                newconstraints.gridy++;
                nhanvienPanel.add(insertButton, newconstraints);
                insertButton.addActionListener(this::buttonInsertActionPerformed);                
            }
            add(nhanvienPanel, constraints);
        }   
        
            private void buttonUpdateActionPerformed(ActionEvent e) {
        
            }
            
            private void buttonInsertActionPerformed(ActionEvent e){
            
            }
        });
                    
        this.rightPanel.revalidate();
        this.rightPanel.repaint();
    }
    private void applyLineBorder(JPanel panel) {
        Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
        panel.setBorder(lineBorder);
    }
}
