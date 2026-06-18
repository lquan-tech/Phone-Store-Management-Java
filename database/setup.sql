-- ============================================================
-- Script: Tao Database va Cac Bang
-- Du An: Quan Ly Cua Hang Dien Thoai
-- Mo Ta: Su dung NVARCHAR cho ky tu tieng Viet
-- ============================================================

-- 1. Tao Database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'QuanLyCuaHangDienThoai')
BEGIN
    CREATE DATABASE QuanLyCuaHangDienThoai
    COLLATE Vietnamese_CI_AS;
    PRINT N'Da tao database QuanLyCuaHangDienThoai thanh cong.';
END
GO

USE QuanLyCuaHangDienThoai;
GO

-- ============================================================
-- 2. Bang DienThoai (San Pham)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'DienThoai') AND type = 'U')
BEGIN
    CREATE TABLE DienThoai (
        MaMay       NVARCHAR(20)    NOT NULL PRIMARY KEY,
        TenMay      NVARCHAR(100)   NOT NULL,
        HangSX      NVARCHAR(50)    NOT NULL,
        Gia         DECIMAL(18, 0)  NOT NULL CHECK (Gia >= 0),
        SoLuong     INT             NOT NULL DEFAULT 0 CHECK (SoLuong >= 0),
        MoTa        NVARCHAR(255)   NULL,
        NgayNhap    DATETIME        NOT NULL DEFAULT GETDATE()
    );
    PRINT N'Da tao bang DienThoai.';
END
GO

-- ============================================================
-- 3. Bang NhanVien
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'NhanVien') AND type = 'U')
BEGIN
    CREATE TABLE NhanVien (
        MaNV        NVARCHAR(20)    NOT NULL PRIMARY KEY,
        HoTen       NVARCHAR(100)   NOT NULL,
        ChucVu      NVARCHAR(50)    NOT NULL,
        SoDienThoai NVARCHAR(15)    NULL,
        DiaChi      NVARCHAR(255)   NULL,
        LuongCoBan  DECIMAL(18, 0)  NOT NULL CHECK (LuongCoBan >= 0),
        NgayVaoLam  DATE            NOT NULL DEFAULT GETDATE()
    );
    PRINT N'Da tao bang NhanVien.';
END
GO

-- ============================================================
-- 4. Bang Luong (Tinh luong hang thang)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'Luong') AND type = 'U')
BEGIN
    CREATE TABLE Luong (
        MaLuong         INT             NOT NULL PRIMARY KEY IDENTITY(1,1),
        MaNV            NVARCHAR(20)    NOT NULL FOREIGN KEY REFERENCES NhanVien(MaNV) ON DELETE CASCADE,
        Thang           INT             NOT NULL CHECK (Thang BETWEEN 1 AND 12),
        Nam             INT             NOT NULL CHECK (Nam >= 2000),
        -- Luu snapshot luong co ban tai thoi diem tinh luong (tranh thay doi hieu luc nguoc)
        LuongCoBan      DECIMAL(18, 0)  NOT NULL CHECK (LuongCoBan >= 0),
        Thuong          DECIMAL(18, 0)  NOT NULL DEFAULT 0,
        Phat            DECIMAL(18, 0)  NOT NULL DEFAULT 0,
        -- Computed column don gian: khong can subquery
        LuongThucNhan   AS (LuongCoBan + Thuong - Phat) PERSISTED,
        GhiChu          NVARCHAR(255)   NULL,
        NgayTao         DATETIME        NOT NULL DEFAULT GETDATE(),
        CONSTRAINT UQ_MaNV_Thang_Nam UNIQUE (MaNV, Thang, Nam)
    );
    PRINT N'Da tao bang Luong.';
END
GO

