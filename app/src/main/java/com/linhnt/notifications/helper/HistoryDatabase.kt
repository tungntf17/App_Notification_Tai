package com.linhnt.notifications.helper

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class HistorySQLiteDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DATABASE_VERSION) {

    val dbHelper: GetHistoryHelper by lazy { GetHistoryHelper(this) }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_HISTORY (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_id TEXT NOT NULL,
                notification_key TEXT NOT NULL DEFAULT '',
                package_name TEXT NOT NULL DEFAULT '',
                device_id TEXT NOT NULL DEFAULT '',
                app TEXT NOT NULL,
                source TEXT NOT NULL,
                amount TEXT NOT NULL,
                account TEXT NOT NULL,
                time TEXT NOT NULL,
                post_time INTEGER NOT NULL DEFAULT 0,
                content TEXT NOT NULL DEFAULT '',
                status INTEGER NOT NULL DEFAULT 0,
                delivery_state TEXT NOT NULL DEFAULT 'PENDING',
                attempt_count INTEGER NOT NULL DEFAULT 0,
                last_error TEXT NOT NULL DEFAULT '',
                created_at INTEGER NOT NULL DEFAULT 0,
                updated_at INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_history_event_id ON $TABLE_HISTORY(event_id)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_history_delivery_state ON $TABLE_HISTORY(delivery_state)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_history_post_time ON $TABLE_HISTORY(post_time)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            migrateFromVersion1(db)
        }
    }

    private fun migrateFromVersion1(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            addColumn(db, "event_id TEXT NOT NULL DEFAULT ''")
            addColumn(db, "notification_key TEXT NOT NULL DEFAULT ''")
            addColumn(db, "package_name TEXT NOT NULL DEFAULT ''")
            addColumn(db, "device_id TEXT NOT NULL DEFAULT ''")
            addColumn(db, "post_time INTEGER NOT NULL DEFAULT 0")
            addColumn(db, "content TEXT NOT NULL DEFAULT ''")
            addColumn(db, "delivery_state TEXT NOT NULL DEFAULT 'PENDING'")
            addColumn(db, "attempt_count INTEGER NOT NULL DEFAULT 0")
            addColumn(db, "last_error TEXT NOT NULL DEFAULT ''")
            addColumn(db, "created_at INTEGER NOT NULL DEFAULT 0")
            addColumn(db, "updated_at INTEGER NOT NULL DEFAULT 0")

            db.execSQL(
                "UPDATE $TABLE_HISTORY SET event_id = 'legacy-' || id WHERE event_id = ''"
            )
            db.execSQL(
                """
                UPDATE $TABLE_HISTORY
                SET delivery_state = CASE WHEN status = 1 THEN 'SENT' ELSE 'FAILED' END
                WHERE delivery_state = 'PENDING'
                """.trimIndent()
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_history_event_id ON $TABLE_HISTORY(event_id)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_history_delivery_state ON $TABLE_HISTORY(delivery_state)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_history_post_time ON $TABLE_HISTORY(post_time)"
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun addColumn(db: SQLiteDatabase, definition: String) {
        db.execSQL("ALTER TABLE $TABLE_HISTORY ADD COLUMN $definition")
    }

    companion object {
        const val DATABASE_VERSION = 2
        const val DB_NAME = "history.db"
        const val TABLE_HISTORY = "history_data"

        @Volatile
        private var instance: HistorySQLiteDatabase? = null

        fun getInstance(context: Context): HistorySQLiteDatabase {
            return instance ?: synchronized(this) {
                instance ?: HistorySQLiteDatabase(context.applicationContext).also { instance = it }
            }
        }
    }
}
