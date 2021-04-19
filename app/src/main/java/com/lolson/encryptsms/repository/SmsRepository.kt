package com.lolson.encryptsms.repository

import android.content.Context
import com.lolson.encryptsms.data.model.Phone
import com.lolson.encryptsms.data.model.Sms
import com.lolson.encryptsms.database.ISmsSvc
import com.lolson.encryptsms.database.SmsSvcImpl
import com.lolson.encryptsms.utility.LogMe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsRepository(
    _context: Context
)
{

    //Context
    private val context = _context
    //DOA Interface
    private val smsDoa: ISmsSvc = SmsSvcImpl(context)
    //Logger
    private var l = LogMe()

    /**
     * GET ALL MESSAGES
     *
     * @param phones: ArrayList<Phone.pho>
     * @return data: ArrayList<Sms.AppSmsShort>
     */
    suspend fun getAllMessages(
        phones: ArrayList<Phone.pho?>
    ): ArrayList<Sms.AppSmsShort>?
    {
        val data = ArrayList<Sms.AppSmsShort>()
        withContext(Dispatchers.IO){
            smsDoa.getAllMessages(phones).let {
                if (it != null)
                {
                    data.addAll(it)
                }
            }
        }
        l.d("SR:: GET-ALL-MESSAGES")
        return data
    }

    /**
     * GET ALL THREADS
     *
     * @return data: ArrayList<Sms.AppSmsShort>
     */
    suspend fun getAllThreads(

    ): ArrayList<Sms.AppSmsShort>?
    {
        val data = ArrayList<Sms.AppSmsShort>()
        withContext(Dispatchers.IO){
            smsDoa.getAllThreads().let {
                if (it != null)
                {
                    data.addAll(it)
                }
            }
        }
        l.d("SR:: GET-ALL-THREADS")
        return data
    }

    /**
     * SEND SMS
     *
     * @param msg: Sms.AppSmsShort
     * @return result: Boolean
     */
    suspend fun send(
        msg: Sms.AppSmsShort
    ): Boolean
    {
        var result: Boolean
        withContext(Dispatchers.IO){
            smsDoa.send(msg).let { result = it }
        }
        l.d("SMS repository: SEND")
        return result
    }

    /**
     * FIND SMS
     *
     * @param address: String
     * @return result: Long
     */
    suspend fun find(
        address: String
    ): Long
    {
        var result: Long
        withContext(Dispatchers.IO){
            smsDoa.find(address).let { result = it }
        }
        l.d("SR:: FIND")
        return result
    }

    /**
     * UPDATE SMS
     *
     * @param msg: Sms.AppSmsShort
     * @return result: Boolean
     */
    suspend fun update(
        msg: Sms.AppSmsShort
    ): Boolean
    {
        var result: Boolean
        withContext(Dispatchers.IO){
            smsDoa.update(msg).let { result = it }
        }
        l.d("SR:: UPDATE")
        return result
    }

    /**
     * DELETE SMS
     *
     * @param msg: Sms.AppSmsShort
     * @return result: Boolean
     */
    suspend fun delete(
        msg: Sms.AppSmsShort
    ): Boolean
    {
        var result: Boolean
        withContext(Dispatchers.IO){
            smsDoa.delete(msg).let { result = it }
        }
        l.d("SR:: DELETE")
        return result
    }
}