package com.linhnt.notifications.activity

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.linhnt.notifications.R
import com.linhnt.notifications.adapter.NotificationAdapter
import com.linhnt.notifications.config.ServerConfig
import com.linhnt.notifications.helper.HistorySQLiteDatabase
import com.linhnt.notifications.helper.PreferenceHelper
import com.linhnt.notifications.service.NotificationService
import com.linhnt.notifications.worker.QueueScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val adapter = NotificationAdapter()
    private val database by lazy { HistorySQLiteDatabase.getInstance(applicationContext) }
    private val preferences by lazy { PreferenceHelper(applicationContext) }
    private val handler = Handler(Looper.getMainLooper())

    private var rvMain: RecyclerView? = null
    private var layoutPlaceholder: View? = null

    private val refreshRunnable = object : Runnable {
        override fun run() {
            refreshScreen()
            handler.postDelayed(this, REFRESH_INTERVAL_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvMain = findViewById(R.id.rvMain)
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder)
        rvMain?.layoutManager = LinearLayoutManager(this)
        rvMain?.adapter = adapter

        requestStoragePermission()
        requestPostNotificationPermission()
        if (!hasNotificationListenerAccess()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        if (preferences.getDeviceId().isBlank()) {
            showDeviceIdDialog()
        }
        QueueScheduler.ensureHeartbeat(this)
        QueueScheduler.enqueueAllPending(this)
    }

    override fun onResume() {
        super.onResume()
        handler.removeCallbacks(refreshRunnable)
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        handler.removeCallbacks(refreshRunnable)
        super.onPause()
    }

    private fun refreshScreen() {
        val history = database.dbHelper.getTodayHistory()
        adapter.replaceAll(history)
        layoutPlaceholder?.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE

        val pending = database.dbHelper.getPendingCount()
        supportActionBar?.title = buildString {
            append(getString(R.string.app_name))
            if (history.isNotEmpty()) append(" (${history.size})")
        }
        supportActionBar?.subtitle = buildStatusText(pending)
    }

    private fun buildStatusText(pending: Int): String {
        val access = hasNotificationListenerAccess()
        val connected = preferences.isListenerConnected()
        val heartbeat = preferences.getLastHeartbeatAt()
        val heartbeatFresh = heartbeat > 0L && System.currentTimeMillis() - heartbeat < HEARTBEAT_STALE_MS
        val listenerText = when {
            !access -> "Chưa cấp quyền đọc thông báo"
            connected && heartbeatFresh -> "Listener: đang kết nối"
            connected -> "Listener: mất heartbeat"
            else -> "Listener: đang chờ kết nối"
        }
        val heartbeatText = if (heartbeat > 0L) {
            val formatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(heartbeat))
            "heartbeat $formatted"
        } else {
            "chưa có heartbeat"
        }
        return "$listenerText • chờ gửi: $pending • $heartbeatText"
    }

    private fun hasNotificationListenerAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.isNotificationListenerAccessGranted(
                ComponentName(this, NotificationService::class.java)
            )
        } else {
            NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        }
    }

    private fun requestPostNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_POST_NOTIFICATIONS
            )
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } else {
                // Đã có quyền, đảm bảo config được load lại (vì file init có thể fail trước đó)
                ServerConfig.init(this)
            }
        } else {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val needed = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (needed.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, needed.toTypedArray(), REQUEST_STORAGE)
            } else {
                ServerConfig.init(this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                ServerConfig.init(this)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, MENU_DEVICE_ID, 0, getString(R.string.add))
            ?.setIcon(R.drawable.ic_add)
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu?.add(0, MENU_SOURCE_NAMES, 0, getString(R.string.edit))
            ?.setIcon(R.drawable.ic_edit)
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            MENU_DEVICE_ID -> {
                showDeviceIdDialog()
                true
            }
            MENU_SOURCE_NAMES -> {
                showSourceNamesDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeviceIdDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setText(preferences.getDeviceId())
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.enter_device_id))
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                preferences.setDeviceId(input.text.toString())
                QueueScheduler.enqueueAllPending(this)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showSourceNamesDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setText(preferences.getAppNames())
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.enter_names))
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                preferences.setAppNames(input.text.toString())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 1001
        private const val REQUEST_STORAGE = 1002
        private const val MENU_DEVICE_ID = 0
        private const val MENU_SOURCE_NAMES = 1
        private const val REFRESH_INTERVAL_MS = 3_000L
        private const val HEARTBEAT_STALE_MS = 150_000L
    }
}
