# Walkthrough - Chuyển file config sang thư mục Download

Tôi đã chuyển đổi cách lưu trữ file cấu hình URL server sang thư mục **Download** để bạn có thể dễ dàng quản lý mà không cần dùng lệnh ADB phức tạp.

## Các thay đổi chính

### 1. Quyền truy cập bộ nhớ (Storage Permissions)
- **Manifest**: Đã thêm các quyền `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` và đặc biệt là `MANAGE_EXTERNAL_STORAGE`.
- **MainActivity**: Thêm logic yêu cầu quyền "Truy cập tất cả các tệp" (All Files Access) khi ứng dụng khởi chạy lần đầu trên Android 11+.

### 2. Thư mục cấu hình mới
- **Vị trí**: `Bộ nhớ máy > Download > NotificationsConfig > server_url.txt`
- **Tự động sinh**: Khi bạn cài app và cấp quyền thành công, ứng dụng sẽ tự tạo thư mục `NotificationsConfig` và file `server_url.txt` bên trong thư mục Download.

### 3. Cập nhật mã nguồn
- [ServerConfig.kt](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/app/src/main/java/com/linhnt/notifications/config/ServerConfig.kt): Chuyển logic đọc/ghi từ bộ nhớ trong sang thư mục Download công cộng.
- [MainActivity.kt](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/app/src/main/java/com/linhnt/notifications/activity/MainActivity.kt): Thêm xử lý quyền runtime.

## Hướng dẫn sử dụng

1. **Cài đặt APK**: Cài đặt ứng dụng như bình thường.
2. **Cấp quyền**: Khi mở app, nó sẽ dẫn bạn đến màn hình cài đặt "Truy cập tất cả các tệp". Hãy tìm app **Notifications** và bật nó lên.
3. **Chỉnh sửa URL**:
   - Dùng bất kỳ ứng dụng quản lý file nào (như Files, ZArchiver...).
   - Vào thư mục **Download** -> **NotificationsConfig**.
   - Mở file **server_url.txt** và sửa URL theo ý muốn.
4. **Áp dụng**: Khởi động lại app hoặc đợi app load lại cấu hình.

> [!CAUTION]
> Đừng xóa thư mục `NotificationsConfig` trong khi app đang chạy, vì app có thể quay lại dùng URL mặc định nếu không tìm thấy file.

## Kết quả xác minh
- **Build**: Hoàn tất thành công.
- **Quyền**: Đã tích hợp đầy đủ cho các đời Android mới nhất.

render_diffs(file:///C:/Users/AD/Documents/Git/App_Notification_Tai/app/src/main/java/com/linhnt/notifications/config/ServerConfig.kt)
render_diffs(file:///C:/Users/AD/Documents/Git/App_Notification_Tai/app/src/main/java/com/linhnt/notifications/activity/MainActivity.kt)
render_diffs(file:///C:/Users/AD/Documents/Git/App_Notification_Tai/app/src/main/AndroidManifest.xml)
