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
import java.util.ArrayList; 


public class DBAdminController extends JFrame implements ActionListener {
    
    // Controller properties
    private final DBManager dbm;
    private ResultSet result;
    
    // GUI Components
    private final JSplitPane panes;
    private final JFrame father;
    private final JButton logOutButton;
    private final JButton showUserListButton;
    private final JButton showRoleListButton;
    private final JButton showTableListButton;
    private final JButton showViewListButton;
    private final JButton showPrivilegeButton;

    private final JButton createNewUserButton;
    private final JButton createNewRoleButton;
    private final JButton createNewTableButton;
    //private final JFrame create_new_user;
    
    public DBAdminController(String password, JFrame father) throws ClassNotFoundException, SQLException {
        dbm = new DBManager("sys as SYSDBA", password);
        this.father = father;
        
        // Set window properties
        this.setTitle("Login");
        this.setSize(1366, 768);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        /* split view into 2-pane: 
         * a panel contains button and a scrollable list to display result
        */
        panes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        panes.setResizeWeight(0.6);
        panes.setDividerLocation(0.6);
        
        GridBagLayout layout = new GridBagLayout();
        JPanel leftPanel = new JPanel(layout);
        JScrollPane rightPane = new JScrollPane();
        panes.setLeftComponent(leftPanel);
        panes.setRightComponent(rightPane);
        this.add(panes);
        
        // components constraints
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.insets = new Insets(10, 10, 10, 10);
        constraint.anchor = GridBagConstraints.CENTER;
        
        // declare component in left panel
        logOutButton = new JButton("Logout");
        constraint.gridx = 0;
        constraint.gridy = 0;
        leftPanel.add(logOutButton, constraint);
        
        showUserListButton = new JButton("Show user list");
        constraint.gridx = 0;
        constraint.gridy = 1;
        leftPanel.add(showUserListButton, constraint);
        
        showRoleListButton = new JButton("Show role list");
        constraint.gridx = 1;
        constraint.gridy = 1;
        leftPanel.add(showRoleListButton, constraint);
        
        showTableListButton = new JButton("Show table list");
        constraint.gridx = 0;
        constraint.gridy = 2;
        leftPanel.add(showTableListButton, constraint);
        
        showViewListButton = new JButton("Show view list");
        constraint.gridx = 1;
        constraint.gridy = 2;
        leftPanel.add(showViewListButton, constraint);
        

        showPrivilegeButton = new JButton("Show user or role privilege");
        constraint.gridx = 0;
        constraint.gridy = 3;
        leftPanel.add(showPrivilegeButton, constraint);

        createNewUserButton = new JButton("Create new user");
        constraint.gridx = 1;
        constraint.gridy = 4;
        leftPanel.add(createNewUserButton, constraint);
        
        createNewRoleButton = new JButton("Create new role");
        constraint.gridx = 1;
        constraint.gridy = 3;
        leftPanel.add(createNewRoleButton, constraint);
        
        createNewTableButton = new JButton("Create new table");
        constraint.gridx = 0;
        constraint.gridy = 4;
        leftPanel.add(createNewTableButton, constraint);

        
        // initilize action on left component
        this.setVisible(true);
        initialActionListener();
    }
    
    // update this method every times you need to add action for a components
    private void initialActionListener() {
        
        // Set action listeners for buttons
        logOutButton.addActionListener(this);
        showUserListButton.addActionListener(this);
        showRoleListButton.addActionListener(this);
        showTableListButton.addActionListener(this);
        showViewListButton.addActionListener(this);

        showPrivilegeButton.addActionListener(this);

        createNewUserButton.addActionListener(this);
        createNewRoleButton.addActionListener(this);
        createNewTableButton.addActionListener(this);

        // Set action for other compunents
    }
    
    // Helper method to convert a ResultSet to a TableModel
    private TableModel buildTableModel(ResultSet rs, int numRows) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // Get number of columns
        int columns = metaData.getColumnCount();

