package com.quanlycuahang.view;

import com.quanlycuahang.dao.BaoCaoDAO;
import com.quanlycuahang.model.DienThoai;
import com.quanlycuahang.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;

public class BaoCaoPanel extends JPanel {
    private final BaoCaoDAO dao = new BaoCaoDAO();

    private final JTextField tfFrom = UIUtils.createTextField();
    private final JTextField tfTo = UIUtils.createTextField();
    private final JLabel lblStatus = UIUtils.createLabel("Sẵn sàng");
    private final JButton btnTaiBaoCao = UIUtils.createPrimaryButton("Tải báo cáo");

    private final JPanel statsPanel = new JPanel(new GridLayout(2, 4, 12, 12));

    private final DefaultTableModel revenueModel = new DefaultTableModel(
        new String[]{"Ngày", "Hóa đơn", "Doanh thu"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable revenueTable = new JTable(revenueModel);

    private final DefaultTableModel topProductModel = new DefaultTableModel(
        new String[]{"Mã máy", "Sản phẩm", "SL bán", "Doanh thu"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable topProductTable = new JTable(topProductModel);

    private final DefaultTableModel lowStockModel = new DefaultTableModel(
        new String[]{"Mã máy", "Sản phẩm", "Hãng", "Tồn", "Giá trị tồn"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable lowStockTable = new JTable(lowStockModel);

    public BaoCaoPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UIUtils.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        initRange();
        btnTaiBaoCao.addActionListener(e -> loadReport());
        loadReport();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(UIUtils.BG_DARK);

        JPanel title = new JPanel(new GridLayout(2, 1, 0, 2));
        title.setBackground(UIUtils.BG_DARK);
        title.add(UIUtils.createTitleLabel("Báo cáo kinh doanh"));
        lblStatus.setForeground(UIUtils.TEXT_MUTED);
        title.add(lblStatus);
        header.add(title, BorderLayout.WEST);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filters.setBackground(UIUtils.BG_DARK);
        tfFrom.setPreferredSize(new Dimension(110, 36));
        tfTo.setPreferredSize(new Dimension(110, 36));
        filters.add(UIUtils.createLabel("Từ"));
        filters.add(tfFrom);
        filters.add(UIUtils.createLabel("Đến"));
        filters.add(tfTo);
        filters.add(btnTaiBaoCao);
        header.add(filters, BorderLayout.EAST);
        return header;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(UIUtils.BG_DARK);

        statsPanel.setBackground(UIUtils.BG_DARK);
        content.add(statsPanel, BorderLayout.NORTH);

        JPanel left = UIUtils.createCard("Doanh thu theo ngày");
        UIUtils.styleTable(revenueTable);
        left.add(UIUtils.createScrollPane(revenueTable), BorderLayout.CENTER);

        JPanel right = new JPanel(new GridLayout(2, 1, 0, 12));
        right.setBackground(UIUtils.BG_DARK);

        JPanel top = UIUtils.createCard("Top sản phẩm bán chạy");
        UIUtils.styleTable(topProductTable);
        top.add(UIUtils.createScrollPane(topProductTable), BorderLayout.CENTER);
        right.add(top);

        JPanel low = UIUtils.createCard("Tồn kho cần nhập");
        UIUtils.styleTable(lowStockTable);
        low.add(UIUtils.createScrollPane(lowStockTable), BorderLayout.CENTER);
        right.add(low);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(470);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setBackground(UIUtils.BG_DARK);
        content.add(split, BorderLayout.CENTER);
        return content;
    }

    private void loadReport() {
        LocalDate from;
        LocalDate to;
        try {
            from = LocalDate.parse(tfFrom.getText().trim());
            to = LocalDate.parse(tfTo.getText().trim());
            if (to.isBefore(from)) throw new IllegalArgumentException("Ngày kết thúc trước ngày bắt đầu.");
        } catch (Exception ex) {
            UIUtils.showError(this, "Khoảng ngày phải theo định dạng yyyy-MM-dd.");
            return;
        }

        lblStatus.setText("Đang tải báo cáo...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private BaoCaoDAO.KpiData kpi;
            private java.util.List<Object[]> revenueRows;
            private java.util.List<Object[]> topProductRows;
            private java.util.List<DienThoai> lowStockRows;

            @Override
            protected Void doInBackground() throws SQLException {
                kpi = dao.layKpi(from, to);
                revenueRows = dao.doanhThuTheoNgay(from, to);
                topProductRows = dao.sanPhamBanChay(from, to);
                lowStockRows = dao.tonKhoThap(5);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    render(kpi, revenueRows, topProductRows, lowStockRows);
                    lblStatus.setText("Cập nhật kỳ " + from + " đến " + to);
                } catch (Exception ex) {
                    lblStatus.setText("Không tải được báo cáo.");
                    UIUtils.showError(BaoCaoPanel.this, "Lỗi báo cáo:\n" + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void render(BaoCaoDAO.KpiData kpi, java.util.List<Object[]> revenueRows,
                        java.util.List<Object[]> topProductRows, java.util.List<DienThoai> lowStockRows) {
        statsPanel.removeAll();
        statsPanel.add(UIUtils.createStatCard("Doanh thu hôm nay", BaoCaoDAO.formatMoney(kpi.doanhThuHomNay), UIUtils.SUCCESS));
        statsPanel.add(UIUtils.createStatCard("Hóa đơn hôm nay", String.valueOf(kpi.hoaDonHomNay), UIUtils.PRIMARY));
        statsPanel.add(UIUtils.createStatCard("Doanh thu kỳ", BaoCaoDAO.formatMoney(kpi.doanhThuKy), UIUtils.INFO));
        statsPanel.add(UIUtils.createStatCard("Chi nhập kỳ", BaoCaoDAO.formatMoney(kpi.chiNhapKy), UIUtils.WARNING));
        statsPanel.add(UIUtils.createStatCard("Dịch vụ kỳ", BaoCaoDAO.formatMoney(kpi.doanhThuDichVuKy), UIUtils.PRIMARY_DARK));
        statsPanel.add(UIUtils.createStatCard("Giá trị tồn", BaoCaoDAO.formatMoney(kpi.giaTriTonKho), UIUtils.INFO));
        statsPanel.add(UIUtils.createStatCard("Khách hàng", String.valueOf(kpi.khachHang), UIUtils.SUCCESS));
        statsPanel.add(UIUtils.createStatCard("Cần nhập", String.valueOf(kpi.sapHetHang), UIUtils.DANGER));
        statsPanel.revalidate();
        statsPanel.repaint();

        revenueModel.setRowCount(0);
        for (Object[] row : revenueRows) revenueModel.addRow(row);

        topProductModel.setRowCount(0);
        for (Object[] row : topProductRows) topProductModel.addRow(row);

        lowStockModel.setRowCount(0);
        for (DienThoai dt : lowStockRows) {
            lowStockModel.addRow(new Object[]{
                dt.getMaMay(),
                dt.getTenMay(),
                dt.getHangSX(),
                dt.getSoLuong(),
                BaoCaoDAO.formatMoney(dt.getGia().multiply(java.math.BigDecimal.valueOf(dt.getSoLuong())))
            });
        }
    }

    private void initRange() {
        LocalDate today = LocalDate.now();
        tfFrom.setText(today.withDayOfMonth(1).toString());
        tfTo.setText(today.toString());
    }
}
