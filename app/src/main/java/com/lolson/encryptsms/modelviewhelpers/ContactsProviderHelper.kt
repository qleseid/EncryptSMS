package com.lolson.encryptsms.modelviewhelpers

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.lolson.encryptsms.data.model.Sms
import com.lolson.encryptsms.utility.LogMe

class ContactsProviderHelper(_context: Context)
{
    private val mContext = _context

    // Logger
    val l = LogMe()

    /**
     * GET CONTACT NAMES
     */
    fun buildContactsMap(
        threads: ArrayList<Sms.AppSmsShort>
    ): Map<Long, String>
    {
        val t = System.currentTimeMillis()
        l.d("CPH:: BUILD CONTACTS")
        val results = mutableMapOf<Long, String>()

        for (person in threads)
        {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(person.address))

            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

            val c = mContext.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )
            if (c != null)
            {
                if (c.moveToFirst())
                {
                    l.d("CPH:: CONTACT NAME FOUND FOR: " +
                            "${c.getString(0)} for ${person.thread_id}")
                    results[person.thread_id] = c.getString(0)
                }
                c.close()
            }
        }
        l.d("CPH:: BUILD COMPLETED: ${System.currentTimeMillis() - t}ms")
        return results
    }
}