        // Create column names array
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
    
    // Update display on right pane with table view
    void displayRightPaneTable(int numRows) throws SQLException {
        JTable table = new JTable(buildTableModel(this.result, numRows)) {
            @Override
            public boolean isCellEditable(int row, int column) {                
                return false;               
            }
        };
        
        JScrollPane rightPane = new JScrollPane(table);
        panes.setRightComponent(rightPane);
    }

    // Update display on right pane with table view
    void displayTwoRightPaneTable(int upperNumRows, ResultSet lowerSet, int lowerNumRows) throws SQLException {
        JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        var ratio = (float)(upperNumRows) / (float)(upperNumRows + lowerNumRows);
        //rightPane.setDividerLocation(ratio);
        rightPane.setResizeWeight(ratio + 0.1);
        
        JTable upperTable = new JTable(buildTableModel(this.result, upperNumRows)) {
            @Override
            public boolean isCellEditable(int row, int column) {                
                return false;               
            }
        };
        JScrollPane upperPane = new JScrollPane(upperTable);
        rightPane.setTopComponent(upperPane);
        
        JTable lowerTable = new JTable(buildTableModel(lowerSet, lowerNumRows)) {
            @Override
            public boolean isCellEditable(int row, int column) {                
                return false;               
            }
        };
        JScrollPane lowerPane = new JScrollPane(lowerTable);
        rightPane.setBottomComponent(lowerPane);
        
        panes.setRightComponent(rightPane);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Define action for every component
        // Logout Button
        if (e.getSource() == logOutButton) {
            this.dispose();
            this.father.setVisible(true);
        }
        
        try {
            // Handle Show User list button
            if (e.getSource() == showUserListButton) {
                result = dbm.getUserList();
                int num_rows = dbm.getNumberRowsOf("USER_LIST");
                displayRightPaneTable(num_rows);
            }
            // Handle Show role list button
            else if (e.getSource() == showRoleListButton) {
                result = dbm.getRoleList();
                int num_rows = dbm.getNumberRowsOf("ROLE_LIST");
                displayRightPaneTable(num_rows);
            }
            // Handle Show table list button
            else if (e.getSource() == showTableListButton) {
                result = dbm.getTableList();
                int num_rows = dbm.getNumberRowsOf("TABLE_LIST");
                displayRightPaneTable(num_rows);
            }
            // Handle Show view list button
            else if (e.getSource() == showViewListButton) {
                result = dbm.getViewList();
                int num_rows = dbm.getNumberRowsOf("VIEW_LIST");
                displayRightPaneTable(num_rows);
            }
            else if(e.getSource() == showPrivilegeButton) {
                String[] option = {"User", "Role"};
                String message = "Show either role or user?";
                int choose = JOptionPane.showOptionDialog(this, message,"Selection", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, option, null);
                String entityType = option[choose];
                
                message = "Enter name of " + entityType;
                String nameOfEntity = JOptionPane.showInputDialog(this, message, entityType + " name", JOptionPane.QUESTION_MESSAGE);
                
                result = dbm.getTablePrivilegesOfRoleOrUser(nameOfEntity);
                int numRows = dbm.getNumberOfRowsInLastQuery();
                
                if (numRows == 0) {
                    JOptionPane.showMessageDialog(this, nameOfEntity + " is not exist", "Error", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    if ("Role".equals(entityType))
                        this.displayRightPaneTable(numRows);
                    else if ("User".equals(entityType)) {
                        ResultSet roleSet = dbm.getRoleOfUser(nameOfEntity);
                        int lowerNumRows = dbm.getNumberOfRowsInLastQuery();
                        this.displayTwoRightPaneTable(numRows, roleSet, lowerNumRows);
                    }
                }
            }
            
            else if (e.getSource() == createNewUserButton) {
                //xu li code
                
                JTextField username = new JTextField();
                JTextField password = new JPasswordField();
                Object[] message = {
                    "Username:", username,
                    "Password:", password
                };
                int option = JOptionPane.showConfirmDialog(null, message, "Create new user", JOptionPane.OK_CANCEL_OPTION);
                int resultt=dbm.createNewUser(username.getText(), password.getText());
                if (option == JOptionPane.OK_OPTION) {
                 if(resultt >0)
                {
                    JOptionPane.showMessageDialog(this, "User " + username.getText() +" created", "Message", JOptionPane.INFORMATION_MESSAGE);
                }
                else
                {
                    JOptionPane.showMessageDialog(this, "User " + username.getText()+" cannot create or exists in database", "Error", JOptionPane.ERROR_MESSAGE);
                }
                }
            }
            // create new role
            else if (e.getSource() == createNewRoleButton) {
                String message = "Enter name of Role "  ;
                String nameOfRole = JOptionPane.showInputDialog(this, message, "New Role", JOptionPane.QUESTION_MESSAGE);
                int resultQuery= dbm.createNewRole(nameOfRole);
                if(resultQuery >0)
                {
                    JOptionPane.showMessageDialog(this, "Role " + nameOfRole +" created", "Message", JOptionPane.INFORMATION_MESSAGE);
                }
                else
                {
                    JOptionPane.showMessageDialog(this, "Role " +nameOfRole +" cannot create or exists in database", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            }
            //create new table
            else if (e.getSource() == createNewTableButton) {
                //xu li code
                Boolean fl=true;
                JTextField tablename = new JTextField();
                JTextField numbercolumn = new JTextField();
                JTextField column = new JTextField();
                JTextField number = new JTextField();
                //type of variable
                String[] typeselect= {" INT "," FLOAT "," VARCHAR2 ", " NVARCHAR2 ", " VARCHAR ", " NVARCHAR ", " DATE "};
                String[] values= {" NULL ", " NOT NULL ", " PRIMARY KEY "};
                
                JComboBox typevar = new JComboBox(typeselect);
                JComboBox value = new JComboBox(values);
                Object[] message = {
                    "Table Name:", tablename,
                    "Numbers of Column:", numbercolumn
                };
                Object[] field = {
                    "Column Name:", column,
                    "Type of Column:", typevar,
                    "Number:", number,
                    "Is NULL:", value
                };
                int option = JOptionPane.showConfirmDialog(null, message, "Create new table", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    ArrayList<String> varname = new ArrayList<String>();
                    ArrayList<String> vartype = new ArrayList<String>();
                    ArrayList<String> varnumber = new ArrayList<String>();
                    ArrayList<String> varisnull = new ArrayList<String>();
                    for(int i=0;i<Integer.parseInt(numbercolumn.getText()); i++)
                    {
                        int att = JOptionPane.showConfirmDialog(null, field, "Add new field " + (i+1), JOptionPane.OK_CANCEL_OPTION);
                        if(att==JOptionPane.OK_OPTION)
                        {
                        varname.add(column.getText());
                        vartype.add(typevar.getSelectedItem().toString());
                        if(number!=null)
                        {
                        varnumber.add(number.getText());
                        }
                        varisnull.add(value.getSelectedItem().toString());
                        }
                        else
                        {
                            fl=false;
                            break;
                        }
                    }
                    if(fl==true)
                    {
                        int resultQuery= dbm.createNewTable(tablename.getText(),Integer.parseInt(numbercolumn.getText()), varname, vartype, varnumber, varisnull);
                        if(resultQuery >0)
                        {
                            JOptionPane.showMessageDialog(this, "Table " + tablename.getText() +" created", "Message", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(this, "Table " +tablename.getText() +" cannot create or exists in database", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
            
        } catch(SQLException ex) {
            String message = "Cannot create new element: " + ex.getMessage();
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            String message = "Unexpected error occured: " + ex.getMessage();
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }
     

}
