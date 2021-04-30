@file:Suppress("SameParameterValue")

package com.lolson.encryptsms.data.manager

import android.app.PendingIntent
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.Telephony
import android.telephony.SmsManager
import androidx.core.content.contentValuesOf
import com.lolson.encryptsms.data.model.Phone
import com.lolson.encryptsms.data.model.Sms
import com.lolson.encryptsms.data.receiver.SendStatusReceiver
import com.lolson.encryptsms.utility.LogMe

class SmsManager(
    baseContext: Context)
{
    //Logger
    var l = LogMe()
    private var _context: Context = baseContext

    companion object
    {
        // URI STRINGS FOR ACCESSING THE DATABASE
        private val SMS_CONTENT_URI: Uri = Uri.parse("content://sms")
        private val COLUMNS = arrayOf("_id", "thread_id", "read", "address", "body", "date")
        private const val COUNT_BUILD = "date ASC"
        private const val SORT_ORDER = "date ASC"


        private val THREADS_CONTENT_URI: Uri = Uri.parse("content://mms-sms/conversations/")
        private val THREAD_COLUMN = arrayOf("*")
        //        private val SMS_CONTENT_URI: Uri = Telephony.Sms.CONTENT_URI.buildUpon().build()
//    private val SMS_INBOX_CONTENT_URI: Uri = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox")
//    private val SMS_SENTBOX_CONTENT_URI: Uri = Uri.withAppendedPath(SMS_CONTENT_URI, "sent")
//    private val COLUMNS = arrayOf("_id", "thread_id")
                private const val SORT_ORDER_DESC = "date DESC"
        //        private const val COUNT_BUILD = " limit 500; GROUP BY thead_id HAVING count(thread_id) > 0"
//        private val THREADS_CONTENT_URI_SMALL: Uri = Uri.parse("content://sms/conversations")
        //        private val THREADS_CONTENT_URI: Uri = Uri.parse("content://service-state/")
//        private val THREADS_CONTENT_URI: Uri = Uri.parse("content://mms/part")
//        private val THREADS_CONTENT_URI: Uri = Uri.parse("content://mms/inbox")
//        private val THREADS_CONTENT_URI_BUILD: Uri = Telephony.Threads.CONTENT_URI.buildUpon().build()
    }

    fun getSms(
        phones: ArrayList<Phone.pho?>
    ): ArrayList<Sms.AppSmsShort>?
    {
        return getSms(phones, null)
    }

    // The ArrayList of phones is because one contact can have multiple numbers
    private fun getSms(
        phones: ArrayList<Phone.pho?>,
        search: String?
    ): ArrayList<Sms.AppSmsShort>?
    {
        val res: ArrayList<Sms.AppSmsShort>? = ArrayList()
        for (phone in phones)
        {
//            var where = "address = ${phone?.getCleanNumber()}"
            var where = "thread_id = ${phone?.mContactName}"
            if (search != null)
            {
                where += " and body LIKE '%" + search.replace("'", "''") + "%'"
            }
            if (res != null)
            {
                getAllSms(where, null)?.let {
                    res.addAll(it)
                }
            }
        }
        return res
    }

    fun getThreadId(
        address: String
    ): ArrayList<Sms.AppSmsShort>?
    {
        val res: ArrayList<Sms.AppSmsShort>? = ArrayList()
        var nn = Phone.pho(
            null,
            address
        ).mCleanNumber

        if (nn?.length!! > 10)
        {
            l.d("SMSM:: GET THREAD ID NUMBER TRIM BEFORE: $nn")
            nn = nn.substring(nn.length - 10, nn.length)
            l.d("SMSM:: GET THREAD ID NUMBER TRIM AFTER: $nn")
        }

        val where = "address LIKE '%$nn%'"
        if (res != null)
        {
            getAllSms(where, null)?.let {
                res.addAll(it)
            }
        }
        return res
    }

    private fun checkPermission(
    ):Boolean
    {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            _context.getSystemService(RoleManager::class.java)
                ?.isRoleHeld(RoleManager.ROLE_SMS) == true
        }
        else
        {
            Telephony.Sms.getDefaultSmsPackage(_context) == _context.packageName
        }
    }

    /**
     * SEND SMS MESSAGE
     */
    fun sendMessage(
        sms: Sms.AppSmsShort,
        uri: Uri
    ): Boolean
    {
        // Return success
        val succ: Boolean
        val smsM = SmsManager.getDefault()

        // Pending Intents
        val sentIntent = arrayListOf(PendingIntent.getBroadcast(
            _context,
            sms.thread_id.toInt(),
        Intent(
            SendStatusReceiver.MESSAGE_SENT,
            Uri.parse("sentsms:${sms.date}"),
            _context,
            SendStatusReceiver::class.java)
            .putExtra("com.lolson.encryptsms.msg", sms),
        0))

        val deliveredIntent = arrayListOf(PendingIntent.getBroadcast(
            _context,
            sms.thread_id.toInt(),
            Intent(
                SendStatusReceiver.MESSAGE_DELIVERED,
                Uri.parse("sentsms:${sms.date}"),
                _context,
                SendStatusReceiver::class.java)
                .putExtra("com.lolson.encryptsms.msg", sms),
            0))

        // Clean number
        sms.address = Phone.pho(
            null,
            sms.address
        ).mCleanNumber.toString()

        // Check if app is default; have to manage provider if so
        if (checkPermission())
            {
                l.d("SMSM:: SEND SMS AS DEFAULT")
                smsM.sendMultipartTextMessage(
                    sms.address,
                    null,
                    smsM.divideMessage(sms.body),
                    sentIntent,
                    deliveredIntent
                )
                succ = insertSms(uri, sms)
            }
        else  // The system handles the provider if not default
        {
            l.d("SMSM:: SEND SMS AS ANOTHER APP")
            smsM.sendMultipartTextMessage(
                sms.address,
                null,
                smsM.divideMessage(sms.body),
                sentIntent,
                deliveredIntent
            )
            succ = true
        }

        return succ
    }

    /**
     * INSERT MESSAGE TO DATABASE
     */
    @Suppress("ConstantConditionIf")
    private fun insertSms(
        uri: Uri,
        sms: Sms.AppSmsShort
    ): Boolean
    {
        var succ = false

        val values = contentValuesOf(
            Telephony.Sms.ADDRESS to sms.address,
            Telephony.Sms.BODY to sms.body,
            Telephony.Sms.DATE_SENT to sms.date_sent,
            Telephony.Sms.DATE to sms.date,
            Telephony.Sms.READ to sms.read,
            Telephony.Sms.TYPE to sms.type
        )

        values.put("creator", sms.creator)
        values.put("sub_id", sms.sub_id)

        // TODO:: Change later with PendingIntents
        if (true)
        {
            values.put("status", 32)
        }
        if (sms.thread_id != -1L)
        {
            values.put("thread_id", sms.thread_id)
        }
        _context.contentResolver.insert(
            uri,
            values
        )?.lastPathSegment?.toLong()?.let {
            l.d("SMSM:: INSERT SMS:: $it")
            succ = true
        }

        return succ
    }

    /**
     * INSERT RECEIVED SMS
     */
    fun insertRecSms(
        subId: Int,
        add: String,
        body: String,
        time: Long
    ): Boolean
    {
        // Return success
        var succ = false
        // Message insert values
        val values = contentValuesOf(
            Telephony.Sms.ADDRESS to add,
            Telephony.Sms.BODY to body,
            Telephony.Sms.DATE_SENT to time
        )

        values.put("sub_id", subId)
        _context.contentResolver.insert(
            SMS_CONTENT_URI,      // content://sms
            values
        )?.lastPathSegment?.toLong()?.let {
            l.d("SMSM:: INSERT RECEIVED SMS:: $it")
            succ = true
        }
        return succ
    }

    /**
     * GET THE CONVERSATION THREADS
     */
    fun getConvoThreads(): ArrayList<Sms.AppSmsShort>?
    {
//        return  getAllSmsThreads(null)
        return getAllSmsThreads()
    }

    /**
     * GET ALL SMS THREADS
     */
    private fun getAllSmsThreads(): ArrayList<Sms.AppSmsShort>?
    {
        // Timing of query, 40,000 message ~ 800 - 1200ms
        var nt: Long
        val t = System.currentTimeMillis()
        l.d("SMSM:: GET ALL SMS THREAD ********** ${t}ms")

        val res = ArrayList<Sms.AppSmsShort>()
        val threadMap = mutableMapOf<Long, Long>()


        val c = _context.contentResolver.query(
            SMS_CONTENT_URI,
            COLUMNS,
            null,
            null,
            COUNT_BUILD
//            SORT_ORDER
        )
        if (c != null)
        {
            l.d("SMSM:: ^^^^^^^^^^^ NEW SMS THREAD ^^^^^^^^^^^^^^^ total msgs: ${c.count}")

            var hasData = c.moveToFirst()
            while (hasData)
            {
                // Map the results to get an id for given thread_id
                threadMap[c.getLong(c.getColumnIndex("thread_id"))] = c.getLong(c.getColumnIndex("_id"))
                hasData = c.moveToNext()
            }

            nt = System.currentTimeMillis()
            l.d("SMSM:: ^^^ MAP BUILT, BUILDING THREAD ^^^ size: ${threadMap.size} :: ${nt - t}ms")
            hasData = c.moveToFirst()
            while (hasData)
            {

                if (
                    threadMap[c.getLong(c.getColumnIndex("thread_id"))] ==
                    c.getLong(c.getColumnIndex("_id")))
                {
                    val sms = Sms.AppSmsShort()
                    sms.id = c.getInt(c.getColumnIndex("_id"))               // _ID
                    sms.thread_id = c.getLong(c.getColumnIndex("thread_id"))           // thread_ID
                    sms.address = c.getString(c.getColumnIndex("address"))         // person
                    sms.date = c.getLong(c.getColumnIndex("date"))        // subject
                    sms.body = c.getString(c.getColumnIndex("body"))    // subject
                    sms.read = c.getInt(c.getColumnIndex("read"))
                    //l.i("SMS MANAGER: Get ALL SMS Data: $sms")

                    res.add(sms)
                }
                hasData = c.moveToNext()
            }
            c.close()
        }
        nt = System.currentTimeMillis()
        l.i("SMSM:: GET ALL SMS ***THREAD*** RETURN SIZE: ${res.size} :: ${nt - t}ms")
        return res
    }

    /**
     * GET ALL SMS
     */
    private fun getAllSms(
        where: String?,
        whereArg: Array<String?>?
    ): ArrayList<Sms.AppSmsShort>?
    {
        val t = System.currentTimeMillis()
        l.d("SMSM:: Get ALL SMs: $where")

        val res = ArrayList<Sms.AppSmsShort>()
        val c = _context.contentResolver.query(
            SMS_CONTENT_URI,      // content://sms
            null,
            where,
            whereArg,
            SORT_ORDER
        )
        if (c != null)
        {
            // Print columns for debugging each SDK
//            columnDebug(c)

            var hasData = c.moveToFirst()
            while (hasData)
            {

                val sms = Sms.AppSmsShort()
                sms.id = c.getInt(c.getColumnIndex("_id"))
                sms.thread_id = c.getLong(c.getColumnIndex("thread_id"))
                sms.address = c.getString(c.getColumnIndex("address"))
                sms.person = c.getInt(c.getColumnIndex("person"))
                sms.date = c.getLong(c.getColumnIndex("date"))
                sms.date_sent = c.getLong(c.getColumnIndex("date_sent"))
                sms.protocol = c.getInt(c.getColumnIndex("protocol"))
                sms.read = c.getInt(c.getColumnIndex("read"))
                sms.status = c.getInt(c.getColumnIndex("status"))
                sms.type = c.getInt(c.getColumnIndex("type"))
                sms.reply_path_present = c.getInt(c.getColumnIndex("reply_path_present"))
                sms.subject = c.getString(c.getColumnIndex("subject"))
                sms.body = c.getString(c.getColumnIndex("body"))
                sms.service_center = c.getString(c.getColumnIndex("service_center"))
                sms.locked = c.getInt(c.getColumnIndex("locked"))
//                sms.sub_id = c.getInt(c.getColumnIndex("sub_id"))
                sms.error_code = c.getInt(c.getColumnIndex("error_code"))
//                    c.getString(c.getColumnIndex("creator"))// Kit-Kat doesn't support
                sms.seen = c.getInt(c.getColumnIndex("seen"))

                //l.i("SMS MANAGER: Get ALL SMS Data: $sms")

                res.add(sms)
                hasData = c.moveToNext()
            }
            c.close()
        }
        val nt: Long = System.currentTimeMillis()
        l.i("SMSM:: Get ALL SMS RETURN SIZE: ${res.size} :: ${nt - t}ms")
        return res
    }


    // Crazy slow!!!!!!
