package com.quanlycuahang.dao;

import com.quanlycuahang.config.DatabaseConnection;
import com.quanlycuahang.model.NhaCungCap;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhaCungCapDAO {

    public boolean them(NhaCungCap ncc) throws SQLException {
        String sql = "INSERT INTO NhaCungCap (MaNCC, TenNCC, SoDienThoai, Email, DiaChi, GhiChu) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, ncc.getMaNCC());
            ps.setNString(2, ncc.getTenNCC());
            ps.setNString(3, ncc.getSoDienThoai());
            ps.setNString(4, ncc.getEmail());
            ps.setNString(5, ncc.getDiaChi());
            ps.setNString(6, ncc.getGhiChu());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhat(NhaCungCap ncc) throws SQLException {
        String sql = "UPDATE NhaCungCap SET TenNCC=?, SoDienThoai=?, Email=?, DiaChi=?, GhiChu=? "
                   + "WHERE MaNCC=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, ncc.getTenNCC());
            ps.setNString(2, ncc.getSoDienThoai());
            ps.setNString(3, ncc.getEmail());
            ps.setNString(4, ncc.getDiaChi());
            ps.setNString(5, ncc.getGhiChu());
            ps.setNString(6, ncc.getMaNCC());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean xoa(String maNCC) throws SQLException {
        String sql = "DELETE FROM NhaCungCap WHERE MaNCC=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, maNCC);
            return ps.executeUpdate() > 0;
        }
    }

    public List<NhaCungCap> layTatCa() throws SQLException {
        List<NhaCungCap> list = new ArrayList<>();
        String sql = "SELECT MaNCC, TenNCC, SoDienThoai, Email, DiaChi, GhiChu "
                   + "FROM NhaCungCap ORDER BY TenNCC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<NhaCungCap> timKiem(String tuKhoa) throws SQLException {
        List<NhaCungCap> list = new ArrayList<>();
        String sql = "SELECT MaNCC, TenNCC, SoDienThoai, Email, DiaChi, GhiChu "
                   + "FROM NhaCungCap "
                   + "WHERE MaNCC LIKE ? OR TenNCC LIKE ? OR SoDienThoai LIKE ? "
                   + "ORDER BY TenNCC";
        String pattern = "%" + tuKhoa + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, pattern);
            ps.setNString(2, pattern);
            ps.setNString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public boolean maTonTai(String maNCC) throws SQLException {
        String sql = "SELECT 1 FROM NhaCungCap WHERE MaNCC=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, maNCC);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String sinhMaNhaCungCap() throws SQLException {
        String sql = "SELECT TOP 1 MaNCC FROM NhaCungCap ORDER BY MaNCC DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String last = rs.getNString("MaNCC");
                int next = Integer.parseInt(last.replaceAll("[^0-9]", "")) + 1;
                return String.format("NCC%03d", next);
            }
        }
        return "NCC001";
    }

    private NhaCungCap map(ResultSet rs) throws SQLException {
        return new NhaCungCap(
            rs.getNString("MaNCC"),
            rs.getNString("TenNCC"),
            rs.getNString("SoDienThoai"),
            rs.getNString("Email"),
            rs.getNString("DiaChi"),
            rs.getNString("GhiChu")
        );
    }
}