-- ============================================================
-- 5. Bang HoaDon (Quan ly ban hang)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'HoaDon') AND type = 'U')
BEGIN
    CREATE TABLE HoaDon (
        MaHD        NVARCHAR(20)    NOT NULL PRIMARY KEY,
        MaNV        NVARCHAR(20)    NOT NULL FOREIGN KEY REFERENCES NhanVien(MaNV),
        TenKhachHang NVARCHAR(100)  NULL DEFAULT N'Khach le',
        SDTKhach    NVARCHAR(15)    NULL,
        TongTien    DECIMAL(18, 0)  NOT NULL DEFAULT 0,
        NgayBan     DATETIME        NOT NULL DEFAULT GETDATE(),
        GhiChu      NVARCHAR(255)   NULL
    );
    PRINT N'Da tao bang HoaDon.';
END
GO

-- ============================================================
-- 6. Bang ChiTietHoaDon (San pham trong tung hoa don)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'ChiTietHoaDon') AND type = 'U')
BEGIN
    CREATE TABLE ChiTietHoaDon (
        MaCTHD      INT             NOT NULL PRIMARY KEY IDENTITY(1,1),
        MaHD        NVARCHAR(20)    NOT NULL FOREIGN KEY REFERENCES HoaDon(MaHD) ON DELETE CASCADE,
        MaMay       NVARCHAR(20)    NOT NULL FOREIGN KEY REFERENCES DienThoai(MaMay),
        SoLuongBan  INT             NOT NULL CHECK (SoLuongBan > 0),
        DonGia      DECIMAL(18, 0)  NOT NULL,
        ThanhTien   AS (SoLuongBan * DonGia) PERSISTED -- Computed column luu tru
    );
    PRINT N'Da tao bang ChiTietHoaDon.';
END
GO

-- ============================================================
-- 7. Stored Procedure: Ban hang & Cap nhat ton kho tu dong
-- ============================================================
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'sp_BanHang') AND type = 'P')
    DROP PROCEDURE sp_BanHang;
GO

CREATE PROCEDURE sp_BanHang
    @MaHD           NVARCHAR(20),
    @MaNV           NVARCHAR(20),
    @TenKhachHang   NVARCHAR(100),
    @SDTKhach       NVARCHAR(15),
    @GhiChu         NVARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        -- Tinh tong tien tu chi tiet hoa don
        DECLARE @TongTien DECIMAL(18, 0);
        SELECT @TongTien = SUM(SoLuongBan * DonGia)
        FROM ChiTietHoaDon
        WHERE MaHD = @MaHD;

        -- Cap nhat tong tien vao HoaDon
        UPDATE HoaDon
        SET TongTien = ISNULL(@TongTien, 0)
        WHERE MaHD = @MaHD;

        -- Cap nhat so luong ton kho
        UPDATE DT
        SET DT.SoLuong = DT.SoLuong - CT.SoLuongBan
        FROM DienThoai DT
        INNER JOIN ChiTietHoaDon CT ON DT.MaMay = CT.MaMay
        WHERE CT.MaHD = @MaHD;

        -- Kiem tra ton kho sau khi tru
        IF EXISTS (SELECT 1 FROM DienThoai WHERE SoLuong < 0)
        BEGIN
            RAISERROR (N'So luong ton kho khong du. Giao dich bi huy.', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END

        COMMIT TRANSACTION;
        PRINT N'Hoa don ' + @MaHD + N' da duoc xu ly thanh cong.';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

-- ============================================================
-- 8. Bang TaiKhoan (Quan ly Dang nhap / Dang ky)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'TaiKhoan') AND type = 'U')
BEGIN
    CREATE TABLE TaiKhoan (
        TenDangNhap NVARCHAR(50)    NOT NULL PRIMARY KEY,
        MatKhau     NVARCHAR(255)   NOT NULL, -- Luu ma hash (vi du SHA-256)
        VaiTro      NVARCHAR(20)    NOT NULL DEFAULT 'NhanVien', -- Admin hoac NhanVien
        NgayTao     DATETIME        NOT NULL DEFAULT GETDATE()
    );
    PRINT N'Da tao bang TaiKhoan.';
