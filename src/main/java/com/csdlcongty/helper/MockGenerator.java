package com.csdlcongty.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MockGenerator {

    private static final Random random = new Random();

    public static List<NhanVienRecord> generateNVRecords(int count) {
        List<NhanVienRecord> records = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String MANV = generateMANV(i);
            String TENNV = "Employee " + i;
            String PHAI = i % 2 == 0 ? "Male" : "Female";
            Date NGAYSINH = getRandomBirthDate();
            String DIACHI = "Address " + i;
            String SODT = generateRandomPhoneNumber();
            String LUONG = String.valueOf(getRandomSalary());
            String PHUCAP = String.valueOf(getRandomAllowance());
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

    public static Integer getRandomSalary() {
        int[] salaryOptions = {1500, 1600, 1700, 1800, 1900};
        int randomIndex = random.nextInt(salaryOptions.length);
        return salaryOptions[randomIndex];
    }

    public static Integer getRandomAllowance() {
        int[] allowanceOptions = {100, 200, 300, 400, 500};
        int randomIndex = random.nextInt(allowanceOptions.length);
        return allowanceOptions[randomIndex];
    }

    public static String generateMANV(int index) {
        String digit = String.format("%03d", index);
        return "NV" + digit;
    }

    public static String generateMANQL(int index) {
        switch (index % 3) {
            case 0: {
                String digit = String.format("%03d", 1 + index % 20);
                return "QL" + digit;
            }
            case 1: {
                String digit = String.format("%03d", 1 + index % 8);
                return "TP" + digit;
            }
            case 2: {
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

    public static List<NhanVienRecord> generateQLRecords(int count) {
        List<NhanVienRecord> records = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String MANV = generateQLMANV(i);
            String TENNV = "QL Truc tiep " + i;
            String PHAI = (i + random.nextInt()) % 2 == 0 ? "Male" : "Female";
            Date NGAYSINH = getRandomBirthDate();
            String DIACHI = "Address QL" + (300 + i);
            String SODT = generateRandomPhoneNumber();
            String LUONG = String.valueOf(getRandomSalary() * 3);
            String PHUCAP = String.valueOf(getRandomAllowance() * 2);
            String VAITRO = "QL trực tiếp";
            String MANQL = ""; // QL employees have no MANQL
            String PHG = "PB00" + ((i % 8) + 1); // Random department ID

            NhanVienRecord record = new NhanVienRecord(MANV, TENNV, PHAI, NGAYSINH, DIACHI, SODT, LUONG, PHUCAP, VAITRO, MANQL, PHG);
            records.add(record);
        }

        return records;
    }

    private static String generateQLMANV(int index) {
        String digit = String.format("%03d", index);
        return "QL" + digit;
    }

    public static List<NhanVienRecord> generateTPRecords(int count) {
        List<NhanVienRecord> records = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String MANV = generateTPMANV(i);
            String TENNV = "Truong phong " + i;
            String PHAI = (i + random.nextInt()) % 2 == 0 ? "Male" : "Female";
            Date NGAYSINH = getRandomBirthDate();
            String DIACHI = "Address TP" + (300 + i);
            String SODT = generateRandomPhoneNumber();
            String LUONG = String.valueOf(getRandomSalary() * 4);
            String PHUCAP = String.valueOf(getRandomAllowance() * 3);
            String VAITRO = "Trưởng phòng";
            String MANQL = ""; // QL employees have no MANQL
            String PHG = "PB00" + ((i % 8) + 1); // Random department ID

            NhanVienRecord record = new NhanVienRecord(MANV, TENNV, PHAI, NGAYSINH, DIACHI, SODT, LUONG, PHUCAP, VAITRO, MANQL, PHG);
            records.add(record);
        }

        return records;
    }

    private static String generateTPMANV(int i) {
        String digit = String.format("%03d", i);
        return "TP" + digit;
    }

    public static List<NhanVienRecord> generateTCRecords(int count) {
        List<NhanVienRecord> records = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String MANV = generateTCMANV(i);
            String TENNV = "Tai chinh " + i;
            String PHAI = (i + random.nextInt()) % 2 == 0 ? "Male" : "Female";
            Date NGAYSINH = getRandomBirthDate();
            String DIACHI = "Address TC" + (300 + i);
            String SODT = generateRandomPhoneNumber();
            String LUONG = String.valueOf(getRandomSalary() * 4);
            String PHUCAP = String.valueOf(getRandomAllowance() * 3);
            String VAITRO = "Tài chính";
            String MANQL = ""; // QL employees have no MANQL
            String PHG = "PB00" + ((i % 8) + 1); // Random department ID

            NhanVienRecord record = new NhanVienRecord(MANV, TENNV, PHAI, NGAYSINH, DIACHI, SODT, LUONG, PHUCAP, VAITRO, MANQL, PHG);
            records.add(record);
        }

        return records;
    }

    private static String generateTCMANV(int i) {
        String digit = String.format("%03d", i);
        return "TC" + digit;
    }

    public static List<NhanVienRecord> generateNhanSuRecords(int count) {
        List<NhanVienRecord> records = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String MANV = generateNhanSuMANV(i);
            String TENNV = "Nhan su " + i;
            String PHAI = (i + random.nextInt()) % 2 == 0 ? "Male" : "Female";
            Date NGAYSINH = getRandomBirthDate();
            String DIACHI = "Address Nhan Su" + (300 + i);
            String SODT = generateRandomPhoneNumber();
            String LUONG = String.valueOf(getRandomSalary() * 4);
            String PHUCAP = String.valueOf(getRandomAllowance() * 4);
            String VAITRO = "Nhân sự";
            String MANQL = ""; // QL employees have no MANQL
            String PHG = "PB00" + ((i % 8) + 1); // Random department ID

            NhanVienRecord record = new NhanVienRecord(MANV, TENNV, PHAI, NGAYSINH, DIACHI, SODT, LUONG, PHUCAP, VAITRO, MANQL, PHG);
            records.add(record);
        }

        return records;
    }

    private static String generateNhanSuMANV(int i) {
        String digit = String.format("%03d", i);
        return "NS" + digit;
    }

    public static List<NhanVienRecord> generateTruongDeAnRecords(int count) {
        List<NhanVienRecord> records = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String MANV = generateTruongDeAnMANV(i);
            String TENNV = "Truong de an " + i;
            String PHAI = (i + random.nextInt()) % 2 == 0 ? "Male" : "Female";
            Date NGAYSINH = getRandomBirthDate();
            String DIACHI = "Address Truong de an" + (300 + i);
            String SODT = generateRandomPhoneNumber();
            String LUONG = String.valueOf(getRandomSalary() * 5);
            String PHUCAP = String.valueOf(getRandomAllowance() * 5);
            String VAITRO = "Trưởng đề án";
            String MANQL = ""; // QL employees have no MANQL
            String PHG = "PB00" + ((i % 8) + 1); // Random department ID

            NhanVienRecord record = new NhanVienRecord(MANV, TENNV, PHAI, NGAYSINH, DIACHI, SODT, LUONG, PHUCAP, VAITRO, MANQL, PHG);
            records.add(record);
        }

        return records;
    }

    private static String generateTruongDeAnMANV(int i) {
        String digit = String.format("%03d", i);
        return "TDA" + digit;
    }
}
