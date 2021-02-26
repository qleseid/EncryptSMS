package com.example.encryptsms.data.manager

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import androidx.core.content.contentValuesOf
import com.example.encryptsms.data.model.Phone
import com.example.encryptsms.data.model.Sms
import com.example.encryptsms.utility.LogMe


class SmsManager
{
    //Logger
    var l = LogMe()
    private var _context: Context
    private lateinit var _settings: SettingsManager

    // URI STRINGS FOR ACCESSING THE DATABASE
    private val THREADS_CONTENT_URI: Uri = Uri.parse("content://mms-sms/conversations/")
    //private val THREADS_CONTENT_URI: Uri = Threads.CONTENT_URI.buildUpon().build()/conversations
    private val SMS_CONTENT_URI: Uri = Uri.parse("content://sms")
    //private val SMS_CONTENT_URI: Uri = Telephony.Sms.CONTENT_URI.buildUpon().build()
    private val SMS_INBOX_CONTENT_URI: Uri = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox")
    private val SMS_SENTBOX_CONTENT_URI: Uri = Uri.withAppendedPath(SMS_CONTENT_URI, "sent")
    private val COLUMNS = arrayOf("person", "address", "body", "date", "type")
    private val SORT_ORDER = "date ASC"
    private val SORT_ORDER_DESC = "date DESC"

    constructor(baseContext: Context)
    {
        _context = baseContext
        _settings = SettingsManager()
    }

    constructor(settings: SettingsManager, baseContext: Context)
    {
        _settings = settings
        _context = baseContext
        l.d("SMSMANAGER: constructor")
    }

    fun getSms(phones: ArrayList<Phone?>): ArrayList<Sms.AppSmsShort>?
    {
        return getSms(phones, null)
    }

    fun getSms(phones: ArrayList<Phone?>, search: String?): ArrayList<Sms.AppSmsShort>?
    {
        val res: ArrayList<Sms.AppSmsShort>? = ArrayList()
        for (phone in phones)
        {
//            var where = "address = ${phone?.getCleanNumber()}"
            var where = "thread_id = ${phone?.getContactName()}"
            if (search != null) {
                where += " and body LIKE '%" + search.replace("'", "''") + "%'"
            }
            if (res != null) {
                getAllSms(where)?.let {
                    res.addAll(it)
                }
            }
        }
        return res
    }

    /**
     * INSERT RECEIVED SMS
     */
    fun insertRecSms (
        subId: Int,
        add: String,
        body: String,
        time: Long
    ): Boolean
    {
        // Return success
        var succ: Boolean = false
        // Message insert values
        val values = contentValuesOf(
            Telephony.Sms.ADDRESS to add,
            Telephony.Sms.BODY to body,
            Telephony.Sms.DATE_SENT to time,
            Telephony.Sms.SUBSCRIPTION_ID to subId
        )

        _context.contentResolver.insert(
            SMS_CONTENT_URI,      // content://sms
            values
        )?.lastPathSegment?.toLong()?.let {
            if (it != null)
            {
                l.d("SMS-MANAGER: INSERT SMS:: $it")
                succ = true
            }
        }
        return succ
    }

    private fun getSmsByThreadId(threadId: Int, search: String?): ArrayList<Sms.AppSmsShort>? {
        var where = "thread_id = $threadId"
        if (search != null) {
            where += " and body LIKE '%" + search.replace("'", "''").toString() + "%'"
        }
        return getAllSms(where)
    }

    /**
     * GET THE CONVERSATION THREADS
     */
    fun getConvoThreads(): ArrayList<Sms.AppSmsShort>?{
        //returns the threads threads
        return getAllSmsThreads(null)
    }

    fun getLastUnreadSms(): ArrayList<Sms.AppSmsShort>? {
        return getAllSms("read = 0")
    }

    fun getLastSms(): ArrayList<Sms.AppSmsShort>? {
        l.d("SMS MANAGER: Get LAST SMs")
        return getAllSms(null)
    }

