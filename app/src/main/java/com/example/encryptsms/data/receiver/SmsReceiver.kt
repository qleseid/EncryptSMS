package com.example.encryptsms.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony.Sms.Intents.getMessagesFromIntent
import androidx.annotation.RequiresApi
import com.example.encryptsms.domain.interactor.ReceiveSms
import com.example.encryptsms.utility.LogMe
import dagger.android.AndroidInjection
import javax.inject.Inject

class SmsReceiver: BroadcastReceiver() {

    @Inject lateinit var receiveMessage: ReceiveSms

    //Logger
    private var l = LogMe()

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        l.d("onReceive")

        getMessagesFromIntent(intent)?.let { messages ->
            val subId = intent.extras?.getInt("subscription", -1) ?: -1

            val pendingResult = goAsync()
            receiveMessage.execute(ReceiveSms.Params(subId, messages)) { pendingResult.finish() }
        }
    }
}