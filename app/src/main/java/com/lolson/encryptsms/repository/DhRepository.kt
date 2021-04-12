package com.lolson.encryptsms.repository

import android.content.Context
import com.lolson.encryptsms.database.DhKeySvcSQLiteImpl
import com.lolson.encryptsms.database.IDhKeySvc
import com.lolson.encryptsms.utility.LogMe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyPair

class DhRepository(_context: Context) {

    //Context
    private val context = _context
    //DOA Interface
    private val dhDoa: IDhKeySvc = DhKeySvcSQLiteImpl(context)
    //Logger
    private var l = LogMe()

    /**
     * GET DH KEY
     */
    suspend fun getKey():
            KeyPair?
    {
        var data: KeyPair? = null
        withContext(Dispatchers.IO){
            dhDoa.getKey().let {
                if (it != null)
                {
                    data = KeyPair(it.public, it.private)
                }
            }
        }
        l.d("DH key repository GET KEY")
        return data
    }

    /**
     * SAVE KEY: STORE DH KEY
     */
    suspend fun saveKey(
        _key: KeyPair
    ):Boolean
    {
        var success: Boolean
        withContext(Dispatchers.IO){

            success = dhDoa.saveKey(_key)
        }
        l.d("DH key repository SAVE KEY: $success")
        return success
    }
}