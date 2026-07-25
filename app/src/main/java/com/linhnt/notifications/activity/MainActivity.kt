package com.linhnt.notifications.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eup.hanzii.databases.history_sqlite.HistorySQLiteDatabase
import com.google.gson.Gson
import com.linhnt.notifications.R
import com.linhnt.notifications.adapter.NotificationAdapter
import com.linhnt.notifications.helper.PreferenceHelper


class MainActivity : AppCompatActivity() {

    val adapter = NotificationAdapter()
    val historySQLiteDatabase = HistorySQLiteDatabase(this, HistorySQLiteDatabase.DB_NAME)
    var preferenceHelper: PreferenceHelper? = null

    private var rvMain: RecyclerView? = null
    private var layoutPlaceholder: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvMain = findViewById(R.id.rvMain)
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder)

        // setup recycler view
        setupListNotification()

        // listen notification service
        val ns = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!ns.isNotificationPolicyAccessGranted) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }
//        LocalBroadcastManager.getInstance(this).registerReceiver(onNotify, IntentFilter("Msg"))

        // show alert nhap device_id lan dau tien
        preferenceHelper = PreferenceHelper(this, "PREF_NOTIFY")
        val device_id = preferenceHelper?.getDeviceId() ?: ""
        if (device_id.isNullOrEmpty()) {
            showAlertInput()
        }

//        testFunc()
    }

    // update toolbar title
    private fun updateToolbar(count: Int = 0) {
        var text = getString(R.string.app_name)
        if (count > 0) {
            text += " ($count)"
        }
        supportActionBar?.title = text
    }

    // setup list notification
    private fun setupListNotification() {
        val layoutManager = LinearLayoutManager(this)
        rvMain?.layoutManager = layoutManager
        rvMain?.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        // History: load today-nofity
        val historyData = historySQLiteDatabase.dbHelper.getTodayHistory()
        adapter.list = historyData
        updateToolbar(adapter.list.size)

        if (adapter.list.isNotEmpty()) {
            layoutPlaceholder?.visibility = View.GONE
        } else {
            layoutPlaceholder?.visibility = View.VISIBLE
        }
    }

    // on notify: add item to list
//    private var onNotify: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            val json = intent.getStringExtra("json") ?: ""
//            val postData = Gson().fromJson(json, PostData::class.java)
//            val time = intent.getStringExtra("time") ?: getCurrentTime()
//            val status = intent.getBooleanExtra("status", false)
//
//            val item = NotifyItem()
//            item.convertFromData(data = postData, time, status)
//
//            if (adapter != null && !this@MainActivity.isFinishing) {
//                if (!adapter.add(item)) {
//                    updateToolbar(adapter.list.size)
//                    layoutPlaceholder.visibility = View.GONE
//
//                    // luu lai db
//                    historySQLiteDatabase.dbHelper.saveHistory(item)
//                }
//            }
//        }
//    }

    // menu options
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 0, 0, getString(R.string.add))?.setIcon(R.drawable.ic_add)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu?.add(0, 1, 0, getString(R.string.edit))?.setIcon(R.drawable.ic_edit)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    // item selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 0) {
            showAlertInput()
            return true
        } else if (item.itemId == 1) {
            showAlertEdit()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // show alert input device_id
    private fun showAlertInput() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.enter_device_id))

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(preferenceHelper!!.getDeviceId())
        builder.setView(input)
        builder.setPositiveButton(getString(android.R.string.ok), DialogInterface.OnClickListener { dialog, which ->
            val device_id = input.text.toString().trim()
            preferenceHelper!!.setDeviceId(device_id)
        })
        builder.setNegativeButton(getString(android.R.string.cancel), DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        builder.show()
    }

    private fun showAlertEdit() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.enter_names))

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(preferenceHelper!!.getAppNames())
        builder.setView(input)
        builder.setPositiveButton(getString(android.R.string.ok), DialogInterface.OnClickListener { dialog, which ->
            val names = input.text.toString().trim()
            preferenceHelper!!.setAppNames(names)
        })
        builder.setNegativeButton(getString(android.R.string.cancel), DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        builder.show()
    }

//    @SuppressLint("SimpleDateFormat")
//    private fun getCurrentTime(): String {
//        val sim = SimpleDateFormat("HH:mm dd-MM-yyyy")
//        return sim.format(Date())
//    }
}