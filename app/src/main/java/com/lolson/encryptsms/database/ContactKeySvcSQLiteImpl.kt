package com.lolson.encryptsms.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import androidx.core.database.getBlobOrNull
import com.lolson.encryptsms.contents.KeyContent
import com.lolson.encryptsms.contracts.KeySQLiteContract
import com.lolson.encryptsms.utility.LogMe
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

/**
 * Contact Key SQLite service to store data in embedded database
 */
class ContactKeySvcSQLiteImpl(
    _context: Context
): SQLiteOpenHelper(_context, DB_NAME,null, DB_VERSION), IContactKeySvc {

    //Filename and version
    companion object {
        //If database schema changes, increment version number
        const val DB_NAME = "keys.db"
        const val DB_VERSION = 2

        /**
         * Create database
         */
        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${KeySQLiteContract.KeyEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${KeySQLiteContract.KeyEntry.COLUMN_NAME_SENT} INTEGER DEFAULT 0," +
                    "${KeySQLiteContract.KeyEntry.COLUMN_NAME_CHECK} INTEGER DEFAULT 3," +
                    "${KeySQLiteContract.KeyEntry.COLUMN_NAME_ID} TEXT," +
                    "${KeySQLiteContract.KeyEntry.COLUMN_NAME_PUBLICKEY} BLOB)"

        /**
         * Delete database
         */
        private const val SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS ${KeySQLiteContract.KeyEntry.TABLE_NAME}"
    }

    //Context for local file path
    private val context: Context = _context

    //Database
    private lateinit var wdb: SQLiteDatabase
    private lateinit var rdb: SQLiteDatabase

    //Logger
    private var l = LogMe()

    override fun onCreate(
        db: SQLiteDatabase?
    )
    {
        l.d("CKSSSLI:: ON_CREATE Contact Key SQLite service implementation")
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVer: Int,
        newVer: Int
    )
    {
        //Simple upgrade policy to delete old data and start over
        l.i("CKSSSLI:: ON_UPGRADE Contact Key SQLite service implementation from version:" +
                    " $oldVer to: $newVer")
        db?.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(
        db: SQLiteDatabase?,
        oldVer: Int,
        newVer: Int
    )
    {
        l.i("CKSSSLI:: ON_DOWNGRADE Contact Key SQLite service implementation from version:" +
                    " $oldVer to: $newVer")
        onUpgrade(db, oldVer, newVer)
    }

    /**
     * GET ALL KEYS
     * @return ArrayList<KeyContent.AppKey>
     */
    override fun getAllKeys(): ArrayList<KeyContent.AppKey>?
    {
        rdb = this.readableDatabase
        val tempList = ArrayList<KeyContent.AppKey>()

        //Projection specifies which columns to read from database
        val projection = arrayOf(
            BaseColumns._ID,
            KeySQLiteContract.KeyEntry.COLUMN_NAME_SENT,
            KeySQLiteContract.KeyEntry.COLUMN_NAME_CHECK,
            KeySQLiteContract.KeyEntry.COLUMN_NAME_ID,
            KeySQLiteContract.KeyEntry.COLUMN_NAME_PUBLICKEY)

        val cursor = rdb.query(
            KeySQLiteContract.KeyEntry.TABLE_NAME,     //Database name
            projection,                                //Columns to read from
            null,
            null,
            null,
            null,
            null)

        //Move read to first position
        cursor.moveToFirst()
        while (!cursor.isAfterLast)
        {
            val tempI = KeyContent.AppKey()
            tempI.id = cursor.getInt(0)                 // key primary id
            tempI.sent = (cursor.getInt(1) == 1)             // sent boolean
            tempI.last_check = cursor.getLong(2)                   // checked long
            tempI.thread_id = cursor.getLong(3)                 // thread id
            cursor.getBlobOrNull(4)?.let { tempI.publicKey = publicKeyGen(it) }      // public key

            tempList.add(tempI)
            cursor.moveToNext()
        }

        cursor.close()
        rdb.close()

        //Number of keys read into list
        l.d("CKSSSLI:: Contact Key SQLite GET ALL: list size: ${tempList.size}")

        return tempList
    }

    /**
     * CREATE KEY
     *
     * @param key: KeyContent.AppKey
     * @return success: Boolean
     */
    override fun create(
        key: KeyContent.AppKey
    ): Long
    {

        //Get writeable database
        wdb = this.writableDatabase

        //Create a new map of values; column names are the keys
        val value = ContentValues()
            .apply {
                put(KeySQLiteContract.KeyEntry.COLUMN_NAME_SENT,
                    key.sent)
                put(KeySQLiteContract.KeyEntry.COLUMN_NAME_CHECK,
                    key.last_check)
                put(KeySQLiteContract.KeyEntry.COLUMN_NAME_ID,
                    key.thread_id)
                if (key.publicKey != null)
                {
                    put(KeySQLiteContract.KeyEntry.COLUMN_NAME_PUBLICKEY,
                        key.publicKey?.let { encodePublicKey(it) })
                }
                else
                {
                    putNull(KeySQLiteContract.KeyEntry.COLUMN_NAME_PUBLICKEY)
                }
            }

        //Insert the new row, returns primary key value: -1 if error
        val newRowId = wdb.insert(KeySQLiteContract.KeyEntry.TABLE_NAME, null, value)
        if (newRowId != -1L)
        {
            // Hope key is a reference, then this will work
            key.id = newRowId.toInt()
        }

        //Close database
        wdb.close()

        //Row ID result
        l.d("CKSSSLI:: Contact Key SQLite CREATE row id: $newRowId")
        return newRowId
    }

    /**
     * UPDATE KEY
     *
     * @param key: KeyContent.AppKey
     * @return success: Boolean
     */
    override fun update(
        key: KeyContent.AppKey
    ): Boolean
    {

        //Get readable database
        rdb = this.readableDatabase

        //Put conversation values in content
        val value = ContentValues()
            .apply {
                put(KeySQLiteContract.KeyEntry.COLUMN_NAME_SENT,
                    key.sent)
                put(KeySQLiteContract.KeyEntry.COLUMN_NAME_CHECK,
                    key.last_check)
                put(KeySQLiteContract.KeyEntry.COLUMN_NAME_ID,
                    key.thread_id)
                if (key.publicKey != null)
                {
                    put(KeySQLiteContract.KeyEntry.COLUMN_NAME_PUBLICKEY,
                        key.publicKey?.let { encodePublicKey(it) })
                }
                else
                {
                    putNull(KeySQLiteContract.KeyEntry.COLUMN_NAME_PUBLICKEY)
                }
            }

        val selection = "${BaseColumns._ID} LIKE ?"
        val selectArg = arrayOf("${key.id}")

        //Use ID to select row to update
        val count = rdb.update(
            KeySQLiteContract.KeyEntry.TABLE_NAME,
            value,
            selection,
            selectArg)

        //Close database
        rdb.close()

        //Number of rows updated
        l.d("CKSSSLI:: Contact Key SQLITE UPDATE # rows: $count")
        return count > 0
    }

    /**
     * DELETE KEY
     *
     * @param key: KeyContent.AppKey
     * @return success: Boolean
     */
    override fun delete(
        key: KeyContent.AppKey
    ): Boolean
    {

        //Get writeable database
        wdb = this.writableDatabase

        val selection = "${BaseColumns._ID} LIKE ?"
        val selectArg = arrayOf("${key.id}")

        //Use ID to select row to delete
        val count = wdb.delete(
            KeySQLiteContract.KeyEntry.TABLE_NAME,
            selection,
            selectArg)

        //Close database
        wdb.close()

        //Number of rows updated
        l.d("CKSSSLI:: Contact Key SQLITE DELETE # rows: $count")
        return count > 0
    }

    /**
     * GENERATE PUBLIC KEY FROM BLOB
     */
    private fun publicKeyGen(
        data: ByteArray
    ): PublicKey
    {
        return KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(data))
    }

    /**
     * ENCODE PUBLIC KEY TO BYTE ARRAY
     */
    private fun encodePublicKey(
        key: PublicKey
    ): ByteArray
    {
        return key.encoded
    }
}