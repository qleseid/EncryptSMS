package com.lolson.encryptsms.repository

import android.content.Context
import com.lolson.encryptsms.database.EcKeySvcSQLiteImpl
import com.lolson.encryptsms.database.IEcKeySvc
import com.lolson.encryptsms.utility.LogMe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyPair

class EcRepository(_context: Context) {

    //Context
    private val context = _context
    //DOA Interface
    private val ecDoa: IEcKeySvc = EcKeySvcSQLiteImpl(context)
    //Logger
    private var l = LogMe()

    /**
     * GET EC KEY
     */
    suspend fun getKey():
            KeyPair?
    {
        var data: KeyPair? = null
        withContext(Dispatchers.IO){
            ecDoa.getKey().let {
                if (it != null)
                {
                    data = KeyPair(it.public, it.private)
                }
            }
        }
        l.d("ER:: EC key repository GET KEY")
        return data
    }

    /**
     * SAVE KEY: STORE EC KEY
     */
    suspend fun saveKey(
        _key: KeyPair
    ):Boolean
    {
        var success: Boolean
        withContext(Dispatchers.IO){

            success = ecDoa.saveKey(_key)
        }
        l.d("ER:: EC key repository SAVE KEY: $success")
        return success
    }
}