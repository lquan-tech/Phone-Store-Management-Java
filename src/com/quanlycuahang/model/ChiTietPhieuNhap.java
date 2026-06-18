package com.quanlycuahang.model;

import java.math.BigDecimal;

public class ChiTietPhieuNhap {
    private int maCTPN;
    private String maPN;
    private String maMay;
    private String tenMay;
    private int soLuong;
    private BigDecimal donGia = BigDecimal.ZERO;
    private BigDecimal thanhTien = BigDecimal.ZERO;

    public ChiTietPhieuNhap() {}

    public ChiTietPhieuNhap(String maPN, String maMay, String tenMay,
                            int soLuong, BigDecimal donGia) {
        this.maPN = maPN;
        this.maMay = maMay;
        this.tenMay = tenMay;
        this.soLuong = soLuong;
        this.donGia = donGia;
        tinhThanhTien();
    }

    public int getMaCTPN() { return maCTPN; }
    public void setMaCTPN(int maCTPN) { this.maCTPN = maCTPN; }

    public String getMaPN() { return maPN; }
    public void setMaPN(String maPN) { this.maPN = maPN; }

    public String getMaMay() { return maMay; }
    public void setMaMay(String maMay) { this.maMay = maMay; }

    public String getTenMay() { return tenMay; }
    public void setTenMay(String tenMay) { this.tenMay = tenMay; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
        tinhThanhTien();
    }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
        tinhThanhTien();
    }

    public BigDecimal getThanhTien() { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }

    private void tinhThanhTien() {
        if (donGia != null) {
            thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));
        }
    }

    public Object[] toTableRow() {
        return new Object[]{
            maMay,
            tenMay != null ? tenMay : "",
            soLuong,
            String.format("%,.0f", donGia),
            String.format("%,.0f VND", thanhTien)
        };
    }
}
