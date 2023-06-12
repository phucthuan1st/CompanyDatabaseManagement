package com.csdlcongty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MockGenerator {

    private static final Random random = new Random();

    public static List<NhanVienRecord> generateRecords(int count) {
        List<NhanVienRecord> records = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String MANV = generateMANV(i);
            String TENNV = "Employee " + i;
            String PHAI = i % 2 == 0 ? "Male" : "Female";
            Date NGAYSINH = getRandomBirthDate();
            String DIACHI = "Address " + i;
            String SODT = generateRandomPhoneNumber();
            String LUONG = getRandomSalary();
            String PHUCAP = getRandomAllowance();
            String VAITRO = "Nhân viên";
            String MANQL = generateMANQL(i);
            String PHG = "PB00" + ((i % 8) + 1); // Random department ID

            NhanVienRecord record = new NhanVienRecord(MANV, TENNV, PHAI, NGAYSINH, DIACHI, SODT, LUONG, PHUCAP, VAITRO, MANQL, PHG);
            records.add(record);
        }

        return records;
    }

    public static Date getRandomBirthDate() {
        return new Date(1970 + random.nextInt(25), 1 + random.nextInt(12), 1 + random.nextInt(28)); // Placeholder implementation
    }

    public static String getRandomSalary() {
        int[] salaryOptions = {1500, 1600, 1700, 1800, 1900};
        int randomIndex = random.nextInt(salaryOptions.length);
        return String.valueOf(salaryOptions[randomIndex]);
    }

    public static String getRandomAllowance() {
        int[] allowanceOptions = {100, 200, 300, 400, 500};
        int randomIndex = random.nextInt(allowanceOptions.length);
        return String.valueOf(allowanceOptions[randomIndex]);
    }

    public static String generateMANV(int index) {
        String digit = String.format("%03d", index);
        return "NV" + digit;
    }

    public static String generateMANQL(int index) {
        switch (index % 3) {
            case 0:
            {
                String digit = String.format("%03d", 1 + index % 20);
                return "QL" + digit;
            }
            case 1:
            {
                String digit = String.format("%03d", 1 + index % 8);
                return "TP" + digit;
            }
            case 2:
            {
                String digit = String.format("%03d", 1 + index % 5);
                return "NS" + digit;
            }
            default:
                throw new IllegalArgumentException("Invalid index for MANQL generation");
        }
    }

    public static String generateRandomPhoneNumber() {
        StringBuilder phoneNumber = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            phoneNumber.append(random.nextInt(10));
        }

        return phoneNumber.toString();
    }
}
