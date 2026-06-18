package com.quanlycuahang.dao;

import com.quanlycuahang.config.DatabaseConnection;
import com.quanlycuahang.model.DienThoai;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BaoCaoDAO {

    public KpiData layKpi(LocalDate from, LocalDate to) throws SQLException {
        KpiData data = new KpiData();
        Timestamp start = Timestamp.valueOf(from.atStartOfDay());
        Timestamp endExclusive = Timestamp.valueOf(to.plusDays(1).atStartOfDay());
        LocalDate today = LocalDate.now();
        Timestamp todayStart = Timestamp.valueOf(today.atStartOfDay());
        Timestamp tomorrowStart = Timestamp.valueOf(today.plusDays(1).atStartOfDay());

        try (Connection conn = DatabaseConnection.getConnection()) {
            data.doanhThuHomNay = scalarMoney(conn,
                "SELECT ISNULL(SUM(TongTien),0) FROM HoaDon WHERE NgayBan >= ? AND NgayBan < ?",
                todayStart, tomorrowStart);
            data.hoaDonHomNay = scalarInt(conn,
                "SELECT COUNT(*) FROM HoaDon WHERE NgayBan >= ? AND NgayBan < ?",
                todayStart, tomorrowStart);
            data.doanhThuKy = scalarMoney(conn,
                "SELECT ISNULL(SUM(TongTien),0) FROM HoaDon WHERE NgayBan >= ? AND NgayBan < ?",
                start, endExclusive);
            data.chiNhapKy = scalarMoney(conn,
                "SELECT ISNULL(SUM(TongTien),0) FROM PhieuNhap WHERE NgayNhap >= ? AND NgayNhap < ?",
                start, endExclusive);
            data.doanhThuDichVuKy = scalarMoney(conn,
                "SELECT ISNULL(SUM(ChiPhi),0) FROM PhieuDichVu WHERE NgayNhan >= ? AND NgayNhan < ?",
                start, endExclusive);
            data.giaTriTonKho = scalarMoney(conn,
                "SELECT ISNULL(SUM(Gia * SoLuong),0) FROM DienThoai");
            data.khachHang = scalarInt(conn, "SELECT COUNT(*) FROM KhachHang");
            data.sapHetHang = scalarInt(conn, "SELECT COUNT(*) FROM DienThoai WHERE SoLuong <= 5");
            data.dichVuDangMo = scalarInt(conn,
                "SELECT COUNT(*) FROM PhieuDichVu WHERE TrangThai IN (N'DangXuLy', N'ChoLinhKien')");
        }
        return data;
    }

    public List<Object[]> doanhThuTheoNgay(LocalDate from, LocalDate to) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT CAST(NgayBan AS date) AS Ngay, COUNT(*) AS SoHoaDon, ISNULL(SUM(TongTien),0) AS DoanhThu "
                   + "FROM HoaDon WHERE NgayBan >= ? AND NgayBan < ? "
                   + "GROUP BY CAST(NgayBan AS date) ORDER BY Ngay";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getDate("Ngay").toLocalDate().toString(),
                        rs.getInt("SoHoaDon"),
                        formatMoney(rs.getBigDecimal("DoanhThu"))
                    });
                }
            }
        }
        return rows;
    }

    public List<Object[]> sanPhamBanChay(LocalDate from, LocalDate to) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT TOP 10 CT.MaMay, DT.TenMay, SUM(CT.SoLuongBan) AS SoLuong, "
                   + "SUM(CT.SoLuongBan * CT.DonGia) AS DoanhThu "
                   + "FROM ChiTietHoaDon CT "
                   + "JOIN HoaDon H ON CT.MaHD = H.MaHD "
                   + "JOIN DienThoai DT ON CT.MaMay = DT.MaMay "
                   + "WHERE H.NgayBan >= ? AND H.NgayBan < ? "
                   + "GROUP BY CT.MaMay, DT.TenMay "
                   + "ORDER BY SoLuong DESC, DoanhThu DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getNString("MaMay"),
                        rs.getNString("TenMay"),
                        rs.getInt("SoLuong"),
                        formatMoney(rs.getBigDecimal("DoanhThu"))
                    });
                }
            }
        }
        return rows;
    }

    public List<DienThoai> tonKhoThap(int limit) throws SQLException {
        List<DienThoai> rows = new ArrayList<>();
        String sql = "SELECT MaMay, TenMay, HangSX, Gia, SoLuong, MoTa, NgayNhap "
                   + "FROM DienThoai WHERE SoLuong <= ? ORDER BY SoLuong ASC, TenMay";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DienThoai dt = new DienThoai();
                    dt.setMaMay(rs.getNString("MaMay"));
                    dt.setTenMay(rs.getNString("TenMay"));
                    dt.setHangSX(rs.getNString("HangSX"));
                    dt.setGia(rs.getBigDecimal("Gia"));
                    dt.setSoLuong(rs.getInt("SoLuong"));
                    dt.setMoTa(rs.getNString("MoTa"));
                    Timestamp ts = rs.getTimestamp("NgayNhap");
                    if (ts != null) dt.setNgayNhap(ts.toLocalDateTime());
                    rows.add(dt);
                }
            }
        }
        return rows;
    }

    private BigDecimal scalarMoney(Connection conn, String sql, Timestamp start, Timestamp end) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    private BigDecimal scalarMoney(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    private int scalarInt(Connection conn, String sql, Timestamp start, Timestamp end) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int scalarInt(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static String formatMoney(BigDecimal value) {
        return String.format("%,.0f VND", value != null ? value : BigDecimal.ZERO);
    }

    public static class KpiData {
        public BigDecimal doanhThuHomNay = BigDecimal.ZERO;
        public int hoaDonHomNay;
        public BigDecimal doanhThuKy = BigDecimal.ZERO;
        public BigDecimal chiNhapKy = BigDecimal.ZERO;
        public BigDecimal doanhThuDichVuKy = BigDecimal.ZERO;
        public BigDecimal giaTriTonKho = BigDecimal.ZERO;
        public int khachHang;
        public int sapHetHang;
        public int dichVuDangMo;
    }
}