END
GO

-- ============================================================
-- 9. Cac bang mo rong cho POS dien thoai
-- ============================================================
-- ============================================================
-- 9A. Bang KhachHang (CRM / tich diem)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'KhachHang') AND type = 'U')
BEGIN
    CREATE TABLE KhachHang (
        MaKH        NVARCHAR(20)    NOT NULL PRIMARY KEY,
        HoTen       NVARCHAR(100)   NOT NULL,
        SoDienThoai NVARCHAR(15)    NOT NULL UNIQUE,
        Email       NVARCHAR(100)   NULL,
        DiaChi      NVARCHAR(255)   NULL,
        DiemTichLuy INT             NOT NULL DEFAULT 0 CHECK (DiemTichLuy >= 0),
        GhiChu      NVARCHAR(255)   NULL,
        NgayTao     DATETIME        NOT NULL DEFAULT GETDATE()
    );
    PRINT N'Da tao bang KhachHang.';
END
GO

-- ============================================================
-- 9B. Bang ImeiMay (Quan ly tung thiet bi theo IMEI/Serial)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'ImeiMay') AND type = 'U')
BEGIN
    CREATE TABLE ImeiMay (
        IMEI         NVARCHAR(30)    NOT NULL PRIMARY KEY,
        MaMay        NVARCHAR(20)    NOT NULL FOREIGN KEY REFERENCES DienThoai(MaMay),
        TinhTrang    NVARCHAR(20)    NOT NULL DEFAULT N'TrongKho'
            CHECK (TinhTrang IN (N'TrongKho', N'DaBan', N'BaoHanh', N'SuaChua', N'DoiTra')),
        MaHD         NVARCHAR(20)    NULL FOREIGN KEY REFERENCES HoaDon(MaHD),
        BaoHanhThang INT             NOT NULL DEFAULT 12 CHECK (BaoHanhThang >= 0),
        NgayNhap     DATETIME        NOT NULL DEFAULT GETDATE(),
        NgayBan      DATETIME        NULL,
        GhiChu       NVARCHAR(255)   NULL
    );
    PRINT N'Da tao bang ImeiMay.';
END
GO

-- ============================================================
-- 9C. Bang PhieuDichVu (Bao hanh / Sua chua)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'PhieuDichVu') AND type = 'U')
BEGIN
    CREATE TABLE PhieuDichVu (
        MaPhieu      NVARCHAR(20)    NOT NULL PRIMARY KEY,
        Loai         NVARCHAR(20)    NOT NULL CHECK (Loai IN (N'BaoHanh', N'SuaChua')),
        IMEI         NVARCHAR(30)    NULL FOREIGN KEY REFERENCES ImeiMay(IMEI),
        TenKhachHang NVARCHAR(100)   NOT NULL,
        SDTKhach     NVARCHAR(15)    NULL,
        NgayNhan     DATETIME        NOT NULL DEFAULT GETDATE(),
        HenTra       DATE            NULL,
        TrangThai    NVARCHAR(20)    NOT NULL DEFAULT N'DangXuLy'
            CHECK (TrangThai IN (N'DangXuLy', N'ChoLinhKien', N'HoanThanh', N'DaTra')),
        ChiPhi       DECIMAL(18, 0)  NOT NULL DEFAULT 0 CHECK (ChiPhi >= 0),
        GhiChu       NVARCHAR(255)   NULL
    );
    PRINT N'Da tao bang PhieuDichVu.';
END
GO

-- ============================================================
-- 9D. Bang NhaCungCap / PhieuNhap (nen tang nhap hang)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'NhaCungCap') AND type = 'U')
BEGIN
    CREATE TABLE NhaCungCap (
        MaNCC       NVARCHAR(20)    NOT NULL PRIMARY KEY,
        TenNCC      NVARCHAR(100)   NOT NULL,
        SoDienThoai NVARCHAR(15)    NULL,
        Email       NVARCHAR(100)   NULL,
        DiaChi      NVARCHAR(255)   NULL,
        GhiChu      NVARCHAR(255)   NULL
    );
    PRINT N'Da tao bang NhaCungCap.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'PhieuNhap') AND type = 'U')
