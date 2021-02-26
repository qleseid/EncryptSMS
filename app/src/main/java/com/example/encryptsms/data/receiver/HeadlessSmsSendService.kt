package com.example.encryptsms.data.receiver

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.encryptsms.utility.LogMe

class HeadlessSmsSendService: Service()
{
    //Logger
    var l = LogMe()

    override fun onBind(intent: Intent?): IBinder?
    {
        //TODO: Finish adding where the received will go.
        l.d("Headless SMS Receiver: ON RECEIVE")
        return Binder()
    }

}