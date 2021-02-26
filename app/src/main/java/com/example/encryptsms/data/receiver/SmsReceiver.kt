package com.example.encryptsms.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.example.encryptsms.data.manager.SmsManager
import com.example.encryptsms.utility.LogMe

class SmsReceiver: BroadcastReceiver()
{
    //Logger
    var l = LogMe()

    // Bundle string
    val SMS_BUNDLE = "pdus"

    override fun onReceive(
        context: Context?,
        intent: Intent?
    ) {
        //TODO: Finish adding where the received will go.
        l.d("SMS RECEIVER: ON RECEIVE: ${context.toString()}")

        Telephony.Sms.Intents.getMessagesFromIntent(intent)?.let {
            val subId = intent?.extras?.getInt("subscription", -1) ?: -1

            val pendingResult = goAsync()

            val thread = Thread{
                run {
                    l.d("SMS RECEIVER: Receive Thread")
                    receiveSms(subId, it, context)
                    pendingResult.resultCode = 1
                    pendingResult.finish()
                }
            }
            thread.start()
        }
    }

    private fun receiveSms(subId: Int, messages: Array<SmsMessage>, context: Context?)
    {
        if (messages.isNotEmpty())
        {
            val add = messages[0].displayOriginatingAddress
            val time = messages[0].timestampMillis
            val body = messages
                .mapNotNull {msgs -> msgs.displayMessageBody }
                .reduce{body, new -> body + new}
            val smsM = context?.let { SmsManager(it) }
            if (smsM!!.insertRecSms(subId, add, body, time))
            {
                l.d("SMS RECEIVE: INSERT SUCCESS")
            }
        }
    }
    /*
        @Inject lateinit var receiveMessage: ReceiveSms

        //Logger

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

     */

}