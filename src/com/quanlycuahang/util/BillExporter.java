package com.quanlycuahang.util;

import com.quanlycuahang.model.ChiTietHoaDon;
import com.quanlycuahang.model.HoaDon;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Xuat hoa don ra file .txt dinh dang chuyen nghiep.
 */
public class BillExporter {

    private static final int LINE_WIDTH = 52;
    private static final String LINE   = "=".repeat(LINE_WIDTH);
    private static final String DLINE  = "-".repeat(LINE_WIDTH);

    /**
     * Xuat hoa don thanh file .txt.
     *
     * @param hd      doi tuong HoaDon (co danh sach chi tiet)
     * @param filePath duong dan file output, vi du "bill/HD001.txt"
     * @throws IOException neu khong ghi duoc file
     */
    public static void xuatBill(HoaDon hd, String filePath) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(filePath, false), StandardCharsets.UTF_8))) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String ngayBan = hd.getNgayBan() != null
                ? hd.getNgayBan().format(dtf)
                : LocalDateTime.now().format(dtf);

            pw.println(LINE);
            pw.println(center("CUA HANG DIEN THOAI ABC", LINE_WIDTH));
            pw.println(center("123 Duong ABC, Quan 1, TP.HCM", LINE_WIDTH));
            pw.println(center("SDT: 0901 234 567", LINE_WIDTH));
            pw.println(LINE);
            pw.println(center("HOA DON BAN HANG", LINE_WIDTH));
            pw.println(LINE);

            pw.printf("Ma HD     : %s%n", hd.getMaHD());
            pw.printf("Nhan vien : %s%n", hd.getHoTenNV() != null ? hd.getHoTenNV() : hd.getMaNV());
            pw.printf("Khach hang: %s%n", hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "Khach le");
            pw.printf("SDT KH    : %s%n", hd.getSdtKhach() != null ? hd.getSdtKhach() : "---");
            pw.printf("Ngay ban  : %s%n", ngayBan);
            pw.println(DLINE);

            // Header chi tiet
            pw.printf("%-4s %-20s %6s %12s%n", "STT", "San pham", "SL", "Thanh tien");
            pw.println(DLINE);

            int stt = 1;
            for (ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                String ten = ct.getTenMay();
                // Cat ten neu qua dai
                if (ten != null && ten.length() > 20) {
                    ten = ten.substring(0, 17) + "...";
                }
                pw.printf("%-4d %-20s %6d %,12.0f%n",
                    stt++,
                    ten != null ? ten : ct.getMaMay(),
                    ct.getSoLuongBan(),
                    ct.getThanhTien()
                );
                // In don gia
                pw.printf("     Don gia: %,.0f VND%n", ct.getDonGia());
            }

            pw.println(LINE);
            pw.printf("%-34s %,16.0f%n", "TONG CONG:", hd.getTongTien());
            pw.println(LINE);

            if (hd.getGhiChu() != null && !hd.getGhiChu().trim().isEmpty()) {
                pw.printf("Ghi chu: %s%n", hd.getGhiChu());
                pw.println(DLINE);
            }

            pw.println(center("Cam on quy khach! Hen gap lai!", LINE_WIDTH));
            pw.println(center("In luc: " + LocalDateTime.now().format(dtf), LINE_WIDTH));
            pw.println(LINE);

            System.out.println("[BILL] Da xuat hoa don: " + filePath);
        }
    }

    /** Can giua chuoi trong do rong cho truoc */
    private static String center(String text, int width) {
        if (text == null || text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text;
    }
}
