package com.riku1227.viewrchat.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.riku1227.viewrchat.data.CacheTimeData

class CacheTimeDataDB(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "CacheTimeDataDB.db"

        const val TABLE_NAME = "cache_time_data_db"
        const val ID = "id"
        const val CACHE_TYPE = "cache_type"
        const val CACHE_TIME = "cache_time"

        const val SQL_CREATE_ENTRIES = "CREATE TABLE $TABLE_NAME ($ID TEXT PRIMARY KEY, $CACHE_TYPE TEXT ,$CACHE_TIME INTEGER);"
        const val  SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"

        private var singletonInstance: CacheTimeDataDB? = null

        @Synchronized
        fun getInstance(context: Context): CacheTimeDataDB {
            if(singletonInstance == null) {
                singletonInstance = CacheTimeDataDB(context)
            }
            return singletonInstance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    fun saveData(cacheTimeData: CacheTimeData) {
        val cursor = this.readableDatabase.query(TABLE_NAME, null, "$ID=?", arrayOf(cacheTimeData.id), null, null, null)
        if(cursor.moveToNext()) {
            cursor.close()
            updateData(this.writableDatabase, cacheTimeData)
        } else {
            cursor.close()
            insertData(this.writableDatabase, cacheTimeData)
        }
    }

    fun readData(id: String): CacheTimeData? {
        val cursor = this.readableDatabase.query(TABLE_NAME, null, "$ID=?", arrayOf(id), null, null, null)
        return if(cursor.moveToNext()) {
            val cacheID = cursor.getString((0))
            val cacheType = cursor.getString(1)
            val cacheTime = cursor.getLong(2)
            cursor.close()

            CacheTimeData(cacheID, cacheType, cacheTime)
        } else {
            cursor.close()

            null
        }
    }

    fun readAllData(): List<CacheTimeData> {
        val cursor = this.readableDatabase.query(TABLE_NAME, null, null, null, null, null, null)
        val result = arrayListOf<CacheTimeData>()
        var move = cursor.moveToFirst()
        while (move) {
            result.add(CacheTimeData(cursor.getString(0), cursor.getString(1), cursor.getLong(2)))
            move = cursor.moveToNext()
        }

        cursor.close()
        return result
    }

    fun deleteData(id: String) {
        this.writableDatabase.delete(TABLE_NAME, "$ID=?", arrayOf(id))
    }

    private fun insertData(db: SQLiteDatabase, cacheTimeData: CacheTimeData) {
        val content = ContentValues()
        content.put(ID, cacheTimeData.id)
        content.put(CACHE_TYPE, cacheTimeData.cacheType)
        content.put(CACHE_TIME, cacheTimeData.cacheTime)
        db.insert(TABLE_NAME, null, content)
    }

    private fun updateData(db: SQLiteDatabase, cacheTimeData: CacheTimeData) {
        val content = ContentValues()
        content.put(ID, cacheTimeData.id)
        content.put(CACHE_TYPE, cacheTimeData.cacheType)
        content.put(CACHE_TIME, cacheTimeData.cacheTime)
        db.update(TABLE_NAME, content, "$ID=?", arrayOf(cacheTimeData.id))
    }
}