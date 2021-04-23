package com.lolson.encryptsms.modelviewhelpers

import com.lolson.encryptsms.data.model.Sms
import com.lolson.encryptsms.utility.LogMe

class SmsHelper
{
    // Logger
    private var l = LogMe()

    fun buildSmsMessage(
        msg: String,
        tempSms: Sms.AppSmsShort
    )
    {
        // Build the message
        tempSms.read = 1
        tempSms.status = 0
        tempSms.type = 2
        tempSms.date = System.currentTimeMillis()
        tempSms.date_sent = tempSms.date
        tempSms.creator = "com.lolson.encryptsms"
        tempSms.body = msg

        l.d("SH:: SMS BUILDER: ${tempSms.read}")
    }

    /**
     * UPDATE THREAD WITH LATEST INFO
     */
    fun updateThread(
        sms: Sms.AppSmsShort,
        threads: ArrayList<Sms.AppSmsShort>
    ):ArrayList<Sms.AppSmsShort>
    {
        val data = ArrayList<Sms.AppSmsShort>()

        // Creates shallow copy of objects
        threads.map { data.add(it.copy()) }

        // Find the thread location by thread_id and update
        for ((con, d) in data.withIndex())
        {
            if (d.thread_id == sms.thread_id)
            {
                // Update thread info
                data[con].body = sms.body
                data[con].read = sms.read
                data[con].date = sms.date

                // Move to top, which is at the end!?
                data.add(data[con])
                // Insert adds to end, so original should be at the same spot
                data.removeAt(con)

                l.d("SH:: SMS THREAD UPDATE: $con")
                break
            }
        }
        return ArrayList(data)
    }
}