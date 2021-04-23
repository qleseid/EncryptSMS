package com.lolson.encryptsms.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lolson.encryptsms.utility.LogMe

class SendStatusReceiver: BroadcastReceiver()
{
    //Logger
    private var l = LogMe()

    override fun onReceive(context: Context?, intent: Intent?)
    {
        l.d("SSR:: Status Received")
    }
}
