# Thêm hỗ trợ package testmessage để kiểm thử

Kế hoạch này sẽ thêm package `testmessage` vào danh sách các ứng dụng được hỗ trợ để bạn có thể gửi thông báo từ một ứng dụng test và kiểm tra khả năng đọc/parse của hệ thống.

## Proposed Changes

### [Component] Configuration

#### [MODIFY] [SupportedBankApps.kt](file:///C:/Users/AD/Documents/Git/App_Notification_Tai/app/src/main/java/com/linhnt/notifications/config/SupportedBankApps.kt)
- Thêm hằng số `TEST = "testmessage"`.
- Thêm `TEST to "TestApp"` vào bản đồ `apps` để hệ thống nhận diện package này là hợp lệ.

## Verification Plan

### Manual Verification
1. Cài đặt một ứng dụng có package name là `testmessage`.
2. Gửi một thông báo từ ứng dụng đó với nội dung mẫu như:
   `"Vừa được cộng 100.000 VNĐ vào tài khoản accfifa 123456"`
3. Kiểm tra xem ứng dụng **Notifications** có nhận được, parse đúng các trường (Amount: 100000, Account: 123456, Source: accfifa) và hiển thị lên danh sách hay không.
4. Kiểm tra xem dữ liệu có được đẩy lên server (nếu đã cấu hình server URL) hay không.

> [!NOTE]
> Do trình parse sử dụng danh sách tên nguồn mặc định là `accfifa|9dmanga|accffia|9dfgc|9dtt`, hãy đảm bảo nội dung tin nhắn test có chứa một trong các tên này để trình parse tìm được "Nguồn" và "Số tài khoản".
