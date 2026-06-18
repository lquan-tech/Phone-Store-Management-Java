package com.quanlycuahang.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PhieuNhap {
    private String maPN;
    private String maNCC;
    private String tenNCC;
    private String maNV;
    private String hoTenNV;
    private LocalDateTime ngayNhap;
    private BigDecimal tongTien = BigDecimal.ZERO;
    private String ghiChu;
    private List<ChiTietPhieuNhap> danhSachChiTiet = new ArrayList<>();

    public PhieuNhap() {}

    public PhieuNhap(String maPN, String maNCC, String maNV, String ghiChu) {
        this.maPN = maPN;
        this.maNCC = maNCC;
        this.maNV = maNV;
        this.ghiChu = ghiChu;
    }

    public String getMaPN() { return maPN; }
    public void setMaPN(String maPN) { this.maPN = maPN; }

    public String getMaNCC() { return maNCC; }
    public void setMaNCC(String maNCC) { this.maNCC = maNCC; }

    public String getTenNCC() { return tenNCC; }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public String getHoTenNV() { return hoTenNV; }
    public void setHoTenNV(String hoTenNV) { this.hoTenNV = hoTenNV; }

    public LocalDateTime getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(LocalDateTime ngayNhap) { this.ngayNhap = ngayNhap; }

    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public List<ChiTietPhieuNhap> getDanhSachChiTiet() { return danhSachChiTiet; }
    public void setDanhSachChiTiet(List<ChiTietPhieuNhap> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet;
        tinhLaiTongTien();
    }

    public void tinhLaiTongTien() {
        tongTien = danhSachChiTiet.stream()
            .map(ChiTietPhieuNhap::getThanhTien)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Object[] toTableRow() {
        return new Object[]{
            maPN,
            tenNCC != null ? tenNCC : maNCC,
            hoTenNV != null ? hoTenNV : maNV,
            String.format("%,.0f VND", tongTien),
            ngayNhap != null ? ngayNhap.toString().replace("T", " ").substring(0, 19) : "",
            ghiChu != null ? ghiChu : ""
        };
    }
}
