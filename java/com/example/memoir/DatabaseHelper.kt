package com.example.memoir

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Memoir.db"
        private const val DATABASE_VERSION = 4

        const val TABLE_USERS = "users"
        const val TABLE_MEMORIES = "memories"
        const val TABLE_JOURNALS = "journals"
        const val TABLE_ALBUMS = "albums"

        const val KEY_ID = "id"
        const val KEY_USER_ID = "user_id"
        const val KEY_DATE = "date"

        const val KEY_ALBUM_NAME = "album_name"

        const val KEY_TITLE = "title"
        const val KEY_CAPTION = "caption"
        const val KEY_IMAGE_URI = "image_uri"
        const val KEY_ALBUM_ID = "album_id"

        const val KEY_CONTENT = "content"

        const val KEY_USERNAME = "username"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_PROFILE_PIC = "profile_pic"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_USERS ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_USERNAME TEXT, $KEY_EMAIL TEXT, $KEY_PASSWORD TEXT, $KEY_PROFILE_PIC TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_ALBUMS ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_USER_ID INTEGER, $KEY_ALBUM_NAME TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_MEMORIES ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_USER_ID INTEGER, $KEY_TITLE TEXT, $KEY_CAPTION TEXT, $KEY_DATE TEXT, $KEY_IMAGE_URI TEXT, $KEY_ALBUM_ID INTEGER)")
        db?.execSQL("CREATE TABLE $TABLE_JOURNALS ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_USER_ID INTEGER, $KEY_CONTENT TEXT, $KEY_DATE TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MEMORIES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_JOURNALS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ALBUMS")
        onCreate(db)
    }

    // --- HELPER: CONVERT CURSOR TO MEMORY ---
    private fun cursorToMemory(cursor: Cursor): Memory {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
        val title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE))
        val caption = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CAPTION))
        val date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
        val imageUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_URI))
        val albId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ALBUM_ID))
        // We get album name from the JOIN
        val albName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ALBUM_NAME))
        return Memory(id, title, caption, date, imageUri, albId, albName)
    }

    // --- ALBUM FUNCTIONS ---
    fun createDefaultAlbum(userId: Int) {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ALBUMS WHERE $KEY_USER_ID = ?", arrayOf(userId.toString()))
        if (cursor.count == 0) {
            addAlbum(userId, "General")
        }
        cursor.close()
    }

    fun addAlbum(userId: Int, name: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_USER_ID, userId)
        values.put(KEY_ALBUM_NAME, name)
        val id = db.insert(TABLE_ALBUMS, null, values)
        db.close()
        return id
    }

    fun getAllAlbums(userId: Int): List<Album> {
        val list = ArrayList<Album>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ALBUMS WHERE $KEY_USER_ID = ? ORDER BY $KEY_ID ASC", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ALBUM_NAME))
                list.add(Album(id, name))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    // --- MEMORY FUNCTIONS ---
    fun addMemory(userId: Int, title: String, caption: String, date: String, imageUri: String, albumId: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_USER_ID, userId)
        values.put(KEY_TITLE, title)
        values.put(KEY_CAPTION, caption)
        values.put(KEY_DATE, date)
        values.put(KEY_IMAGE_URI, imageUri)
        values.put(KEY_ALBUM_ID, albumId)
        val result = db.insert(TABLE_MEMORIES, null, values)
        db.close()
        return result != -1L
    }

    fun getAllMemories(userId: Int): List<Memory> {
        val memoryList = ArrayList<Memory>()
        val db = this.readableDatabase
        val query = "SELECT m.*, a.$KEY_ALBUM_NAME FROM $TABLE_MEMORIES m JOIN $TABLE_ALBUMS a ON m.$KEY_ALBUM_ID = a.$KEY_ID WHERE m.$KEY_USER_ID = ? ORDER BY m.$KEY_ID DESC"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do { memoryList.add(cursorToMemory(cursor)) } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return memoryList
    }

    fun getMemoriesByAlbum(userId: Int, albumId: Int): List<Memory> {
        val list = ArrayList<Memory>()
        val db = this.readableDatabase
        val query = "SELECT m.*, a.$KEY_ALBUM_NAME FROM $TABLE_MEMORIES m JOIN $TABLE_ALBUMS a ON m.$KEY_ALBUM_ID = a.$KEY_ID WHERE m.$KEY_USER_ID = ? AND m.$KEY_ALBUM_ID = ? ORDER BY m.$KEY_DATE DESC"
        val cursor = db.rawQuery(query, arrayOf(userId.toString(), albumId.toString()))
        if (cursor.moveToFirst()) {
            do { list.add(cursorToMemory(cursor)) } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun getMemory(id: Int): Memory? {
        val db = this.readableDatabase
        val query = "SELECT m.*, a.$KEY_ALBUM_NAME FROM $TABLE_MEMORIES m JOIN $TABLE_ALBUMS a ON m.$KEY_ALBUM_ID = a.$KEY_ID WHERE m.$KEY_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))
        var memory: Memory? = null
        if (cursor.moveToFirst()) { memory = cursorToMemory(cursor) }
        cursor.close()
        db.close()
        return memory
    }

    fun updateMemory(id: Int, title: String, caption: String, date: String, imageUri: String, albumId: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_TITLE, title)
        values.put(KEY_CAPTION, caption)
        values.put(KEY_DATE, date)
        values.put(KEY_IMAGE_URI, imageUri)
        values.put(KEY_ALBUM_ID, albumId)
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
        val query = "SELECT m.*, a.$KEY_ALBUM_NAME FROM $TABLE_MEMORIES m JOIN $TABLE_ALBUMS a ON m.$KEY_ALBUM_ID = a.$KEY_ID WHERE m.$KEY_USER_ID = ? AND m.$KEY_DATE = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString(), date))
        if (cursor.moveToFirst()) {
            do { memoryList.add(cursorToMemory(cursor)) } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return memoryList
    }

    // --- MIXED FEED & SEARCH ---
    fun getMixedFeed(userId: Int): List<FeedItem> {
        val mixedList = ArrayList<FeedItem>()
        mixedList.addAll(getAllMemories(userId))
        mixedList.addAll(getAllJournals(userId))
        return mixedList.sortedByDescending { it.date }
    }

    // UPDATED SEARCH: Now searches Album Name instead of Category
    fun searchFeed(userId: Int, query: String): List<FeedItem> {
        val mixedList = ArrayList<FeedItem>()
        val db = this.readableDatabase

        // Search Memories (Title, Caption, or ALBUM NAME)
        val memQuery = """
            SELECT m.*, a.$KEY_ALBUM_NAME 
            FROM $TABLE_MEMORIES m 
            JOIN $TABLE_ALBUMS a ON m.$KEY_ALBUM_ID = a.$KEY_ID
            WHERE m.$KEY_USER_ID = ? AND 
            (m.$KEY_TITLE LIKE ? OR m.$KEY_CAPTION LIKE ? OR a.$KEY_ALBUM_NAME LIKE ?)
        """

        val memCursor = db.rawQuery(memQuery, arrayOf(userId.toString(), "%$query%", "%$query%", "%$query%"))

        if (memCursor.moveToFirst()) {
            do { mixedList.add(cursorToMemory(memCursor)) } while (memCursor.moveToNext())
        }
        memCursor.close()

        // Search Journals
        val journalCursor = db.rawQuery("SELECT * FROM $TABLE_JOURNALS WHERE $KEY_USER_ID = ? AND $KEY_CONTENT LIKE ?", arrayOf(userId.toString(), "%$query%"))
        if (journalCursor.moveToFirst()) {
            do {
                val id = journalCursor.getInt(journalCursor.getColumnIndexOrThrow(KEY_ID))
                val content = journalCursor.getString(journalCursor.getColumnIndexOrThrow(KEY_CONTENT))
                val date = journalCursor.getString(journalCursor.getColumnIndexOrThrow(KEY_DATE))
                mixedList.add(Journal(id, content, date))
            } while (journalCursor.moveToNext())
        }
        journalCursor.close()
        db.close()

        return mixedList.sortedByDescending { it.date }
    }

    // --- USER / JOURNAL HELPERS ---
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
        val list = ArrayList<Journal>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_JOURNALS WHERE $KEY_USER_ID = ? ORDER BY $KEY_ID DESC", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
                val content = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONTENT))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
                list.add(Journal(id, content, date))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun checkUser(input: String, password: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $KEY_ID FROM $TABLE_USERS WHERE ($KEY_USERNAME = ? OR $KEY_EMAIL = ?) AND $KEY_PASSWORD = ?", arrayOf(input, input, password))
        var userId = -1
        if (cursor.moveToFirst()) userId = cursor.getInt(0)
        cursor.close()
        db.close()
        return userId
    }

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
        if (imageUri != null) values.put(KEY_PROFILE_PIC, imageUri)
        val result = db.update(TABLE_USERS, values, "$KEY_ID = ?", arrayOf(userId.toString()))
        db.close()
        return result > 0
    }
}