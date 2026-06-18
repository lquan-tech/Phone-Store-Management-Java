package com.quanlycuahang.model;

public class NhaCungCap {
    private String maNCC;
    private String tenNCC;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String ghiChu;

    public NhaCungCap() {}

    public NhaCungCap(String maNCC, String tenNCC, String soDienThoai,
                      String email, String diaChi, String ghiChu) {
        this.maNCC = maNCC;
        this.tenNCC = tenNCC;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
        this.ghiChu = ghiChu;
    }

    public String getMaNCC() { return maNCC; }
    public void setMaNCC(String maNCC) { this.maNCC = maNCC; }

    public String getTenNCC() { return tenNCC; }
    public void setTenNCC(String tenNCC) { this.tenNCC = tenNCC; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public Object[] toTableRow() {
        return new Object[]{
            maNCC,
            tenNCC,
            soDienThoai != null ? soDienThoai : "",
            email != null ? email : "",
            diaChi != null ? diaChi : "",
            ghiChu != null ? ghiChu : ""
        };
    }

    @Override
    public String toString() {
        return maNCC + " - " + tenNCC;
    }
}
