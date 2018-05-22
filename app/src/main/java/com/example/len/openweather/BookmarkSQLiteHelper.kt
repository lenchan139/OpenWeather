package com.example.len.openweather

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class BookmarkSQLiteHelper(context: Context): SQLiteOpenHelper(context, "bookmarks.db", null, 4){
    val tableName = "bookmarks"
    class Bookmark(var id :Int, var name:String, var lat:String,var long:String){}
    override fun onCreate(db: SQLiteDatabase) {

        val sql = "CREATE TABLE if not exists $tableName ( id integer PRIMARY KEY autoincrement, name text, lat text, long text)"
        db.execSQL(sql)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    fun addBookmark(name:String,lat:String,long:String) {
        val values = ContentValues()
        values.put("name", name)
        values.put("lat", lat)
        values.put("long", long)
        writableDatabase.insert(tableName, null, values)
    }
    fun getBookmarks(): ArrayList<Bookmark> {
        val cursor = readableDatabase.query(tableName, arrayOf("id", "name", "lat", "long "), null, null, null, null, null)
        val members = ArrayList<Bookmark>()

        try {
            if(cursor.moveToFirst()){
                do {
                    val name = cursor.getString(cursor.getColumnIndex("name"))
                    val id = cursor.getInt(cursor.getColumnIndex("id"))
                    val lat = cursor.getString(cursor.getColumnIndex("lat"))
                    val long = cursor.getString(cursor.getColumnIndex("long"))
                    val item = Bookmark(id, name, lat, long)
                    members.add(item)
                } while(cursor.moveToNext())

            }
        } catch (e:Exception) {

        } finally {
            if(cursor != null && !cursor.isClosed){
                cursor.close()
            }
        }
        Log.v("SQLitePrint", cursor.count.toString())
        return members

    }
}