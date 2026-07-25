package com.eup.hanzii.databases.history_sqlite

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.linhnt.notifications.helper.GetHistoryHelper

/**
 * Created by DuyNM on 2/11/2019
 */
class HistorySQLiteDatabase(context: Context, databaseName: String) : SQLiteOpenHelper(context, databaseName, null, DATABASE_VERSION) {

    val dbHelper = GetHistoryHelper(this)

    companion object {
        const val DATABASE_VERSION = 1
        const val DB_NAME = "history.db"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var database: HistorySQLiteDatabase? = null

        fun getInstance(context: Context): HistorySQLiteDatabase {
            if (database == null) {
                synchronized(this) { //thread safe singleton
                    if (database == null) database = HistorySQLiteDatabase(context.applicationContext, DB_NAME)
                }
            }
            return database!!
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(" CREATE TABLE " + dbHelper.TABLE_HISTORY + " (" +
                "id" + " INTEGER PRIMARY KEY, " +
                "app" + " TEXT NOT NULL, " +
                "source" + " TEXT NOT NULL, " +
                "amount" + " TEXT NOT NULL, " +
                "account" + " TEXT NOT NULL, " +
                "time" + " TEXT NOT NULL, " +
                "status" + " INTEGER NOT NULL);"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
}