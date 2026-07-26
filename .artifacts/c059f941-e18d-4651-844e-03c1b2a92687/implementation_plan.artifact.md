# Kế hoạch sửa lỗi build và hỗ trợ Genimotion

Kế hoạch này tập trung vào việc sửa đổi cấu hình Gradle để tích hợp module `test-app` đúng cách và đảm bảo ứng dụng có thể chạy ổn định trên trình giả lập Genimotion (kiến trúc x86).

## Proposed Changes

### [Component] Project Structure & Integration

#### [MODIFY] [settings.gradle](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/settings.gradle)
- Thêm lại `include ':test-app'` để root project nhận diện module này.

#### [DELETE] [test-app/settings.gradle](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/test-app/settings.gradle)
- Xóa file này để `test-app` không còn là project độc lập mà trở thành một module của project chính.

#### [DELETE] [test-app/gradlew](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/test-app/gradlew), [test-app/gradlew.bat](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/test-app/gradlew.bat), [test-app/gradle.properties](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/test-app/gradle.properties)
- Dọn dẹp các file thừa của project con để tránh xung đột cấu hình.

### [Component] App Module Configuration

#### [MODIFY] [app/build.gradle](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/app/build.gradle)
- Dọn dẹp khối `plugins`, loại bỏ các dòng `apply plugin` dư thừa để tuân thủ style Gradle mới.
- Thêm `ndk { abiFilters "armeabi-v7a", "arm64-v8a", "x86", "x86_64" }` vào `defaultConfig` để hỗ trợ chạy trên Genimotion (x86).

### [Component] Test App Module Configuration

#### [MODIFY] [test-app/build.gradle](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/test-app/build.gradle)
- Loại bỏ thông tin phiên bản trong khối `plugins` (sẽ kế thừa từ root project).
- Thêm `ndk { abiFilters "armeabi-v7a", "arm64-v8a", "x86", "x86_64" }` vào `defaultConfig` tương tự như app chính.

## Verification Plan

### Automated Tests
- Chạy lệnh `./gradlew clean assembleDebug` để đảm bảo toàn bộ project build thành công.
- Kiểm tra danh sách các task gradle để xác nhận `:app` và `:test-app` đều có mặt.

### Manual Verification
- Người dùng thử Run app trên Genimotion.
- Kiểm tra xem cả hai ứng dụng có hiển thị trong danh sách run của Android Studio hay không.
