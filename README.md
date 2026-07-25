# Android Notifications

Ứng dụng đọc notification giao dịch từ các package được cho phép, parse số tiền/source/account, lưu vào hàng đợi SQLite rồi gửi server bằng WorkManager.

## App được hỗ trợ

- MoMo: `com.mservice.momotransfer`
- VCB Digibank: `com.VCB`
- TPBank Mobile: `com.tpb.mb.gprsandroid`
- MBBank: `com.mbmobile`
- VPBank NEO: `com.vnpay.vpbankonline`
- ACB ONE: `mobile.acb.com.vn`

Danh sách nằm tại:

```text
app/src/main/java/com/linhnt/notifications/config/SupportedBankApps.kt
```

## Đổi server POST

Chỉ sửa:

```text
app/src/main/java/com/linhnt/notifications/config/ServerConfig.kt
```

```kotlin
const val POST_URL = "http://103.139.202.23:3006/api/forwarder"
```

## Luồng xử lý

```text
NotificationListenerService
  -> WorkManager CaptureNotificationWorker
  -> parse an toàn
  -> SQLite PENDING
  -> UploadWorker (có mạng)
  -> retry/backoff
  -> SENT hoặc FAILED
```

Parse thất bại bị bỏ qua và không gửi server.

## Chống gửi trùng

App gửi `event_id` trong JSON và header `Idempotency-Key`. Server bắt buộc phải UNIQUE theo `event_id`; xem `SERVER_IDEMPOTENCY_REQUIRED.md`.

## Build

- JDK tương thích Android Gradle Plugin 7.4.2
- Android SDK 33

```bash
./gradlew clean assembleDebug
```

Các thư mục test mẫu đã được xóa theo yêu cầu.
