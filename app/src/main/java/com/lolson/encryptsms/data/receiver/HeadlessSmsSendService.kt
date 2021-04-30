package com.lolson.encryptsms.data.receiver

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import com.lolson.encryptsms.utility.LogMe

class HeadlessSmsSendService: Service()
{
    //Logger
    var l = LogMe()

    override fun onBind(intent: Intent?): IBinder?
    {
        l.d("HSSS:: ON RECEIVE")
        return Binder()
    }

    fun onHandleIntent(intent: Intent?)
    {
//        val action = intent?.action
        val extras = intent?.extras
        val message = extras?.getString(Intent.EXTRA_TEXT)
        val num = intent?.data?.let { getRecipients(it) }

        l.d("HSSS:: ON HANDLE INTENT: $num :--:$message")

    }

    private fun getRecipients(uri: Uri): String?
    {
        val base: String = uri.schemeSpecificPart
        val pos = base.indexOf('?')
        return if (pos == -1) base else base.substring(0, pos)
    }

}