    fun getLastSms(search: String?): ArrayList<Sms.AppSmsShort>?
    {
            return getAllSms(
                "body LIKE '%" +
                        search?.replace("'", "''").toString() + "%'"
            )
    }

    private fun getAllSms(where: String?): ArrayList<Sms.AppSmsShort>?
    {
        l.d("SMS MANAGER: Get ALL SMs: $where")

        val res = ArrayList<Sms.AppSmsShort>()
        val c = _context.contentResolver.query(
            SMS_CONTENT_URI,      // content://sms
            null,
            where,
            null,
            SORT_ORDER
        )
        if (c != null)
        {
//            for (i in 0 until c.columnCount)
//            {
//                l.d(
//                    "SMS MANAGER: Get ALL SMS COLUMNS: ${c.getColumnName(i)} ${
//                        c.getColumnIndex(
//                            c.getColumnName(
//                                i
//                            )
//                        )
//                    }"
//                )
//               // l.d("SMS MANAGER: Get ALL SMS COLUMNS: ${c.getIntOrNull(c.getColumnIndex(c.getColumnName(i)))}")
//            }
            var hasData = c.moveToFirst()
            while (hasData) {

                val sms = Sms.AppSmsShort(
                    c.getInt(0),      // _ID
                    c.getInt(1),      // thread_ID
                    c.getString(2),   // address
                    c.getInt(3),      // person
                    c.getLong(4),      // date
                    c.getLong(5),      // date_sent
                    c.getInt(6),      // protocol
                    c.getInt(7),      // read
                    c.getInt(8),      // status
                    c.getInt(9),      // type
                    c.getInt(10),     // reply_path_present
                    c.getString(11),  // subject
                    c.getString(12),  // body
                    c.getString(13),  // service_center
                    c.getInt(14),     // locked
                    c.getInt(15),     // sub_id
                    c.getInt(16),     // error_code
                    c.getString(17),  //  creator
                    c.getInt(18)      // seen
                )

                //l.i("SMS MANAGER: Get ALL SMS Data: $sms")

                res.add(sms)
                hasData = c.moveToNext()
            }
            c.close()
        }
        l.i("SMS MANAGER: Get ALL SMS RETURN SIZE: ${res.size}")
        return res
    }

    /**
     * GET ALL THREADS
     */


    private fun getAllSmsThreads(where: String?): ArrayList<Sms.AppSmsShort>?
    {
        l.d("SMS MANAGER: Get ALL SMs THREADS: $where")

        val res = ArrayList<Sms.AppSmsShort>()
        // This reads the base column table info for some reason
        val c = _context.contentResolver.query(
            THREADS_CONTENT_URI,  // content://mms-sms/conversations/
            null,
            where,
            null,
            SORT_ORDER_DESC
        )
        if (c != null) {
            var hasData = c.moveToFirst()
            while (hasData) {

                try {

                    val sms = Sms.AppSmsShort(
                        c.getInt(37),      // _ID
                        c.getInt(10),      // thread_ID
                        c.getString(25),   // address
                        c.getInt(34),      // person
                        c.getLong(0),      // date
                        c.getLong(20),      // date_sent
                        c.getInt(33),      // protocol/mms version
                        c.getInt(21),      // read
                        c.getInt(39),      // status
                        c.getInt(8),      // type
                        c.getInt(6),     // reply_path_present
                        c.getString(3),  // subject
                        c.getString(7),  // body
                        c.getString(35),  // service_center
                        c.getInt(15),     // locked
                        c.getInt(26),     // sub_id
                        c.getInt(36),     // error_code
                        c.getString(8),  //  creator
                        c.getInt(17)      // seen
                    )

                    l.i("SMS MANAGER: Get ALL Thread Data: $sms")

                    res.add(sms)
//                    hasData = c.moveToNext()
                }catch (e: Exception){
                    l.e("SMS THREAD BUILD ERROR: $e")
                }
                hasData = c.moveToNext()
            }
//            */
            c.close()
        }
        return res
    }
}