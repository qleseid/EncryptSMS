package com.lolson.encryptsms.repository

import android.content.Context
import com.lolson.encryptsms.contents.KeyContent
import com.lolson.encryptsms.database.ContactKeySvcSQLiteImpl
import com.lolson.encryptsms.database.IContactKeySvc
import com.lolson.encryptsms.utility.LogMe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsKeyRepository(_context: Context)
{

    //Context
    private val context = _context
    //DOA Interface
    private val contactKeyDoa: IContactKeySvc = ContactKeySvcSQLiteImpl(context)
    //Logger
    private var l = LogMe()

    /**
     * GET ALL KEYS FOR CONTACTS
     */
    suspend fun getAll(): ArrayList<KeyContent.AppKey>
    {
        val data = ArrayList<KeyContent.AppKey>()
        withContext(Dispatchers.IO){
            contactKeyDoa.getAllKeys().let {
                if (it != null)
                {
                    data.addAll(it)
                }
            }
        }
        l.d("CONTACTS KEY REPOSITORY: GET ALL SIZE: ${data.size}")
        return data
    }

    /**
     * CREATE: STORE CONTACTS PUBLIC KEY
     */
    suspend fun create(
        _key: KeyContent.AppKey
    ):Boolean
    {
        var success = false

        withContext(Dispatchers.IO){
            if (contactKeyDoa.create(_key) != -1L)
            {
                success = true
            }
        }
        l.d("CONTACTS KEY REPOSITORY: CREATE: $success")
        return success
    }

    /**
     * UPDATE: UPDATE CONTACTS PUBLIC KEY
     */
    suspend fun update(
        _key: KeyContent.AppKey
    ):Boolean
    {
        var success = false

        withContext(Dispatchers.IO){
            success = contactKeyDoa.update(_key)
        }
        l.d("CONTACTS KEY REPOSITORY: UPDATE: $success")
        return success
    }

    /**
     * DELETE: DELETE CONTACTS PUBLIC KEY
     */
    suspend fun delete(
        _key: KeyContent.AppKey
    ):Boolean
    {
        var success = false

        withContext(Dispatchers.IO){
            success = contactKeyDoa.delete(_key)
        }
        l.d("CONTACTS KEY REPOSITORY: DELETE: $success")
        return success
    }
}