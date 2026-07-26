# Walkthrough - Tạo Module TestApp (test.message)

Tôi đã tạo thành công module **TestApp** giúp bạn giả lập các thông báo ngân hàng để kiểm tra ứng dụng chính.

> [!NOTE]
> Do Android yêu cầu package name phải có ít nhất một dấu chấm (ví dụ: `test.message`), tôi đã đổi package name từ `testmessage` thành `test.message` và cập nhật tương ứng trong ứng dụng chính.

## Các thành phần đã tạo

### 1. Cấu trúc Module mới
- **Module name**: `:test-app`
- **Package name**: `test.message`
- **Build file**: [test-app/build.gradle](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/test-app/build.gradle)

### 2. Giao diện TestApp
- **Layout**: [activity_main.xml](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/test-app/src/main/res/layout/activity_main.xml)
- Cung cấp các nút bấm:
  - **Test Momo**: Gửi thông báo cộng tiền Momo.
  - **Test VCB**: Gửi thông báo cộng tiền Vietcombank.
  - **Test ACB**: Gửi thông báo cộng tiền ACB.
  - **Test Custom**: Gửi thông báo tùy chỉnh.

### 3. Logic gửi thông báo
- **File**: [MainActivity.kt](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/test-app/src/main/java/test/message/MainActivity.kt)
- Tự động xin quyền `POST_NOTIFICATIONS` trên Android 13+.
- Gửi thông báo với nội dung "Good Case" mà ứng dụng chính có thể parse được ngay lập tức.

## Hướng dẫn sử dụng

1. **Build và cài đặt**:
   - Chọn module `test-app` trong thanh công cụ Android Studio và nhấn **Run**.
   - Hoặc dùng lệnh: `./gradlew :test-app:installDebug`
2. **Cấp quyền**:
   - Mở **TestApp** và đồng ý cho phép gửi thông báo.
3. **Kiểm thử**:
   - Đảm bảo ứng dụng **Notifications** (chính) đã được cài đặt và đã cấp quyền **Notification Listener**.
   - Trong **TestApp**, nhấn các nút bấm để gửi thông báo.
   - Quay lại app **Notifications** để xem kết quả bắt và parse thông báo.

## Kết quả xác minh
- **Gradle Sync**: Thành công.
- **Build `:test-app`**: Thành công (`assembleDebug`).
- **Tương thích**: Đã cập nhật `SupportedBankApps.kt` để nhận diện package `test.message`.

render_diffs(file:///C:/Users/AD/Documents/Git/App_Notification_Tai/test-app/src/main/java/test/message/MainActivity.kt)
render_diffs(file:///C:/Users/AD/Documents/Git/App_Notification_Tai/app/src/main/java/com/linhnt/notifications/config/SupportedBankApps.kt)
render_diffs(file:///C:/Users/AD/Documents/Git/App_Notification_Tai/settings.gradle)