//    /**
//     * GET ALL SMS THREADS
//     */
//    private fun getAllSmsThreads(): ArrayList<Sms.AppSmsShort>?
//    {
//        var nt: Long
//        val t = System.currentTimeMillis()
//        l.d("SMS MANAGER: GET ALL SMS THREAD ********** $t $THREADS_CONTENT_URI_SMALL")
//
//
//        val res = ArrayList<Sms.AppSmsShort>()
//
//        val threadMap = mutableMapOf<Long, Long>()
//        val threadPair = ArrayList<Pair<Long, String>>()
//
//
//        val c = _context.contentResolver.query(
//            THREADS_CONTENT_URI_SMALL,
//            null,
//            null,
//            null,
//            COUNT_BUILD
////            SORT_ORDER
//        )
//        if (c != null)
//        {
//            l.d("^^^^^^^^^^^ NEW SMS THREAD ^^^^^^^^^^^^^^^ ${c.count}")
//
//
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
//                // l.d("SMS MANAGER: Get ALL SMS COLUMNS: ${c.getIntOrNull(c.getColumnIndex(c.getColumnName(i)))}")
//            }
//
//            var hasData = c.moveToFirst()
//            while (hasData)
//            {
////                // Map the results to get an id for given thread_id
////                threadMap[c.getLong(c.getColumnIndex("thread_id"))] =
////                        c.getLong(c.getColumnIndex("_id"))
//
//                threadPair.add(Pair(c.getLong(c.getColumnIndex("thread_id")),
//                    c.getString(c.getColumnIndex("snippet"))))
//
//                hasData = c.moveToNext()
//            }
//
//            nt = System.currentTimeMillis()
//            l.d("^^^^^^^^^^^ MAP BUILT, BUILDING THREAD ^^^^^^^^^^^^^^^${nt - t} :: ${threadPair
//                .size}")
////            hasData = c.moveToFirst()
////            while (hasData)
////            {
////
////                if (
////                    threadMap[c.getLong(c.getColumnIndex("thread_id"))] ==
////                    c.getLong(c.getColumnIndex("_id")))
////                {
////                    val sms = Sms.AppSmsShort()
////                    sms.id = c.getInt(c.getColumnIndex("_id"))               // _ID
////                    sms.thread_id = c.getLong(c.getColumnIndex("thread_id"))           // thread_ID
////                    sms.address = c.getString(c.getColumnIndex("address"))         // person
////                    sms.date = c.getLong(c.getColumnIndex("date"))        // subject
////                    sms.body = c.getString(c.getColumnIndex("body"))    // subject
////                    sms.read = c.getInt(c.getColumnIndex("read"))
////                    //l.i("SMS MANAGER: Get ALL SMS Data: $sms")
////
////                    res.add(sms)
////                }
////                hasData = c.moveToNext()
////            }
//            c.close()
//
//            for (p in threadPair)
//            {
//                getSms(arrayListOf(
//                    Phone.pho(
//                        p.first.toString()
//                    )
//                ), p.second)?.let { res.addAll(it) }
//            }
//        }
//        nt = System.currentTimeMillis()
//        l.i("SMS MANAGER: GET ALL SMS ***THREAD*** RETURN SIZE: ${nt - t} :: ${res.size}")
//        return res
//    }
    /**
     * GET ALL THREADS
     */
    private fun getAllSmsThreads(
        where: String?
    ): ArrayList<Sms.AppSmsShort>?
    {
        val t = System.currentTimeMillis()
        l.d("SMSM:: GET ALL SMS THREAD ********** ${t}ms")

        val res = ArrayList<Sms.AppSmsShort>()
        // This reads the base column table info for some reason
        val c = _context.contentResolver.query(
            THREADS_CONTENT_URI,  // content://mms-sms/conversations/
            THREAD_COLUMN,
//            "*",
//            THREAD_COLUMN,
//            null,
            where,

            null,
            null
//            SORT_ORDER_DESC
        )
        if (c != null)
        {
            // Print columns for debugging each SDK
//            columnDebug(c)

            var hasData = c.moveToFirst()
            while (hasData)
            {

                try {

                    val sms = Sms.AppSmsShort()
                    sms.id = c.getInt(c.getColumnIndex("_id"))
                    sms.thread_id = c.getLong(c.getColumnIndex("thread_id"))
                    sms.address = c.getString(c.getColumnIndex("address"))
                    sms.person = c.getInt(c.getColumnIndex("person"))
                    sms.date = c.getLong(c.getColumnIndex("date"))
                    sms.date_sent = c.getLong(c.getColumnIndex("date_sent"))
//                    sms.protocol = c.getInt(c.getColumnIndex("protocol"))
                    sms.read = c.getInt(c.getColumnIndex("read"))
                    sms.status = c.getInt(c.getColumnIndex("status"))
                    sms.type = c.getInt(c.getColumnIndex("type"))
                    sms.reply_path_present = c.getInt(c.getColumnIndex("reply_path_present"))
                    sms.subject = c.getString(c.getColumnIndex("subject"))
                    sms.body = c.getString(c.getColumnIndex("body"))
                    sms.service_center = c.getString(c.getColumnIndex("service_center"))
                    sms.locked = c.getInt(c.getColumnIndex("locked"))
//                sms.sub_id = c.getInt(c.getColumnIndex("sub_id"))
                    sms.error_code = c.getInt(c.getColumnIndex("error_code"))
//                    c.getString(c.getColumnIndex("creator"))// Kit-Kat doesn't support
//                    sms.seen = c.getInt(c.getColumnIndex("seen"))
//                    l.i("SMS MANAGER: Get ALL Thread Data: $sms")

                    res.add(sms)
                }
                catch (e: Exception)
                {
                    l.e(
                        "SMSM:: THREAD BUILD ERROR: " +
                                "$e " +
                                "${c.getInt(c.getColumnIndex("thread_id"))} " +
                                c.getString(c.getColumnIndex("subject")))
                }
                hasData = c.moveToNext()
            }
            val nt: Long = System.currentTimeMillis()
            l.e("SMSM:: THREAD BUILD SIZE:  ${res.size} :: ${nt - t}ms")
            c.close()
        }
        return res
    }

    /**
     * COLUMN DEBUG
     */
    private fun columnDebug(
        column: Cursor
    )
    {
        for (i in 0 until column.columnCount)
        {
            l.d("SMSM:: COLUMNS DEBUGGER: ${column.getColumnName(i)} ${
                    column.getColumnIndex(
                        column.getColumnName(
                            i
                        )
                    )
                }"
            )
        }
    }

    /**
     * UPDATE MESSAGE BY _ID
     */
    fun updateMessage(
        sms: Sms.AppSmsShort
    ): Int
    {
        val values = contentValuesOf(
            Telephony.Sms.READ to sms.read,
            Telephony.Sms.STATUS to sms.status,
            Telephony.Sms.DATE_SENT to sms.date_sent
        )

        return _context.contentResolver.update(
            SMS_CONTENT_URI,
            values,
            Telephony.Sms.DATE + "=" + sms.date,
            null
        )
    }

    /**
     * DELETE MESSAGE BY _ID
     */
    fun deleteMessage(
        id: Int
    ): Int
    {
        return _context.contentResolver.delete(
            SMS_CONTENT_URI, BaseColumns._ID + "=" + id,
            null)
    }
}