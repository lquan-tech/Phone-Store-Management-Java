package com.quanlycuahang.view;

import com.quanlycuahang.dao.HoaDonDAO;
import com.quanlycuahang.model.ChiTietHoaDon;
import com.quanlycuahang.model.HoaDon;
import com.quanlycuahang.util.BillExporter;
import com.quanlycuahang.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HoaDonPanel extends JPanel {
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    private final String[] invoiceColumns = {"Mã HD", "Nhân viên", "Khách hàng", "SĐT", "Tổng tiền", "Ngày bán"};
    private final DefaultTableModel invoiceModel = new DefaultTableModel(invoiceColumns, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable invoiceTable = new JTable(invoiceModel);

    private final String[] detailColumns = {"Mã máy", "Sản phẩm", "Đơn giá", "SL", "Thành tiền"};
    private final DefaultTableModel detailModel = new DefaultTableModel(detailColumns, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable detailTable = new JTable(detailModel);

    private final JTextField tfSearch = UIUtils.createSearchField("Tìm mã HD, khách hàng, SĐT...");
    private final JTextField tfFrom = UIUtils.createTextField();
    private final JTextField tfTo = UIUtils.createTextField();
    private final JLabel lblSummary = UIUtils.createLabel("Chưa tải dữ liệu");

    private final JButton btnLoc = UIUtils.createPrimaryButton("Lọc");
    private final JButton btnLamMoi = UIUtils.createInfoButton("Tất cả");
    private final JButton btnXuatBill = UIUtils.createSuccessButton("Xuất bill");

    private List<HoaDon> allInvoices = new ArrayList<>();
    private HoaDon selectedInvoice;

    public HoaDonPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UIUtils.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        registerEvents();
        resetDateRange();
        loadData();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(UIUtils.BG_DARK);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setBackground(UIUtils.BG_DARK);
        titlePanel.add(UIUtils.createTitleLabel("Lịch sử hóa đơn"));
        lblSummary.setForeground(UIUtils.TEXT_MUTED);
        titlePanel.add(lblSummary);
        bar.add(titlePanel, BorderLayout.WEST);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filter.setBackground(UIUtils.BG_DARK);
        tfSearch.setPreferredSize(new Dimension(250, 36));
        tfFrom.setPreferredSize(new Dimension(105, 36));
        tfTo.setPreferredSize(new Dimension(105, 36));
        filter.add(tfSearch);
        filter.add(UIUtils.createLabel("Từ"));
        filter.add(tfFrom);
        filter.add(UIUtils.createLabel("Đến"));
        filter.add(tfTo);
        filter.add(btnLoc);
        filter.add(btnLamMoi);
        filter.add(btnXuatBill);
        bar.add(filter, BorderLayout.EAST);
        return bar;
    }

    private JSplitPane buildContent() {
        JPanel invoices = UIUtils.createCard("Hóa đơn");
        UIUtils.styleTable(invoiceTable);
        int[] widths = {80, 140, 150, 110, 130, 145};
        for (int i = 0; i < widths.length; i++) {
            invoiceTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        invoices.add(UIUtils.createScrollPane(invoiceTable), BorderLayout.CENTER);

        JPanel details = UIUtils.createCard("Chi tiết hóa đơn");
        UIUtils.styleTable(detailTable);
        int[] detailWidths = {80, 220, 120, 60, 130};
        for (int i = 0; i < detailWidths.length; i++) {
            detailTable.getColumnModel().getColumn(i).setPreferredWidth(detailWidths[i]);
        }
        details.add(UIUtils.createScrollPane(detailTable), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, invoices, details);
        split.setDividerLocation(390);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setBackground(UIUtils.BG_DARK);
        return split;
    }

    private void registerEvents() {
        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && invoiceTable.getSelectedRow() >= 0) {
                loadSelectedInvoice();
            }
        });
        btnLoc.addActionListener(e -> renderFiltered());
        btnLamMoi.addActionListener(e -> {
            resetDateRange();
            tfSearch.setText("Tìm mã HD, khách hàng, SĐT...");
            loadData();
        });
        btnXuatBill.addActionListener(e -> xuatBill());
        tfSearch.addActionListener(e -> renderFiltered());
        UIUtils.addDebouncedTextChangeListener(tfSearch, 250, this::renderFiltered);
    }

    private void loadData() {
        selectedInvoice = null;
        detailModel.setRowCount(0);
        try {
            allInvoices = hoaDonDAO.layTatCa();
            renderFiltered();
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tải hóa đơn:\n" + ex.getMessage());
        }
    }

    private void renderFiltered() {
        invoiceModel.setRowCount(0);
        detailModel.setRowCount(0);
        selectedInvoice = null;

        String kw = tfSearch.getText().trim().toLowerCase();
        boolean hasKeyword = !kw.isEmpty() && !kw.equals("tìm mã hd, khách hàng, sđt...");
        LocalDate from = parseDateOrNull(tfFrom.getText().trim());
        LocalDate to = parseDateOrNull(tfTo.getText().trim());

        int count = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (HoaDon hd : allInvoices) {
            if (!matchesKeyword(hd, kw, hasKeyword) || !matchesDate(hd, from, to)) continue;
            invoiceModel.addRow(hd.toTableRow());
            count++;
            if (hd.getTongTien() != null) total = total.add(hd.getTongTien());
        }
        lblSummary.setText("Hiển thị " + count + " hóa đơn | Tổng doanh thu: " + formatMoney(total));
    }

    private boolean matchesKeyword(HoaDon hd, String keyword, boolean hasKeyword) {
        if (!hasKeyword) return true;
        return contains(hd.getMaHD(), keyword)
            || contains(hd.getHoTenNV(), keyword)
            || contains(hd.getTenKhachHang(), keyword)
            || contains(hd.getSdtKhach(), keyword);
    }

    private boolean matchesDate(HoaDon hd, LocalDate from, LocalDate to) {
        if (hd.getNgayBan() == null) return true;
        LocalDate date = hd.getNgayBan().toLocalDate();
        if (from != null && date.isBefore(from)) return false;
        return to == null || !date.isAfter(to);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private void loadSelectedInvoice() {
        int row = invoiceTable.getSelectedRow();
        if (row < 0) return;
        String maHD = invoiceModel.getValueAt(row, 0).toString();
        try {
            selectedInvoice = hoaDonDAO.layTheoMa(maHD);
            detailModel.setRowCount(0);
            if (selectedInvoice != null) {
                for (ChiTietHoaDon ct : selectedInvoice.getDanhSachChiTiet()) {
                    detailModel.addRow(ct.toTableRow());
                }
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tải chi tiết hóa đơn:\n" + ex.getMessage());
        }
    }

    private void xuatBill() {
        if (selectedInvoice == null) {
            UIUtils.showError(this, "Chọn hóa đơn cần xuất bill.");
            return;
        }
        try {
            File dir = new File("bills");
            dir.mkdirs();
            String filePath = "bills/" + selectedInvoice.getMaHD() + ".txt";
            BillExporter.xuatBill(selectedInvoice, filePath);
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(new File(filePath));
            UIUtils.showSuccess(this, "Đã xuất bill: " + filePath);
        } catch (IOException ex) {
            UIUtils.showError(this, "Lỗi xuất bill:\n" + ex.getMessage());
        }
    }

    private void resetDateRange() {
        LocalDate now = LocalDate.now();
        tfFrom.setText(now.withDayOfMonth(1).toString());
        tfTo.setText(now.toString());
    }

    private LocalDate parseDateOrNull(String value) {
        if (value.isEmpty()) return null;
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private String formatMoney(BigDecimal value) {
        return String.format("%,.0f VND", value != null ? value : BigDecimal.ZERO);
    }
}
