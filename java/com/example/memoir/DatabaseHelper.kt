package com.example.memoir

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Memoir.db"
        private const val DATABASE_VERSION = 3

        const val TABLE_USERS = "users"
        const val TABLE_MEMORIES = "memories"
        const val TABLE_JOURNALS = "journals"

        const val KEY_ID = "id"
        const val KEY_USER_ID = "user_id"
        const val KEY_DATE = "date"

        const val KEY_USERNAME = "username"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_PROFILE_PIC = "profile_pic"

        const val KEY_TITLE = "title"
        const val KEY_CAPTION = "caption"
        const val KEY_IMAGE_URI = "image_uri"
        const val KEY_CATEGORY = "category"
        const val KEY_CONTENT = "content"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = ("CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USERNAME + " TEXT,"
                + KEY_EMAIL + " TEXT,"
                + KEY_PASSWORD + " TEXT,"
                + KEY_PROFILE_PIC + " TEXT" + ")")

        val createMemoriesTable = ("CREATE TABLE " + TABLE_MEMORIES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_ID + " INTEGER,"
                + KEY_TITLE + " TEXT,"
                + KEY_CAPTION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_IMAGE_URI + " TEXT,"
                + KEY_CATEGORY + " TEXT" + ")")

        val createJournalsTable = ("CREATE TABLE " + TABLE_JOURNALS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_ID + " INTEGER,"
                + KEY_CONTENT + " TEXT,"
                + KEY_DATE + " TEXT" + ")")

        db?.execSQL(createUsersTable)
        db?.execSQL(createMemoriesTable)
        db?.execSQL(createJournalsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MEMORIES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_JOURNALS")
        onCreate(db)
    }

    // --- USER FUNCTIONS ---
    fun addUser(username: String, email: String, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_USERNAME, username)
        values.put(KEY_EMAIL, email)
        values.put(KEY_PASSWORD, password)
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    fun checkUser(input: String, password: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $KEY_ID FROM $TABLE_USERS WHERE ($KEY_USERNAME = ? OR $KEY_EMAIL = ?) AND $KEY_PASSWORD = ?",
            arrayOf(input, input, password)
        )
        var userId = -1
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return userId
    }

    fun checkUserExists(username: String, email: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $KEY_USERNAME = ? OR $KEY_EMAIL = ?", arrayOf(username, email))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUserDetails(userId: Int): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $KEY_ID = ?", arrayOf(userId.toString()))
    }

    fun updateProfile(userId: Int, newName: String, imageUri: String?): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_USERNAME, newName)
        if (imageUri != null) {
            values.put(KEY_PROFILE_PIC, imageUri)
        }
        val result = db.update(TABLE_USERS, values, "$KEY_ID = ?", arrayOf(userId.toString()))
        db.close()
        return result > 0
    }

    // --- MEMORY FUNCTIONS ---
    fun addMemory(userId: Int, title: String, caption: String, date: String, imageUri: String, category: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_USER_ID, userId)
        values.put(KEY_TITLE, title)
        values.put(KEY_CAPTION, caption)
        values.put(KEY_DATE, date)
        values.put(KEY_IMAGE_URI, imageUri)
        values.put(KEY_CATEGORY, category)
        val result = db.insert(TABLE_MEMORIES, null, values)
        db.close()
        return result != -1L
    }

    fun getAllMemories(userId: Int): List<Memory> {
        val memoryList = ArrayList<Memory>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MEMORIES WHERE $KEY_USER_ID = ? ORDER BY $KEY_ID DESC", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE))
                val caption = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CAPTION))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
                val imageUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_URI))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY))
                val memory = Memory(id, title, caption, date, imageUri, category)
                memoryList.add(memory)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return memoryList
    }

    fun getMemory(id: Int): Memory? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MEMORIES WHERE $KEY_ID = ?", arrayOf(id.toString()))
        var memory: Memory? = null
        if (cursor.moveToFirst()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE))
            val caption = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CAPTION))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
            val imageUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_URI))
            val category = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY))
            memory = Memory(id, title, caption, date, imageUri, category)
        }
        cursor.close()
        db.close()
        return memory
    }

    fun updateMemory(id: Int, title: String, caption: String, date: String, imageUri: String, category: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_TITLE, title)
        values.put(KEY_CAPTION, caption)
        values.put(KEY_DATE, date)
        values.put(KEY_IMAGE_URI, imageUri)
        values.put(KEY_CATEGORY, category)
        val result = db.update(TABLE_MEMORIES, values, "$KEY_ID = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun deleteMemory(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_MEMORIES, "$KEY_ID = ?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    // --- TIMELINE ---
    fun getMemoriesByDate(userId: Int, date: String): List<Memory> {
        val memoryList = ArrayList<Memory>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_MEMORIES WHERE $KEY_USER_ID = ? AND $KEY_DATE = ?", arrayOf(userId.toString(), date))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE))
                val caption = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CAPTION))
                val dbDate = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
                val imageUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_URI))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY))
                val memory = Memory(id, title, caption, dbDate, imageUri, category)
                memoryList.add(memory)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return memoryList
    }

    // --- JOURNAL FUNCTIONS ---
    fun addJournal(userId: Int, content: String, date: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_USER_ID, userId)
        values.put(KEY_CONTENT, content)
        values.put(KEY_DATE, date)
        val result = db.insert(TABLE_JOURNALS, null, values)
        db.close()
        return result != -1L
    }

    fun getAllJournals(userId: Int): List<Journal> {
        val journalList = ArrayList<Journal>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_JOURNALS WHERE $KEY_USER_ID = ? ORDER BY $KEY_ID DESC", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
                val content = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONTENT))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
                val journal = Journal(id, content, date)
                journalList.add(journal)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return journalList
    }

    // --- NEW: MIXED FEED ---
    fun getMixedFeed(userId: Int): List<FeedItem> {
        val mixedList = ArrayList<FeedItem>()
        // 1. Get all Memories
        mixedList.addAll(getAllMemories(userId))
        // 2. Get all Journals
        mixedList.addAll(getAllJournals(userId))
        // 3. Sort by Date Descending
        return mixedList.sortedByDescending { it.date }
    }
}