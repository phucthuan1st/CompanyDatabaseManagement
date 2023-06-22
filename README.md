# Security in Infomation System
(update later)

## Introduction:
This project is built in JavaSwing with 2 subsystem: DBA and Normal User

## Contributors:
- Nguyen Phuc Thuan
- Nguyen Thi Hong Nhung
- Nguyen Quang Nghi Sinh
- Nguyen Buu Thach

# About the app:

## Architecture:
Using MVC architecture

## Model

### View
Java Swing GUI application

### Controller
- LoginController
- DBAdminController
- NormalUserController

### Model
- DBManager
- CryptographyUtilities

# About the Database policies:

## CS#1:
CS#1: Những người dùng có thuộc tính VAITRO là “Nhân viên” cho biết đó là một nhân viên thông thường, không kiêm nhiệm công việc nào khác. 
Những người dùng có VAITRO là “Nhân viên” có quyền được mô tả như sau:
- Có quyền xem tất cả các thuộc tính trên quan hệ NHANVIEN và PHANCONG liên quan đến chính nhân viên đó.
- Có thể sửa trên các thuộc tính NGAYSINH, DIACHI, SODT liên quan đến chính nhân viên đó.
- Có thể xem dữ liệu của toàn bộ quan hệ PHONGBAN và DEAN.

## CS#2:
CS#2: Những người dùng có VAITRO là “QL trực tiếp” nếu họ phụ trách quản lý trực tiếp nhân viên khác. Nhân viên Q là quản lý trực tiếp nhân viên N, có quyền được mô tả như sau:
- Q có quyền như là một nhân viên thông thường (vai trò “Nhân viên”). Ngoài ra, với các dòng dữ liệu trong quan hệ NHANVIEN liên quan đến các nhân viên N mà Q quản lý trực tiếp thì Q được xem tất cả các thuộc tính, trừ thuộc tính LUONG và PHUCAP.
- Có thể xem các dòng trong quan hệ PHANCONG liên quan đến chính Q và các nhân viên N được quản lý trực tiếp bởi Q.

## CS#3:
CS#3: Những người dùng có VAITRO là “Trưởng phòng” cho biết đó là một nhân viên kiêm nhiệm thêm vai trò trưởng phòng. Một người dùng T có VAITRO là “Trưởng phòng” có quyền được mô tả như sau:
- T có quyền như là một nhân viên thông thường (vai trò “Nhân viên”). Ngoài ra, với các dòng trong quan hệ NHANVIEN liên quan đến các nhân viên thuộc phòng ban mà T làm trưởng phòng thì T có quyền xem tất cả các thuộc tính, trừ thuộc tính LUONG và PHUCAP.
- Có thể thêm, xóa, cập nhật trên quan hệ PHANCONG liên quan đến các nhân viên thuộc phòng ban mà T làm trưởng phòng.

## CS#4:
CS#4: Những người dùng có VAITRO là “Tài chính” cho biết đó là một nhân viên phụ trách công tác tài chính tiền lương của công ty. Một người dùng có vai trò là “Tài chính” có quyền được mô tả như sau:
- Có quyền như là một nhân viên thông thường (vai trò “Nhân viên”).
- Xem trên toàn bộ quan hệ NHANVIEN và PHANCONG, có thể sửa trên thuộc tính LUONG và PHUCAP (thừa hành ban giám đốc).

## CS#5:
CS#5: Những người dùng có VAITRO là “Nhân sự” cho biết đó là nhân viên phụ trách công tác nhân sự trong công ty. Một người dùng có VAITRO là “Nhân sự” có quyền được mô tả như sau:
- Có quyền như là một nhân viên thông thường (vai trò “Nhân viên”).
- Được quyền thêm, cập nhật trên quan hệ PHONGBAN.
- Thêm, cập nhật dữ liệu trong quan hệ NHANVIEN với giá trị các trường LUONG, PHUCAP là mang giá trị mặc định là NULL, không được xem LUONG, PHUCAP của người khác và không được cập nhật trên các trường LUONG, PHUCAP.

## CS#6:
CS#6: Những người dùng có VAITRO là “Trưởng đề án” cho biết đó là nhân viên là trưởng các
đề án. Một người dùng là “Trưởng đề án” có quyền được mô tả như sau:
- Có quyền như là một nhân viên thông thường (vai trò “Nhân viên”).
- Được quyền thêm, xóa, cập nhật trên quan hệ ĐEAN.

# Build instruction:
## Build database

Using SQL Developer or any SQL Tool that could connect to Oracle Database
- Connect to database as SYS as SYSDBA
- Run all script in QLNhanVienDeAn_Open_Connect_To_PDB.sql
- Run all script in QLNhanVienDeAn_PhanHe1.sql
- Run all script in QLNhanVienDeAn_PhanHe2_TableStructure.sql
- Run all script in QLNhanVienDeAn_PhanHe2_TablesRecords.sql (Optional)
- Run all script in QLNhanVienDeAn_PhanHe2_HelperProceduresAndFunctions.sql
- Run all script in QLNhanVienDeAn_PhanHe2_Policies.sql

Now you can connect to database using the application or anything

Notice that the dba user for our database (for test purpose) is COMPANY_PUBLIC with pasword [astrongpassword]

## Build application

## Wrap jar to exe

