package com.csdlcongty;

/**
 * @project CompanyDatabaseOperation
 * @author 20H3T-02
 */
import java.io.IOException;

public class AppEntry {

    public static final int WIDTH = 720;
    public static final int HEIGHT = 720;

    public static void main(String[] args) throws IOException {
        new LoginController(WIDTH, HEIGHT);
    }
}