BEGIN
    CREATE TABLE PhieuNhap (
        MaPN      NVARCHAR(20)    NOT NULL PRIMARY KEY,
        MaNCC     NVARCHAR(20)    NULL FOREIGN KEY REFERENCES NhaCungCap(MaNCC),
        MaNV      NVARCHAR(20)    NOT NULL FOREIGN KEY REFERENCES NhanVien(MaNV),
        NgayNhap  DATETIME        NOT NULL DEFAULT GETDATE(),
        TongTien  DECIMAL(18, 0)  NOT NULL DEFAULT 0,
        GhiChu    NVARCHAR(255)   NULL
    );
    PRINT N'Da tao bang PhieuNhap.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'ChiTietPhieuNhap') AND type = 'U')
BEGIN
    CREATE TABLE ChiTietPhieuNhap (
        MaCTPN   INT             NOT NULL PRIMARY KEY IDENTITY(1,1),
        MaPN     NVARCHAR(20)    NOT NULL FOREIGN KEY REFERENCES PhieuNhap(MaPN) ON DELETE CASCADE,
        MaMay    NVARCHAR(20)    NOT NULL FOREIGN KEY REFERENCES DienThoai(MaMay),
        SoLuong  INT             NOT NULL CHECK (SoLuong > 0),
        DonGia   DECIMAL(18, 0)  NOT NULL CHECK (DonGia >= 0),
        ThanhTien AS (SoLuong * DonGia) PERSISTED
    );
    PRINT N'Da tao bang ChiTietPhieuNhap.';
END
GO

-- ============================================================
-- 9E. Bang NhatKyHeThong (audit log / phan quyen mo rong)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'NhatKyHeThong') AND type = 'U')
BEGIN
    CREATE TABLE NhatKyHeThong (
        MaLog        INT             NOT NULL PRIMARY KEY IDENTITY(1,1),
        TenDangNhap  NVARCHAR(50)    NULL,
        HanhDong     NVARCHAR(100)   NOT NULL,
        DoiTuong     NVARCHAR(100)   NULL,
        NoiDung      NVARCHAR(500)   NULL,
        ThoiGian     DATETIME        NOT NULL DEFAULT GETDATE()
    );
    PRINT N'Da tao bang NhatKyHeThong.';
END
GO

-- ============================================================
-- 9F. Index va View phuc vu dashboard/bao cao
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_HoaDon_NgayBan' AND object_id = OBJECT_ID(N'HoaDon'))
    CREATE INDEX IX_HoaDon_NgayBan ON HoaDon(NgayBan);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_ChiTietHoaDon_MaMay' AND object_id = OBJECT_ID(N'ChiTietHoaDon'))
    CREATE INDEX IX_ChiTietHoaDon_MaMay ON ChiTietHoaDon(MaMay);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_PhieuNhap_NgayNhap' AND object_id = OBJECT_ID(N'PhieuNhap'))
    CREATE INDEX IX_PhieuNhap_NgayNhap ON PhieuNhap(NgayNhap);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_PhieuDichVu_NgayNhan' AND object_id = OBJECT_ID(N'PhieuDichVu'))
    CREATE INDEX IX_PhieuDichVu_NgayNhan ON PhieuDichVu(NgayNhan);
GO

CREATE OR ALTER VIEW vw_DoanhThuNgay AS
SELECT
    CAST(NgayBan AS DATE) AS Ngay,
    COUNT(*) AS SoHoaDon,
    SUM(TongTien) AS DoanhThu
FROM HoaDon
GROUP BY CAST(NgayBan AS DATE);
GO

