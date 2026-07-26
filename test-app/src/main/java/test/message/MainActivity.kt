package test.message

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(resources.getIdentifier("activity_main", "layout", packageName))

        createNotificationChannel()

        findViewById<Button>(resources.getIdentifier("btnVPBank", "id", packageName)).setOnClickListener {
            sendNotification("VPBank NEO + 20,000 ₫", "Tài khoản: 77****94\nSố dư: 860,000 ₫\nNHAN TU 0011004199711 TRACE 519345 ND MBVCB.15295440178.6207BFTVGL2279QQ. taivm.CT tu 0011004199711 VU MANH TAI toi 7721091994 VU MANH TAI tai VPBANK")
        }

        findViewById<Button>(resources.getIdentifier("btnACB", "id", packageName)).setOnClickListener {
            sendNotification("ACB ONE Thong bao thay doi so du tai khoan", "ACB: TK 26790181(VND) + 20,000 luc 18:09 26/07/2026. So du 1,962,000. GD: MBVCB.15295352595.6207BFTVGL227283.taivm.CT tu 0011004199711 VU MANH TAI toi 2679018")
        }

        findViewById<Button>(resources.getIdentifier("btnMBBank", "id", packageName)).setOnClickListener {
            sendNotification("MB Bank Thông báo biến động số dư", "TK 03xxx682|GD: +20,000VND 26/07/26 18:10 |SD: 1,034,001VND|TU: VU MANH TAI - 0011004199711|ND: MBVCB.15295363489.469132.taivm.CT tu 0011004199711 VU MANH TAI toi 0363020682 Vu Manh Tai tai MB- Ma GD ACSP/ mw469132")
        }

        findViewById<Button>(resources.getIdentifier("btnTPBank", "id", packageName)).setOnClickListener {
            sendNotification("TPBank Biz", "(TPBank): 26/07/26;18:08 TK: xxxx2725618 PS:+20.000VND SD: 2.465.028VND SD KHA DUNG: 2.465.028VND ND: MBVCB.15295323519.6207BFTVGL226D24.taivm.CT tu 0011004199711 VU MANH TAI toi 29912725618 HKD VU MANH TAI 1994 tai TPBANK SO GD: 045ITC1262081778")
        }

        findViewById<Button>(resources.getIdentifier("btnMomo", "id", packageName)).setOnClickListener {
            sendNotification("Momo", "Vừa được cộng 100.000 VNĐ vào tài khoản accfifa 123456")
        }

        findViewById<Button>(resources.getIdentifier("btnVCB", "id", packageName)).setOnClickListener {
            sendNotification("VCB", "VCB: +500.000 VND; TK: 9dmanga_8888; ND: Qua tang")
        }

        findViewById<Button>(resources.getIdentifier("btnVCBDigiBiz", "id", packageName)).setOnClickListener {
            sendNotification("VCB DigiBiz Thông tin VCB Digibiz", "Số dư TK VCB 3363020682 +20,000 VND lúc 26-07-2026 18:13:05. Số dư 138,916,198 VND. Ref MBVCB.15295411586.taivm.CT tu 0011004199711 VU MANH TAI toi 3363020682 HKD VU MANH TAI 1994")
        }

        findViewById<Button>(resources.getIdentifier("btnCustom", "id", packageName)).setOnClickListener {
            sendNotification("TestApp", "Thông báo test: ghi có 1.000.000 VND cho 9dfgc - user123")
        }

        checkPermission()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun sendNotification(title: String, content: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Test Channel"
            val descriptionText = "Channel for test notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "test_channel"
    }
}
