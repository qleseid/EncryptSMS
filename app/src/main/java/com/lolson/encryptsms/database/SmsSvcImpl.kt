@file:Suppress("unused", "unused")

package com.lolson.encryptsms.database

import android.content.Context
import android.net.Uri
import com.lolson.encryptsms.data.model.Phone
import com.lolson.encryptsms.data.model.Sms
import com.lolson.encryptsms.utility.LogMe

/**
 * Service to store key data to a local file
 */
class SmsSvcImpl(
    _context: Context
): ISmsSvc
{
    companion object
    {
        // SMS URI
        private val SMS_CONTENT_URI: Uri = Uri.parse("content://sms")
    }

    private var context: Context = _context

    // SmsManger
    private val smsM = com.lolson.encryptsms.data.manager.SmsManager(context)

    //Logger
    private var l = LogMe()

    /**
     * GET ALL MESSAGES
     */
    override fun getAllMessages(
        phones: ArrayList<Phone.pho?>
    ): ArrayList<Sms.AppSmsShort>?
    {
        l.d("In SMS Service Implement GET ALL MESSAGES")

        return smsM.getSms(phones)
    }

    /**
     * GET ALL THREADS
     */
    override fun getAllThreads(

    ): ArrayList<Sms.AppSmsShort>?
    {
        return smsM.getConvoThreads()
    }

    /**
     * SEND
     */
    override fun send(
        msg: Sms.AppSmsShort
    ): Boolean
    {
        return smsM.sendMessage(msg, SMS_CONTENT_URI)
    }

    /**
     * FIND
     */
    override fun find(
        address: String
    ): Long
    {
        return smsM.getThreadId(address)
    }

    /**
     * UPDATE
     */
    override fun update(
        msg: Sms.AppSmsShort
    ): Boolean
    {
        TODO("Not yet implemented")
    }

    /**
     * DELETE
     */
    override fun delete(
        msg: Sms.AppSmsShort
    ): Boolean
    {
        TODO("Not yet implemented")
    }


}