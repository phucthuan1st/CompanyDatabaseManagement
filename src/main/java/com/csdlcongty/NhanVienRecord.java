package com.csdlcongty;

import java.util.Date;

public class NhanVienRecord {
    public String MANV;
    public String TENNV;
    public String PHAI;
    public Date NGAYSINH;
    public String DIACHI;
    public String SODT;
    public String LUONG;
    public String PHUCAP;
    public String VAITRO;
    public String MANQL;
    public String PHG;

    public NhanVienRecord(String MANV, String TENNV, String PHAI, Date NGAYSINH, String DIACHI, String SODT,
                          String LUONG, String PHUCAP, String VAITRO, String MANQL, String PHG) {
        this.MANV = MANV;
        this.TENNV = TENNV;
        this.PHAI = PHAI;
        this.NGAYSINH = NGAYSINH;
        this.DIACHI = DIACHI;
        this.SODT = SODT;
        this.LUONG = LUONG;
        this.PHUCAP = PHUCAP;
        this.VAITRO = VAITRO;
        this.MANQL = MANQL;
        this.PHG = PHG;
    }
}
