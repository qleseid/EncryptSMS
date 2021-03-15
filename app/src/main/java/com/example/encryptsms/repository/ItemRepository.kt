package com.example.encryptsms.repository

import android.content.Context
import com.example.encryptsms.database.ContactKeySvcSQLiteImpl
import com.example.encryptsms.database.IContactKeySvc
import com.example.encryptsms.keys.KeyContent
import com.example.encryptsms.utility.LogMe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemRepository(_context: Context) {

    //Context
    private val context = _context
    //DOA Interface
    private val contactKeyDoa: IContactKeySvc = ContactKeySvcSQLiteImpl(context)
    //Logger
    private var l = LogMe()

    /**
     * GET ALL ITEMS
     */
    suspend fun getAll():
            ArrayList<KeyContent.AppKey> {
        val data = ArrayList<KeyContent.AppKey>()
        withContext(Dispatchers.IO){
            contactKeyDoa.getAllKeys()?.let { data.addAll(it) }
        }
        l.d("Repository GET ALL")
        return data
    }

    /**
     * CREATE ITEM
     */
    suspend fun create(_key: KeyContent.AppKey):
            Boolean {

        var success = false

        withContext(Dispatchers.IO){
            if (contactKeyDoa.create(_key) != -1L)
            {
                success = true
            }
        }
        l.d("Repository CREATE: $success")
        return success
    }

    /**
     * UPDATE ITEM
     */
    suspend fun update(_key: KeyContent.AppKey):
            Boolean {
        var success = false

        withContext(Dispatchers.IO){
            success = contactKeyDoa.update(_key)
        }
        l.d("Repository UPDATE: $success")
        return success
    }

    /**
     * DELETE ITEM
     */
    suspend fun delete(_key: KeyContent.AppKey):
            Boolean {
        var success = false

        withContext(Dispatchers.IO){
            success = contactKeyDoa.delete(_key)
        }
        l.d("Repository DELETE: $success")
        return success
    }
}