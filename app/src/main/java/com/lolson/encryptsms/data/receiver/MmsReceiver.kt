package com.lolson.encryptsms.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class MmsReceiver: BroadcastReceiver() {


    @Override
    override fun onReceive(context: Context?, intent: Intent?)
    {
        if (intent != null)
        {
            if (Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION == intent.action
                && "application/vnd.wap.mms-message" == intent.type) {
                // TODO::make this work; MMS
                // Always convert negative subIds into -1
//                var subId = -1
//                var data = intent.getByteArrayExtra("data")
//                MmsWapPushReceiver.mmsReceived(subId, data)
            }
        }
    }
}
