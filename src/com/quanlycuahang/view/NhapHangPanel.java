package com.quanlycuahang.view;

import com.quanlycuahang.dao.DienThoaiDAO;
import com.quanlycuahang.dao.NhaCungCapDAO;
import com.quanlycuahang.dao.NhanVienDAO;
import com.quanlycuahang.dao.PhieuNhapDAO;
import com.quanlycuahang.model.ChiTietPhieuNhap;
import com.quanlycuahang.model.DienThoai;
import com.quanlycuahang.model.NhaCungCap;
import com.quanlycuahang.model.NhanVien;
import com.quanlycuahang.model.PhieuNhap;
import com.quanlycuahang.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NhapHangPanel extends JPanel {
    private final DienThoaiDAO dienThoaiDAO = new DienThoaiDAO();
    private final NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final PhieuNhapDAO phieuNhapDAO = new PhieuNhapDAO();

    private final DefaultTableModel productModel = new DefaultTableModel(
        new String[]{"Mã", "Tên máy", "Hãng", "Giá bán", "Tồn"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable productTable = new JTable(productModel);

    private final DefaultTableModel cartModel = new DefaultTableModel(
        new String[]{"Mã", "Tên máy", "SL", "Giá nhập", "Thành tiền"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable cartTable = new JTable(cartModel);

    private final DefaultTableModel receiptModel = new DefaultTableModel(
        new String[]{"Mã PN", "Nhà cung cấp", "Nhân viên", "Tổng tiền", "Ngày nhập", "Ghi chú"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable receiptTable = new JTable(receiptModel);

    private final DefaultTableModel receiptDetailModel = new DefaultTableModel(
        new String[]{"Mã máy", "Sản phẩm", "SL", "Giá nhập", "Thành tiền"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable receiptDetailTable = new JTable(receiptDetailModel);

    private final DefaultTableModel supplierModel = new DefaultTableModel(
        new String[]{"Mã NCC", "Tên NCC", "SĐT", "Email", "Địa chỉ", "Ghi chú"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable supplierTable = new JTable(supplierModel);

    private final JTextField tfProductSearch = UIUtils.createSearchField("Tìm điện thoại...");
    private final JTextField tfReceiptSearch = UIUtils.createSearchField("Tìm phiếu nhập, NCC, nhân viên...");
    private final JTextField tfSupplierSearch = UIUtils.createSearchField("Tìm nhà cung cấp...");
    private final JTextField tfSoLuong = UIUtils.createTextField();
    private final JTextField tfGiaNhap = UIUtils.createTextField();
    private final JTextField tfGhiChuNhap = UIUtils.createTextField();
    private final JComboBox<String> cbNhaCungCap = new JComboBox<>();
    private final JComboBox<String> cbNhanVien = new JComboBox<>();
    private final JLabel lblMaPN = new JLabel("---");
    private final JLabel lblTongTien = new JLabel("0 VND");
    private final JLabel lblSoMatHang = new JLabel("0");
    private final JLabel lblTongSoLuong = new JLabel("0");

    private final JTextField tfMaNCC = UIUtils.createTextField();
    private final JTextField tfTenNCC = UIUtils.createTextField();
    private final JTextField tfSdtNCC = UIUtils.createTextField();
    private final JTextField tfEmailNCC = UIUtils.createTextField();
    private final JTextField tfDiaChiNCC = UIUtils.createTextField();
    private final JTextField tfGhiChuNCC = UIUtils.createTextField();

    private final List<ChiTietPhieuNhap> cartItems = new ArrayList<>();

    public NhapHangPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UIUtils.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildTopTitle(), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Tạo phiếu nhập", buildCreateReceiptTab());
        tabs.addTab("Lịch sử nhập", buildReceiptHistoryTab());
        tabs.addTab("Nhà cung cấp", buildSupplierTab());
        add(tabs, BorderLayout.CENTER);

        registerEvents();
        reloadAll();
        initPhieuMoi();
    }

    private JPanel buildTopTitle() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BG_DARK);
        panel.add(UIUtils.createTitleLabel("Nhập hàng / Nhà cung cấp"), BorderLayout.WEST);
        return panel;
    }

    private JPanel buildCreateReceiptTab() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(UIUtils.BG_DARK);
        root.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel productPanel = UIUtils.createCard("Kho sản phẩm");
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBackground(UIUtils.BG_CARD);
        searchBar.add(tfProductSearch, BorderLayout.CENTER);
        JButton btnTimSP = UIUtils.createPrimaryButton("Tìm");
        btnTimSP.addActionListener(e -> timSanPham());
        searchBar.add(btnTimSP, BorderLayout.EAST);
        productPanel.add(searchBar, BorderLayout.NORTH);
        UIUtils.styleTable(productTable);
        productPanel.add(UIUtils.createScrollPane(productTable), BorderLayout.CENTER);

        JPanel addBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        addBar.setBackground(UIUtils.BG_CARD);
        tfSoLuong.setPreferredSize(new Dimension(68, 34));
        tfGiaNhap.setPreferredSize(new Dimension(130, 34));
        tfSoLuong.setText("1");
        addBar.add(UIUtils.createLabel("SL"));
        addBar.add(tfSoLuong);
        addBar.add(UIUtils.createLabel("Giá nhập"));
        addBar.add(tfGiaNhap);
        JButton btnThem = UIUtils.createSuccessButton("Thêm vào phiếu");
        btnThem.addActionListener(e -> themVaoPhieu());
        addBar.add(btnThem);
        productPanel.add(addBar, BorderLayout.SOUTH);

        JPanel cartPanel = UIUtils.createCard("Chi tiết phiếu nhập");
        UIUtils.styleTable(cartTable);
        cartPanel.add(UIUtils.createScrollPane(cartTable), BorderLayout.CENTER);
        JPanel cartBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        cartBottom.setBackground(UIUtils.BG_CARD);
        JButton btnXoaDong = UIUtils.createDangerButton("Xóa dòng");
        btnXoaDong.addActionListener(e -> xoaKhoiPhieu());
        cartBottom.add(btnXoaDong);
        cartPanel.add(cartBottom, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, productPanel, cartPanel);
        split.setDividerLocation(510);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setBackground(UIUtils.BG_DARK);
        root.add(split, BorderLayout.CENTER);
        root.add(buildReceiptSidePanel(), BorderLayout.EAST);
        return root;
    }

    private JPanel buildReceiptSidePanel() {
        JPanel panel = UIUtils.createCard("Thông tin nhập");
        panel.setPreferredSize(new Dimension(310, 0));

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 8));
        fields.setBackground(UIUtils.BG_CARD);

        lblMaPN.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMaPN.setForeground(UIUtils.PRIMARY);
        addRow(fields, "Mã phiếu", lblMaPN);

        UIUtils.styleComboBox(cbNhaCungCap);
        UIUtils.styleComboBox(cbNhanVien);
        addRow(fields, "Nhà cung cấp", cbNhaCungCap);
        addRow(fields, "Nhân viên nhập *", cbNhanVien);
        addRow(fields, "Ghi chú", tfGhiChuNhap);

        JPanel totalPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        totalPanel.setBackground(UIUtils.BG_CARD);
        totalPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.SUCCESS, 1),
            new EmptyBorder(12, 12, 12, 12)
        ));
        totalPanel.add(UIUtils.createLabel("TỔNG CHI PHÍ NHẬP"));
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTongTien.setForeground(UIUtils.SUCCESS);
        totalPanel.add(lblTongTien);
        fields.add(totalPanel);

        JPanel countPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        countPanel.setBackground(UIUtils.BG_CARD);
        countPanel.add(createMiniMetric("Mặt hàng", lblSoMatHang));
        countPanel.add(createMiniMetric("Tổng SL", lblTongSoLuong));
        fields.add(countPanel);

        panel.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 8));
        buttons.setBackground(UIUtils.BG_CARD);
        buttons.setBorder(new EmptyBorder(12, 0, 0, 0));
        JButton btnLuu = UIUtils.createSuccessButton("Lưu phiếu nhập");
        JButton btnMoi = UIUtils.createInfoButton("Phiếu mới");
        btnLuu.addActionListener(e -> luuPhieuNhap());
        btnMoi.addActionListener(e -> initPhieuMoi());
        buttons.add(btnLuu);
        buttons.add(btnMoi);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildReceiptHistoryTab() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(UIUtils.BG_DARK);
        root.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(UIUtils.BG_DARK);
        header.add(UIUtils.createTitleLabel("Lịch sử nhập hàng"), BorderLayout.WEST);
        JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        search.setBackground(UIUtils.BG_DARK);
        tfReceiptSearch.setPreferredSize(new Dimension(280, 36));
        JButton btnTim = UIUtils.createPrimaryButton("Tìm");
        JButton btnLamMoi = UIUtils.createInfoButton("Làm mới");
        btnTim.addActionListener(e -> timPhieuNhap());
        btnLamMoi.addActionListener(e -> loadReceiptHistory());
        search.add(tfReceiptSearch);
        search.add(btnTim);
        search.add(btnLamMoi);
        header.add(search, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JPanel receipts = UIUtils.createCard("Phiếu nhập");
        UIUtils.styleTable(receiptTable);
        receipts.add(UIUtils.createScrollPane(receiptTable), BorderLayout.CENTER);

        JPanel details = UIUtils.createCard("Chi tiết phiếu nhập");
        UIUtils.styleTable(receiptDetailTable);
        details.add(UIUtils.createScrollPane(receiptDetailTable), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, receipts, details);
        split.setDividerLocation(360);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setBackground(UIUtils.BG_DARK);
        root.add(split, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildSupplierTab() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(UIUtils.BG_DARK);
        root.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(UIUtils.BG_DARK);
        header.add(UIUtils.createTitleLabel("Quản lý nhà cung cấp"), BorderLayout.WEST);
        JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        search.setBackground(UIUtils.BG_DARK);
        tfSupplierSearch.setPreferredSize(new Dimension(240, 36));
        JButton btnTim = UIUtils.createPrimaryButton("Tìm");
        JButton btnLamMoi = UIUtils.createInfoButton("Làm mới");
        btnTim.addActionListener(e -> timNhaCungCap());
        btnLamMoi.addActionListener(e -> {
            loadSuppliers();
            clearSupplierForm();
        });
        search.add(tfSupplierSearch);
        search.add(btnTim);
        search.add(btnLamMoi);
        header.add(search, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        UIUtils.styleTable(supplierTable);
        root.add(UIUtils.createScrollPane(supplierTable), BorderLayout.CENTER);
        root.add(buildSupplierForm(), BorderLayout.EAST);
        return root;
    }

    private JPanel buildSupplierForm() {
        JPanel form = UIUtils.createCard("Thông tin NCC");
        form.setPreferredSize(new Dimension(310, 0));
        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 6));
        fields.setBackground(UIUtils.BG_CARD);
        tfMaNCC.setEditable(false);
        addRow(fields, "Mã NCC", tfMaNCC);
        addRow(fields, "Tên NCC *", tfTenNCC);
        addRow(fields, "SĐT", tfSdtNCC);
        addRow(fields, "Email", tfEmailNCC);
        addRow(fields, "Địa chỉ", tfDiaChiNCC);
        addRow(fields, "Ghi chú", tfGhiChuNCC);
        form.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
        buttons.setBackground(UIUtils.BG_CARD);
        buttons.setBorder(new EmptyBorder(12, 0, 0, 0));
        JButton btnThem = UIUtils.createSuccessButton("Thêm");
        JButton btnSua = UIUtils.createWarningButton("Cập nhật");
        JButton btnXoa = UIUtils.createDangerButton("Xóa");
        JButton btnClear = UIUtils.createButton("Xóa form", UIUtils.BG_PANEL);
        btnThem.addActionListener(e -> themNhaCungCap());
        btnSua.addActionListener(e -> suaNhaCungCap());
        btnXoa.addActionListener(e -> xoaNhaCungCap());
        btnClear.addActionListener(e -> clearSupplierForm());
        buttons.add(btnThem);
        buttons.add(btnSua);
        buttons.add(btnXoa);
        buttons.add(btnClear);
        form.add(buttons, BorderLayout.SOUTH);
        return form;
    }

    private void registerEvents() {
        UIUtils.addDebouncedTextChangeListener(tfProductSearch, 250, this::timSanPham);
        UIUtils.addDebouncedTextChangeListener(tfReceiptSearch, 250, this::timPhieuNhap);
        UIUtils.addDebouncedTextChangeListener(tfSupplierSearch, 250, this::timNhaCungCap);
        tfProductSearch.addActionListener(e -> timSanPham());
        tfSoLuong.addActionListener(e -> themVaoPhieu());
        tfGiaNhap.addActionListener(e -> themVaoPhieu());

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && productTable.getSelectedRow() >= 0) {
                String giaBan = productModel.getValueAt(productTable.getSelectedRow(), 3).toString()
                    .replace(" VND", "").replace(",", "");
                tfGiaNhap.setText(giaBan);
            }
        });

        receiptTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && receiptTable.getSelectedRow() >= 0) {
                loadReceiptDetail(receiptModel.getValueAt(receiptTable.getSelectedRow(), 0).toString());
            }
        });

        supplierTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && supplierTable.getSelectedRow() >= 0) {
                fillSupplierForm(supplierTable.getSelectedRow());
            }
        });
    }

    private void reloadAll() {
        loadProducts();
        loadSuppliers();
        loadEmployees();
        loadReceiptHistory();
    }

    private void loadProducts() {
        productModel.setRowCount(0);
        try {
            for (DienThoai dt : dienThoaiDAO.layTatCa()) {
                productModel.addRow(new Object[]{
                    dt.getMaMay(),
                    dt.getTenMay(),
                    dt.getHangSX(),
                    String.format("%,.0f VND", dt.getGia()),
                    dt.getSoLuong()
                });
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tải sản phẩm:\n" + ex.getMessage());
        }
    }

    private void timSanPham() {
        String kw = tfProductSearch.getText().trim();
        if (kw.isEmpty() || kw.equals("Tìm điện thoại...")) {
            loadProducts();
            return;
        }
        productModel.setRowCount(0);
        try {
            for (DienThoai dt : dienThoaiDAO.timKiem(kw)) {
                productModel.addRow(new Object[]{
                    dt.getMaMay(),
                    dt.getTenMay(),
                    dt.getHangSX(),
                    String.format("%,.0f VND", dt.getGia()),
                    dt.getSoLuong()
                });
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tìm sản phẩm:\n" + ex.getMessage());
        }
    }

    private void loadSuppliers() {
        supplierModel.setRowCount(0);
        cbNhaCungCap.removeAllItems();
        cbNhaCungCap.addItem("-- Chưa chọn --");
        try {
            for (NhaCungCap ncc : nhaCungCapDAO.layTatCa()) {
                supplierModel.addRow(ncc.toTableRow());
                cbNhaCungCap.addItem(ncc.toString());
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tải nhà cung cấp:\n" + ex.getMessage());
        }
    }

    private void timNhaCungCap() {
        String kw = tfSupplierSearch.getText().trim();
        if (kw.isEmpty() || kw.equals("Tìm nhà cung cấp...")) {
            loadSuppliers();
            return;
        }
        supplierModel.setRowCount(0);
        try {
            for (NhaCungCap ncc : nhaCungCapDAO.timKiem(kw)) {
                supplierModel.addRow(ncc.toTableRow());
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tìm nhà cung cấp:\n" + ex.getMessage());
        }
    }

    private void loadEmployees() {
        cbNhanVien.removeAllItems();
        try {
            for (NhanVien nv : nhanVienDAO.layTatCa()) {
                cbNhanVien.addItem(nv.getMaNV() + " - " + nv.getHoTen());
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tải nhân viên:\n" + ex.getMessage());
        }
    }

    private void loadReceiptHistory() {
        receiptModel.setRowCount(0);
        receiptDetailModel.setRowCount(0);
        try {
            for (PhieuNhap pn : phieuNhapDAO.layTatCa()) {
                receiptModel.addRow(pn.toTableRow());
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tải phiếu nhập:\n" + ex.getMessage());
        }
    }

    private void timPhieuNhap() {
        String kw = tfReceiptSearch.getText().trim();
        if (kw.isEmpty() || kw.equals("Tìm phiếu nhập, NCC, nhân viên...")) {
            loadReceiptHistory();
            return;
        }
        receiptModel.setRowCount(0);
        receiptDetailModel.setRowCount(0);
        try {
            for (PhieuNhap pn : phieuNhapDAO.timKiem(kw)) {
                receiptModel.addRow(pn.toTableRow());
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tìm phiếu nhập:\n" + ex.getMessage());
        }
    }

    private void loadReceiptDetail(String maPN) {
        receiptDetailModel.setRowCount(0);
        try {
            for (ChiTietPhieuNhap ct : phieuNhapDAO.layChiTiet(maPN)) {
                receiptDetailModel.addRow(ct.toTableRow());
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi tải chi tiết phiếu nhập:\n" + ex.getMessage());
        }
    }

    private void initPhieuMoi() {
        cartItems.clear();
        cartModel.setRowCount(0);
        tfSoLuong.setText("1");
        tfGiaNhap.setText("");
        tfGhiChuNhap.setText("");
        refreshCart();
        try {
            lblMaPN.setText(phieuNhapDAO.sinhMaPhieuNhap());
        } catch (SQLException ex) {
            lblMaPN.setText("PN001");
        }
    }

    private void themVaoPhieu() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Chọn sản phẩm cần nhập.");
            return;
        }
        int soLuong;
        BigDecimal giaNhap;
        try {
            soLuong = Integer.parseInt(tfSoLuong.getText().trim());
            giaNhap = parseMoney(tfGiaNhap.getText());
            if (soLuong <= 0 || giaNhap.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
        } catch (Exception ex) {
            UIUtils.showError(this, "Số lượng phải > 0 và giá nhập phải >= 0.");
            return;
        }

        String maMay = productModel.getValueAt(row, 0).toString();
        String tenMay = productModel.getValueAt(row, 1).toString();
        for (ChiTietPhieuNhap ct : cartItems) {
            if (ct.getMaMay().equals(maMay)) {
                ct.setSoLuong(ct.getSoLuong() + soLuong);
                ct.setDonGia(giaNhap);
                refreshCart();
                return;
            }
        }
        cartItems.add(new ChiTietPhieuNhap(lblMaPN.getText(), maMay, tenMay, soLuong, giaNhap));
        refreshCart();
    }

    private void xoaKhoiPhieu() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            UIUtils.showError(this, "Chọn dòng cần xóa trong phiếu nhập.");
            return;
        }
        cartItems.remove(row);
        refreshCart();
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        BigDecimal tong = BigDecimal.ZERO;
        int tongSoLuong = 0;
        for (ChiTietPhieuNhap ct : cartItems) {
            cartModel.addRow(ct.toTableRow());
            tong = tong.add(ct.getThanhTien());
            tongSoLuong += ct.getSoLuong();
        }
        lblTongTien.setText(String.format("%,.0f VND", tong));
        lblSoMatHang.setText(String.valueOf(cartItems.size()));
        lblTongSoLuong.setText(String.valueOf(tongSoLuong));
    }

    private void luuPhieuNhap() {
        if (cartItems.isEmpty()) {
            UIUtils.showError(this, "Phiếu nhập chưa có sản phẩm.");
            return;
        }
        if (cbNhanVien.getSelectedItem() == null) {
            UIUtils.showError(this, "Chọn nhân viên nhập hàng.");
            return;
        }
        String maPN = lblMaPN.getText();
        if (!UIUtils.showConfirm(this, "Lưu phiếu nhập " + maPN + " và cộng tồn kho?")) return;

        PhieuNhap pn = new PhieuNhap(maPN, getSelectedMaNCC(), getSelectedMaNV(), tfGhiChuNhap.getText().trim());
        pn.setDanhSachChiTiet(new ArrayList<>(cartItems));
        try {
            phieuNhapDAO.taoPhieuNhap(pn);
            UIUtils.showSuccess(this, "Đã lưu phiếu nhập và cập nhật tồn kho.");
            initPhieuMoi();
            loadProducts();
            loadReceiptHistory();
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi lưu phiếu nhập:\n" + ex.getMessage());
        }
    }

    private void themNhaCungCap() {
        if (!validateSupplierForm()) return;
        try {
            NhaCungCap ncc = buildSupplierFromForm();
            ncc.setMaNCC(nhaCungCapDAO.sinhMaNhaCungCap());
            if (nhaCungCapDAO.them(ncc)) {
                UIUtils.showSuccess(this, "Đã thêm nhà cung cấp.");
                loadSuppliers();
                clearSupplierForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi thêm nhà cung cấp:\n" + ex.getMessage());
        }
    }

    private void suaNhaCungCap() {
        if (tfMaNCC.getText().trim().isEmpty()) {
            UIUtils.showError(this, "Chọn nhà cung cấp cần cập nhật.");
            return;
        }
        if (!validateSupplierForm()) return;
        try {
            if (nhaCungCapDAO.capNhat(buildSupplierFromForm())) {
                UIUtils.showSuccess(this, "Đã cập nhật nhà cung cấp.");
                loadSuppliers();
                clearSupplierForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi cập nhật nhà cung cấp:\n" + ex.getMessage());
        }
    }

    private void xoaNhaCungCap() {
        if (tfMaNCC.getText().trim().isEmpty()) {
            UIUtils.showError(this, "Chọn nhà cung cấp cần xóa.");
            return;
        }
        if (!UIUtils.showConfirm(this, "Xóa nhà cung cấp " + tfTenNCC.getText().trim() + "?")) return;
        try {
            if (nhaCungCapDAO.xoa(tfMaNCC.getText().trim())) {
                UIUtils.showSuccess(this, "Đã xóa nhà cung cấp.");
                loadSuppliers();
                clearSupplierForm();
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Không thể xóa nhà cung cấp đang có phiếu nhập.\n" + ex.getMessage());
        }
    }

    private void fillSupplierForm(int row) {
        tfMaNCC.setText(supplierModel.getValueAt(row, 0).toString());
        tfTenNCC.setText(supplierModel.getValueAt(row, 1).toString());
        tfSdtNCC.setText(supplierModel.getValueAt(row, 2).toString());
        tfEmailNCC.setText(supplierModel.getValueAt(row, 3).toString());
        tfDiaChiNCC.setText(supplierModel.getValueAt(row, 4).toString());
        tfGhiChuNCC.setText(supplierModel.getValueAt(row, 5).toString());
    }

    private NhaCungCap buildSupplierFromForm() {
        return new NhaCungCap(
            tfMaNCC.getText().trim(),
            tfTenNCC.getText().trim(),
            tfSdtNCC.getText().trim(),
            tfEmailNCC.getText().trim(),
            tfDiaChiNCC.getText().trim(),
            tfGhiChuNCC.getText().trim()
        );
    }

    private boolean validateSupplierForm() {
        if (tfTenNCC.getText().trim().isEmpty()) {
            UIUtils.showError(this, "Nhập tên nhà cung cấp.");
            return false;
        }
        return true;
    }

    private void clearSupplierForm() {
        tfMaNCC.setText("");
        tfTenNCC.setText("");
        tfSdtNCC.setText("");
        tfEmailNCC.setText("");
        tfDiaChiNCC.setText("");
        tfGhiChuNCC.setText("");
        supplierTable.clearSelection();
    }

    private String getSelectedMaNCC() {
        Object selected = cbNhaCungCap.getSelectedItem();
        if (selected == null || selected.toString().startsWith("--")) return null;
        String value = selected.toString();
        return value.contains(" - ") ? value.split(" - ")[0] : value;
    }

    private String getSelectedMaNV() {
        Object selected = cbNhanVien.getSelectedItem();
        if (selected == null) return null;
        String value = selected.toString();
        return value.contains(" - ") ? value.split(" - ")[0] : value;
    }

    private BigDecimal parseMoney(String value) {
        String normalized = value.trim().replace(" VND", "").replace(",", "").replace(".", "");
        if (normalized.isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(normalized);
    }

    private void addRow(JPanel parent, String label, JComponent input) {
        JPanel row = new JPanel(new GridLayout(2, 1, 0, 2));
        row.setBackground(UIUtils.BG_CARD);
        row.add(UIUtils.createLabel(label));
        row.add(input);
        parent.add(row);
    }

    private JPanel createMiniMetric(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 2));
        panel.setBackground(UIUtils.BG_PANEL);
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));
        panel.add(UIUtils.createLabel(title));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(UIUtils.TEXT_PRIMARY);
        panel.add(valueLabel);
        return panel;
    }
}
