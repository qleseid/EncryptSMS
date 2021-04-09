@file:Suppress("DEPRECATION")

package com.lolson.encryptsms.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.security.KeyPairGeneratorSpec
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import com.lolson.encryptsms.contracts.DhSQLiteContract
import com.lolson.encryptsms.utility.LogMe
import java.math.BigInteger
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal
import kotlin.collections.ArrayList

/**
 * SQLite service to store data in embedded database
 */
class DhKeySvcSQLiteImpl(
    _context: Context
): SQLiteOpenHelper(_context, DB_NAME,null, DB_VERSION), IDhKeySvc {

    // Filename and version
    companion object {
        // If database schema changes, increment version number
        const val DB_NAME = "dh_keys.db"
        const val DB_VERSION = 1
        const val ANDROID_KEYSTORE = "AndroidKeyStore"

        // KeyStore
        const val KEY_STORE_NAME = "sms_store_key"
        private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)

        /**
         * Create database
         */
        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${DhSQLiteContract.KeyEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${DhSQLiteContract.KeyEntry.COLUMN_NAME_ID} TEXT," +
                    "${DhSQLiteContract.KeyEntry.COLUMN_NAME_PRIVATEKEY} BLOB," +
                    "${DhSQLiteContract.KeyEntry.COLUMN_NAME_PUBLICKEY} BLOB)"

        /**
         * Delete database
         */
        private const val SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS ${DhSQLiteContract.KeyEntry.TABLE_NAME}"
    }

    // Database
    private lateinit var wdb: SQLiteDatabase
    private lateinit var rdb: SQLiteDatabase
    private val mContext = _context

    // Logger
    private var l = LogMe()

    override fun onCreate(
        db: SQLiteDatabase?
    )
    {
        l.d("ON_CREATE DH SQLite service implementation")

        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVer: Int,
        newVer: Int
    )
    {
        //Simple upgrade policy to delete old data and start over
        l.i("ON_UPGRADE DH SQLite service implementation from version:" +
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
        l.i("ON_DOWNGRADE DH SQLite service implementation from version:" +
                    " $oldVer to: $newVer")
        onUpgrade(db, oldVer, newVer)
    }

    /**
     * GET KEY
     * @return KeyPair?
     */
    override fun getKey(

    ): KeyPair?
    {
        l.d("GETKEY: DH KEY SQLITE IMPL")

        rdb = this.readableDatabase
        var tempList: KeyPair? = null

        keyStore.load(null)
//        keyStore.deleteEntry(KEY_STORE_NAME)

        if (!keyStore.containsAlias(KEY_STORE_NAME))
        {
            generateStoreKeys()
        }

        printStoreKeys()

        //Projection specifies which columns to read from database
        val projection = arrayOf(
            BaseColumns._ID,
            DhSQLiteContract.KeyEntry.COLUMN_NAME_ID,
            DhSQLiteContract.KeyEntry.COLUMN_NAME_PUBLICKEY,
            DhSQLiteContract.KeyEntry.COLUMN_NAME_PRIVATEKEY)

        val cursor = rdb.query(
            DhSQLiteContract.KeyEntry.TABLE_NAME,     //Database name
            projection,                                //Columns to read from
            null,
            null,
            null,
            null,
            null)

//        Move read to first position, return false if empty
        if(cursor.moveToFirst() &&
            cursor.getString(1) == DhSQLiteContract.KeyEntry.COLUMN_NAME_ID)
        {
            tempList = KeyPair(
//                publicKeyGen(cursor.getBlob(2)),
//                privateKeyGen(cursor.getBlob(3)))
            publicKeyGen(decrypt(cursor.getBlob(2))),
            privateKeyGen(decrypt(cursor.getBlob(3))))
        }
        cursor.close()
        rdb.close()

        // Print public key
        l.d("DH SQLite GET KEY SUCCESS:  ${tempList != null}")

        return tempList
    }

    /**
     * SAVE KEY
     * @param key: KeyPair
     * @return result: Boolean
     */
    override fun saveKey(
        key: KeyPair
    ): Boolean
    {
        return if (getKey() != null) // There are keys already in database; update
        {
            update(key)
        }
        else
        {
            create(key)
        }
    }

    /**
     * CREATE ITEM
     *
     * @param key: KeyPair
     * @return success: Boolean
     */
    private fun create(
        key: KeyPair
    ): Boolean
    {
        var result = false

        // Get writeable database
        wdb = this.writableDatabase

        // Create a new map of values; column names are the keys
        val value = ContentValues()
            .apply {
                put(DhSQLiteContract.KeyEntry.COLUMN_NAME_ID,
                    DhSQLiteContract.KeyEntry.COLUMN_NAME_ID)
                put(DhSQLiteContract.KeyEntry.COLUMN_NAME_PUBLICKEY,
                    encrypt(encodePublicKey(key.public)))
                put(DhSQLiteContract.KeyEntry.COLUMN_NAME_PRIVATEKEY,
                    encrypt(encodePrivateKey(key.private)))
            }

        // Insert, returns primary key value: -1 if error
        val newRowId = wdb.insert(DhSQLiteContract.KeyEntry.TABLE_NAME, null, value)
        if (newRowId != -1L)
        {
            result = true
        }

        // Close database
        wdb.close()

        // Result
        l.d("DH SQLite CREATE result: $result")
        return result
    }

    /**
     * UPDATE DH
     *
     * @param key: KeyPair
     * @return success: Boolean
     */
    private fun update(
        key: KeyPair
    ): Boolean
    {
        // Get writeable database
        wdb = this.writableDatabase

        // Create a new map of values; column names are the keys
        val value = ContentValues()
            .apply {
                put(DhSQLiteContract.KeyEntry.COLUMN_NAME_ID,
                    DhSQLiteContract.KeyEntry.COLUMN_NAME_ID)
                put(DhSQLiteContract.KeyEntry.COLUMN_NAME_PUBLICKEY,
                    encrypt(encodePublicKey(key.public)))
                put(DhSQLiteContract.KeyEntry.COLUMN_NAME_PRIVATEKEY,
                    encrypt(encodePrivateKey(key.private)))
            }

        val selection = "${DhSQLiteContract.KeyEntry.COLUMN_NAME_ID} LIKE ?"
        val selectArg = arrayOf(DhSQLiteContract.KeyEntry.COLUMN_NAME_ID)

        // Use ID to select row to update
        val count = wdb.update(
            DhSQLiteContract.KeyEntry.TABLE_NAME,
            value,
            selection,
            selectArg)

        // Close database
        wdb.close()

        // Number of rows updated
        l.d("DH SQLITE UPDATE # rows: $count")
        return count > 0
    }

    /**
     * GENERATE PUBLIC KEY FROM BLOB
     */
    private fun publicKeyGen(
        data: ByteArray
    ): PublicKey
    {
        return KeyFactory.getInstance("DH").generatePublic(X509EncodedKeySpec(data))
    }

    /**
     * GENERATE PRIVATE KEY FROM BLOB
     */
    private fun privateKeyGen(
        data: ByteArray
    ): PrivateKey
    {
        return KeyFactory.getInstance("DH").generatePrivate(PKCS8EncodedKeySpec(data))
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

    /**
     * ENCODE PRIVATE KEY TO BYTE ARRAY
     */
    private fun encodePrivateKey(
        key: PrivateKey
    ): ByteArray
    {
        return key.encoded
    }

    /**
     * GENERATE KEYSTORE KEYS
     */
    @SuppressWarnings("deprecation")
    private fun generateStoreKeys()
    {
        val kpGen = KeyPairGenerator.getInstance("RSA", ANDROID_KEYSTORE)
        try
        {
            val dateSt = Calendar.getInstance().time
            val dateEn = Calendar.getInstance()
            dateEn.add(Calendar.YEAR, 10)

            val spec = KeyPairGeneratorSpec
                .Builder(mContext)
                .setKeySize(4096)
                .setAlias("sms_store_key")
                .setSubject(X500Principal("CN=sms_store_key, O=Android Authority"))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(dateSt)
                .setEndDate(dateEn.time)
                .build()

            kpGen.initialize(spec)
            val temp = kpGen.generateKeyPair()

            l.d("KEYSTORE KEY GENERATE SUCCESS: ${
                encodeToString(temp.public.encoded,
                DEFAULT)
            } :: ${temp.private.algorithm}")
        }
        catch (e: Exception)
        {
            l.e("KEYSTORE KEY GENERATE ERROR: $e")
        }

    }

    /**
     * PRINT KEYSTORE ALIASES
     */
    private fun printStoreKeys()
    {
        try
        {
            for ((con, ali) in keyStore.aliases().iterator().withIndex())
            {
                l.d("KEYSTORE ALIASES: ($con) $ali")
            }
        }
        catch (e: Exception)
        {
            l.e("KEYSTORE ALIASES READ ERROR: $e")
        }
    }

    /**
     * DH ENCRYPT
     */
    private fun encrypt(
        data: ByteArray
    ):ByteArray
    {
//        l.d("START ENCRYPT RSA: ${data.size} ${encodeToString(data, DEFAULT).length} ::" +
//                encodeToString(data, DEFAULT))

        val byteSlices = ArrayList<ByteArray>()
        byteSlices.add(data.sliceArray(0..405))
        byteSlices.add(data.sliceArray(406 until data.size))


        val publicKey = keyStore.getCertificate(KEY_STORE_NAME).publicKey

        val cipher = Cipher
            .getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        var result = ByteArray(0)

        for (d in byteSlices)
        {
            result += cipher.doFinal(d)
//            l.d("SLICE SIZE: ${d.size} ${result.size} ${
//                encodeToString(result, DEFAULT)}")
//            decrypt(result)
        }

//        l.d("ENCRYPT RSA: ${result.size} ${encodeToString(result, DEFAULT).length} " +
//                ":: ${encodeToString(result, DEFAULT)}")

        return result
    }

    /**
     * DH DECRYPT
     */
    private fun decrypt(
        data: ByteArray
    ):ByteArray
    {
//        l.d("START DECRYPT RSA: ${data.size}  ${encodeToString(data, DEFAULT).length} :: ${encodeToString
//            (data, DEFAULT)}")

        val byteSlices = ArrayList<ByteArray>()
        try
        {
            byteSlices.add(data.sliceArray(0..511))
            byteSlices.add(data.sliceArray(512 until data.size))
        }
        catch (e: java.lang.Exception){}

        val privateKeyEntry = keyStore.getEntry(KEY_STORE_NAME, null) as KeyStore.PrivateKeyEntry
        val privateKey = privateKeyEntry.privateKey

        val cipherD = Cipher
            .getInstance("RSA/ECB/PKCS1Padding")
        cipherD.init(Cipher.DECRYPT_MODE, privateKey)

        var result = ByteArray(0)

        for (d in byteSlices)
        {
//            l.d("DE SLICE SIZE: ${d.size} ${result.size}")
            if (d.isNotEmpty())
            {
                result += cipherD.doFinal(d)
            }
        }

//        l.d("DECRYPT RSA: ${result.size} ${encodeToString(result, DEFAULT).length} " +
//                ":: ${encodeToString(result, DEFAULT)}")

        return result
    }
}