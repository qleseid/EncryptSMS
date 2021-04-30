package com.lolson.encryptsms.data.receiver

import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lolson.encryptsms.data.model.Sms
import com.lolson.encryptsms.repository.SmsRepository
import com.lolson.encryptsms.utility.LogMe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SendStatusReceiver: BroadcastReceiver()
{
    //Logger
    private var l = LogMe()

    override fun onReceive(context: Context?, intent: Intent?)
    {
        GlobalScope.launch(Dispatchers.Default) {
            val smsRep = context?.let { SmsRepository(it) }
            val action = intent?.action

            if (intent?.data?.scheme.equals("sentsms"))
            {
                val date = intent?.data?.schemeSpecificPart
                l.d("SSR:: DATA: $date")
            }
            val extra = intent?.getSerializableExtra(
                        "com.lolson.encryptsms.msg") as? Sms.AppSmsShort
            val resultCode = resultCode

            when (action)
            {
                MESSAGE_SENT      ->
                {
                    if (extra != null)
                    {
                        if (resultCode == RESULT_OK) // ok = -1
                        {
                            // Update the status to sent '0'
                            extra.status = 0
                            extra.date_sent = System.currentTimeMillis()
                        }
                        else
                        {
                            extra.status = 64 // Failure = 64
                        }
                        l.d("SSR:: MESSAGE SENT UPDATE: ${smsRep?.update(extra)}")
                    }
                }
                MESSAGE_DELIVERED ->
                {
                    if (extra != null)
                    {
                        if (resultCode == RESULT_OK) // ok = -1
                        {
                            // Update the status to sent '0'
                            extra.status = 1
                        }
                        else
                        {
                            extra.status = 65 // Delivery Failure = 65
                        }
                        l.d("SSR:: MESSAGE DELIVERED UPDATE: ${smsRep?.update(extra)}")
                    }
                }
                MMS_SENT          ->
                {
                    l.d("SSR:: ON RECEIVER: MMS_SENT")
                }
                MMS_DELIVERED     ->
                {
                    l.d("SSR:: ON RECEIVER: MMS_DELIVERED")
                }
                else              ->
                {
                    l.d("SSR:: ON RECEIVER: WHEN ELSE")
                }
            }
        }
    }

    companion object
    {
        // Actions
        const val MESSAGE_SENT = "com.android.messaging.receiver.SendStatusReceiver.MESSAGE_SENT"
        const val MESSAGE_DELIVERED = "com.android.messaging.receiver.SendStatusReceiver.MESSAGE_DELIVERED"
        const val MMS_SENT = "com.android.messaging.receiver.SendStatusReceiver.MMS_SENT"
        const val MMS_DELIVERED = "com.android.messaging.receiver.SendStatusReceiver" +
                ".MMS_DELIVERED"

//        Activity.RESULT_OK // -1
//        SmsManager.RESULT_ERROR_GENERIC_FAILURE // 1
//        SmsManager.RESULT_ERROR_NO_SERVICE // 4
//        SmsManager.RESULT_ERROR_RADIO_OFF // 2
//        SmsManager.RESULT_ERROR_NULL_PDU // 3
    }
}
