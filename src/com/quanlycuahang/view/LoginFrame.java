package com.quanlycuahang.view;

import com.quanlycuahang.MainApp;
import com.quanlycuahang.dao.TaiKhoanDAO;
import com.quanlycuahang.model.TaiKhoan;
import com.quanlycuahang.util.SecurityUtils;
import com.quanlycuahang.util.UIUtils;
import com.quanlycuahang.config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private final JTextField tfUser = UIUtils.createTextField();
    private final JPasswordField pfPass = new JPasswordField();
    private final TaiKhoanDAO dao = new TaiKhoanDAO();

    public LoginFrame() {
        UIUtils.applyGlobalUI();
        initUI();
        connectDatabase();
    }

    private void initUI() {
        setTitle("Đăng Nhập Hệ Thống");
        setSize(400, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BG_DARK);

        pfPass.setFont(UIUtils.FONT_BODY);
        pfPass.setBackground(UIUtils.BG_INPUT);
        pfPass.setForeground(UIUtils.TEXT_PRIMARY);
        pfPass.setCaretColor(UIUtils.TEXT_PRIMARY);
        pfPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 10));
        panel.setBackground(UIUtils.BG_DARK);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("ĐĂNG NHẬP");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIUtils.PRIMARY);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(title);
        
        panel.add(new JLabel(" ")); // spacer

        panel.add(UIUtils.createLabel("Tên Đăng Nhập:"));
        panel.add(tfUser);
        panel.add(UIUtils.createLabel("Mật Khẩu:"));
        panel.add(pfPass);
        
        panel.add(new JLabel(" ")); // spacer

        JButton btnLogin = UIUtils.createPrimaryButton("Đăng Nhập");
        btnLogin.addActionListener(e -> handleLogin());
        panel.add(btnLogin);

        JButton btnRegister = UIUtils.createButton("Tạo Tài Khoản Mới", UIUtils.BG_PANEL);
        btnRegister.addActionListener(e -> new RegisterDialog(this).setVisible(true));
        panel.add(btnRegister);

        add(panel);
    }
    
    private void connectDatabase() {
        // Connect in background to avoid blocking UI when opening
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try (Connection ignored = DatabaseConnection.getConnection()) {
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            @Override
            protected void done() {
                try {
                    if (!get()) {
                        UIUtils.showError(LoginFrame.this, "Không thể kết nối CSDL!\nXem log console để biết chi tiết.");
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void handleLogin() {
        String user = tfUser.getText().trim();
        String pass = new String(pfPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            UIUtils.showError(this, "Vui lòng nhập tên đăng nhập và mật khẩu!");
            return;
        }

        try {
            String hash = SecurityUtils.hashPassword(pass);
            TaiKhoan tk = dao.dangNhap(user, hash);
            if (tk != null) {
                // Login successful
                dispose();
                SwingUtilities.invokeLater(() -> {
                    MainApp app = new MainApp(tk);
                    app.setVisible(true);
                });
            } else {
                UIUtils.showError(this, "Sai tên đăng nhập hoặc mật khẩu!");
            }
        } catch (SQLException ex) {
            UIUtils.showError(this, "Lỗi kết nối CSDL: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.stdout.encoding", "UTF-8");
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
