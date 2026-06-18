package com.quanlycuahang;

import com.quanlycuahang.config.DatabaseConnection;
import com.quanlycuahang.model.TaiKhoan;
import com.quanlycuahang.util.UIUtils;
import com.quanlycuahang.view.BaoCaoPanel;
import com.quanlycuahang.view.BaoHanhSuaChuaPanel;
import com.quanlycuahang.view.BanHangPanel;
import com.quanlycuahang.view.DashboardPanel;
import com.quanlycuahang.view.DienThoaiPanel;
import com.quanlycuahang.view.HoaDonPanel;
import com.quanlycuahang.view.KhachHangPanel;
import com.quanlycuahang.view.LoginFrame;
import com.quanlycuahang.view.LuongPanel;
import com.quanlycuahang.view.NhanVienPanel;
import com.quanlycuahang.view.NhapHangPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends JFrame {
    private final TaiKhoan currentUser;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final List<JButton> navButtons = new ArrayList<>();
    private NavItem[] navItems;

    private JLabel lblClock;
    private JLabel lblSectionTitle;
    private JLabel lblSectionSubtitle;
    private JLabel lblUser;

    public MainApp() {
        this(null);
    }

    public MainApp(TaiKhoan currentUser) {
        this.currentUser = currentUser;
        UIUtils.applyGlobalUI();
        initFrame();
        initComponents();
        connectDatabase();
        startClock();
    }

    private void initFrame() {
        setTitle("Quản lý cửa hàng điện thoại");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1240, 760));
        setSize(1440, 860);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BG_DARK);
        setIconImage(createIconImage());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                DatabaseConnection.closeConnection();
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        navItems = createNavItems();
        add(buildSidebar(), BorderLayout.WEST);
        add(buildWorkspace(), BorderLayout.CENTER);
        setJMenuBar(createMenuBar());

        for (NavItem item : navItems) {
            contentPanel.add(item.component, item.id);
        }
        selectNav(navItems[0]);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 16));
        sidebar.setPreferredSize(new Dimension(238, 0));
        sidebar.setBackground(UIUtils.SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIUtils.BORDER_COLOR));

        JPanel brand = new JPanel(new BorderLayout(0, 4));
        brand.setBackground(UIUtils.SIDEBAR_BG);
        brand.setBorder(new EmptyBorder(22, 20, 12, 18));

        JLabel logo = new JLabel("PS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);
        logo.setBackground(UIUtils.PRIMARY);
        logo.setOpaque(true);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(44, 44));

        JPanel brandText = new JPanel(new GridLayout(2, 1, 0, 2));
        brandText.setBackground(UIUtils.SIDEBAR_BG);
        JLabel name = new JLabel("Phone Store");
        name.setFont(new Font("Segoe UI", Font.BOLD, 18));
        name.setForeground(UIUtils.TEXT_PRIMARY);
        JLabel sub = new JLabel("Retail operations");
        sub.setFont(UIUtils.FONT_SMALL);
        sub.setForeground(UIUtils.TEXT_MUTED);
        brandText.add(name);
        brandText.add(sub);

        JPanel brandRow = new JPanel(new BorderLayout(12, 0));
        brandRow.setBackground(UIUtils.SIDEBAR_BG);
        brandRow.add(logo, BorderLayout.WEST);
        brandRow.add(brandText, BorderLayout.CENTER);
        brand.add(brandRow, BorderLayout.CENTER);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(UIUtils.SIDEBAR_BG);
        nav.setBorder(new EmptyBorder(0, 12, 0, 12));
        for (NavItem item : navItems) {
            JButton button = createNavButton(item);
            navButtons.add(button);
            nav.add(button);
            nav.add(Box.createVerticalStrut(4));
        }
        sidebar.add(nav, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 4));
        footer.setBackground(UIUtils.SIDEBAR_BG);
        footer.setBorder(new EmptyBorder(12, 20, 20, 18));
        JLabel status = UIUtils.createBadge("SQL Server", UIUtils.INFO);
        footer.add(status);
        JLabel version = new JLabel("Desktop POS v2.0");
        version.setFont(UIUtils.FONT_SMALL);
        version.setForeground(UIUtils.TEXT_MUTED);
        footer.add(version);
        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buildWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout(0, 0));
        workspace.setBackground(UIUtils.BG_DARK);
        workspace.add(buildHeader(), BorderLayout.NORTH);
        contentPanel.setBackground(UIUtils.BG_DARK);
        workspace.add(contentPanel, BorderLayout.CENTER);
        return workspace;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(UIUtils.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_COLOR),
            new EmptyBorder(14, 24, 14, 24)
        ));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setBackground(UIUtils.BG_CARD);
        lblSectionTitle = new JLabel("Tổng quan");
        lblSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblSectionTitle.setForeground(UIUtils.TEXT_PRIMARY);
        lblSectionSubtitle = new JLabel("Theo dõi vận hành cửa hàng");
        lblSectionSubtitle.setFont(UIUtils.FONT_SMALL);
        lblSectionSubtitle.setForeground(UIUtils.TEXT_MUTED);
        titlePanel.add(lblSectionTitle);
        titlePanel.add(lblSectionSubtitle);
        header.add(titlePanel, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(UIUtils.BG_CARD);
        lblUser = new JLabel(userDisplayName());
        lblUser.setFont(UIUtils.FONT_LABEL);
        lblUser.setForeground(UIUtils.TEXT_SECONDARY);
        lblClock = new JLabel("--:--:--");
        lblClock.setFont(UIUtils.FONT_LABEL);
        lblClock.setForeground(UIUtils.TEXT_PRIMARY);
        right.add(UIUtils.createBadge("Đang hoạt động", UIUtils.SUCCESS));
        right.add(lblUser);
        right.add(lblClock);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JButton createNavButton(NavItem item) {
        JButton button = new JButton(item.title);
        button.setName(item.id);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(UIUtils.FONT_BUTTON);
        button.setForeground(UIUtils.TEXT_SECONDARY);
        button.setBackground(UIUtils.SIDEBAR_BG);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setPreferredSize(new Dimension(200, 42));
        button.setBorder(new EmptyBorder(10, 14, 10, 14));
        button.addActionListener(e -> selectNav(item));
        return button;
    }

    private void selectNav(NavItem selected) {
        cardLayout.show(contentPanel, selected.id);
        lblSectionTitle.setText(selected.title);
        lblSectionSubtitle.setText(selected.subtitle);
        for (JButton button : navButtons) {
            boolean active = selected.id.equals(button.getName());
            button.setBackground(active ? UIUtils.NAV_ACTIVE_BG : UIUtils.SIDEBAR_BG);
            button.setForeground(active ? UIUtils.PRIMARY : UIUtils.TEXT_SECONDARY);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, active ? UIUtils.PRIMARY : UIUtils.SIDEBAR_BG),
                new EmptyBorder(10, 10, 10, 14)
            ));
        }
    }

    private NavItem[] createNavItems() {
        return new NavItem[]{
            new NavItem("dashboard", "Tổng quan", "Theo dõi doanh thu, tồn kho và cảnh báo", new DashboardPanel()),
            new NavItem("sales", "Bán hàng", "Tạo hóa đơn, tự trừ kho và tích điểm", new BanHangPanel()),
            new NavItem("invoices", "Hóa đơn", "Tra cứu lịch sử bán hàng và xuất lại bill", new HoaDonPanel()),
            new NavItem("purchase", "Nhập hàng", "Quản lý nhà cung cấp, phiếu nhập và cộng tồn kho", new NhapHangPanel()),
            new NavItem("products", "Điện thoại", "Danh mục sản phẩm và giá bán", new DienThoaiPanel()),
            new NavItem("customers", "Khách hàng", "CRM, liên hệ và điểm tích lũy", new KhachHangPanel()),
            new NavItem("service", "Bảo hành", "Phiếu bảo hành, sửa chữa và chi phí dịch vụ", new BaoHanhSuaChuaPanel()),
            new NavItem("staff", "Nhân viên", "Hồ sơ nhân viên cửa hàng", new NhanVienPanel()),
            new NavItem("salary", "Lương", "Tính lương tháng, thưởng và phạt", new LuongPanel()),
            new NavItem("reports", "Báo cáo", "Doanh thu, nhập hàng, top sản phẩm và tồn kho", new BaoCaoPanel())
        };
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(UIUtils.BG_CARD);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_COLOR));

        JMenu menuHeThong = new JMenu("Hệ thống");
        menuHeThong.setForeground(UIUtils.TEXT_PRIMARY);

        JMenuItem itemDangXuat = new JMenuItem("Đăng xuất");
        itemDangXuat.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JMenuItem itemThoat = new JMenuItem("Thoát");
        itemThoat.addActionListener(e -> System.exit(0));
        menuHeThong.add(itemDangXuat);
        menuHeThong.addSeparator();
        menuHeThong.add(itemThoat);

        JMenu menuTroGiup = new JMenu("Trợ giúp");
        menuTroGiup.setForeground(UIUtils.TEXT_PRIMARY);
        JMenuItem itemThongTin = new JMenuItem("Thông tin phần mềm");
        itemThongTin.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "Quản lý cửa hàng điện thoại v2.0\nPOS, CRM, kho, nhập hàng và báo cáo.",
                "Thông tin", JOptionPane.INFORMATION_MESSAGE)
        );
        menuTroGiup.add(itemThongTin);

        menuBar.add(menuHeThong);
        menuBar.add(menuTroGiup);
        return menuBar;
    }

    private void connectDatabase() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try (Connection ignored = DatabaseConnection.getConnection()) {
                    return true;
                } catch (Exception e) {
                    System.err.println("[DB ERROR] " + e.getMessage());
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (!get()) {
                        JOptionPane.showMessageDialog(MainApp.this,
                            "Không kết nối được SQL Server. Hãy chạy database/setup.sql và kiểm tra cấu hình SQL Server.",
                            "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainApp.this, "Lỗi bất ngờ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss  dd/MM/yyyy"));
            lblClock.setText(time);
        });
        timer.start();
    }

    private Image createIconImage() {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(64, 64,
            java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UIUtils.PRIMARY);
        g2.fillRoundRect(10, 4, 44, 56, 12, 12);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(16, 11, 32, 40, 6, 6);
        g2.setColor(UIUtils.PRIMARY_DARK);
        g2.fillOval(29, 53, 6, 6);
        g2.dispose();
        return img;
    }

    private String userDisplayName() {
        if (currentUser == null) return "Chưa xác định";
        return currentUser.getTenDangNhap() + " · " + currentUser.getVaiTro();
    }

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.stdout.encoding", "UTF-8");
        SwingUtilities.invokeLater(() -> {
            UIUtils.applyGlobalUI();
            new LoginFrame().setVisible(true);
        });
    }

    private static class NavItem {
        final String id;
        final String title;
        final String subtitle;
        final JPanel component;

        NavItem(String id, String title, String subtitle, JPanel component) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.component = component;
        }
    }
}