CREATE OR ALTER VIEW vw_NhapHangNgay AS
SELECT
    CAST(NgayNhap AS DATE) AS Ngay,
    COUNT(*) AS SoPhieuNhap,
    SUM(TongTien) AS ChiNhap
FROM PhieuNhap
GROUP BY CAST(NgayNhap AS DATE);
GO

CREATE OR ALTER VIEW vw_TonKhoCanhBao AS
SELECT
    MaMay, TenMay, HangSX, Gia, SoLuong, Gia * SoLuong AS GiaTriTon
FROM DienThoai
WHERE SoLuong <= 5;
GO

-- ============================================================
-- 10. Du lieu mau (Sample Data)
-- ============================================================
-- Them nhan vien mau
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE MaNV = N'NV001')
BEGIN
    INSERT INTO NhanVien (MaNV, HoTen, ChucVu, SoDienThoai, DiaChi, LuongCoBan)
    VALUES
        (N'NV001', N'Nguyen Van An',    N'Quan ly',      N'0901234567', N'Ha Noi',      15000000),
        (N'NV002', N'Tran Thi Binh',   N'Nhan vien',    N'0912345678', N'Ho Chi Minh', 8000000),
        (N'NV003', N'Le Van Cuong',    N'Nhan vien',    N'0923456789', N'Da Nang',     8000000);
    PRINT N'Da them du lieu mau NhanVien.';
END
GO

-- Them nhan vien bo sung
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE MaNV = N'NV004')
BEGIN
    INSERT INTO NhanVien (MaNV, HoTen, ChucVu, SoDienThoai, DiaChi, LuongCoBan)
    VALUES
        (N'NV004', N'Pham Minh Khang',  N'Tu van ban hang', N'0934567890', N'Ho Chi Minh', 9000000),
        (N'NV005', N'Hoang Thu Ha',     N'Thu ngan',        N'0945678901', N'Ha Noi',      8500000),
        (N'NV006', N'Do Quang Huy',     N'Ky thuat vien',   N'0956789012', N'Da Nang',     9500000),
        (N'NV007', N'Nguyen Mai Linh',  N'Kho hang',        N'0967890123', N'Can Tho',     8200000);
    PRINT N'Da them 4 nhan vien bo sung.';
END
GO

-- Them dien thoai mau
IF NOT EXISTS (SELECT 1 FROM DienThoai WHERE MaMay = N'IP15PM')
BEGIN
    INSERT INTO DienThoai (MaMay, TenMay, HangSX, Gia, SoLuong, MoTa)
    VALUES
        (N'IP15PM',  N'iPhone 15 Pro Max',      N'Apple',   33990000,  10, N'256GB, Titan tu nhien'),
        (N'IP15',    N'iPhone 15',               N'Apple',   22990000,  15, N'128GB, Mau den'),
        (N'SS24U',   N'Samsung Galaxy S24 Ultra',N'Samsung', 31990000,  8,  N'512GB, Titan xam'),
        (N'SS24',    N'Samsung Galaxy S24',      N'Samsung', 19990000,  20, N'256GB, Cobalt Violet'),
        (N'OP12',    N'OPPO Find X7',            N'OPPO',    18990000,  12, N'256GB, Den'),
        (N'XIR13',   N'Xiaomi 13',               N'Xiaomi',  13990000,  25, N'256GB, Trang'),
        (N'VVS21',   N'Vivo V29',                N'Vivo',    9990000,   30, N'256GB, Tim');
    PRINT N'Da them du lieu mau DienThoai.';
END
GO

