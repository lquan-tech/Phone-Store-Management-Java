package com.quanlycuahang.dao;

import com.quanlycuahang.config.DatabaseConnection;
import com.quanlycuahang.model.ChiTietPhieuNhap;
import com.quanlycuahang.model.PhieuNhap;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhieuNhapDAO {

    public void taoPhieuNhap(PhieuNhap phieuNhap) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            phieuNhap.tinhLaiTongTien();

            String sqlPN = "INSERT INTO PhieuNhap (MaPN, MaNCC, MaNV, TongTien, GhiChu) "
                         + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlPN)) {
                ps.setNString(1, phieuNhap.getMaPN());
                ps.setNString(2, blankToNull(phieuNhap.getMaNCC()));
                ps.setNString(3, phieuNhap.getMaNV());
                ps.setBigDecimal(4, phieuNhap.getTongTien());
                ps.setNString(5, phieuNhap.getGhiChu());
                ps.executeUpdate();
            }

            String sqlCT = "INSERT INTO ChiTietPhieuNhap (MaPN, MaMay, SoLuong, DonGia) "
                         + "VALUES (?, ?, ?, ?)";
            String sqlTon = "UPDATE DienThoai SET SoLuong = SoLuong + ? WHERE MaMay=?";
            try (PreparedStatement psCT = conn.prepareStatement(sqlCT);
                 PreparedStatement psTon = conn.prepareStatement(sqlTon)) {
                for (ChiTietPhieuNhap ct : phieuNhap.getDanhSachChiTiet()) {
                    psCT.setNString(1, phieuNhap.getMaPN());
                    psCT.setNString(2, ct.getMaMay());
                    psCT.setInt(3, ct.getSoLuong());
                    psCT.setBigDecimal(4, ct.getDonGia());
                    psCT.addBatch();

                    psTon.setInt(1, ct.getSoLuong());
                    psTon.setNString(2, ct.getMaMay());
                    psTon.addBatch();
                }
                psCT.executeBatch();
                psTon.executeBatch();
            }

            conn.commit();
        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw ex;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public List<PhieuNhap> layTatCa() throws SQLException {
        List<PhieuNhap> list = new ArrayList<>();
        String sql = baseSelect() + " ORDER BY PN.NgayNhap DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapPhieuNhap(rs));
        }
        return list;
    }

    public List<ChiTietPhieuNhap> layChiTiet(String maPN) throws SQLException {
        List<ChiTietPhieuNhap> list = new ArrayList<>();
        String sql = "SELECT CT.MaCTPN, CT.MaPN, CT.MaMay, DT.TenMay, CT.SoLuong, CT.DonGia, CT.ThanhTien "
                   + "FROM ChiTietPhieuNhap CT "
                   + "JOIN DienThoai DT ON CT.MaMay = DT.MaMay "
                   + "WHERE CT.MaPN = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, maPN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapChiTiet(rs));
            }
        }
        return list;
    }

    public List<PhieuNhap> timKiem(String tuKhoa) throws SQLException {
        List<PhieuNhap> list = new ArrayList<>();
        String sql = baseSelect()
                   + " WHERE PN.MaPN LIKE ? OR NCC.TenNCC LIKE ? OR NV.HoTen LIKE ? "
                   + " ORDER BY PN.NgayNhap DESC";
        String pattern = "%" + tuKhoa + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, pattern);
            ps.setNString(2, pattern);
            ps.setNString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapPhieuNhap(rs));
            }
        }
        return list;
    }

    public String sinhMaPhieuNhap() throws SQLException {
        String sql = "SELECT TOP 1 MaPN FROM PhieuNhap ORDER BY MaPN DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String last = rs.getNString("MaPN");
                int next = Integer.parseInt(last.replaceAll("[^0-9]", "")) + 1;
                return String.format("PN%03d", next);
            }
        }
        return "PN001";
    }

    private String baseSelect() {
        return "SELECT PN.MaPN, PN.MaNCC, NCC.TenNCC, PN.MaNV, NV.HoTen, "
             + "PN.NgayNhap, PN.TongTien, PN.GhiChu "
             + "FROM PhieuNhap PN "
             + "LEFT JOIN NhaCungCap NCC ON PN.MaNCC = NCC.MaNCC "
             + "JOIN NhanVien NV ON PN.MaNV = NV.MaNV";
    }

    private PhieuNhap mapPhieuNhap(ResultSet rs) throws SQLException {
        PhieuNhap pn = new PhieuNhap();
        pn.setMaPN(rs.getNString("MaPN"));
        pn.setMaNCC(rs.getNString("MaNCC"));
        pn.setTenNCC(rs.getNString("TenNCC"));
        pn.setMaNV(rs.getNString("MaNV"));
        pn.setHoTenNV(rs.getNString("HoTen"));
        pn.setTongTien(rs.getBigDecimal("TongTien"));
        pn.setGhiChu(rs.getNString("GhiChu"));
        Timestamp ts = rs.getTimestamp("NgayNhap");
        if (ts != null) pn.setNgayNhap(ts.toLocalDateTime());
        return pn;
    }

    private ChiTietPhieuNhap mapChiTiet(ResultSet rs) throws SQLException {
        ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
        ct.setMaCTPN(rs.getInt("MaCTPN"));
        ct.setMaPN(rs.getNString("MaPN"));
        ct.setMaMay(rs.getNString("MaMay"));
        ct.setTenMay(rs.getNString("TenMay"));
        ct.setSoLuong(rs.getInt("SoLuong"));
        ct.setDonGia(rs.getBigDecimal("DonGia"));
        ct.setThanhTien(rs.getBigDecimal("ThanhTien"));
        return ct;
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
