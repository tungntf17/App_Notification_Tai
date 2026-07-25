# Chuyển đổi BCrypt sang Kotlin và dọn dẹp thư mục

Kế hoạch này thực hiện chuyển đổi file Java cuối cùng trong project (`BCrypt.java`) sang Kotlin để đồng bộ hóa mã nguồn và xóa bỏ các thư mục rỗng không còn cần thiết sau khi đã xóa các file test example.

## Proposed Changes

### [Component] Helper & Cleanup

#### [NEW] [BCrypt.kt](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/app/src/main/java/com/linhnt/notifications/helper/BCrypt.kt)
- Tạo file Kotlin mới thay thế cho `BCrypt.java`.
- Chuyển đổi logic mã hóa Blowfish và các phương thức static (`hashpw`, `gensalt`, `checkpw`) sang Kotlin.
- Sử dụng `companion object` để giữ nguyên cách gọi `BCrypt.hashpw(...)` trong các file khác.

#### [MODIFY] [UploadWorker.kt](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/app/src/main/java/com/linhnt/notifications/worker/UploadWorker.kt)
- Kiểm tra lại import và đảm bảo tương thích với file `BCrypt.kt` mới (thực tế Kotlin tự động nhận diện nếu cùng package và tên class).

#### [DELETE] [BCrypt.java](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/app/src/main/java/com/linhnt/notifications/helper/BCrypt.java)
- Xóa file Java cũ sau khi đã chuyển đổi thành công.

#### [DELETE] Thư mục Test rỗng
- Xóa thư mục `app/src/androidTest/java` và `app/src/test/java` nếu chúng không còn chứa file nào, để làm sạch cấu trúc thư mục.

## Verification Plan

### Automated Tests
- Chạy lệnh build project: `./gradlew assembleDebug` để đảm bảo không có lỗi compile sau khi chuyển đổi.
- Kiểm tra việc gọi các hàm `BCrypt` trong `UploadWorker` vẫn hoạt động bình thường.

### Manual Verification
- Xác nhận các thư mục `androidTest` và `test` đã được xóa sạch.
