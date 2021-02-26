package com.example.encryptsms.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.example.encryptsms.items.ItemContent
import com.example.encryptsms.items.ItemSQLiteContract
import com.example.encryptsms.utility.LogMe

/**
 * SQLite service to store data in embedded database
 */
class ItemSvcSQLiteImpl(_context: Context): SQLiteOpenHelper(_context, DB_NAME,null, DB_VERSION), IItemSvc {

    //Filename and version
    companion object {
        //If database schema changes, increment version number
        const val DB_NAME = "items.db"
        const val DB_VERSION = 1

        /**
         * Create database
         */
        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${ItemSQLiteContract.ItemEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${ItemSQLiteContract.ItemEntry.COLUMN_NAME_CONTENT} TEXT," +
                    "${ItemSQLiteContract.ItemEntry.COLUMN_NAME_DETAILS} TEXT," +
                    "${ItemSQLiteContract.ItemEntry.COLUMN_NAME_ICON} TEXT," +
                    "${ItemSQLiteContract.ItemEntry.COLUMN_NAME_AMOUNT} FLOAT)"

        /**
         * Delete database
         */
        private const val SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS ${ItemSQLiteContract.ItemEntry.TABLE_NAME}"
    }

    //Context for local file path
    private val context: Context = _context

    //Database
    private lateinit var wdb: SQLiteDatabase
    private lateinit var rdb: SQLiteDatabase

    //Logger
    private var l = LogMe()

    override fun onCreate(db: SQLiteDatabase?) {
        l.d("ON_CREATE SQLite service implementation")
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVer: Int, newVer: Int) {
        //Simple upgrade policy to delete old data and start over
        l.i("ON_UPGRADE SQLite service implementation from version:" +
                    " $oldVer to: $newVer")
        db?.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVer: Int, newVer: Int) {
        l.i("ON_DOWNGRADE SQLite service implementation from version:" +
                    " $oldVer to: $newVer")
        onUpgrade(db, oldVer, newVer)
    }

    /**
     * GET ALL ITEMS
     * @return ArrayList<ItemContent.AppItem>
     */
    override fun getAllItems(): ArrayList<ItemContent.AppItem>? {

        rdb = this.readableDatabase
        val tempList = ArrayList<ItemContent.AppItem>()

        //Projection specifies which columns to read from database
        val projection = arrayOf(
            BaseColumns._ID,
            ItemSQLiteContract.ItemEntry.COLUMN_NAME_CONTENT,
            ItemSQLiteContract.ItemEntry.COLUMN_NAME_DETAILS,
            ItemSQLiteContract.ItemEntry.COLUMN_NAME_ICON,
            ItemSQLiteContract.ItemEntry.COLUMN_NAME_AMOUNT)

        val cursor = rdb.query(
            ItemSQLiteContract.ItemEntry.TABLE_NAME,   //Database name
            projection,                                //Columns to read from
            null,
            null,
            null,
            null,
            null)

        //Move read to first position
        cursor.moveToFirst()
        while (!cursor.isAfterLast){
            val tempI = ItemContent.AppItem(
                cursor.getString(0),       // Item ID
                cursor.getString(1),       // Content
                cursor.getString(2),       // Details
                cursor.getString(3),       // icon
                cursor.getFloat(4)         // amount
            )
            tempList.add(tempI)
            cursor.moveToNext()
        }

        rdb.close()

        //Number of items read into list
        l.d("SQLite GET ALL: list size: ${tempList.size}")

        return tempList
    }

    /**
     * CREATE ITEM
     *
     * @param item: ItemContent.AppItem
     * @return success: Boolean
     */
    override fun create(item: ItemContent.AppItem): Boolean {

        //Get writeable database
        wdb = this.writableDatabase

        //Create a new map of values; column names are the keys
        val value = ContentValues().apply {
            put(ItemSQLiteContract.ItemEntry.COLUMN_NAME_CONTENT, item.content)
            put(ItemSQLiteContract.ItemEntry.COLUMN_NAME_DETAILS, item.details)
            put(ItemSQLiteContract.ItemEntry.COLUMN_NAME_ICON, item.icon)
            put(ItemSQLiteContract.ItemEntry.COLUMN_NAME_AMOUNT, item.amount)
        }

        //Insert the new row, returns primary key value: -1 if error
        val newRowId = wdb.insert(ItemSQLiteContract.ItemEntry.TABLE_NAME, null, value)

        //Close database
        wdb.close()

        //Row ID result
        l.d("SQLite CREATE row id: $newRowId")
        return (newRowId > -1)
    }

    /**
     * UPDATE ITEM
     *
     * @param item: ItemContent.AppItem
     * @return success: Boolean
     */
    override fun update(item: ItemContent.AppItem): Boolean {

        //Get readable database
        rdb = this.readableDatabase

        //Put conversation values in content
        val value = ContentValues().apply {
            put(ItemSQLiteContract.ItemEntry.COLUMN_NAME_CONTENT, item.content)
            put(ItemSQLiteContract.ItemEntry.COLUMN_NAME_DETAILS, item.details)
            put(ItemSQLiteContract.ItemEntry.COLUMN_NAME_ICON, item.icon)
            put(ItemSQLiteContract.ItemEntry.COLUMN_NAME_AMOUNT, item.amount)
        }

        val selection = "${BaseColumns._ID} LIKE ?"
        val selectArg = arrayOf("${item.id.toInt()}")

        //Use ID to select row to update
        val count = rdb.update(
            ItemSQLiteContract.ItemEntry.TABLE_NAME,
            value,
            selection,
            selectArg)

        //Close database
        rdb.close()

        //Number of rows updated
        l.d("SQLITE UPDATE # rows: $count")
        return count > 0
    }

    /**
     * DELETE ITEM
     *
     * @param item: ItemContent.AppItem
     * @return success: Boolean
     */
    override fun delete(item: ItemContent.AppItem): Boolean {

        //Get writeable database
        wdb = this.writableDatabase

        val selection = "${BaseColumns._ID} LIKE ?"
        val selectArg = arrayOf("${item.id.toInt()}")

        //Use ID to select row to update
        val count = wdb.delete(
            ItemSQLiteContract.ItemEntry.TABLE_NAME,
            selection,
            selectArg)

        //Close database
        wdb.close()

        //Number of rows updated
        l.d("SQLITE DELETE # rows: $count")
        return count > 0
    }
}