-- Them khach hang mau
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE MaKH = N'KH001')
BEGIN
    INSERT INTO KhachHang (MaKH, HoTen, SoDienThoai, Email, DiaChi, DiemTichLuy, GhiChu)
    VALUES
        (N'KH001', N'Nguyen Hoang Nam',  N'0971000001', N'nam.nguyen@example.com',   N'Quan 1, TP.HCM',       18, N'Khach than thiet'),
        (N'KH002', N'Tran My Duyen',     N'0971000002', N'duyen.tran@example.com',   N'Quan 3, TP.HCM',       9,  N'Thich iPhone'),
        (N'KH003', N'Le Thanh Phong',    N'0971000003', N'phong.le@example.com',     N'Thu Duc, TP.HCM',      12, N'Hay mua phu kien'),
        (N'KH004', N'Pham Gia Bao',      N'0971000004', N'bao.pham@example.com',     N'Ba Dinh, Ha Noi',      6,  N'Can tu van tra gop'),
        (N'KH005', N'Vo Ngoc Anh',       N'0971000005', N'anh.vo@example.com',       N'Hai Chau, Da Nang',    21, N'Khach VIP'),
        (N'KH006', N'Dang Minh Chau',    N'0971000006', N'chau.dang@example.com',    N'Ninh Kieu, Can Tho',   4,  N'Mua cho gia dinh'),
        (N'KH007', N'Bui Quoc Viet',     N'0971000007', N'viet.bui@example.com',     N'Thanh Xuan, Ha Noi',   15, N'Quan tam Samsung'),
        (N'KH008', N'Hoang Thuy Linh',   N'0971000008', N'linh.hoang@example.com',   N'Quan 7, TP.HCM',       7,  N'Can bao hanh nhanh'),
        (N'KH009', N'Do Anh Tuan',       N'0971000009', N'tuan.do@example.com',      N'Go Vap, TP.HCM',       10, N'Khach cong ty'),
        (N'KH010', N'Nguyen Bao Tran',   N'0971000010', N'tran.nguyen@example.com',  N'Hoan Kiem, Ha Noi',    3,  N'Khach moi'),
        (N'KH011', N'Trinh Minh Quan',   N'0971000011', N'quan.trinh@example.com',   N'Cam Le, Da Nang',      8,  N'Quan tam Xiaomi'),
        (N'KH012', N'Vu Hai Yen',        N'0971000012', N'yen.vu@example.com',       N'Binh Thanh, TP.HCM',   19, N'Khach than thiet'),
        (N'KH013', N'Mai Duc Long',      N'0971000013', N'long.mai@example.com',     N'Cau Giay, Ha Noi',     5,  N'Hay doi may moi'),
        (N'KH014', N'Lam Phuong Nhi',    N'0971000014', N'nhi.lam@example.com',      N'Tan Binh, TP.HCM',     11, N'Can xuat hoa don'),
        (N'KH015', N'Phan Thanh Dat',    N'0971000015', N'dat.phan@example.com',     N'Long Bien, Ha Noi',    2,  N'Khach online');
    PRINT N'Da them 15 khach hang mau.';
END
GO

-- Them nha cung cap mau
IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE MaNCC = N'NCC001')
BEGIN
    INSERT INTO NhaCungCap (MaNCC, TenNCC, SoDienThoai, Email, DiaChi, GhiChu)
    VALUES
        (N'NCC001', N'Apple Authorized Distributor',   N'02873009999', N'sales@apple-dist.vn',   N'TP.HCM', N'Nha phan phoi Apple'),
        (N'NCC002', N'Samsung Mobile Vietnam',         N'02473008888', N'b2b@samsung.vn',       N'Ha Noi', N'Nha cung cap Samsung'),
        (N'NCC003', N'FPT Trading Mobile',             N'02873006666', N'mobile@fpt.com.vn',    N'TP.HCM', N'Doi tac nhap hang');
    PRINT N'Da them du lieu mau NhaCungCap.';
END
GO

-- Tai khoan demo: admin / admin123
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE TenDangNhap = N'admin')
BEGIN
    INSERT INTO TaiKhoan (TenDangNhap, MatKhau, VaiTro)
    VALUES (N'admin', N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', N'Admin');
    PRINT N'Da them tai khoan demo admin/admin123.';
END
GO

PRINT N'===== Script hoan thanh! Database QuanLyCuaHangDienThoai san sang su dung. =====';
GO
